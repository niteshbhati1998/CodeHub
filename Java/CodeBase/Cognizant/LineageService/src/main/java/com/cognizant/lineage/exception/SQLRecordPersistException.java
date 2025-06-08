package com.cognizant.lineage.exception;

public class SQLRecordPersistException extends RuntimeException{

    public SQLRecordPersistException() {super();}

    public SQLRecordPersistException(String message) {
        super(message);
    }

    public SQLRecordPersistException(String message, Exception e) {
        super(message, e);
    }
}
