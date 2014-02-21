package com.zhangyue.hella.engine.web.service;

import java.util.HashSet;
import java.util.Set;

import com.zhangyue.hella.common.conf.Configuration;
import com.zhangyue.hella.common.util.MD5Util;

public class SystemAdministrator {

    private static Set<String> adminSet = null;
    private static Set<String> passwordSet = null;

    public static void initialize(Configuration schedConf) {
        adminSet = new HashSet<String>();
        passwordSet=new HashSet<String>();

        String admins = schedConf.get("administrator.user");
        if (admins != null) {
            String[] adminArr = admins.split(",");
            for (String str : adminArr) {
                adminSet.add(str);
            }
        }
        String passwords = schedConf.get("administrator.password");
        if (passwords != null) {
            String[] passwordArr = passwords.split(",");
            for (String str : passwordArr) {
                passwordSet.add(str);
            }
        }
    }

    public static boolean doValidation(String user,String password) throws Exception {
        password=MD5Util.doMD5(password.getBytes());
        if(adminSet.contains(user)&& passwordSet.contains(password)){
            return true;
        }
        return false;
    }
}
