package com.hibernate.envers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;

import com.hibernate.envers.application.ApplicationService;

public class EnversTestRunner implements CommandLineRunner {

  @Autowired
  private ApplicationService applicationService;

  @Override
  public void run(String... args) throws Exception {
    applicationService.createBook();
    applicationService.print();

    applicationService.updateBook();
    applicationService.print();

    applicationService.reAddBooks();
    applicationService.print();
  }
}
