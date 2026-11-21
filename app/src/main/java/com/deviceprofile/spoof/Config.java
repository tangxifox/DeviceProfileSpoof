package com.deviceprofile.spoof;
final class Config {
  static final String MODULE="com.deviceprofile.spoof", FILE="device_profile";
  static final String[] KEYS={"manufacturer","brand","model","name","device"};
  static final String[] FIELDS={"MANUFACTURER","BRAND","MODEL","PRODUCT","DEVICE"};
  static final String[] LABELS={"制造商","品牌","型号","产品名称","设备代号"};
  static final String[] DEFAULTS={"Xiaomi","Xiaomi","23127PN0CC","houji","houji"};
  static String key(int i){return "ro.product."+KEYS[i];}
  static boolean valid(String v){return v!=null && !v.trim().isEmpty() && v.getBytes(java.nio.charset.StandardCharsets.UTF_8).length<=91 && v.codePoints().noneMatch(Character::isISOControl);}
}
