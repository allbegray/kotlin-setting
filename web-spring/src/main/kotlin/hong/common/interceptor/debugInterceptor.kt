package hong.common.interceptor

import javassist.ClassPool
import mu.toKLogger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class DebugInterceptor : HandlerInterceptorAdapter() {

    private val logger = LoggerFactory.getLogger(DebugInterceptor::class.java).toKLogger()

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        if (logger.isDebugEnabled &&handler is HandlerMethod) {
            val method = handler.method
            val declaringClass = method.declaringClass

            val default = ClassPool.getDefault()
            val ctClass = default.getCtClass(declaringClass.name)
            val ctMethod = default.getMethod(declaringClass.name, method.name)

            logger.debug {
                val sourceFile = ctClass.classFile.sourceFile
                val lineNumber = ctMethod.methodInfo.getLineNumber(0)
                "${request.requestURL} - $handler ($sourceFile:$lineNumber)"
            }
        }
        return super.preHandle(request, response, handler)
    }
}