package com.example.demo.utils;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.*;
import javax.mail.util.ByteArrayDataSource;
import java.io.InputStream;
import java.util.*;

/**
 * @author wmy
 */
@Getter
@Component
@Slf4j
@RefreshScope
public class SendMailUtil {

    static {
        System.setProperty("mail.mime.splitlongparameters", "false");
        System.setProperty("mail.mime.charset", "UTF-8");
    }

    private static final Logger logger = LoggerFactory.getLogger(SendMailUtil.class);



    public void main(String[] args) throws Exception {
        sendMail("", "");
    }

    public void sendMail(String subject, String content) throws Exception {
        /**发件人账户名*/
        String senderAccount = "xxx";
        /**发件人账户密码*/
        String senderPassword = "xxx";
        //1、连接邮件服务器的参数配置
        Properties props = new Properties();
        //设置用户的认证方式
        props.setProperty("mail.smtp.auth", "true");
        //设置传输协议
        props.setProperty("mail.transport.protocol", "smtp");
        //设置发件人的SMTP服务器地址
        props.setProperty("mail.smtp.host", "smtp.exmail.qq.com");
        //2、创建定义整个应用程序所需的环境信息的 Session 对象
        Session session = Session.getInstance(props);
        //设置调试信息在控制台打印出来
        session.setDebug(true);
        //3、创建邮件的实例对象
        Message msg = getMimeMessage(session, subject, content);
        //4、根据session对象获取邮件传输对象Transport
        Transport transport = session.getTransport();
        //设置发件人的账户名和密码
        transport.connect(senderAccount, senderPassword);
        //发送邮件，并发送到所有收件人地址，message.getAllRecipients() 获取到的是在创建邮件对象时添加的所有收件人, 抄送人, 密送人
        transport.sendMessage(msg,msg.getAllRecipients());

        //如果只想发送给指定的人，可以如下写法
        //transport.sendMessage(msg, new Address[]{new InternetAddress("xxx@qq.com")});

        //5、关闭邮件连接
        transport.close();
    }

    /**
     * 获得创建一封邮件的实例对象
     * @param session
     * @return
     * @throws MessagingException
     * @throws AddressException
     */
    public MimeMessage getMimeMessage(Session session, String subject, String content) throws Exception{
        /**发件人地址*/
        String senderAddress = "xxx";
        /**收件人地址*/
        String recipientAddressStr = "xxx";
        //创建一封邮件的实例对象
        MimeMessage msg = new MimeMessage(session);
        //设置发件人地址
        msg.setFrom(new InternetAddress(senderAddress));
        /**
         * 设置收件人地址（可以增加多个收件人、抄送、密送），即下面这一行代码书写多行
         * MimeMessage.RecipientType.TO:发送
         * MimeMessage.RecipientType.CC：抄送
         * MimeMessage.RecipientType.BCC：密送
         */

        List<String> recipientT0List = new ArrayList<>();
        for (String recipientAddress : recipientAddressStr.split(",")){
            recipientT0List.add(recipientAddress);
        }
        if (recipientT0List.size() > 0) {
            InternetAddress[] sendTo = new InternetAddress[recipientT0List.size()];
            for (int i = 0; i < recipientT0List.size(); i++) {
                System.out.println("发送到:" + recipientT0List.get(i));
                sendTo[i] = new InternetAddress(recipientT0List.get(i), "", "UTF-8");
            }
            msg.addRecipients(MimeMessage.RecipientType.TO, sendTo);
        }

        //设置邮件主题
        msg.setSubject(subject,"UTF-8");
        //设置邮件正文
        msg.setContent(content, "text/html;charset=UTF-8");
        //设置邮件的发送时间,默认立即发送
        msg.setSentDate(new Date());
        return msg;
    }


    public void sendMailAttach(Map<InputStream, String> mapResult, String subject, String content)throws Exception{
        /**发件人账户名*/
        String senderAccount = "xxx";
        log.info("senderAccount------------{}",senderAccount);
        /**发件人账户密码*/
        String senderPassword = "xxx";
        log.info("senderPassword------------{}",senderPassword);
        /**收件人地址*/
        String receiveAddress = "xxx";

        System.out.println("sendMailServlet-----start2");

        //1.创建邮件对象
        Properties properties = new Properties();
        // 指定SMTP服务器
        properties.put("mail.smtp.host", "smtp.exmail.qq.com");
        // 指定是否需要SMTP验证
        properties.put("mail.smtp.auth", "true");
        Session session = Session.getInstance(properties);
        MimeMessage message =new MimeMessage(session);

        /*2.设置发件人
         * 其中 InternetAddress 的三个参数分别为: 邮箱, 显示的昵称(只用于显示, 没有特别的要求), 昵称的字符集编码
         * */
        message.setFrom(new InternetAddress(senderAccount, "", "UTF-8"));
        /*3.设置收件人
        To收件人   CC 抄送  BCC密送*/
        String[] recipientT0List = receiveAddress.split(",");
        if (recipientT0List.length > 0) {
            InternetAddress[] sendTo = new InternetAddress[recipientT0List.length];
            for (int i = 0; i < recipientT0List.length; i++) {
                System.out.println("发送到:" + recipientT0List[i]);
                sendTo[i] = new InternetAddress(recipientT0List[i], "", "UTF-8");
            }
            message.addRecipients(MimeMessage.RecipientType.TO, sendTo);
        }
        /*4.设置标题*/
        message.setSubject(subject,"UTF-8");
        message.setContent(content,"text/html;charset=UTF-8");

        /*5.设置邮件正文*/

        //一个Multipart对象包含一个或多个bodypart对象，组成邮件正文
        MimeMultipart multipart = new MimeMultipart();
        //创建附件节点  读取本地文件,并读取附件名称
        for(Map.Entry<InputStream, String> entry : mapResult.entrySet()){
            MimeBodyPart file = new MimeBodyPart();
            if(entry.getKey()!=null){
                DataSource source = new ByteArrayDataSource(entry.getKey(), "application/msexcel");
                file.setDataHandler(new DataHandler(source));
                file.setFileName(MimeUtility.encodeText(entry.getValue()));
                multipart.addBodyPart(file);
            }
        }
        //混合关系
        multipart.setSubType("mixed");
        message.setContent(multipart);
        message.setSentDate(new Date());
        message.saveChanges();
        Transport transport = session.getTransport("smtp");
        transport.connect("smtp.exmail.qq.com",senderAccount,senderPassword);
        transport.sendMessage(message,message.getAllRecipients());
        transport.close();
        Boolean isFlag = true;
        logger.info(Calendar.getInstance().getTime()+" : # Send mail to "+" success #");

        System.out.println("sendMailServlet-----end2");
    }

}
