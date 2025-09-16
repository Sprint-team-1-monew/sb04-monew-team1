package com.codeit.monew.global.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.stream.Collectors;

public class LogParameterFormatter {

  public static String getFormattedParameters(Object[] datas) {
    if (datas == null || datas.length == 0) {
      return "";
    }

    StringBuilder sb = new StringBuilder();
    for (Object data : datas) {
      if (data == null) {
        sb.append("null");
      } else {
        sb.append(formatSingleParameterSafely(data));
      }
      sb.append(", ");
    }

    return sb.length() > 2 ? sb.substring(0, sb.length() - 2) : "";
  }

  private static String formatSingleParameterSafely(Object data) {
    try {
      Class<?> clazz = data.getClass();
      String name = clazz.getSimpleName();

      // JDK 내장 클래스들은 toString() 사용 (UUID, LocalDateTime 등)
      if (isJdkInternalClass(clazz)) {
        return formatField(name, data.toString());
      }

      // 원시타입 래퍼 클래스나 String인 경우
      if (isPrimitiveWrapper(clazz) || data instanceof String) {
        return formatField(name, data);
      }

      // 사용자 정의 클래스만 리플렉션으로 필드 분석
      return formatUserDefinedObject(name, data);

    } catch (Exception e) {
      return data.getClass().getSimpleName() + "=<formatting_error>";
    }
  }

  private static String formatUserDefinedObject(String className, Object data) {
    StringBuilder sb = new StringBuilder();
    sb.append(className).append(" = (");

    Field[] fields = data.getClass().getDeclaredFields();
    String fieldsStr = Arrays.stream(fields)
        .map(field -> formatFieldSafely(field, data))
        .filter(result -> result != null)
        .collect(Collectors.joining(", "));

    sb.append(fieldsStr).append(")");
    return sb.toString();
  }

  private static String formatFieldSafely(Field field, Object obj) {
    try {
      field.setAccessible(true);

      int modifiers = field.getModifiers();
      Object value;

      if (Modifier.isStatic(modifiers)) {
        value = field.get(null);
      } else {
        value = field.get(obj);
      }

      if (isSensitiveField(field.getName())) {
        return formatFieldWithModifier(field.getName(), "***MASKED***", modifiers);
      }

      return formatFieldWithModifier(field.getName(), value, modifiers);

    } catch (Exception e) {
      // access_denied 오류를 로그에서 제거하고 필드 자체를 생략
      return null; // null 반환하면 filter에서 제외됨
    }
  }

  private static String formatFieldWithModifier(String fieldName, Object value, int modifiers) {
    StringBuilder prefix = new StringBuilder();

    if (Modifier.isStatic(modifiers)) {
      prefix.append("static ");
    }

    if (Modifier.isFinal(modifiers)) {
      prefix.append("final ");
    }

    return String.format("%s%s=%s", prefix.toString(), fieldName, value);
  }

  private static boolean isJdkInternalClass(Class<?> clazz) {
    String packageName = clazz.getPackageName();
    return packageName.startsWith("java.") ||
        packageName.startsWith("javax.") ||
        packageName.startsWith("sun.") ||
        packageName.startsWith("com.sun.") ||
        packageName.startsWith("jdk.");
  }

  private static boolean isPrimitiveWrapper(Class<?> clazz) {
    return clazz == Integer.class || clazz == Long.class ||
        clazz == Double.class || clazz == Float.class ||
        clazz == Boolean.class || clazz == Character.class ||
        clazz == Byte.class || clazz == Short.class;
  }

  private static boolean isSensitiveField(String fieldName) {
    String lowerName = fieldName.toLowerCase();
    return lowerName.contains("password");
  }

  private static String formatField(String name, Object data) {
    return String.format("%s=%s", name, data);
  }
}

