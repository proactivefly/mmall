package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.mmall.service.IFileService;
import com.mmall.util.FTPUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * 上传文件服务
 */
@Service("iFileService")
public class FileServiceImpl implements IFileService {

    private Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);


    public String upload(MultipartFile file,String path){
        //获取原始名字
        String fileName = file.getOriginalFilename();

        //扩展名
        String fileExtensionName = fileName.substring(fileName.lastIndexOf(".")+1);

        // 避免文件名重复
        String uploadFileName = UUID.randomUUID().toString()+"."+fileExtensionName;

        logger.info("开始上传文件,上传文件的文件名:{},上传的路径:{},新文件名:{}",fileName,path,uploadFileName);

        //创建文件夹目录
        File fileDir = new File(path);
        if(!fileDir.exists()){
            fileDir.setWritable(true); //获取tomcat读写权限
            fileDir.mkdirs(); //创建文件夹
        }

        //文件对象
        File targetFile = new File(path,uploadFileName);

        //上传文件到服务器
        try {

            file.transferTo(targetFile);

            // 将target文件上传到FTP服务器上
            FTPUtil.uploadFile(Lists.newArrayList(targetFile));

            //已经上传到ftp服务器上，删除临时文件即可
            targetFile.delete();

        } catch (IOException e) {
            logger.error("上传文件异常",e);
            return null;
        }
        return targetFile.getName();
    }

}
