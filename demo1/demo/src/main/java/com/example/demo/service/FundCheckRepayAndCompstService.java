package com.example.demo.service;

/**
 * @author wmy
 */
public interface FundCheckRepayAndCompstService {

    /**
     * 资金方 还款 代偿文件上传 数据落库
     * @param type
     * */
    void fundCheckRepayAndCompst(String type, String paramDate);
}
