package com.example.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;  
import org.springframework.beans.factory.annotation.Value;

@RestController
public class DisplayMessageController {

  @Value("${message.text}")
  private String messageText;

  @Value("${message.source}")
  private String messageSource;

  @RequestMapping("/")
  private String getHome() {
    return ("message.text: " + messageText + "<br/>" + "message.source: " + messageSource);
  }

}