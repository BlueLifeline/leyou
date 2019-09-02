package com.leyou.upload.service;

import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.MyRuntimeException;
import com.leyou.config.UploadProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

@Service
@Slf4j
@EnableConfigurationProperties(UploadProperties.class)  //使用application.yml配置的属性
public class UploadService {
    /**
     * 1、校验文件大小。spring配置文件已配置
     * 2、校验文件的媒体类型
     * 3、校验文件的内容
     * @param file
     */

    //限制文件类型
    //private static final List<String> fileTypes = Arrays.asList("image/png", "image/jpeg");

    @Autowired
    private FastFileStorageClient storageClient;

    @Autowired
    private UploadProperties props;

    public String uploadFile(MultipartFile file) {

        try {
            //文件类型校验
            String fileType = file.getContentType();
            if(!props.getAllowImageTypes().contains(fileType)){
                log.error("上传文件格式不支持");
                throw new MyRuntimeException(ExceptionEnum.FILETYPE_NOT_SUPPORT);
            }

            //校验文件内容
            BufferedImage image = ImageIO.read(file.getInputStream());
            if(image == null){
                log.error("文件为空");
                throw new MyRuntimeException(ExceptionEnum.FILE_IS_NULL);
            }

            /**
             * 原始方式：上传到服务器
             *
             * //准备存储到的目标路径
             File fileDir = new File("G:\\leyou\\assets\\images");
             //如果不存在该目录则生成
             if(!fileDir.exists()){
             fileDir.mkdirs();
             }
             //创建目标文件
             File fileDest = new File(fileDir, file.getOriginalFilename());
             //上传文件到目标。相当于把上传的文件内容写入到fileDest中
             file.transferTo(fileDest);
             */

            //上传到分布式文件系统：fdfs的数据仓库storage
            StorePath storePath = storageClient.uploadFile(file.getInputStream(), file.getSize(),
                    getExtension(file.getOriginalFilename()), null);

            /**
             * 这里返回值是一个url，是因为图片资源不能存放在服务器内部，需要单独存放。所以这里返回一个访问该资源的url。
             * 调用该服务的服务可以将该url存入到数据库，然后可以通过该url来获取到静态资源。
             */
            String url =  props.getBaseUrl() + storePath.getFullPath();

            return url;

        } catch (IOException e) {
            log.error("文件上传失败");
            throw new MyRuntimeException(ExceptionEnum.UPLOAD_FAILED);
        }
    }

    public String getExtension(String fileName){
        return StringUtils.substringAfterLast(fileName,".");
    }
}
