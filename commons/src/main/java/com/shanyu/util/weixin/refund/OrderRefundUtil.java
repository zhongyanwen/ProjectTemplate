package com.shanyu.util.weixin.refund;

import com.shanyu.weixin.pay.PayCommonUtil;
import com.shanyu.weixin.pay.XMLUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 退款
 */
@Component
public class OrderRefundUtil {
    @Value("${wxpay.appid}")
    private String appid ;       //微信分配的小程序ID
    @Value("${wxpay.mch_id}")
    private String mch_id ;      //商户号
    @Value("${wxpay.apiCert}")
    public String apiCert ;      //密钥
    @Value("${wxpay.refund_url}")
    public String refund_url ;      //退款接口地址
    @Autowired
    public CertHttpUtil certHttpUtil;

    /**
     * 退款
     * @param id             订单id
     * @param refund_desc   退款原因
     * @param out_trade_no  out_trade_no商户订单号
     * @param total_fee     订单金额
     * @param refund_fee    退款金额
     * @return
     */
    public boolean refund(Integer id,String refund_desc,String out_trade_no,Double total_fee,Double refund_fee){
        Double  realTotal_fee = total_fee*100;  //微信以分为单位所以要乘100，要转成int类型
        Double  realRefund_fee = refund_fee*100;  //微信以分为单位所以要乘100，要转成int类型
        SortedMap<Object, Object> param = new TreeMap<Object, Object>();
        param.put("appid", appid);
        param.put("mch_id", mch_id);
        param.put("nonce_str", System.currentTimeMillis() + "");
        param.put("out_trade_no", out_trade_no);
        // 支付网关生成订单流水
        param.put("out_refund_no", getOutTradeNo());
        param.put("total_fee", realTotal_fee.intValue());// 单位为分
        param.put("refund_fee", realRefund_fee.intValue());// 单位为分
        param.put("refund_desc", refund_desc);    //退款原因
        String sign = PayCommonUtil.createSign("UTF-8", param, apiCert);//最后这个是自己设置的32位密钥
        //转成XML
        param.put("sign",sign);
        //转成XML
        String requestXML = PayCommonUtil.getRequestXml(param);
        //得到含有prepay_id的XML
        String resXml = certHttpUtil.postData(refund_url, requestXML);
        //解析XML存入Map
        Map map = null;
        try{
            map = XMLUtil.doXMLParse(resXml);
        }catch (Exception e){
            e.printStackTrace();
        }

        String returnCode = (String)map.get("return_code");
        if("SUCCESS".equals(returnCode)){
            System.out.println("退款成功");
            // 这里是退款成功

            return true;
        }else {
            System.out.println("退款失败");
            return false;
        }

    }

    //随机获取订单编号
    public static String getOutTradeNo(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        int i = new Random().nextInt(1000)+1;       //随机获取1到1000的正整数
        String out_trade_no = sdf.format(new Date()) + i;     //订单编号
        return out_trade_no;
    }
}
