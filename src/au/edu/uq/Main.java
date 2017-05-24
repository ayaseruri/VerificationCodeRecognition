package au.edu.uq;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 程序目录下有两个文件夹 train 和 recognition ，其中 train 文件夹下面所有的文件用来存储训练文件，recognition 文件夹下面所有文件用来
 * 储存识别文件
 */
public class Main {

    private static final String TITLE = "   --------   VerificationCodeRecognition   --------   ";

    private static final String TRAIN_RAW_IMAGE_PATH = "./train/0rawimage/";
    private static final String TRAIN_PRE_PROGRESS_IMAGE_PATH = "./train/1preprogress/";
    private static final String TRAIN_SPLIT_IMAGE_PATH = "./train/2split/";
    private static final String TRAIN_DATA_PATH = "./train/3data/";

    private static final String RECOGNITION_RAW_IMAGE_PATH = "./recognition/0rawimage/";

    private static final int SPAN_COUNT = 5;
    private static final int SPLIT_LENGTH_HOLD = 2;

    /**
     * 整个程序开始执行的地方
     * @param args
     */
    public static void main(String[] args) throws IOException {
        System.out.println(TITLE);

        recognition();
    }

    private static void recognition() throws IOException {
        File recRawImageFile = new File(RECOGNITION_RAW_IMAGE_PATH);
        if (!recRawImageFile.exists()) {
            recRawImageFile.mkdirs();
        }

        File trainDataFile = new File(TRAIN_DATA_PATH);
        if (!trainDataFile.exists()) {
            trainDataFile.mkdirs();
        }

        File[] rawImageFiles = Utils.getImages(recRawImageFile);
        for (File imageFile : rawImageFiles) {
            BufferedImage bufferedImage = ImageIO.read(imageFile);
            bufferedImage = imagePreProgress(bufferedImage);
            for (BufferedImage slitImage : imageSplit(bufferedImage)) {
                for (File dataFile : trainDataFile.listFiles()) {
                    byte[][] trainData = Utils.readBytesFormFile(dataFile);
                    byte[][] recData = getData(Utils.toBufferedImage(
                            slitImage.getScaledInstance(trainData.length, trainData[0].length, Image.SCALE_DEFAULT)));

                }

            }
        }
    }

    private static void train() throws IOException {
        File trainRawImageFile = new File(TRAIN_RAW_IMAGE_PATH);
        if (!trainRawImageFile.exists()) {
            trainRawImageFile.mkdirs();
        }

        File trainPreImageFile = new File(TRAIN_PRE_PROGRESS_IMAGE_PATH);
        if (!trainPreImageFile.exists()) {
            trainPreImageFile.mkdirs();
        }

        File trainSplitImageFile = new File(TRAIN_SPLIT_IMAGE_PATH);
        if (!trainSplitImageFile.exists()) {
            trainSplitImageFile.mkdirs();
        }

        File trainDataFile = new File(TRAIN_DATA_PATH);
        if (!trainDataFile.exists()) {
            trainDataFile.mkdirs();
        }

        File[] images = Utils.getImages(trainRawImageFile);
        if (null != images) {
            for (File imageFile : images) {
                BufferedImage preProgressImage = imagePreProgress(ImageIO.read(imageFile));
                String imageFileName = imageFile.getName();
                ImageIO.write(preProgressImage,
                        "JPG",
                        new File(TRAIN_PRE_PROGRESS_IMAGE_PATH + imageFileName));
                List<BufferedImage> splitImages = imageSplit(preProgressImage);
                if (splitImages.size() == imageFileName.substring
                        (0, imageFileName.length() - Utils.IMAGE_FILE_EX.length()).length()) {
                    for (int i = 0; i < splitImages.size(); i++) {
                        BufferedImage splitImage = splitImages.get(i);
                        File charFile = new File(TRAIN_SPLIT_IMAGE_PATH + imageFileName.charAt(i) + Utils.IMAGE_FILE_EX);
                        if (!charFile.exists()) {
                            ImageIO.write(splitImage, "JPG", charFile);
                        }

                        File dataFile = new File(TRAIN_DATA_PATH + imageFileName.charAt(i));
                        if (!dataFile.exists()) {
                            Utils.writeBytes2File(getData(splitImage), dataFile);
                        }
                    }
                } else {
                    throw new IOException("raw image file name is wrong");
                }
            }
        } else {
            throw new IOException("image folder path is unaviliable");
        }
    }

    /**
     * 第一步图片预处理：将五彩的具有干扰元素的图片转化为黑白图片（其中字母数字为黑色，背景为白色）并存储在
     * TRAIN_PRE_PROGRESS_IMAGE_PATH 文件夹下
     * @param bufferedImage
     * @return
     */
    private static BufferedImage imagePreProgress(BufferedImage bufferedImage) {
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        double subWidth = 1.0d * width / SPAN_COUNT;

        for (int i = 0; i < SPAN_COUNT; i++) {
            HashMap<Integer, Integer> map = new HashMap<>();
            for (int x = (int) (i * subWidth); x < (i + 1) * subWidth && x < width; x++) {
                for (int y = 0; y < height; y++) {
                    int color = bufferedImage.getRGB(x, y);
                    if (Utils.isWhite(color) || Utils.isBlack(color)) {
                        continue;
                    }
                    map.put(color, map.getOrDefault(color, 0) + 1);
                }
            }

            int max = 0;
            int colorMax = 0;
            for (Integer color : map.keySet()) {
                if (max < map.get(color)) {
                    max = map.get(color);
                    colorMax = color;
                }
            }

            for (int x = (int) (i * subWidth); x < (i + 1) * subWidth && x < width; x++) {
                for (int y = 0; y < height; y++) {
                    int color = bufferedImage.getRGB(x, y);
                    if (color == colorMax) {
                        bufferedImage.setRGB(x, y, Color.BLACK.getRGB());
                    } else {
                        bufferedImage.setRGB(x, y, Color.WHITE.getRGB());
                    }
                }
            }
        }

        return bufferedImage;
    }

    /**
     * 第二步图片裁剪：将第一步的图片按照字母数字的边界切割为对应的小图片，由于字母数字的高度不一，所以西安横向扫描再纵向扫描，储存在
     * TRAIN_SPLIT_IMAGE_PATH 文件夹下
     * @param bufferedImage
     * @return
     */
    private static List<BufferedImage> imageSplit(BufferedImage bufferedImage) {
        ArrayList<BufferedImage> subImages = new ArrayList<>();
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        ArrayList<Integer> tempList = new ArrayList<>();

        int count;
        for (int x = 0; x < width; x++) {
            count = 0;
            for (int y = 0; y < height; y++) {
                if (Utils.isBlack(bufferedImage.getRGB(x, y))) {
                    count++;
                }
            }
            tempList.add(count);
        }

        int length;
        for (int i = 0; i < tempList.size(); i++) {
            length = 0;
            while (i < tempList.size() && tempList.get(i) > 0) {
                i++;
                length++;
            }
            if (length > SPLIT_LENGTH_HOLD) {
                BufferedImage subImage = bufferedImage.getSubimage(i - length, 0, length, height);

                width = subImage.getWidth();
                height = subImage.getHeight();
                int start = 0, end = 0;
                Top2Bottom: for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        if (Utils.isBlack(subImage.getRGB(x, y))) {
                            start = y;
                            break Top2Bottom;
                        }
                    }
                }
                Bottom2Top: for(int y = height - 1; y >= 0; y--) {
                    for (int x = 0; x < width; x++) {
                        if (Utils.isBlack(subImage.getRGB(x, y))) {
                            end = y;
                            break Bottom2Top;
                        }
                    }
                }

                subImages.add(subImage.getSubimage(0, start, width, end - start + 1));
            }
        }

        return subImages;
    }

    /**
     * 第三部训练数据模型：将第二步的图片中黑色用 1 表示，其他用 0 表示，建立一个二维数组；
     */
    private static byte[][] getData(BufferedImage bufferedImage) {
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();

        byte[][] pixs = new byte[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                pixs[x][y] = (byte) (Utils.isBlack(bufferedImage.getRGB(x, y)) ? 1 : 0);
            }
        }

        return pixs;
    }
}
