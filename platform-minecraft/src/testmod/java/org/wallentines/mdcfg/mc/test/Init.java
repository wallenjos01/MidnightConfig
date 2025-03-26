package org.wallentines.mdcfg.mc.test;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.mc.api.ServerSQLManager;
import org.wallentines.mdcfg.sql.DataType;
import org.wallentines.mdcfg.sql.TableSchema;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Init implements ModInitializer {


    private static final Logger log = LoggerFactory.getLogger(Init.class);

    @Override
    public void onInitialize() {

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {

            ServerSQLManager.getPresetRegistry(server).connect(new ConfigSection().with("preset", "default")).thenAccept(conn -> {

                if(conn.hasTable("test")) {
                    String msg = conn.select("test")
                            .withColumn("message")
                            .execute()
                            .get(0)
                            .getString("message");

                    log.info("Got message: {}", msg);

                } else {
                    String msg = "Hello World! This table was created at " + SimpleDateFormat.getDateTimeInstance().format(new Date());
                    conn.createTable("test", TableSchema.builder().withColumn("message", DataType.VARCHAR(255)).build()).execute();
                    conn.insert("test", new ConfigSection().with("message", msg)).execute();
                    log.info("Inserted message: {}", msg);
                }

            });

        });

    }
}
