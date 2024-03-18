/*
 *  Copyright Osiris Team
 *  All rights reserved.
 *
 *  This software is licensed work.
 *  Please consult the file "LICENSE" for details.
 */

package com.mohistmc.yml.exceptions;

import java.util.Objects;

public class DuplicateKeyException extends Exception {
    private final String message;

    public DuplicateKeyException(String message) {
        this(message, null, null);
    }

    public DuplicateKeyException(String fileName, String key) {
        this(null, fileName, key);
    }

    public DuplicateKeyException(String message, String fileName, String key) {
        super();
        this.message = Objects.requireNonNullElseGet(message, () -> "Duplicate key '" + key + "' found in '" + fileName + "' file.");
    }

    @Override
    public String getMessage() {
        return message;
    }
}
