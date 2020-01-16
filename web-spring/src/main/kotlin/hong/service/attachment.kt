package hong.service

import hong.common.exception.BaseException
import hong.common.exception.FileNotFoundException
import hong.common.exception.StorageException
import hong.meta.jooq.Tables.ATTACHMENT
import hong.meta.jooq.enums.AttachmentStorageType
import hong.meta.jooq.tables.pojos.Attachment
import org.jooq.DSLContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.stereotype.Controller
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.FileSystemUtils
import org.springframework.util.StringUtils
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.net.MalformedURLException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.stream.Stream
import javax.annotation.PostConstruct

@ConfigurationProperties(prefix = "storage")
class StorageProperties {
    lateinit var location: String
}

interface StorageService {
    fun init()
    fun store(file: MultipartFile): String
    fun loadAll(): Stream<Path>
    fun load(filename: String): Path
    fun loadAsResource(filename: String): Resource
    fun deleteAll(): Boolean
}

@Component
class FileStorageService : StorageService {

    @Autowired
    lateinit var storageProperties: StorageProperties
    private lateinit var rootLocation: Path
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")

    @PostConstruct
    override fun init() {
        rootLocation = Paths.get(storageProperties.location)
        Files.createDirectories(rootLocation)
    }

    override fun store(file: MultipartFile): String {
        val (filename, extension) = StringUtils.cleanPath(file.originalFilename!!).let {
            StringUtils.stripFilenameExtension(it) to StringUtils.getFilenameExtension(it)
        }
        val newFilename = "${filename}_${LocalDateTime.now().format(dateTimeFormatter)}.$extension"
        try {
            file.inputStream.use {
                Files.copy(it, rootLocation.resolve(newFilename), StandardCopyOption.REPLACE_EXISTING)
            }
        } catch (e: IOException) {
            throw StorageException("Failed to store file $newFilename", e)
        }
        return newFilename
    }

    override fun loadAll(): Stream<Path> {
        try {
            return Files.walk(rootLocation, 1)
                .filter { it != rootLocation }
                .map(rootLocation::relativize)
        } catch (e: IOException) {
            throw StorageException("Failed to read stored files", e)
        }
    }

    override fun load(filename: String): Path {
        return rootLocation.resolve(filename)
    }

    override fun loadAsResource(filename: String): Resource {
        try {
            val file = load(filename)
            val resource = UrlResource(file.toUri())
            if (resource.exists() || resource.isReadable) {
                return resource
            } else {
                throw FileNotFoundException("Could not read file: $filename")
            }
        } catch (e: MalformedURLException) {
            throw FileNotFoundException("Could not read file: $filename", e)
        }
    }

    override fun deleteAll(): Boolean {
        return FileSystemUtils.deleteRecursively(rootLocation)
    }
}

@Controller
@RequestMapping("/attachment")
class AttachmentController {

    @Autowired
    lateinit var attachmentService: AttachmentService

    @Autowired
    lateinit var fileStorageService: FileStorageService

    @GetMapping("/{id}")
    fun download(@PathVariable("id") id: Long): ResponseEntity<Resource> {
        val attachment = attachmentService.findOne(id) ?: throw BaseException.NotFound
        val resource = fileStorageService.loadAsResource(attachment.resourceUri)
        return ResponseEntity.ok()
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"${attachment.fileName}\""
            )
            .body(resource)
    }

    @PostMapping("/upload-file")
    @ResponseBody
    fun uploadFile(@RequestParam("file") file: MultipartFile): Long {
        return attachmentService.save(file)
    }
}

@Service
@Transactional
class AttachmentService {

    @Autowired
    lateinit var fileStorageService: FileStorageService

    @Autowired
    lateinit var ctx: DSLContext

    fun save(file: MultipartFile): Long {
        val newFilename = fileStorageService.store(file)
        return save(AttachmentStorageType.file, file.originalFilename!!, file.contentType, file.size, newFilename)
    }

    fun save(
        storageType: AttachmentStorageType,
        fileName: String,
        mimeType: String?,
        size: Long,
        resourceUri: String
    ): Long {
        return ctx
            .insertInto(ATTACHMENT)
            .set(ATTACHMENT.STORAGE_TYPE, storageType)
            .set(ATTACHMENT.FILE_NAME, fileName)
            .set(ATTACHMENT.MIME_TYPE, mimeType)
            .set(ATTACHMENT.SIZE, size)
            .set(ATTACHMENT.RESOURCE_URI, resourceUri)
            .returning(ATTACHMENT.ID)
            .fetchOne()
            .getValue(ATTACHMENT.ID)
    }

    @Transactional(readOnly = true)
    fun findOne(id: Long): Attachment? {
        return ctx
            .selectFrom(ATTACHMENT)
            .where(
                ATTACHMENT.ID.eq(id).and(ATTACHMENT.DELETED_AT.isNull)
            )
            .fetchOneInto(Attachment::class.java)
    }

    @Transactional(readOnly = true)
    fun findByIds(vararg ids: Long): List<Attachment> {
        return ctx
            .selectFrom(ATTACHMENT)
            .where(
                ATTACHMENT.ID.`in`(ids.toList()).and(ATTACHMENT.DELETED_AT.isNull)
            )
            .fetchInto(Attachment::class.java)
    }

}