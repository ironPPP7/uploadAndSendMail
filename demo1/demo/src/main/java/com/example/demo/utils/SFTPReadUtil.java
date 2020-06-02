package com.example.demo.utils;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.util.IOUtils;
import com.example.demo.constant.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class SFTPReadUtil <T>{
    private static Logger log = LoggerFactory.getLogger(SFTPReadUtil.class);
    /**
     * 读取普通文件如 txt
     * 返回文件内容
     * @param inputStream
     */
    public static String sftpRead(InputStream inputStream){
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            while ((line = br.readLine()) != null){
                sb.append(line);
            }
            return sb.toString();
        } catch(Exception e) {
            throw new RuntimeException("SFTPReadUtil#sftpRead:io运行错误"+e.getMessage());
        }finally {
            IOUtils.close(br);
            IOUtils.close(inputStream);
        }
    }
    public static  <T> List<T> zipCSVSftpRead(InputStream inputStream, String[] headers, Class clazz, SftpFileNameEnum emum) throws Exception {
        return zipCSVSftpRead(inputStream,headers,clazz,StandardCharsets.UTF_8,emum);
    }

    /**
     * 根据参数流 把csv文件转换成list集合
     *
     * @param inputStream  要处理的字节流
     * @param headers  要获取的字段 ps：一定要按照顺序写到数组里面
     * @param clazz   获取数据后反射生成对应的实体类
     * @param charset   设置字符集类型 ，设置方式 1、StandardCharsets.UTF_8 2、Charset.forName("ISO-8859-1");
     * @param <T>
     * @return
     * @throws IOException
     */
    public static  <T> List<T> zipCSVSftpRead(InputStream inputStream, String[] headers, Class clazz, Charset charset, SftpFileNameEnum emum) throws Exception {
        if(emum == null){
            throw new RuntimeException("SFTPReadUtil#zipCSVSftpRead：参数枚举类不能为空!");
        }
        ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(inputStream), charset);
        ZipEntry ze = null;
        List<T> list = new LinkedList<>();
        try{
            while ((ze = zipInputStream.getNextEntry()) != null) {

                if(!emum.getCode().equals(ze.getName())){
                    continue;
                }
                log.info("文件名：" + ze.getName() + " 文件大小：" + ze.getSize() + " bytes");
                log.debug("文件内容：");
                //读取
                BufferedReader br = new BufferedReader(new InputStreamReader(zipInputStream,charset));
                //循环遍历
                CSVParser records = CSVFormat.RFC4180.withHeader(headers).parse(br);
                //转换对象
                for (CSVRecord record : records) {
                    Map<String,String> map = record.toMap();
                    log.debug(map.toString());
                    if(null != map && !map.isEmpty()) {
                        T t = (T) JSONObject.parseObject(JSONObject.toJSONString(map), clazz);
                        list.add(t);
                    }
                }

            }
        }catch (Exception e) {
            throw new RuntimeException("SFTPReadUtil#zipCSVSftpRead:zip读取文件错误：" + e.getMessage());
        }finally {
            IOUtils.close(zipInputStream);
        }
        return list;
    }

    /**
     * 读取csv文件转换成对象
     * @param <T>
     * @return
     */
    public static  <T> List<T> csvSftpRead(InputStream inputStream, String[] headers, Class clazz, Charset charset){
        List<T> list = new LinkedList<>();
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream,charset));
        //循环遍历
        CSVParser records = null;
        try {
            int p = (inputStream.read() << 8) + inputStream.read();
            records = CSVFormat.RFC4180.withHeader(headers).parse(br);
            int i =0;
            for (CSVRecord record : records) {
                if(i == 0){ i++; continue; }// 第一行头部数据不获取
                Map<String,String> map = record.toMap();
                log.debug("SFTPReadUtil#csvSftpRead:map数据"+map.toString());
                if(null != map && !map.isEmpty()) {
                    T t = (T) JSONObject.parseObject(JSONObject.toJSONString(map), clazz);
                    log.debug("map->"+map);
                    list.add(t);
                }
            }
        } catch (Exception e) {
            log.error("SFTPReadUtil#csvSftpRead:csv文件转换错误："+ ExceptionUtils.getFullStackTrace(e));
        }finally {
            IOUtils.close(br);
            IOUtils.close(inputStream);
            return list;
        }
    }

}
