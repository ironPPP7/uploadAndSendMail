package com.example.demo.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class SFTPTest {

    public static void main(String[] args) {
        SimpleDateFormat simpledateformat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
        String format = simpledateformat.format(new Date(1588040847000L));
        System.out.println("--"+format);
    }

    public static class stu{
        String id;
        String name;

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public void setId(String id) {
            this.id = id;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    private static void zipsftpRead(){
        SFTPUtil s = new SFTPUtil("hanhuadb","%@LL!W7Kz8","106.38.48.226",777);
            try {
            s.login();
            Map<String, InputStream> fileMap = s.listFiles("/upload/upload/test_fj/20190513/");
            InputStream value = fileMap.get("123ck.zip");
            ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(value), Charset.forName("UTF-8"));
            ZipEntry ze = null;
            //循环遍历
            while ((ze = zipInputStream.getNextEntry()) != null) {
                System.out.println("文件名：" + ze.getName() + " 文件大小：" + ze.getSize() + " bytes");
                System.out.println("文件内容：");
                //读取
                BufferedReader br = new BufferedReader(new InputStreamReader(zipInputStream,Charset.forName("UTF-8")));
                String line;

                //内容不为空，输出
                while ((line = br.readLine()) != null) {
                    System.out.println(line);
                }
            }

            //一定记得关闭流
            zipInputStream.closeEntry();
            value.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

   private static void sftpDown(){
        SFTPUtil s = new SFTPUtil("hanhuadb","%@LL!W7Kz8","106.38.48.226",777);
        try {
            s.login();
            Map<String, InputStream> fileMap = s.listFiles("/upload/upload/test_fj/20190513/");
            for (String fileName : fileMap.keySet()) {
                InputStream value = fileMap.get(fileName);
                String fileExt = fileName.substring(fileName.indexOf(".") + 1);
                //String fileType = transFileType(fileExt);
                String fileSaveName = String.valueOf(SnowflakeIdWorker.nextId());
                BufferedReader br = new BufferedReader(new InputStreamReader(value));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null)
                {
                    sb.append(line);
                }
                System.out.println(sb.toString());
                System.out.println("\nDone!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
