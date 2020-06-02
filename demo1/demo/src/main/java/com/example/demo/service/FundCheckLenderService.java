package com.example.demo.service;


import javax.mail.Part;

/**
 * @author wmy
 */
public interface FundCheckLenderService {

    /**
     * 资金方放款对账文件上传 数据落库
     * @param date:获取邮件的时间
     */
    void fundCheckLender(String date);

    /**
     * 资金方放款对账文件保存到oss
     * @param part
     * @exception
     */
    void saveToOss(Part part) throws Exception;
}
