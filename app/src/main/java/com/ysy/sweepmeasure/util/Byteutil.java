package com.ysy.sweepmeasure.util;

/**
 * User: ysy
 * Date: 2015/9/6
 */

/**
 * <ul>
 * <li>文件名称: com.born.util.ByteUtil.java</li>
 * <li>文件描述: byte转换工具</li>
 * <li>版权所有: 版权所有(C)2001-2006</li>
 * <li>公 司: bran</li>
 * <li>内容摘要:</li>
 * <li>其他说明:</li>
 * <li>完成日期：2011-7-18</li>
 * <li>修改记录0：无</li>
 * </ul>
 *
 * @author 许力多
 * @version 1.0
 */
public class ByteUtil {
    //long类型转成byte数组
    public static byte[] longToBytes(long number) {
        long temp = number;
        byte[] b = new byte[8];
        for (int i = 0; i < b.length; i++) {
            b[i] = new Long(temp & 0xff).byteValue();// 将最低位保存在最低位
            temp = temp >> 8; // 向右移8位
        }
        return b;
    }

    //byte数组转成long
    public static long bytesToLong(byte[] b) {

        long s = 0;
        long s0 = b[0] & 0xff;// 最低位
        long s1 = b[1] & 0xff;
        long s2 = b[2] & 0xff;
        long s3 = b[3] & 0xff;
        long s4 = b[4] & 0xff;// 最低位
        long s5 = b[5] & 0xff;
        long s6 = b[6] & 0xff;
        long s7 = b[7] & 0xff;

        // s0不变
        s1 <<= 8;
        s2 <<= 16;
        s3 <<= 24;
        s4 <<= 8 * 4;
        s5 <<= 8 * 5;
        s6 <<= 8 * 6;
        s7 <<= 8 * 7;
        s = s0 | s1 | s2 | s3 | s4 | s5 | s6 | s7;
        return s;
    }

    public static byte[] intToBytes(int number) {

        int temp = number;
        byte[] b = new byte[4];

        for (int i = 0; i < b.length; i++) {
            b[i] = new Integer(temp & 0xff).byteValue();// 将最低位保存在最低位
            temp = temp >> 8; // 向右移8位
        }
        return b;
    }

    public static int bytesToInt(byte[] b) {

        int s = 0;
        int s0 = b[0] & 0xff;// 最低位
        int s1 = b[1] & 0xff;
        int s2 = b[2] & 0xff;
        int s3 = b[3] & 0xff;
        s3 <<= 24;
        s2 <<= 16;
        s1 <<= 8;
        s = s0 | s1 | s2 | s3;
        return s;
    }

    //浮点到字节转换
    public static byte[] doubleToBytes(double d) {
        byte writeBuffer[] = new byte[8];
        long v = Double.doubleToLongBits(d);
        writeBuffer[7] = (byte) (v >>> 56);
        writeBuffer[6] = (byte) (v >>> 48);
        writeBuffer[5] = (byte) (v >>> 40);
        writeBuffer[4] = (byte) (v >>> 32);
        writeBuffer[3] = (byte) (v >>> 24);
        writeBuffer[2] = (byte) (v >>> 16);
        writeBuffer[1] = (byte) (v >>> 8);
        writeBuffer[0] = (byte) (v >>> 0);
        return writeBuffer;

    }

    //字节到浮点转换
    public static double bytesToDouble(byte[] readBuffer) {
        return Double.longBitsToDouble((((long) readBuffer[7] << 56) +
                        ((long) (readBuffer[6] & 255) << 48) +
                        ((long) (readBuffer[5] & 255) << 40) +
                        ((long) (readBuffer[4] & 255) << 32) +
                        ((long) (readBuffer[3] & 255) << 24) +
                        ((readBuffer[2] & 255) << 16) +
                        ((readBuffer[1] & 255) << 8) +
                        ((readBuffer[0] & 255) << 0))
        );
    }

    /**
     * 将short转成byte[2]
     * @param a
     * @param b
     * @param offset b中的偏移量
     */
    public static void short2Byte(short a, byte[] b, int offset){
        b[offset+1] = (byte) (a >> 8);
        b[offset] = (byte) (a);
    }

    /**
     * 将byte[2]转换成short
     * @param b
     * @return
     */
    public static short byte2Short(byte[] b){
        return (short) (((b[1] & 0xff) << 8) | (b[0] & 0xff));
    }
}
