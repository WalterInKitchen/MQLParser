package org.walterinkitchen.parser.exception;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class MqlRuntimeException extends RuntimeException{
    @Getter
    private int code;
    @Getter
    private int msg;
}
