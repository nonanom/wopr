package com.example;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class WoprApplicationTest {

  @Test
  public void main() throws Exception {
    WoprApplication.main(new String[] {});
  }
  
}