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
    public static void writeBytes2File(byte[][] bytes, File outFile) throws IOException {
        if (!outFile.exists()) {
            File outParent = outFile.getParentFile();
            if (!outParent.exists()) {
                outParent.mkdirs();
            }
            outFile.createNewFile();
        }

        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outFile));

        StringBuilder row = new StringBuilder();
        for (int y = 0; y < bytes[0].length; y++) {
            row.setLength(0);
            for (int x = 0; x < bytes.length; x++) {
                row.append(bytes[x][y]);
            }
            bufferedWriter.write(row.toString() + "\n");
        }
        bufferedWriter.close();
    }

    /**
     * 从文件中读取 bytes[][] 数据
     * @param byteFile
     * @return
     */
    public static byte[][] readBytesFormFile(File byteFile) throws IOException {
        ArrayList<String> temp = new ArrayList<>();
        BufferedReader bufferedReader = new BufferedReader(new FileReader(byteFile));
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            temp.add(line);
        }

        byte[][] result = new byte[temp.size()][temp.get(0).length()];
        for (int i = 0; i < temp.size(); i++) {
            for (int j = 0; j < temp.get(i).length(); j++) {
                result[i][j] = Byte.valueOf(temp.get(i).charAt(j) + "");
            }
        }
        return result;
    }

    public static BufferedImage toBufferedImage(Image image) {
        BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null)
                , BufferedImage.TYPE_INT_ARGB);
        Graphics2D bGr = bufferedImage.createGraphics();
        bGr.drawImage(image, 0, 0, null);
        bGr.dispose();
        return bufferedImage;
    }
}
