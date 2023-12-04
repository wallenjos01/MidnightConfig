package org.wallentines.mdcfg.tool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.codec.*;
import org.wallentines.mdcfg.serializer.ConfigContext;

import java.io.File;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger("");

    public static void main(String[] args) {

        if(args.length != 2) {
            logger.error("Usage: cfgtool [input] [output]");
            return;
        }

        File input = new File(args[0]);
        if(!input.exists()) {
            logger.error("Input file " + args[0] + " does not exist!");
            return;
        }

        File output = new File(args[1]);
        if(output.exists()) {
            logger.error("Output file " + args[1] + " already exists!");
            return;
        }

        FileCodecRegistry reg = new FileCodecRegistry();
        reg.registerFileCodec(JSONCodec.fileCodec());
        reg.registerFileCodec(BinaryCodec.fileCodec());

        FileWrapper<ConfigObject> inputWrapper = reg.fromFile(ConfigContext.INSTANCE, input);
        if(inputWrapper == null) {
            logger.error("Unable to find codec for " + args[0] + "!");
            return;
        }

        FileWrapper<ConfigObject> outputWrapper = reg.fromFile(ConfigContext.INSTANCE, output);
        if(outputWrapper == null) {
            logger.error("Unable to find codec for " + args[1] + "!");
            return;
        }

        try {
            inputWrapper.load();
        } catch (DecodeException ex) {
            logger.error("An error occurred while reading the input file!", ex);
            return;
        }

        outputWrapper.setRoot(inputWrapper.getRoot());

        try {
            outputWrapper.save();
        } catch (EncodeException ex) {
            logger.error("An error occurred while writing the output file!", ex);
            return;
        }

        logger.info("Conversion complete");
    }

}
