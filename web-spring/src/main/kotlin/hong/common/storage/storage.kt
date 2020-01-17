package hong.common.storage

import hong.common.exception.FileNotFoundException
import hong.common.exception.StorageException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.stereotype.Component
import org.springframework.util.FileSystemUtils
import org.springframework.util.StringUtils
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
import javax.servlet.http.HttpSession

@ConstructorBinding
@ConfigurationProperties(prefix = "storage")
class StorageProperties(val location: String)

interface StorageService

interface FileStorageService : StorageService {
    fun init()
    fun store(file: MultipartFile): String
    fun loadAll(): Stream<Path>
    fun load(filename: String): Path
    fun loadAsResource(filename: String): Resource
    fun deleteAll(): Boolean
}

interface KeyAndValueStorageService : StorageService {
    fun set(name: String, value: Any?)
    fun <T> get(name: String): T?
    fun <T> get(name: String, default: () -> T): T
    fun remove(name: String)
}

@Component
class SessionStorageService : KeyAndValueStorageService {

    @Autowired
    lateinit var session: HttpSession

    override fun set(name: String, value: Any?) {
        session.setAttribute(name, value)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> get(name: String): T? {
        return session.getAttribute(name) as T?
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> get(name: String, default: () -> T): T {
        val value = get<T>(name)
        if (value != null) {
            return value
        }
        val defaultValue = default()
        set(name, defaultValue)
        return defaultValue
    }

    override fun remove(name: String) {
        session.removeAttribute(name)
    }

    fun expire() {
        session.invalidate()
    }
}

@Component
class LocalFileStorageService : FileStorageService {

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