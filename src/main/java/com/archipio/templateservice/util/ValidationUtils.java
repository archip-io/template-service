package com.archipio.templateservice.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ValidationUtils {

  public static final String TEMPLATE_CODE_PATTERN = "^[a-z0-9_]*$";
  public static final int TEMPLATE_CODE_MIN_LENGTH = 1;
  public static final int TEMPLATE_CODE_MAX_LENGTH = 255;
  public static final int TEMPLATE_NAME_MIN_LENGTH = 1;
  public static final int TEMPLATE_NAME_MAX_LENGTH = 255;
}
