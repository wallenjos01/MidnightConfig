package org.wallentines.mdcfg.mc.gametest;

import java.lang.reflect.Method;

import org.jspecify.annotations.NonNull;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.mc.api.ServerSQLManager;

import net.fabricmc.fabric.api.gametest.v1.CustomTestMethodInvoker;
import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.gametest.framework.GameTestHelper;

public class GameTests implements CustomTestMethodInvoker {

    @Override
    public void invokeTestMethod(GameTestHelper helper, @NonNull Method method) throws ReflectiveOperationException {
        method.invoke(this, helper);
    }


    @GameTest
    public void canConnect(GameTestHelper helper) {

        try {
            ServerSQLManager.getPresetRegistry(helper.getLevel().getServer())
                    .connect(new ConfigSection().with("preset", "default"))
                    .thenAccept(conn -> {

                if(conn == null) {
                    helper.fail("Unable to connect to SQL!");
                } else {
                    helper.succeed();
                }
            }).join();
        } catch(Exception ex) {
            helper.fail(ex.getMessage());
        }   
    }
}
