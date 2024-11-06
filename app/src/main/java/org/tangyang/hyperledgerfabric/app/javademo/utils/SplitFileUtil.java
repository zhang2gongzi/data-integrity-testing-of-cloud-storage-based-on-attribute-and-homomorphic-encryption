package org.tangyang.hyperledgerfabric.app.javademo.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;

public class SplitFileUtil {
    /**
     * 将密文数据切片
     * @param file
     * @param filepath 切片数据存放目录
     * @param count 切片数量
     */
    public static void cut(File file, String filepath, int count) {
        //获取当前文件大小 只读模式
        RandomAccessFile in = null;
        long length=0;
        try {
            in = new RandomAccessFile(file, "r");
            length = in.length();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("当前文件长度："+length);
        //文件切片后的长度
        long maxSize = length / count;
        //初始化偏移量
        long offSet = 0L;
        //普通分片处理
        for (int i = 0; i < count - 1; i++) {
            long begin = offSet;
            long end = (i + 1) * maxSize;
            offSet = WriteFile(filepath, i, begin, end);
        }
        //终分片 处理
        if (length - offSet > 0) {
            WriteFile(filepath, count-1, offSet, length);
        }
        //需要关闭文件流
    }

    /**
     * 写入文件并返回读取进度
     * file 源文件
     * index 分割编码
     * begin 开始指针位置
     * end 结束指针位置
     * */
    public static long WriteFile(String file,int index,long begin,long end){
        String sourceFileName=getStrBeforeFileExtension(file);
        long endPointer = 0L;
        try {
            //读取被分割 目标文件
            RandomAccessFile in = new RandomAccessFile(new File(file), "r");
            //读写模式开启 分片文件temp
            RandomAccessFile out = new RandomAccessFile(new File(sourceFileName + "_block" + index + ".block"), "rw");

            //作为缓存 的字节数组
            byte[] tempByteArray = new byte[1024];
            int n = 0;

            //从指定位置读取文件字节
            in.seek(begin);
            //判断文件流读取的边界
            while(in.getFilePointer() <= end && (n = in.read(tempByteArray)) != -1){
                //写入temp分片文件内
                out.write(tempByteArray, 0, n);
            }
            //定义当前读取文件的指针
            endPointer = in.getFilePointer();
            //关闭输入流
            in.close();
            //关闭输出流
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return endPointer;
    }

    /**
     * 合并文件
     * @param file
     * @param blockFile 分块文件存放目录
     * @param blockCount 分块数目
     */
    public static void mergeFile(String file,String blockFile,int blockCount) {
        String nameAndPath=getStrBeforeBlockName(blockFile);

        RandomAccessFile raf = null;
        RandomAccessFile reader=null;
        try {
            //申明随机读取文件RandomAccessFile
            raf = new RandomAccessFile(new File(file), "rw");
            //开始合并文件，对应切片的二进制文件
            for (int i = 0; i < blockCount; i++) {
                //读取切片文件
                reader = new RandomAccessFile(new File(nameAndPath + "_block" + i + ".block"), "r");
                byte[] tempByteArray = new byte[1024];
                int n = 0;

                while ((n = reader.read(tempByteArray)) != -1) {
                    raf.write(tempByteArray, 0, n);
                }
                //解除暂存文件  占用  才能删除 temp文件
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(reader!=null){
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(raf!=null){
                try {
                    raf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }
    //获取扩展名前字符串
    public static String getStrBeforeFileExtension(String originalFileName) {

        return originalFileName.substring(0,originalFileName.lastIndexOf(""));
    }
    public static String getStrBeforeBlockName(String originalFileName) {

        return originalFileName.substring(0,originalFileName.lastIndexOf("_"));
    }
}
