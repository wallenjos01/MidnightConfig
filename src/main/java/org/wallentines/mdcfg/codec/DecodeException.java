package org.wallentines.mdcfg.codec;

/**
 * An exception thrown when a Codec fails to decode something
 */
public class DecodeException extends RuntimeException {

    public DecodeException(String message) {
        super(message);
    }
}
