package com.itheima.reggie.controller;

import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/common")
@Slf4j
public class CommonController {

    @Value("${reggie.path}")
    private String basePath;

    /**
     * 文件上传
     *
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file) {
        //file是一个临时文件，本次请求结束后，临时文件会自动删除掉
        log.info(file.toString());
        //使用UUID来随机生成文件名
        String originalFilename = file.getOriginalFilename(); //获取上传的原图的名字
        String suffix = originalFilename.substring(originalFilename.lastIndexOf(".")); //获取原图文件的后缀
        String fileName = UUID.randomUUID().toString() + suffix; //拼接UUID与文件后缀

        File dir = new File(basePath);
        //如果不存在文件目录
        if (!dir.exists()) {
            dir.mkdir(); //创建
        }

        try {
            file.transferTo(new File(basePath + fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }

        //返回文件名称
        return R.success(fileName);
    }

    /**
     * 文件下载
     */
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response) {
        try {
            //输入流，来读取文件内容
            FileInputStream fileInputStream = new FileInputStream(basePath + name);
            //输出流 通过输出流将文件写会浏览器 展示图片
            ServletOutputStream outputStream = response.getOutputStream();
            //设置格式 图片文件
            response.setContentType("image/jpeg");
            int len = 0;
            byte[] bytes = new byte[1024];
            while ((len = fileInputStream.read(bytes)) != -1) {
                outputStream.write(bytes,0,len);
                outputStream.flush();
            }
            fileInputStream.close();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
