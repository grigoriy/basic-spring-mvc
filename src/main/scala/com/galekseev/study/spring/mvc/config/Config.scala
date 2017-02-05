package com.galekseev.study.spring.mvc.config

import com.galekseev.study.spring.mvc.controller.MyController
import com.galekseev.study.spring.mvc.service.SlowService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.{Bean, ComponentScan, Configuration, PropertySource}
import org.springframework.web.servlet.config.annotation.EnableWebMvc

@Configuration
@PropertySource(Array("classpath:application.properties"))
@EnableWebMvc
@ComponentScan(Array("com.galekseev.study.spring.mvc"))
class Config {
  @Value("${slow.service.message}")
  var slowServiceMessage: String = _

  @Bean
  def slowService() = new SlowService(slowServiceMessage)

  @Bean
  def myController() = new MyController(slowService())
}
