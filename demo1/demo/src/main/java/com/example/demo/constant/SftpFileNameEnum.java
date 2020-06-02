package com.example.demo.constant;

import lombok.Getter;

/**
 * @version v1.0
 * @ClassName: ClassNameEnum
 * @Description: sftp获取csv文件对应的名字
 * @Date: 2020/4/7 16:16
 */
@Getter
public enum SftpFileNameEnum {
    CUST_INFO("CustInfo.csv","客户基本信息"),
    RISK_INFO("RiskInfo.csv","风控信息表"),
    ASSET_REPAY_PLAN("AssetRepayPlan.csv","资产方还款计划"),
    BALANCE_INFO("BalanceInfo.csv","用户日结余额每日同步"),
    FUND_REPAY_PLAN("FundRepayPlan.csv","资金方还款计划"),
    LOAN_INFO("LoanInfo.csv","借据信息表"),
    BANK_CARD("BankCard.csv","换绑卡银行卡信息")
    ;


    SftpFileNameEnum(String code, String msg){
        this.code = code;
        this.msg = msg;
    }

    String code;
    String msg;
}
