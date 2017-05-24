package au.edu.uq;

import sun.jvm.hotspot.runtime.Bytes;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;

/**
 * Created by wufeiyang on 2017/5/24.
 */
public class Utils {

    public static final String IMAGE_FILE_EX = ".jpg";
    public static final String DATA_FILE_EX = ".data";

    /**
     * 获取一个文件夹下面所有的 jpg 图片
     * @param dirFile
     * @return
     */
    public static File[] getImages(File dirFile) {
        if (dirFile.exists() && dirFile.isDirectory()) {
            return dirFile.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().endsWith(IMAGE_FILE_EX);
                }
            });
        }
        return null;
    }

    /**
     * 判断一个像素点是否为黑色
     * @return
     */
    public static boolean isBlack(int color) {
        return color == Color.BLACK.getRGB();
    }

    /**
     * 判断一个像素点是否为白色
     * @return
     */
    public static boolean isWhite(int color) {
        return color == Color.WHITE.getRGB();
    }

    /**
     * 讲 bytes 写入文件
     * @param bytes
     */
    public static void writeBytes2File(byte[] bytes, File outFile) throws IOException {
        if (!outFile.exists()) {
            File outParent = outFile.getParentFile();
            if (!outParent.exists()) {
                outParent.mkdirs();
            }
            outFile.createNewFile();
        }

        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outFile));

        StringBuilder row = new StringBuilder();
        for (byte b : bytes) {
            row.append(b);
        }
        bufferedWriter.write(row.toString());
        bufferedWriter.flush();
        bufferedWriter.close();
    }

    /**
     * 从文件中读取 bytes[] 数据
     * @param byteFile
     * @return
     */
    public static byte[] readBytesFormFile(File byteFile) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(byteFile));
        String line = bufferedReader.readLine();
        if (null == line) {
            return new byte[0];
        } else {
            byte[] result = new byte[line.length()];
            for (int i = 0; i < line.length(); i++) {
                result[i] = Byte.valueOf(line.charAt(i) + "");
            }
            return result;
        }
    }

    public static BufferedImage toBufferedImage(Image image) {
        BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null)
                , BufferedImage.TYPE_INT_RGB);
        Graphics bGr = bufferedImage.getGraphics();
        bGr.drawImage(image, 0, 0, null);
        bGr.dispose();
        return bufferedImage;
    }

    public static double similarity(byte[] image1, byte[] image2) {
        if (image1.length != image2.length) {
            return -1;
        } else {
            int sum = 0;
            int squareSum1 = 0;
            int squareSum2 = 0;
            for (int i = 0; i < image1.length; i++) {
                sum += image1[i] * image2[i];
                squareSum1 += image1[i] * image1[i];
                squareSum2 += image2[i] * image2[i];
            }

            return sum * 1.0f / (Math.sqrt(squareSum1) * Math.sqrt(squareSum2));
        }
    }
}
