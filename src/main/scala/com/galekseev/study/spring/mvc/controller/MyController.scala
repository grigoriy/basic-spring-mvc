package com.galekseev.study.spring.mvc.controller

import com.galekseev.study.spring.mvc.service.SlowService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.{RequestMapping, RequestMethod, ResponseBody, ResponseStatus}
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter

import scala.concurrent.duration.Deadline
import scala.concurrent.{Future, Promise}

@Controller
class MyController(service: SlowService) {
  import MyController._

  @RequestMapping(path=Array("/test"), method = Array(RequestMethod.GET))
  @ResponseBody
  def test(timeout: Long): ResponseBodyEmitter = {
    Deadline
    import scala.concurrent.ExecutionContext.Implicits.global
    val promise = Promise[Unit]()
    Future({
      Thread.sleep(timeout * 1000L)
      if (promise.tryFailure(TimeoutException()))
        log.warn("Time is out")
      else
        log.debug("Time is out but we've started responding already")
    })
    val responseBodyEmitter = new ResponseBodyEmitter
    Future({
      service.slowProcessing(responseBodyEmitter, promise, 2)
    })
    promise.future.recover{ case e => responseBodyEmitter.completeWithError(e) }

    responseBodyEmitter
  }
}

@ResponseStatus(HttpStatus.BAD_GATEWAY)
case class TimeoutException() extends RuntimeException

object MyController {
  private val log = LoggerFactory.getLogger(MyController.getClass)
  def apply(service: SlowService): MyController = new MyController(service)
}
