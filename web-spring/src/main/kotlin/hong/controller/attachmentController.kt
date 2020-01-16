package hong.controller

import hong.common.exception.BaseException
import hong.common.storage.FileStorageService
import hong.meta.jooq.Tables.ATTACHMENT
import hong.meta.jooq.enums.AttachmentStorageType
import hong.meta.jooq.tables.pojos.Attachment
import org.jooq.DSLContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

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
        val filename = fileStorageService.store(file)
        return attachmentService.save(file, filename)
    }
}

@Service
@Transactional
class AttachmentService {

    @Autowired
    lateinit var ctx: DSLContext

    fun save(file: MultipartFile, newFilename: String): Long {
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