package com.shanyu.util.uploadFile;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.region.Region;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

@Component
public class QCloudFileUploadUtil {
    @Value("${qcloud.appid}")
    private String appid ;
    @Value("${qcloud.secretid}")
    private String secretid ;
    @Value("${qcloud.secretkey}")
    private String secretkey ;
    @Value("${qcloud.bucket}")
    private String bucket ;
    @Value("${qcloud.region}")
    private String region ;
    @Value("${qcloud.etag}")
    private String etag ;

    public String upload(File file,String filename){
        // 1 初始化用户身份信息(secretId, secretKey)
        COSCredentials cred = new BasicCOSCredentials(secretid, secretkey);
        // 2 设置bucket的区域, COS地域的简称请参照 https://cloud.tencent.com/document/product/436/6224
        ClientConfig clientConfig = new ClientConfig(new Region(region));
        // 3 生成cos客户端
        COSClient cosClient = new COSClient(cred, clientConfig);
        // bucket的命名规则为{name}-{appid} ，此处填写的存储桶名称必须为此格式
        String bucketName = bucket;
        // 简单文件上传, 最大支持 5 GB, 适用于小文件上传, 建议 20 M 以下的文件使用该接口
        // 大文件上传请参照 API 文档高级 API 上传
        // 指定要上传到 COS 上的路径
        if(filename==null){
            filename = "/"+UUID.randomUUID().toString().replaceAll("-", "")+".png";
        }
        else{
            String[] ff = filename.split("/");
            filename = "/"+ff[ff.length-1];
        }
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, filename,file);
        PutObjectResult putObjectResult = cosClient.putObject(putObjectRequest);
        cosClient.shutdown();
        file.deleteOnExit();
        return etag+filename;
    }

    public String upload(MultipartFile file,String filename){
        // 1 初始化用户身份信息(secretId, secretKey)
        COSCredentials cred = new BasicCOSCredentials(secretid, secretkey);
        // 2 设置bucket的区域, COS地域的简称请参照 https://cloud.tencent.com/document/product/436/6224
        ClientConfig clientConfig = new ClientConfig(new Region(region));
        // 3 生成cos客户端
        COSClient cosClient = new COSClient(cred, clientConfig);
        // bucket的命名规则为{name}-{appid} ，此处填写的存储桶名称必须为此格式
        String bucketName = bucket;
        // 简单文件上传, 最大支持 5 GB, 适用于小文件上传, 建议 20 M 以下的文件使用该接口
        // 大文件上传请参照 API 文档高级 API 上传
        // 指定要上传到 COS 上的路径
        File f = null;
        try {
            f=File.createTempFile("tmp", null);
            file.transferTo(f);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String suffix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);
        if(filename!=null){
            delete(filename);
//            String[] ff = filename.split("/");
//            filename = "/"+ff[ff.length-1];
        }
        filename = "/"+UUID.randomUUID().toString().replaceAll("-", "") +"."+suffix;
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, filename,f);
        PutObjectResult putObjectResult = cosClient.putObject(putObjectRequest);
        cosClient.shutdown();
        f.deleteOnExit();
        return etag+filename;
    }

    public void delete(String filename){
        // 1 初始化用户身份信息(secretId, secretKey)
        COSCredentials cred = new BasicCOSCredentials(secretid, secretkey);
        // 2 设置bucket的区域, COS地域的简称请参照 https://cloud.tencent.com/document/product/436/6224
        ClientConfig clientConfig = new ClientConfig(new Region(region));
        // 3 生成cos客户端
        COSClient cosClient = new COSClient(cred, clientConfig);
        // bucket的命名规则为{name}-{appid} ，此处填写的存储桶名称必须为此格式
        String bucketName = bucket;

        String[] ff = filename.split("/");
        filename = ff[ff.length-1];
        System.out.println("删除文件:"+filename);

        // 指定要删除的 bucket 和对象键
        cosClient.deleteObject(bucketName, filename);

        cosClient.shutdown();
    }

    public Integer download(String filePath,HttpServletResponse response){
        // 1 初始化用户身份信息(secretId, secretKey)
        COSCredentials cred = new BasicCOSCredentials(secretid, secretkey);
        // 2 设置bucket的区域, COS地域的简称请参照 https://cloud.tencent.com/document/product/436/6224
        ClientConfig clientConfig = new ClientConfig(new Region(region));
        // 3 生成cos客户端
        COSClient cosClient = new COSClient(cred, clientConfig);
        // bucket的命名规则为{name}-{appid} ，此处填写的存储桶名称必须为此格式
        String bucketName = bucket;
        //从cos的文件路径截取要下载的文件名字
        String[] ss = filePath.split("/");
        String key = ss[ss.length-1];
        // 指定要下载到的本地路径
        File downFile = null;
        FileInputStream in = null;
        OutputStream out = null;
        try {
            downFile = File.createTempFile("tmp",null);
            //设置响应头，控制浏览器下载该文件
            //URLEncoder.encode(key, "ISO-8859-1")   https://blog.csdn.net/aeroleo/article/details/51554243
            response.setContentType("application/force-download");
            response.setHeader("content-disposition", "attachment;filename=" + key);

            // 指定要下载的文件所在的 bucket 和对象键
            GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, key);
            ObjectMetadata downObjectMeta = cosClient.getObject(getObjectRequest, downFile);

            //读取要下载的文件，保存到文件输入流
            in = new FileInputStream(downFile);
            //创建输出流
            out = response.getOutputStream();
            //创建缓冲区
            byte buffer[] = new byte[1024];
            int len = 0;
            //循环将输入流中的内容读取到缓冲区当中
            while((len=in.read(buffer))>0){
                //输出缓冲区的内容到浏览器，实现文件下载
                out.write(buffer, 0, len);
            }

            return null;
        } catch (IOException e) {
            return 0;
        }finally {
            //关闭文件输入流
            try {
                in.close();
                //关闭输出流
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            downFile.deleteOnExit();
            cosClient.shutdown();
        }

    }

}
