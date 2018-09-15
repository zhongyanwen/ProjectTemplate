package com.shanyu.util.weixin.webAuthorize;

import com.alibaba.fastjson.JSONObject;
import com.shanyu.assistUtil.HttpUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class WxAuthorizeUtil {
    @Value("${wxwebpage.authorize.appid}")
    private String appid ;
    @Value("${wxwebpage.authorize.appsecret}")
    private String appsecret ;
    @Value("${wxwebpage.authorize.backUrl}")
    private String backUrl ;

    public void authorize(HttpServletResponse response) throws IOException {
        String url = "https://open.weixin.qq.com/connect/oauth2/authorize?appid="+appid
                + "&redirect_uri="+backUrl
                + "&response_type=code"
                + "&scope=snsapi_userinfo"
                + "&state=STATE#wechat_redirect";

        //重定向到授权页面
        System.out.println(url);
        response.sendRedirect(url);
    }

    public Object getUserInfo(String code){
        //获取code后，请求以下链接获取access_token
        String url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=" + appid
                + "&secret=" + appsecret
                + "&code=" + code
                + "&grant_type=authorization_code";

        JSONObject jsonObject = HttpUtil.doGetJson(url);
        String openid = jsonObject.getString("openid");
        //通过openid查看数据库中有没有该用户,有就直接返回该用户信息,没有就去获取
        //业务处理

        String access_token = jsonObject.getString("access_token");
        String refresh_token = jsonObject.getString("refresh_token");
        /*//验证access_token是否失效；展示都不需要
        String chickUrl="https://api.weixin.qq.com/sns/auth?access_token="+access_token+"&openid="+openid;
        JSONObject chickuserInfo = HttpUtil.doGetJson(chickUrl);
        if(!"0".equals(chickuserInfo.getString("errcode"))){
            System.out.println("更新access_token");
            String refreshTokenUrl="https://api.weixin.qq.com/sns/oauth2/refresh_token?appid="+openid+"&grant_type=refresh_token&refresh_token="+refresh_token;

            JSONObject refreshInfo = HttpUtil.doGetJson(chickUrl);
            access_token=refreshInfo.getString("access_token");
        }*/

        // 第四步：拉取用户信息(需scope为 snsapi_userinfo)
        String infoUrl = "https://api.weixin.qq.com/sns/userinfo?access_token="+access_token
                + "&openid="+openid
                + "&lang=zh_CN";
        System.out.println("infoUrl:"+infoUrl);
        JSONObject userInfo = HttpUtil.doGetJson(infoUrl);

        return null;
    }
}
