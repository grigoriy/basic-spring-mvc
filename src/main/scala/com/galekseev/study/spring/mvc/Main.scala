package com.galekseev.study.spring.mvc

import java.util.concurrent.CountDownLatch

import org.eclipse.jetty.server.{Server, ServerConnector}
import org.eclipse.jetty.servlet.{ServletContextHandler, ServletHolder}
import org.eclipse.jetty.util.component.LifeCycle
import org.eclipse.jetty.util.thread.QueuedThreadPool
import org.slf4j.LoggerFactory
import org.springframework.web.context.ContextLoaderListener
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext
import org.springframework.web.servlet.DispatcherServlet

object Main extends App {
  val MAX_SERVER_THREADS = 8
  val SPRING_CONFIG_LOCATION = "com.galekseev.study.spring.mvc.config"
  val SERVLET_CONTEXT_PATH = "/root"
  val DISPATCHER_SERVLET_PATH = "/dispatcher/*"
  private val log = LoggerFactory.getLogger(Main.getClass)

  startServer(8080)

  def startServer(port: Int): Unit = {
    val server = new Server(new QueuedThreadPool(MAX_SERVER_THREADS))
    val connector = new ServerConnector(server)
    connector.setPort(port)
    server.addConnector(connector)
    val webApplicationContext = new AnnotationConfigWebApplicationContext()
    webApplicationContext.setConfigLocation(SPRING_CONFIG_LOCATION)
    val dispatcherServlet = new DispatcherServlet(webApplicationContext)
    val servletContextHandler = new ServletContextHandler(ServletContextHandler.NO_SESSIONS)
    servletContextHandler.setContextPath(SERVLET_CONTEXT_PATH)
    servletContextHandler.addServlet(new ServletHolder(dispatcherServlet), DISPATCHER_SERVLET_PATH)
    servletContextHandler.addEventListener(new ContextLoaderListener(webApplicationContext))
    server.setHandler(servletContextHandler)

    val latch = new CountDownLatch(1)
    servletContextHandler.addLifeCycleListener(new LifeCycle.Listener() {
      override def lifeCycleStopped(event: LifeCycle): Unit = latch.countDown()
      override def lifeCycleStarted(event: LifeCycle): Unit = latch.countDown()
      override def lifeCycleFailure(event: LifeCycle, cause: Throwable): Unit = latch.countDown()
      override def lifeCycleStarting(event: LifeCycle): Unit = latch.countDown()
      override def lifeCycleStopping(event: LifeCycle): Unit = ()
    })
    servletContextHandler.start()
    latch.await()
    server.start()
    log.info(s"Server has started and is listening at port $port")
  }
}
