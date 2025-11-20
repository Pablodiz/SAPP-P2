package com.tasks.business.exceptions;

public class PermissionException extends RuntimeException{
    public PermissionException() {
        super("Operation not permitted");
    }
}
