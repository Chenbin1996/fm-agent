package com.ruxuanwo.fm.agent.web;


import com.ruxuanwo.fm.client.client.FileManagerClient;
import com.ruxuanwo.fm.client.model.FileItem;
import com.ruxuanwo.fm.client.utils.FileUtil;
import com.ruxuanwo.fm.client.utils.ImageUtil;
import com.ruxuanwo.fm.client.utils.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 处理任何请求的controller
 *
 * @author 陈斌
 */
@RestController
public class MainController {
    private static final Logger LOGGER = LoggerFactory.getLogger(MainController.class);
    @Autowired
    private FileManagerClient fileManagerClient;
    @Value("${fm.agent.isDownload}")
    private Boolean isDownload;

    @RequestMapping("/**")
    public void resource(HttpServletResponse response, HttpServletRequest request) {

        try {
            String uri = request.getRequestURI();
            Map<String, String[]> parameterMap = request.getParameterMap();
            //排除 下载文件路径为 http://www.xxx.com/ccc/
            if (uri.lastIndexOf("/") != uri.length() - 1) {
                String fileName = uri.substring(uri.lastIndexOf("/") + 1);

                if (fileName.contains(".")) {
                    //如果是文件类型，直接下载
                    downloadFile(response, uri, fileName, parameterMap);
                } else {
                    //没有文件后缀的情况，检查是否是文件，不带后缀的文件类型
                    if (checkFileIsDir(fileManagerClient, uri)) {
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    } else {
                        downloadFile(response, uri, fileName, parameterMap);
                    }
                }
            } else {
                //不处理这种情况 --下载文件路径为 http://www.xxx.com/ccc/
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }

        } catch (IOException e) {
            LOGGER.error("MainController IOException:", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            LOGGER.error("MainController Exception:", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void downloadFile(HttpServletResponse response, String uri, String fileName, Map<String, String[]> parameterMap)
            throws Exception {
        Integer width = null;
        Integer height = null;
        if (parameterMap.size() > 0){
            if (parameterMap.containsKey("width")) {
                width = "".equals(parameterMap.get("width")[0]) || parameterMap.get("width")[0] == null ? null : Integer.valueOf(parameterMap.get("width")[0]);
            }
            if (parameterMap.containsKey("height")){
                height = "".equals(parameterMap.get("height")[0]) || parameterMap.get("height")[0] == null ? null : Integer.valueOf(parameterMap.get("height")[0]);
            }
        }
        Result<byte[]> result = fileManagerClient.download(uri);
        if (result.getData() != null && result.getResCode() != 0) {
            byte[] data = result.getData();
            if (width != null && height != null){
                data = ImageUtil.setImageSize(width, height, new ByteArrayInputStream(data), FileUtil.getFileExtName(uri));
            }
            if(isDownload){
                response.setContentType("application/octet-stream;");
                response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
            }else {
                //改为预览，而不是下载
                response.setHeader("Content-Disposition", "inline;filename=" + fileName);
                response.setHeader("Content-Length", "" + data.length);
            }
            response.getOutputStream().write(data);
            response.getOutputStream().flush();
            response.getOutputStream().close();
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private boolean checkFileIsDir(FileManagerClient fileManagerClient, String uri) throws Exception {
        Result<FileItem> result = fileManagerClient.getFile(uri);
        if (result != null && result.getResCode() != null && result.getResCode().equals(1)) {
            FileItem item = result.getData();
            if (item != null && item.getDir()) {
                return true;
            }
            return false;
        }
        throw new Exception("the '" + uri + "' not found!!!");
    }

    private String getParamByUrl(String url, String name) {
        String pattern = "(\\?|&){1}#{0,1}" + name + "=[a-zA-Z0-9]*";
        Matcher m = Pattern.compile(pattern).matcher(url);
        if (m.find()) {
            return m.group(0).split("=")[1].replace("&", "");
        }
        return null;
    }
}
