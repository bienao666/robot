package com.bienao.robot.utils;

import com.alibaba.druid.util.StringUtils;

import java.util.regex.Pattern;

public class VerifyUtil {

    public static boolean verifyPhone(String phone) {
        String regex = "^[1]([3-9])[0-9]{9}$";
        boolean isMatch = false;
        if (StringUtils.isEmpty(phone)) {
            System.out.println("手机号不能为空");
        } else if (phone.length() != 11) {
            System.out.println("手机号应为11位数");
        } else {
            isMatch = Pattern.matches(regex, phone);
        }
        return isMatch;
    }
}
