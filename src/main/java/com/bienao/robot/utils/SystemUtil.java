package com.bienao.robot.utils;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;

public class SystemUtil {

    //D:\robot
    private static String projectPath = "";

    public static String getProjectPath(){
        if (StringUtils.isEmpty(projectPath)){
            File file = new File("");// 参数为空
            try {
                projectPath = file.getCanonicalPath();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return projectPath;
    }
}
