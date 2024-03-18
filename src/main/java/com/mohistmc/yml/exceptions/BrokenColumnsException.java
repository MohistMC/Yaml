package com.mohistmc.yml.exceptions;

import com.mohistmc.yml.db.YamlColumn;

/**
 * Usually thrown when there is something wrong
 * with a {@link YamlColumn}s length.
 */
public class BrokenColumnsException extends RuntimeException {
    public BrokenColumnsException() {
    }

    public BrokenColumnsException(String message) {
        super(message);
    }

    public BrokenColumnsException(String message, Throwable cause) {
        super(message, cause);
    }

    public BrokenColumnsException(Throwable cause) {
        super(cause);
    }

    public BrokenColumnsException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
