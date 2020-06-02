package com.example.demo.utils;


import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.metadata.BaseRowModel;
import com.alibaba.excel.metadata.Sheet;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.util.CollectionUtils;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.*;
import java.util.*;

@Getter
@Component
@Slf4j
@RefreshScope
public class ExcelUtil {
    private static Sheet initSheet;

    static {
        initSheet = new Sheet(1, 0);
        initSheet.setSheetName("sheet");
        //设置自适应宽度
        initSheet.setAutoWidth(Boolean.TRUE);
    }

    /**
     * 读取少于1000行数据
     * @param filePath 文件绝对路径
     * @return
     */
    public static List<Object> readLessThan1000Row(String filePath){
        return readLessThan1000RowBySheet(filePath,null);
    }

    /**
     * 读小于1000行数据, 带样式
     * filePath 文件绝对路径
     * initSheet ：
     *      sheetNo: sheet页码，默认为1
     *      headLineMun: 从第几行开始读取数据，默认为0, 表示从第一行开始读取
     *      clazz: 返回数据List<Object> 中Object的类名
     */
    public static List<Object> readLessThan1000RowBySheet(String filePath, Sheet sheet){
        if(!StringUtils.hasText(filePath)){
            return null;
        }

        sheet = sheet != null ? sheet : initSheet;

        InputStream fileStream = null;
        try {
            fileStream = new FileInputStream(filePath);
            return EasyExcelFactory.read(fileStream, sheet);
        } catch (FileNotFoundException e) {
            log.info("找不到文件或文件路径错误, 文件：{}", filePath);
        }finally {
            try {
                if(fileStream != null){
                    fileStream.close();
                }
            } catch (IOException e) {
                log.info("excel文件读取失败, 失败原因：{}", e);
            }
        }
        return null;
    }

    /**
     * 读大于1000行数据
     * @param filePath 文件觉得路径
     * @return
     */
    public static List<Object> readMoreThan1000Row(String filePath){
        return readMoreThan1000RowBySheet(filePath,null);
    }

    /**
     * 读大于1000行数据, 带样式
     * @param filePath 文件觉得路径
     * @return
     */
    public static List<Object> readMoreThan1000RowBySheet(String filePath, Sheet sheet){
        if(!StringUtils.hasText(filePath)){
            return null;
        }

        sheet = sheet != null ? sheet : initSheet;

        InputStream fileStream = null;
        try {
            fileStream = new FileInputStream(filePath);
            ExcelListener excelListener = new ExcelListener();
            EasyExcelFactory.readBySax(fileStream, sheet, excelListener);
            return excelListener.getDatas();
        } catch (FileNotFoundException e) {
            log.error("找不到文件或文件路径错误, 文件：{}", filePath);
        }finally {
            try {
                if(fileStream != null){
                    fileStream.close();
                }
            } catch (IOException e) {
                log.error("excel文件读取失败, 失败原因：{}", e);
            }
        }
        return null;
    }

    /**
     * 生成excle
     * @param filePath  绝对路径, 如：/home/chenmingjian/Downloads/aaa.xlsx
     * @param data 数据源
     * @param head 表头
     */
    public static void writeBySimple(String filePath, List<List<Object>> data, List<String> head){
        writeSimpleBySheet(filePath,data,head,null);
    }

    /**
     * 生成excle
     * @param filePath 绝对路径, 如：/home/chenmingjian/Downloads/aaa.xlsx
     * @param data 数据源
     * @param sheet excle页面样式
     * @param head 表头
     */
    public static void writeSimpleBySheet(String filePath, List<List<Object>> data, List<String> head, Sheet sheet){
        sheet = (sheet != null) ? sheet : initSheet;

        if(head != null){
            List<List<String>> list = new ArrayList<>();
            head.forEach(h -> list.add(Collections.singletonList(h)));
            sheet.setHead(list);
        }

        OutputStream outputStream = null;
        ExcelWriter writer = null;
        try {
            outputStream = new FileOutputStream(filePath);
            writer = EasyExcelFactory.getWriter(outputStream);
            writer.write1(data,sheet);
        } catch (FileNotFoundException e) {
            log.error("找不到文件或文件路径错误, 文件：{}", filePath);
        }finally {
            try {
                if(writer != null){
                    writer.finish();
                }

                if(outputStream != null){
                    outputStream.close();
                }

            } catch (IOException e) {
                log.error("excel文件导出失败, 失败原因：{}", e);
            }
        }

    }

    /**
     * 生成excle
     * @param filePath 绝对路径, 如：/home/chenmingjian/Downloads/aaa.xlsx
     * @param data 数据源
     */
    public static void writeWithTemplate(String filePath, List<? extends BaseRowModel> data){
        writeWithTemplateAndSheet(filePath,data,null);
    }

    /**
     * 生成excle
     * @param filePath 绝对路径, 如：/home/chenmingjian/Downloads/aaa.xlsx
     * @param data 数据源
     * @param sheet excle页面样式
     */
    public static void writeWithTemplateAndSheet(String filePath, List<? extends BaseRowModel> data, Sheet sheet){
        if(CollectionUtils.isEmpty(data)){
            return;
        }

        sheet = (sheet != null) ? sheet : initSheet;
        sheet.setClazz(data.get(0).getClass());

        OutputStream outputStream = null;
        ExcelWriter writer = null;
        try {
            outputStream = new FileOutputStream(filePath);
            writer = EasyExcelFactory.getWriter(outputStream);
            writer.write(data,sheet);
        } catch (FileNotFoundException e) {
            log.error("找不到文件或文件路径错误, 文件：{}", filePath);
        }finally {
            try {
                if(writer != null){
                    writer.finish();
                }

                if(outputStream != null){
                    outputStream.close();
                }
            } catch (IOException e) {
                log.error("excel文件导出失败, 失败原因：{}", e);
            }
        }

    }


    @Data
    public static class MultipleSheelPropety{

        private List<? extends BaseRowModel> data;

        private Sheet sheet;
    }

    /**
     * 解析监听器，
     * 每解析一行会回调invoke()方法。
     * 整个excel解析结束会执行doAfterAllAnalysed()方法
     *
     * @author: chenmingjian
     * @date: 19-4-3 14:11
     */
    @Getter
    @Setter
    public static class ExcelListener extends AnalysisEventListener {

        private List<Object> datas = new ArrayList<>();

        /**
         * 逐行解析
         * object : 当前行的数据
         */
        @Override
        public void invoke(Object object, AnalysisContext context) {
            //当前行
            // context.getCurrentRowNum()
            if (object != null) {
                datas.add(object);
            }
        }


        /**
         * 解析完所有数据后会调用该方法
         */
        @Override
        public void doAfterAllAnalysed(AnalysisContext context) {
            //解析结束销毁不用的资源
        }

    }

    public List<List<String>> writeWithoutHead(InputStream inputStream) {
        StringExcelListener listener = new StringExcelListener();
        ExcelReader excelReader = EasyExcelFactory.read(inputStream, null, listener).headRowNumber(0).build();
        excelReader.read();
        List<List<String>> datas = listener.getDatas();
        excelReader.finish();
        return datas;
    }

    /**
     * StringList 解析监听器
     *
     * @author zhangcanlong
     * @since 2019-10-21
     */
    private static class StringExcelListener extends AnalysisEventListener {

        /**
         * 自定义用于暂时存储data
         * 可以通过实例获取该值
         */
        private List<List<String>> datas = new ArrayList<>();

        /**
         * 每解析一行都会回调invoke()方法
         *
         * @param object  读取后的数据对象
         * @param context 内容
         */
        @Override
        public void invoke(Object object, AnalysisContext context) {
            @SuppressWarnings("unchecked") Map<String, String> stringMap = (HashMap<String, String>) object;
            //数据存储到list，供批量处理，或后续自己业务逻辑处理。
            datas.add(new ArrayList<>(stringMap.values()));
            //根据自己业务做处理
        }

        @Override
        public void doAfterAllAnalysed(AnalysisContext context) {
            //解析结束销毁不用的资源
            //注意不要调用datas.clear(),否则getDatas为null
        }

        /**
         * 返回数据
         *
         * @return 返回读取的数据集合
         **/
        public List<List<String>> getDatas() {
            return datas;
        }

    }

    /**
     *
     * @param os 文件输出流
     * @param clazz Excel实体映射类
     * @param data 导出数据
     * @return
     */
    public static Boolean writeExcel(OutputStream os, Class<? extends BaseRowModel> clazz, List<? extends BaseRowModel> data,int seetNo){
        BufferedOutputStream bos= null;
        try {
            bos = new BufferedOutputStream(os);
            ExcelWriter writer = new ExcelWriter(bos, ExcelTypeEnum.XLSX);
            //写第一个sheet, sheet1  数据全是List<String> 无模型映射关系
            Sheet sheet1 = new Sheet(seetNo, 0,clazz);
            writer.write(data, sheet1);
            writer.finish();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }
    /************************匿名内部类结束，可以提取出去***************************/

    //测试生成cvs文件并上传到sftp
   /* public void test(){
        SFTPUtil sftpUtil = null;
        try {
            log.info("登录sftp,用户名{},密码{},host,端口号{}", config.getQinjia_sftp_username(), config.getQinjia_sftp_password(), config.getQinjia_sftp_host(),config.getQinjia_sftp_port());
            sftpUtil = new SFTPUtil(config.getQinjia_sftp_username(), config.getQinjia_sftp_password(), config.getQinjia_sftp_host(),config.getQinjia_sftp_port());
            sftpUtil.login();
            List<String> dataList = new ArrayList<>();
            dataList.add("number,name,sex");
            dataList.add("1,张三,男");
            dataList.add("2,李四,男");
            dataList.add("3,小红,女");
            String dataS = "";
            for(int i = 0; i<dataList.size(); i++){
                dataS = dataS + dataList.get(i) + "\r\n";
            }
            byte[] data = dataS.getBytes("UTF-8");
            String path = config.getJirongUploadPath() + getPath();
            String fileName = "test1.csv";
            ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
            sftpUtil.upload(path, fileName, inputStream);
        }catch (Exception e){
            log.error("cvs上传到sftp异常！{}", e.getMessage() );
        }
    }*/
}
