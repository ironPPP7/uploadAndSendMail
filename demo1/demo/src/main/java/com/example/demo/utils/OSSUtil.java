package com.example.demo.utils;

import com.alibaba.fastjson.JSON;
import com.aliyun.oss.ClientException;
import com.aliyun.oss.HttpMethod;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.utils.IOUtils;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.PutObjectResult;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.Date;

/**
 * @Description OSS操作工具类
 * @Author liuh
 * @Date 2019/11/29
 **/
@Getter
@Component
@Slf4j
@RefreshScope
public class OSSUtil {
    @Value("${qj.oss.endpoint}")
    private String endpoint;
    @Value("${qj.oss.accessKeyId}")
    private String accessKeyId;
    @Value("${qj.oss.accessKeySecret}")
    private String accessKeySecret;
    @Value("${qj.oss.bucketName}")
    private String bucketName;
    @Value("${qj.oss.contractFilePath}")
    private String contractFilePath;
    @Value("${qj.oss.photoFilePath}")
    private String photoFilePath;


    /**
     * @param inputStream
     * @param fileName  文件名
     * @param fileSaveName 存储文件名称
     * @param fileType   文件类型：11TXT、12PDF、13WORD、14EXCEL
     * @param fileExt  文件扩展名
     * @param busiType 业务类型
     * @param busiNo 业务编号
     * @param busiSubNo 业务编号
     * @return
     */
    public boolean uploadInputStreamFile(InputStream inputStream, String fileName,String fileSaveName,String fileType,String fileExt,int busiType,String busiNo,String busiSubNo) {
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        String path = "";
        String key = "";
        if(busiType == 1 || busiType==2){
            path = photoFilePath;
        }else {
            path = contractFilePath;
        }
        key = path + fileSaveName + "." + fileExt;
        try {
            PutObjectResult putObjectResult = ossClient.putObject(bucketName, key, inputStream);
            log.info("上传文件至OSS返回结果:{}",JSON.toJSONString(putObjectResult));
            /*BaseAttach baseAttach = new BaseAttach();
            baseAttach.setId(SnowflakeIdWorker.nextId());
            baseAttach.setBusiType(busiType);
            baseAttach.setBusiNo(busiNo);
            baseAttach.setBusiSubNo(busiSubNo);
            baseAttach.setFileExt(fileExt);
            baseAttach.setFileName(fileName);
            baseAttach.setFileType(fileType);
            baseAttach.setFileSaveName(fileSaveName);
            baseAttach.setFileSavePath(path);
            int insertCount = baseAttachMapper.insertSelective(baseAttach);
            log.info("busiNo业务号{}文件插入附件表条数:{}",busiNo,insertCount);*/
            return true;
        } catch (Exception e) {
            log.error("上传文件至OSS异常:{}",e);
            return false;
        } finally {
            IOUtils.safeClose(inputStream);
            ossClient.shutdown();
        }
    }

    /**
     * @desc 上传URL地址文件到指定路径
     * @author liuh
     * @create 2019/11/29
     **/
    public boolean uploadExcelInputStreamFile(InputStream inputStream, String fileName,String fileSaveName,String fileType,String fileExt,int busiType,String busiNo,String busiSubNo, String path) {
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try {
            String key = path + fileSaveName;
            PutObjectResult putObjectResult = ossClient.putObject(bucketName, key, inputStream);
            log.info("上传文件至OSS返回结果:{}",JSON.toJSONString(putObjectResult));
            //修改attach表重复数据为无效
            log.info("修改attach表重复数据为无效,文件名为：----{}, busiNo为：{}", fileName, busiNo);
           /* Example example = new Example(BaseAttach.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andEqualTo("fileName", fileName);
            criteria.andEqualTo("busiNo", busiNo);
            BaseAttach baseAttachNew = new BaseAttach();
            baseAttachNew.setIsValid("N");
            int count = baseAttachMapper.updateByExampleSelective(baseAttachNew,example);
            log.info("修改attach表结束！修改成功条数：{}", count);
            BaseAttach baseAttach = new BaseAttach();
            baseAttach.setId(SnowflakeIdWorker.nextId());
            baseAttach.setBusiType(busiType);
            baseAttach.setBusiNo(busiNo);
            baseAttach.setBusiSubNo(busiSubNo);
            baseAttach.setFileExt(fileExt);
            baseAttach.setFileName(fileName);
            baseAttach.setFileType(fileType);
            baseAttach.setFileSaveName(fileSaveName);
            baseAttach.setFileSavePath(path);
            int insertCount = baseAttachMapper.insertSelective(baseAttach);
            log.info("busiNo业务号{}文件插入附件表条数:{}",busiNo,insertCount);*/
            return true;
        } catch (Exception e) {
            log.error("上传文件至OSS异常:{}",e);
            return false;
        } finally {
            IOUtils.safeClose(inputStream);
            ossClient.shutdown();
        }
    }

    /**
     * @param inputStream
     * @param fileName  文件名
     * @param fileSaveName 存储文件名称
     * @param fileType   文件类型：11TXT、12PDF、13WORD、14EXCEL
     * @param fileExt  文件扩展名
     * @param busiType 业务类型
     * @param busiNo 业务编号
     * @param busiSubNo 业务编号
     * @param path 文件名字
     * @return
     */
    public boolean uploadInputStreamFile(InputStream inputStream, String fileName,String fileSaveName,String fileType,String fileExt,int busiType,
                                         String busiNo,String busiSubNo,String path,String remark) {
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try {
            String key = path + fileSaveName;
            PutObjectResult putObjectResult = ossClient.putObject(bucketName, key, inputStream);
            log.info("上传文件至OSS返回结果:{}",JSON.toJSONString(putObjectResult));
           /* BaseAttach baseAttach = new BaseAttach();
            baseAttach.setId(SnowflakeIdWorker.nextId());
            baseAttach.setBusiType(busiType);
            baseAttach.setBusiNo(busiNo);
            baseAttach.setBusiSubNo(busiSubNo);
            baseAttach.setFileExt(fileExt);
            baseAttach.setFileName(fileName);
            baseAttach.setFileType(fileType);
            baseAttach.setFileSaveName(fileSaveName);
            baseAttach.setFileSavePath(path);

            baseAttach.setRemark(remark);
            int insertCount = baseAttachMapper.insertSelective(baseAttach);
            log.info("busiNo业务号{}文件插入附件表条数:{}",busiNo,insertCount);*/
            return true;
        } catch (Exception e) {
            log.error("上传文件至OSS异常:{}",e);
            return false;
        } finally {
            IOUtils.safeClose(inputStream);
            ossClient.shutdown();
        }
    }


    /**
     * @desc 上传URL地址文件到指定路径
     * @author liuh
     * @create 2019/11/29
     **/
    public boolean uploadUrlFile(String fileUrl, String fileName,String fileSaveName,String fileType,String fileExt,int busiType,String busiNo,String busiSubNo) {
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        String key = contractFilePath + fileSaveName + "." + fileExt;
        InputStream inputStream = null;
        try {
            inputStream = new URL(fileUrl).openStream();
            PutObjectResult putObjectResult = ossClient.putObject(bucketName, key, inputStream);
            log.info("上传文件至OSS返回结果:{}",JSON.toJSONString(putObjectResult));
           /* BaseAttach baseAttach = new BaseAttach();
            baseAttach.setId(SnowflakeIdWorker.nextId());
            baseAttach.setBusiType(busiType);
            baseAttach.setBusiNo(busiNo);
            baseAttach.setBusiSubNo(busiSubNo);
            baseAttach.setFileExt(fileExt);
            baseAttach.setFileName(fileName);
            baseAttach.setFileType(fileType);
            baseAttach.setFileSaveName(fileSaveName);
            baseAttach.setFileSavePath(contractFilePath);
            int insertCount = baseAttachMapper.insertSelective(baseAttach);
            log.info("busiNo业务号{}文件插入附件表条数:{}",busiNo,insertCount);*/
            return true;
        } catch (Exception e) {
            log.error("上传文件至OSS异常:{}",e);
            return false;
        } finally {
            IOUtils.safeClose(inputStream);
            ossClient.shutdown();
        }
    }

    /**
     * @desc 获取指定路径文件访问临时URL
     * @author liuh
     * @create 2019/11/29
     **/
    public URL generatePresignedUrl(String key) throws ClientException {
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        Date expiration = new Date(System.currentTimeMillis() + 3600 * 1000);
        return ossClient.generatePresignedUrl(bucketName, key, expiration, HttpMethod.GET);
    }

    /**
     * 根据下载链接获取文件名
     * @param urlStr
     * @return
     */
    public static String getFileName(String urlStr){
        String fileName = null;
        try {
            URL url = new URL(urlStr);
            URLConnection uc = url.openConnection();
            fileName = uc.getHeaderField("Content-Disposition");
            fileName = new String(fileName.getBytes("ISO-8859-1"), "GBK");
            fileName = URLDecoder.decode(fileName.substring(fileName.indexOf("filename=")+9),"UTF-8");
            log.info("文件名为：" + fileName + "  大小" + (uc.getContentLength()/1024)+"KB");
            InputStream inputStream = uc.getInputStream();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileName;
    }

    /**
     * 从 oss服务器上获取流
     * @param path
     * @return
     */
    public InputStream getSSOInputStream(String path){
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        boolean exist = ossClient.doesObjectExist(bucketName, path);
        if (exist){
            OSSObject ossObject = ossClient.getObject(bucketName, path);
            InputStream content = ossObject.getObjectContent();
            return content;
        }else {
            return null;
        }
    }

    /**
     * 判断指定路径下是否有该文件
     * @param path
     * @return
     */
    public boolean doesObjectExist(String path){
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        boolean exist = ossClient.doesObjectExist(bucketName, path);
        return exist;
    }

    /**
     * 根据路径删除
     * @param pathandFileName
     */
    public void deleteFileName(String pathandFileName){
        OSS ossClient = null;
        try {
            ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            ossClient.deleteObject(bucketName, pathandFileName);
        }catch (Exception e){
            log.info("删除oss异常异常信息："+ ExceptionUtils.getFullStackTrace(e));
        }
        finally {
            if(ossClient != null){
                ossClient.shutdown();
            }
        }
    }

}
