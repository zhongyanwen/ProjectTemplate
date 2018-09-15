package com.shanyu.util.weixin.payutil;

import com.shanyu.weixin.pay.HttpUtil;
import com.shanyu.weixin.pay.PayCommonUtil;
import com.shanyu.weixin.pay.XMLUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 统一下单
 */
@Component
public class PayUtil {
    @Value("${wxpay.appid}")
    private String appid ;       //微信分配的小程序ID
    @Value("${wxpay.mch_id}")
    private String mch_id ;      //商户号
    @Value("${wxpay.spbill_create_ip}")
    private String spbill_create_ip ;    //终端IP
    @Value("${wxpay.notify_url}")
    private String notify_url ;      //支付返回地址
    @Value("${wxpay.trade_type}")
    private String trade_type;      //交易类型
    @Value("${wxpay.signType}")
    private String signType ;      //加密方法
    @Value("${wxpay.createOrder_url}")
    private String createOrder_url ;         //支付的url
    @Value("${wxpay.apiCert}")
    public String apiCert ;      //密钥

    public Map<Object,Object> createOrder(String openid, Double price,String body,String out_trade_no)throws Exception{
        Double  reaPprice = price*100;  //微信以分为单位所以要乘100，要转成int类型
        SortedMap<Object, Object> packageParams = new TreeMap<Object, Object>();
        packageParams.put("appid", appid);
        packageParams.put("mch_id", mch_id);
        packageParams.put("nonce_str", new Date().getTime());   //时间戳
        packageParams.put("body", body);                    //支付主体
        packageParams.put("out_trade_no", out_trade_no);    //编号
        packageParams.put("total_fee", reaPprice.intValue());          //价格
        packageParams.put("spbill_create_ip",spbill_create_ip);
        packageParams.put("notify_url", notify_url);
        packageParams.put("trade_type", trade_type);
        packageParams.put("openid", openid);        //openid
        //获取sign
        String sign = PayCommonUtil.createSign("UTF-8", packageParams, apiCert);//最后这个是自己设置的32位密钥
        packageParams.put("sign", sign);
        //转成XML
        String requestXML = PayCommonUtil.getRequestXml(packageParams);

        //得到含有prepay_id的XML
        String resXml = HttpUtil.postData(createOrder_url, requestXML);

        //解析XML存入Map
        Map map = XMLUtil.doXMLParse(resXml);

        // String return_code = (String) map.get("return_code");
        //时间戳
        String times = System.currentTimeMillis() + "";
        //得到prepay_id
        String prepay_id = (String) map.get("prepay_id");
        SortedMap<Object, Object> packageP = new TreeMap<Object, Object>();
        packageP.put("appId", appid);  //注意，这里是appId,上面是appid，真怀疑写这个东西的人。。。
        packageP.put("nonceStr", times);//时间戳
        packageP.put("package", "prepay_id=" + prepay_id);//必须把package写成 "prepay_id="+prepay_id这种形式
        packageP.put("signType", signType);    //paySign加密
        packageP.put("timeStamp", String.valueOf(new Date().getTime()));
        //得到paySign
        String paySign = PayCommonUtil.createSign("UTF-8", packageP, apiCert);
        packageP.put("paySign", paySign);

        return packageP;
    }

    public static String getOutTradeNo(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        int i = new Random().nextInt(1000)+1;       //随机获取1到1000的正整数
        String out_trade_no = sdf.format(new Date()) + i;     //订单编号
        return out_trade_no;
    }
}
