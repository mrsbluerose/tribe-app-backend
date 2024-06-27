package com.savvato.tribeapp.utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class ValidationUtil {
  private ValidationUtil() {}

  public static boolean isPhoneValid(String str) {

    log.warn("ValidationUtil::isPhoneValid()    NEEDS    TO    BE     IMPLEMENTED    !!!");

    return str != null;
  }

  public static boolean isEmailValid(String str) {

    log.warn("ValidationUtil::isEmailValid()    NEEDS    TO    BE     IMPLEMENTED    !!!");

    return str != null;
  }
}
