package com.zhangyue.hella.common.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 
 * @Descriptions The class MD5Util.java's implementation：计算字符串的MD5值
 * @author scott 
 * @date 2013-8-21 上午11:39:39
 * @version 1.0
 */
public class MD5Util {
    public static String doMD5(byte[] value) throws Exception{ 
        byte[] buf;
        try { 
            MessageDigest md = MessageDigest.getInstance("MD5"); 
            md.update(value);      
            buf=md.digest(); 
        } catch (NoSuchAlgorithmException e) { 
            throw new Exception("Can't find algorithm of MD5");
        } 
        StringBuffer sb=new StringBuffer();
        for(byte b:buf){
            sb.append(byteToHex(b, 2));
        }
        return sb.toString(); 
    }
    public static String byteToHex(byte value, int minlength) { 
        String s = Integer.toHexString(value & 0xff); 
        if (s.length() < minlength) { 
            for (int i = 0; i < (minlength - s.length()); i++) 
                s = "0" + s; 
        } 
        return s; 
    } 
}
