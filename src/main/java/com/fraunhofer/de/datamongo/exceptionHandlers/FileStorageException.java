package com.fraunhofer.de.datamongo.exceptionHandlers;

public class FileStorageException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private final String msg;

    public FileStorageException(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }
}
