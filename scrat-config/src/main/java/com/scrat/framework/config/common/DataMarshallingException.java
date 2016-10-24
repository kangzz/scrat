package com.scrat.framework.config.common;
/**
 * 描述：异常
 * 作者 ：kangzz
 * 日期 ：2016-10-21 16:20:19
 */
public class DataMarshallingException extends RuntimeException {

    private static final long serialVersionUID = -8059710342787509974L;

    public DataMarshallingException() {
        super();
    }

    public DataMarshallingException(Throwable cause) {
        super(cause);
    }

    public DataMarshallingException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataMarshallingException(String message) {
        super(message);
    }
}
