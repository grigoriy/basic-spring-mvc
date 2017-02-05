package com.galekseev.study.spring.mvc.service

import java.util.concurrent.TimeUnit

import org.slf4j.LoggerFactory
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter

import scala.concurrent.Promise

class SlowService(message: String) {
  import SlowService._

  def slowProcessing(responseBodyEmitter: ResponseBodyEmitter, promise: Promise[Unit], delaySeconds: Int): Unit = {
    def iterateWords(sentence: String): Stream[String] = {
      sentence match {
        case s if s.length > 0 =>
          val (word, tail) = sentence.dropWhile(_ == ' ').span(_ != ' ')
          word #:: iterateWords(tail)
        case _ => Stream.empty
      }
    }
    TimeUnit.SECONDS.sleep(delaySeconds)
    promise.trySuccess(())
    for (word <- iterateWords(message)) {
      log.debug(s"Sending word [$word]")
      responseBodyEmitter.send(s"$word ")
      TimeUnit.SECONDS.sleep(delaySeconds)
    }
    responseBodyEmitter.complete()
  }
}

object SlowService {
  private val log = LoggerFactory.getLogger(SlowService.getClass)
}
