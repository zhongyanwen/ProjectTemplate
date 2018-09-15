package com.shanyu.shiro;

import sun.misc.BASE64Encoder;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ShiroUtil {
    /**
     * 获取md5加密后的密码
     * @param password
     * @return
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     */
    public static String getEncryptedPassword(String password){
        try{
            MessageDigest md5=MessageDigest.getInstance("MD5");
            BASE64Encoder base64en = new BASE64Encoder();
            //加密后的字符串
            String newPassword=base64en.encode(md5.digest(password.getBytes("utf-8")));
            return newPassword;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
