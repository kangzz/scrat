package com.scrat.framework.config.common;

import java.io.*;
/**
 * 描述：序列化
 * 作者 ：kangzz
 * 日期 ：2016-10-21 16:20:08
 */
public class SerializableSerializer {

    public static Object deserialize(byte[] bytes) throws DataMarshallingException {
        try {
            ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(bytes));
            Object object = inputStream.readObject();
            return object;
        } catch (ClassNotFoundException e) {
            throw new DataMarshallingException("Unable to find object class.", e);
        } catch (IOException e) {
            try {
                return new String(bytes, "UTF-8");
            } catch (UnsupportedEncodingException e1) {
                throw new DataMarshallingException(e1);
            }
        }
    }

    public static byte[] serialize(Object serializable) throws DataMarshallingException {
        try {
            ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
            ObjectOutputStream stream = new ObjectOutputStream(byteArrayOS);
            stream.writeObject(serializable);
            stream.close();
            return byteArrayOS.toByteArray();
        } catch (IOException e) {
            try {
                return ((String)serializable).getBytes("UTF-8");
            } catch (UnsupportedEncodingException e1) {
                throw new DataMarshallingException(e1);
            }
        }
    }
}
