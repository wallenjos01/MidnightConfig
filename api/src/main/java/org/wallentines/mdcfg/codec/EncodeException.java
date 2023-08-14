package org.wallentines.mdcfg.codec;

/**
 * An exception thrown when a Codec fails to encode something
 */
public class EncodeException extends RuntimeException {

    public EncodeException(String message) {
        super(message);
    }
}
