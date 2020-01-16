package hong.service

import hong.common.exception.StorageException
import hong.meta.jooq.Tables.ATTACHMENT
import hong.meta.jooq.enums.AttachmentStorageType
import hong.meta.jooq.tables.pojos.Attachment
import org.jooq.DSLContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.StringUtils
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.annotation.PostConstruct

@ConfigurationProperties(prefix = "storage")
class StorageProperties {
    lateinit var location: String
}

@Component
class FileStorageService {

    @Autowired
    lateinit var storageProperties: StorageProperties
    private lateinit var rootLocation: Path
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")

    @PostConstruct
    fun init() {
        rootLocation = Paths.get(storageProperties.location)
        Files.createDirectories(rootLocation)
    }

    fun store(file: MultipartFile): String {
        val (filename, extension) = StringUtils.cleanPath(file.originalFilename!!).let {
            StringUtils.getFilename(it) to StringUtils.getFilenameExtension(it)
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
}

@Service
class AttachmentService {

    @Autowired
    lateinit var fileStorageService: FileStorageService

    @Autowired
    lateinit var ctx: DSLContext

    fun save(file: MultipartFile): Long {
        val newFilename = fileStorageService.store(file)
        return save(AttachmentStorageType.file, file.originalFilename!!, file.contentType, file.size, newFilename)
    }

    @Transactional
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