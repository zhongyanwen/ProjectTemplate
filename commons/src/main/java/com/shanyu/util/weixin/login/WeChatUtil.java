package com.shanyu.util.weixin.login;

import com.shanyu.assistUtil.JsonUtils;
import com.shanyu.weixin.assist.AuthorizeVO;
import com.shanyu.weixin.assist.WeChatUser;
import com.shanyu.weixin.pay.AesCbcUtil;
import com.squareup.okhttp.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.Base64Utils;
import org.springframework.util.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.AlgorithmParameters;
import java.util.Map;


@Component
public class WeChatUtil {
    private final Logger logger = LoggerFactory.getLogger(WeChatUtil.class);

    @Value("${wx.url}")
    private String url;

    @Value("${wx.app-id}")
    private String appId;

    @Value("${wx.app-secret}")
    private String appSecret;

    /**
     * wx.login授权获得用户信息
     */
    public WeChatUser login(String code) {
        //请求参数
        String params = "appid=" + appId + "&secret=" + appSecret + "&js_code=" + code + "&grant_type=authorization_code";
        String requestURL = url + "?" + params;
        //发送请求
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url(requestURL).build();
        Call call = okHttpClient.newCall(request);
        WeChatUser userInfo = null;
        try {
            Response res = call.execute();
            ResponseBody responseBody = res.body();
            logger.info(" responseBody is {}", responseBody);
            Assert.notNull(responseBody, "登录失败");
            //解析响应内容（转换成json对象）
            String response = responseBody.string();
            logger.info(" response is {} ", response);
            Map<String, Object> userInfoMap = JsonUtils.json2object(response, Map.class, String.class, Object.class);
            logger.info(" userInfoMap is {} ", userInfoMap.toString());
            if (!userInfoMap.isEmpty()) {
                userInfo = new WeChatUser();
                userInfo.setOpenId(userInfoMap.get("openid").toString());
            }
        } catch (IOException e) {
            logger.error(" login get user info error {}", e);
        }
        return userInfo;
    }

    /**
     * wx.authorize授权获得用户信息
     *
     * @param authorizeVO
     * @return userInfo
     */
    @Transactional
    public WeChatUser authorize(AuthorizeVO authorizeVO) {
        //请求参数
        String params = "appid=" + appId + "&secret=" + appSecret + "&js_code=" + authorizeVO.getCode() + "&grant_type=authorization_code";
        String requestURL = url + "?" + params;
        //发送请求
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url(requestURL).build();
        Call call = okHttpClient.newCall(request);

        WeChatUser userInfo = null;
        try {
            Response res = call.execute();
            ResponseBody responseBody = res.body();
            logger.info(" responseBody is {}", responseBody);
            Assert.notNull(responseBody, "授权失败");

            String response = responseBody.string();
            //解析响应内容（转换成json对象）
            Map<String, Object> map = JsonUtils.json2object(response, Map.class, String.class, Object.class);
            //获取会话密钥（session_key）
            String sessionKey = map.get("session_key").toString();
            //对data进行AES解密
//            String result = decrypt(authorizeVO.getData(), sessionKey, authorizeVO.getIv());
            String result = AesCbcUtil.decrypt(authorizeVO.getData(),sessionKey,authorizeVO.getIv(),"UTF-8");

            if (!StringUtils.isEmpty(result)) {
                Map<String, Object> userInfoMap = JsonUtils.json2object(result, Map.class, String.class, Object.class);

                logger.info(" userInfoMap {}",userInfoMap);
                userInfo = new WeChatUser();
                userInfo.setOpenId(userInfoMap.get("openId").toString());
                userInfo.setNickname(userInfoMap.get("nickName").toString());
                userInfo.setGender(Integer.parseInt(userInfoMap.get("gender").toString()));
                userInfo.setCity(userInfoMap.get("city").toString());
                userInfo.setProvince(userInfoMap.get("province").toString());
//                userInfo.setUnionId(userInfoMap.get("unionId").toString());
                userInfo.setAvatar(userInfoMap.get("avatarUrl").toString());
            }
        } catch (Exception e) {
            logger.error(" authorize get user info error {}", e);
        }
        logger.info(" userInfo is {} ", userInfo);
        return userInfo;
    }

        /**
     * AES解密
     *
     * @param data 密文
     * @param key  秘钥
     * @param iv   偏移量
     * @return
     */
    private String decrypt(String data, String key, String iv) {
        //待加密数据,加密秘钥,偏移量
        byte[] dataByte = Base64Utils.decode(data.getBytes()), keyByte = Base64Utils.decode(key.getBytes()),
                ivByte = Base64Utils.decode(iv.getBytes());
        try {
//            Cipher.getInstance("AES/DES/CBC/PKCS5Padding");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            SecretKeySpec spec = new SecretKeySpec(keyByte, "AES");
            AlgorithmParameters parameters = AlgorithmParameters.getInstance("AES");
            parameters.init(new IvParameterSpec(ivByte));
            cipher.init(Cipher.DECRYPT_MODE, spec, parameters);
            byte[] resultByte = cipher.doFinal(dataByte);

            if (null != resultByte && resultByte.length > 0) {
                String result = new String(resultByte, "UTF-8");
                logger.error(" result is {} ", result);
                return result;
            }
        } catch (Exception e) {
            logger.error(" decrypt error {} ", e);
        }
        return null;
    }

}
