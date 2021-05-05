package com.achilles.wild.server.tool.file;

import com.achilles.wild.server.business.controller.demo.ImageController;
import net.coobird.thumbnailator.Thumbnails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class ImageUtil {

    private final static Logger log = LoggerFactory.getLogger(ImageController.class);

    static String srcPath = "C:\\Users\\Achilles\\Desktop\\photo\\10028.jpg";
    static String destPath = "C:\\Users\\Achilles\\Desktop\\test2.jpg";

    static String format = "jpg";

    public static void main(String[] args) {

//        trimByWidth(FileUtil.getInputStream(path),"jpg",50,desc);
//        trimByScale(FileUtil.getInputStream(path),"jpg",0.5,desc);

//        int size = trimByQuality(FileUtil.getInputStream(path).

//        trimByQuality(FileUtil.getInputStream(path),"jpg",0.1414213,1f,desc);

//        compressForScale(path,desc,220*1024L,0.8);

//        trimBySizeLimit(srcPath,destPath,300);

        System.out.println();
    }



    /**
     * trimBySizeLimit
     *
     * @param srcPath
     * @param destPath
     * @param sizeLimit
     */
    public static void trimBySizeLimit(String srcPath,String destPath,double quality,int sizeLimit){

        File srcFile = new File(srcPath);
        int srcFileSize = (int)srcFile.length()/1024;
        if (srcFileSize <= sizeLimit) {
            return;
        }

        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(srcFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        inputStream = trimBySizeLimit(inputStream,quality,sizeLimit);
        FileUtil.toFile(inputStream,destPath);
    }


    /**
     * trimBySizeLimit
     *
     * @param inputStream
     * @param sizeLimit
     * @return
     */
    public static InputStream trimBySizeLimit(InputStream inputStream,double quality,int sizeLimit){

        if (inputStream == null){
            throw new IllegalArgumentException("inputStream can not be null !");
        }

        int srcFileSize = 0;
        try {
            srcFileSize = inputStream.available()/1024;
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (srcFileSize <= sizeLimit) {
            return inputStream;
        }

        double times = new BigDecimal(srcFileSize).divide(new BigDecimal(sizeLimit),10,BigDecimal.ROUND_HALF_UP).doubleValue();
        double scale = 1/Math.sqrt(times);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            log.info("11111111111");
            Thumbnails.of(inputStream).scale(scale).outputQuality(quality).outputFormat(format).toOutputStream(outputStream);
            log.info("222222222");
        } catch (IOException e) {
            e.printStackTrace();
        }

        InputStream trimInputStream = FileUtil.getInputStream(outputStream);
        try {
            srcFileSize = trimInputStream.available()/1024;
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (srcFileSize <= sizeLimit) {
            return trimInputStream;
        }

        return trimBySizeLimit(trimInputStream,quality,sizeLimit);
    }

    /**
     * getWidthAndHeight
     *
     * @param desPath
     * @return
     */
    public static Map<String,Integer> getWidthAndHeight(String desPath) {

        BufferedImage bim = null;
        try {
            bim = ImageIO.read(new FileInputStream(srcPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        int width = bim.getWidth();
        int height = bim.getHeight();

        Map<String,Integer> map = new HashMap<>();
        map.put("width",width);
        map.put("height",height);

        return map;
    }

    /**
     * trimByWidth
     *
     * @param inputStream
     * @param format
     * @param width
     * @param path
     */
    public static void trimByWidth(InputStream inputStream,String format, int width,String path){

        try {
            Thumbnails.of(inputStream).width(width).outputFormat(format).toFile(path);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void trimByScale(InputStream inputStream,String format,double scale,String path){

        try {
            Thumbnails.of(inputStream).scale(scale).outputFormat(format).toFile(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
