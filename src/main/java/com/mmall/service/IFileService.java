package com.mmall.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * Created by geely
 */
public interface IFileService {
    /**
     * 文件上传
     * @param file 文件
     * @param path 上下文路径
     * @return
     */
    String upload(MultipartFile file, String path);
}
