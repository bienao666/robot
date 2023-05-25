package com.bienao.robot.Constants;

import java.util.regex.Pattern;

public class PatternConstant {
    //日期
    public static Pattern lowerDatePattern = Pattern.compile("/Date\\((\\d+)\\+");
    //京东ck
    public static Pattern ckPattern = Pattern.compile("pt_key=(.+?);pt_pin=(.+?);");
    //京东ck
    public static Pattern ckPattern2 = Pattern.compile("(.+?)pt_key=(.+?);pt_pin=(.+?);(.+?)");
    //京东ckpin
    public static Pattern jdPinPattern = Pattern.compile("pt_pin=(.+?);");
    //京东标准备注
    public static Pattern jdRemarkPattern = Pattern.compile("(.+?)@@(.+?)@@(.+)");

}
