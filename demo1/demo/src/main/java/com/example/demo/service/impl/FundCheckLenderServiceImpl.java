package com.example.demo.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.service.FundCheckLenderService;
import com.example.demo.utils.ExcelUtil;
import com.example.demo.utils.IMAPReceiveMailUtil;
import com.example.demo.utils.OSSUtil;
import com.example.demo.utils.SendMailUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.mail.BodyPart;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.MimeUtility;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wmy
 */
@Service
public class FundCheckLenderServiceImpl implements FundCheckLenderService {

    private static Logger log = LoggerFactory.getLogger(FundCheckLenderServiceImpl.class);

    @Resource
    private IMAPReceiveMailUtil imapReceiveMailUtil;
    @Resource
    private OSSUtil ossUtil;
    @Resource
    private ExcelUtil excelUtil;
    @Resource
    private SendMailUtil sendMailUtil;

    /**
     * oss上传路径
     */
    @Value("${qj.oss.fjLenderFundPath}")
    public String fjLenderFundPath;


    @Override
    public void fundCheckLender(String paramDate) {
        /**
         * 1、从邮件上拉取附件excel
         * 2、上传到oss
         * 3、落库
         */
        try {
            //解析邮件
            Map<String, Object> map = new HashMap<>(3);
            try{
                log.info("开始解析邮件----------------！");
                map = imapReceiveMailUtil.receive(new Date(), new Date());
                log.info("解析邮件结束----------------！");
            }catch (Exception e){
                log.error("解析邮件异常，{}", e);
            }
            int length = (int)map.get("length");
            if(length == 0){
                log.info("没有获取到放款邮件！发送报警邮件！！！");
               try {
                   //（发邮件）
                   log.info("开始发送报警邮件-----------！");
                   /*sendMailUtil.sendEmail(FundCheckConstants.ALARM_MAIL_SUBJECT_LENDER, FundCheckConstants.ALARM_MAIL_CONTENT_LENDER);*/
                   log.info("发送报警邮件结束-----------！");
               }catch (Exception e){
                   log.info("发送报警邮件异常！！！{}", e);
               }
            }
        }catch (Exception e) {
            log.info("异常！{}"+e);
        }
    }


    @Override
    public void saveToOss(Part part) throws Exception{

        if (part.isMimeType("multipart/*")) {
            // 复杂体邮件
            Multipart multipart = (Multipart) part.getContent();
            // 复杂体邮件包含多个邮件体
            int partCount = multipart.getCount();
            for (int i = 0; i < partCount; i++) {
                // 获得复杂体邮件中其中一个邮件体
                BodyPart bodyPart = multipart.getBodyPart(i);
                // 某一个邮件体也有可能是由多个邮件体组成的复杂体
                String disp = bodyPart.getDisposition();
                if (disp != null && (disp.equalsIgnoreCase(Part.ATTACHMENT) || disp.equalsIgnoreCase(Part.INLINE))) {
                    String fileName = bodyPart.getFileName();
                    if (fileName.toLowerCase().indexOf("gb2312") != -1) {
                        fileName = MimeUtility.decodeText(fileName);
                    }
                    if (fileName.toLowerCase().indexOf("gbk") != -1) {
                        fileName = MimeUtility.decodeText(fileName);
                    }
                    if (fileName.toLowerCase().indexOf("utf-8") != -1) {
                        fileName = MimeUtility.decodeText(fileName);
                    }
                    //文件流
                    InputStream is = bodyPart.getInputStream();
                    //截取不带后缀的文件名
                    String fileSaveName = fileName.substring(0, fileName.indexOf("."));
                    log.info("放款excel上传到oss！！！！！！！路径为{}", fjLenderFundPath);
                    ossUtil.uploadExcelInputStreamFile(is, fileName, fileName, "fileType", "fileExt", 0, "busiNo", "batchNo", fjLenderFundPath);
                    //解析excel
                    log.info("开始解析excel！！！！！");
                    String key = fjLenderFundPath + fileSaveName + ".xlsx";
                    InputStream lenderInputStream = ossUtil.getSSOInputStream(key);
                    List<List<String>> data = excelUtil.writeWithoutHead(lenderInputStream);
                    log.info("解析excel结束！！！！！");
                    log.info("解析excel文件生成的数据集合：{}", JSONObject.toJSON(data));
                } else if (bodyPart.isMimeType("multipart/*")) {
                    saveToOss(bodyPart);
                } else {
                    String contentType = bodyPart.getContentType();
                    if (contentType.indexOf("name") != -1 || contentType.indexOf("application") != -1) {
                       //上传excel到oss
                       // ossUtil.uploadExcelInputStreamFile();
                    }
                }
            }
        } else if (part.isMimeType("message/rfc822")) {
            saveToOss((Part) part.getContent());
        }
    }

}
