package com.example.demo.service.impl;

import com.example.demo.service.AssetUploadService;
import com.example.demo.utils.OSSUtil;
import com.example.demo.utils.SFTPUtil;
import com.google.common.collect.Lists;
import com.google.common.primitives.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wmy
 */
@Service
public class AssetUploadServiceImpl implements AssetUploadService {

    /**
     * oss上传路径
     */
    @Value("${qj.oss.jirongAssetPath}")
    public String jirongAssetPath;

    private static Logger log = LoggerFactory.getLogger(AssetUploadServiceImpl.class);

    @Resource
    private OSSUtil ossUtil;

    @Override
    public void assetUpload(String paramDate, String busiType) {
        /**
         * 1、按条件查询出要生成的文件
         * 2、生成csv文件
         * 3、上传oss
         * 4、上传sftp
         * 5、发送邮件
         * */
        List<Object> list = Lists.newArrayList();
        try {
            //3、遍历每个list，根据fund_busi_id查找对账表的数据，每个资产方的每种业务类型生成一个excel文件，上传到oss和sftp
            //遍历数据
            getExcel(list);
            Map<String, String> map = new HashMap<>();
            //添加汇总文件附件（从oss上拿）
            setMailCollectFile(map, paramDate,busiType);

            //发送邮件
            List<Map<String,String>> files = new ArrayList<>();
            if(map.size() != 0){
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    System.out.println("key = " + entry.getKey() + ", value = " + entry.getValue());
                    Map<String,String> params = new HashMap<>();
                    params.put("url", entry.getKey());
                    params.put("name", entry.getValue());
                    params.put("description","邮件附件");
                    files.add(params);
                }
               //发送邮件 todo（有些发送邮件的方法在本地可用，发布到linux上会出现发送失败的情况。本次发送邮件使用的是公司内部独立的消息中心发送邮件）
            }
        }catch (Exception e){
            log.error("生成csv文件异常！！！", e.getMessage());
        }
    }


    /**添加汇总文件*/
    private void setMailCollectFile(Map<String, String> map, String paramDate, String busiType) {
        String lender = "nacos路径+文件名带后缀";
        if (ossUtil.doesObjectExist(lender)) {
            URL lenderUrl = ossUtil.generatePresignedUrl(lender);
            map.put(lenderUrl.toString(), "文件名.xlsx");
        }
    }

    private void getExcel(List<Object> assetLists){
        log.info("生成对账数据csv文件开始!!!!!");
        //创建csv文件头
        //代偿对账文件
        List<String> compstLists = new ArrayList<>();
        compstLists.add("交易订单号,合同ID(借据编号),交易类型,操作类型,期数,应还日期,交易金额,应还本金,应还利息,交易时间");
        for(Object checkAcctDetail : assetLists){
            //根据数据生成csv文件
            String results = "\\t" + "123" + "," +
                    "\\t" + "234" + "," + "\\t" + "123" + "," +
                    "haaha" + "," + "100" + "," +
                    "2020-08-08" + "," + "\\t" + "123" + "," +
                    "2020-09-09" + "," + "way";
            compstLists.add(results);
        }
        //上传sftp和oss
        log.info("开始上传sftp和oss！！！！！");
        uploadSftpAndOss(compstLists);
        log.info("生成对账数据csv文件结束!!!!!");
    }

    private void uploadSftpAndOss(List<String> compstLists) {
        SFTPUtil sftpUtil = null;
        String dataResult = "";
        try {
            log.info("登录sftp,用户名{},密码{},host{},端口号{}", "");
            sftpUtil = new SFTPUtil("", "", "",1);
            sftpUtil.login();
            if(compstLists.size() > 1){
                for(int i = 0; i<compstLists.size(); i++){
                    dataResult = dataResult + compstLists.get(i) + "\r\n";
                }
                //添加bom头，如果不添加office打开csv文件会出现乱码
                byte[] uft8bom={(byte)0xef,(byte)0xbb,(byte)0xbf};
                byte[] data = dataResult.getBytes("UTF-8");
                byte[] bytes = Bytes.concat(uft8bom,data);
                ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
                String fileName ="测试123.csv";
                sftpUtil.upload("path", fileName, inputStream);
            }
            log.info("上传sftp结束！！！！！");


            //获取文件流
            Map<String, InputStream> fileMap = sftpUtil.listFiles("path");
            for (String fileName1 : fileMap.keySet()) {
                InputStream value = fileMap.get(fileName1);
                //获取文件名（不带后缀）
                log.info("获取文件名{}", fileName1);
                if(value != null){
                    //上传文件流到oss
                    ossUtil.uploadExcelInputStreamFile(value, fileName1, fileName1, "", "fileExt", 0, "", "busiSubNo", "ossPath");
                }
            }
            log.info("上传OSS结束！！！！！！！！！！！！！");
        }catch (Exception e){
            log.error("上传sftp异常！！{}", e);
        }finally {
            sftpUtil.logout();
        }
    }

}
