package hong.common.web.controller

import hong.common.exception.BaseException

abstract class BaseController {

    protected fun redirect(url: String): String = "redirect:$url"

    protected fun badRequest(): Unit = throw BaseException.BadRequest

    protected fun unauthorized(): Unit = throw BaseException.Unauthorized

    protected fun notFound(): Unit = throw BaseException.NotFound
}