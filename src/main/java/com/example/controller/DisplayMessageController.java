package com.example.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;  
import org.springframework.beans.factory.annotation.Value;

@RestController
public class DisplayMessageController {

  @Value("${message.text}")
  private String messageText;

  @RequestMapping("/")
  private String getHome() {
    return (messageText);
  }

}
