package com.example.demo.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.service.FundCheckRepayAndCompstService;
import com.example.demo.utils.ExcelUtil;
import com.example.demo.utils.OSSUtil;
import com.example.demo.utils.SFTPUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * @author wmy
 */
@Service
public class FundCheckRepayAndCompstServiceImpl implements FundCheckRepayAndCompstService {
    /**
     * oss上传路径
     */
    @Value("${qj.oss.fjRepayFundPath}")
    public String fjRepayFundPath;

    @Value("${fengjr.sftp.host}")
    private String fengjr_sftp_host;
    @Value("${fengjr.sftp.port}")
    private Integer fengjr_sftp_port;
    @Value("${fengjr.sftp.username}")
    private String fengjr_sftp_username;
    @Value("${fengjr.sftp.password}")
    private String fengjr_sftp_password;
    @Value("${fengjr.sftp.dir}")
    private String fengjr_sftp_dir;


    private static Logger log = LoggerFactory.getLogger(FundCheckRepayAndCompstServiceImpl.class);

    @Resource
    private OSSUtil ossUtil;
    @Resource
    private ExcelUtil excelUtil;


    @Override
    public void fundCheckRepayAndCompst(String type, String paramDate) {
        SFTPUtil sftpUtil = null;
        try {
            //登录sftp
            log.info("登录sftp,用户名{},密码{},host,端口号{}", "", "", "", "");
            sftpUtil = new SFTPUtil("", "", "", 1);
            sftpUtil.login();
            //获取文件流
            Map<String, InputStream> fileMap = sftpUtil.listFiles("sftp文件路径");
            String haveFile = "F";
            for (String fileName : fileMap.keySet()) {
                //获取文件名（不带后缀）
                String fileSaveName = fileName.substring(0, fileName.indexOf("."));
                log.info("获取文件名{}", fileName);
                String file = "文件名20200528";
                log.info("匹配文件名file: {}, fileSaveName: {}", file, fileSaveName);
                //根据文件名判断当天是否有文件
                if (file.equals(fileSaveName)) {
                    log.info("匹配成功，准备上传oss！！！");
                    haveFile = "Y";
                    InputStream value = fileMap.get(fileName);
                    //上传文件流到oss
                    String ossPath = fjRepayFundPath;
                    log.info("上传文件到oss！！！！！！！！！路径为{}", ossPath);
                    ossUtil.uploadExcelInputStreamFile(value, fileName, fileName, "fileType", "fileExt", 0, "busiNo", "busiSubNo", ossPath);
                    try {
                        //解析excel存入数据库
                        ParsingExcel();
                    } catch (Exception e) {
                        log.info("存入t_check_compst表时，解析excel出现异常！", e.getMessage());
                    }
                }
            }
            if ("F".equals(haveFile)) {
                log.info("未找到今日对账文件！！！发送报警邮件！！！");
                try {
                    //（报警发邮件） todo
                } catch (Exception e) {
                    log.info("发送报警邮件异常！！！{}", e.getMessage());
                }
            }
            log.info("凤金还款excel对账成功！！！！！！！！");
        } catch (Exception e) {
            log.info("凤金代偿excel对账异常：" + e.getMessage());
        } finally {
            sftpUtil.logout();
        }
    }

    /**
     * 解析excel，数据落库
     */
    private void ParsingExcel() {
        log.info("开始解析excel文件！！！！");
        //从oss获取文件流
        InputStream repayInputStream = ossUtil.getSSOInputStream("ossPath(nacos上配置的路径)");
        //解析excel
        List<List<String>> data = excelUtil.writeWithoutHead(repayInputStream);
        log.info("解析excel!!!!!!!!!!得到的数据集{}", JSONObject.toJSON(data));
        log.info("解析excel完成！！！！");
        //根据数据集落库 todo
    }


}
