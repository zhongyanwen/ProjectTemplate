package com.shanyu.util.uploadFile;

import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.shanyu.assistUtil.copy.VideoVo;
import com.shanyu.util.vo.QiNiuVo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
public class QiniuUtil {
    @Value("${qiniu.access-key}")
    private String accessKey;

    @Value("${qiniu.secret-key}")
    private String secretKey;

    @Value("${qiniu.bucket}")
    private String bucket;

    @Value("${qiniu.link-address}")
    private String address;


    public String getToken() {
        //密钥配置
        Auth auth = Auth.create(accessKey, secretKey);
        //获取token并返回
        return auth.uploadToken(bucket);
    }


    //上传视频
    public VideoVo upload(MultipartFile file) {
        System.out.println("sss----:"+file.getSize());
        //构造一个带指定Zone对象的配置类
        Configuration cfg = new Configuration(Zone.zone0());
        //...其他参数参考类注释
        UploadManager uploadManager = new UploadManager(cfg);
        //...生成上传凭证，然后准备上传
        String suffix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);
        String key = UUID.randomUUID().toString().replaceAll("-", "")+"."+suffix;

        try {
            System.out.println("dddddddddd-----"+file);
            Response response = uploadManager.put(file.getBytes(), key, getToken());
            //解析上传成功的结果
            QiNiuVo qiniu = response.jsonToObject(QiNiuVo.class);
            //视频地址
            String videoUrl = address +"/"+key;
            System.out.println(videoUrl);
            //截取视频图片
            String imageUrl = videoUrl + "?vframe/jpg/offset/1/w/520/h/360";
            VideoVo videoVo = new VideoVo();
            videoVo.setVideoUrl(videoUrl);
            videoVo.setImageUrl(imageUrl);
            return videoVo;
        } catch (Exception ex) {
            return null;
        }
    }


    //删除
    public void delete(String fileUrl){
        Auth auth = Auth.create(accessKey, secretKey);
        Configuration config = new Configuration(Zone.autoZone());
        BucketManager bucketMgr = new BucketManager(auth, config);
        //指定需要删除的文件，和文件所在的存储空间
        String bucketName = "java-bucket";
        String[] ff = fileUrl.split("/");
        String filename = ff[ff.length-1];
        try {
            bucketMgr.delete(bucketName, filename);//当前为7.2.1；  7.2.2后才能传多个key ，即：第二个参数为数组 (String... deleteTargets)
        } catch (QiniuException e) {
            e.printStackTrace();
        }
    }
}
