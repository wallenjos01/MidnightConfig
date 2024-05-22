import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wallentines.mdcfg.ConfigList;
import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.codec.*;
import org.wallentines.mdcfg.serializer.ConfigContext;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

public class TestSNBT {

    @Test
    public void testHelloWorld() {
        testFile("hello_world", false);
    }

    @Test
    public void testBig() {
        testFile("bigtest", true);
    }

    private void testFile(String name, boolean compress) {

        NBTCodec codec = new NBTCodec(true);
        SNBTCodec out = new SNBTCodec(true);
        File f = new File(name + ".nbt");
        File fOut = new File(name + ".snbt");

        Assertions.assertTrue(f.isFile());
        ConfigObject decoded;

        try(FileInputStream fis = new FileInputStream(f)) {

            InputStream is = new BufferedInputStream(compress ? new GZIPInputStream(fis) : fis);
            decoded = codec.decode(ConfigContext.INSTANCE, is, StandardCharsets.UTF_8);

        } catch (Exception ex) {

            Assertions.fail("An error occurred while decoding NBT! File: " + name + ", Compressed: " + compress, ex);
            return;
        }

        Assertions.assertTrue(decoded.isSection());


        try(FileOutputStream fos = new FileOutputStream(fOut)) {

            out.encode(ConfigContext.INSTANCE, decoded, fos, StandardCharsets.UTF_8);

        } catch (Exception ex) {
            Assertions.fail("An error occurred while encoding NBT! File: " + name + ", Compressed: " + compress, ex);
        }


        ConfigObject reDecoded;
        try(FileInputStream fis = new FileInputStream(fOut)) {
            reDecoded = out.decode(ConfigContext.INSTANCE, fis, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            Assertions.fail("An error occurred while re-decoding NBT! File: " + name + ", Compressed: " + compress, ex);
            return;
        }

        Assertions.assertEquals(decoded, reDecoded);
    }

    @Test
    public void testString() {

        SNBTCodec codec = new SNBTCodec();

        String snbt = "{Brain:{memories:{}},HurtByTimestamp:0,bukkit:{newLevel:0,newExp:0,newTotalExp:0,firstPlayed:1716304392858L,lastKnownName:\"Player\",keepLevel:0b,expToDrop:0,lastPlayed:1716311099573L},SleepTimer:0s,Attributes:[{Base:1.0d,Name:\"minecraft:generic.attack_damage\"},{Base:0.0d,Name:\"minecraft:generic.armor_toughness\"},{Base:0.10000000149011612d,Name:\"minecraft:generic.movement_speed\"},{Base:4.0d,Name:\"minecraft:generic.attack_speed\"},{Base:0.0d,Name:\"minecraft:generic.armor\"},{Base:20.0d,Name:\"minecraft:generic.max_health\"}],Invulnerable:0b,FallFlying:0b,PortalCooldown:0,AbsorptionAmount:0.0f,abilities:{invulnerable:0b,mayfly:0b,instabuild:0b,walkSpeed:0.1f,mayBuild:1b,flying:0b,flySpeed:0.05f},FallDistance:0.0f,recipeBook:{recipes:[\"minecraft:iron_nugget_from_blasting\",\"minecraft:gold_nugget_from_blasting\",\"minecraft:painting\",\"minecraft:red_bed\",\"minecraft:gold_nugget_from_smelting\",\"minecraft:red_carpet\",\"minecraft:red_banner\",\"minecraft:iron_nugget_from_smelting\"],isBlastingFurnaceFilteringCraftable:0b,isSmokerGuiOpen:0b,isFilteringCraftable:0b,toBeDisplayed:[\"minecraft:iron_nugget_from_blasting\",\"minecraft:gold_nugget_from_blasting\",\"minecraft:painting\",\"minecraft:red_bed\",\"minecraft:gold_nugget_from_smelting\",\"minecraft:red_carpet\",\"minecraft:red_banner\",\"minecraft:iron_nugget_from_smelting\"],isFurnaceGuiOpen:0b,isGuiOpen:0b,isFurnaceFilteringCraftable:0b,isBlastingFurnaceGuiOpen:0b,isSmokerFilteringCraftable:0b},DeathTime:0s,XpSeed:-2140122175,WorldUUIDMost:-9154524897287582949L,Spigot.ticksLived:12256,XpTotal:0,UUID:[I;1729298858,906775680,-1985245204,14760118],playerGameType:0,seenCredits:0b,Motion:[0.0d,-0.0784000015258789d,0.0d],Health:20.0f,Bukkit.updateLevel:2,foodSaturationLevel:0.0f,Air:300s,OnGround:1b,Dimension:\"minecraft:the_nether\",Rotation:[73.054665f,47.54999f],XpLevel:0,Score:0,Pos:[-1.300000011920929d,98.0d,6.905815973765763d],previousPlayerGameType:1,Fire:-20s,XpP:0.0f,EnderItems:[],DataVersion:2584,foodLevel:15,foodExhaustionLevel:3.5610077f,HurtTime:0s,SelectedItemSlot:2,WorldUUIDLeast:-7240935019876135968L,Inventory:[{Slot:0b,id:\"minecraft:command_block\",Count:1b,tag:{display:{Name:'{\"text\":\"Hello\",\"color\":\"#398f3c\",\"italic\":false}'}}},{Slot:1b,id:\"minecraft:stone_sword\",Count:1b,tag:{Damage:0,display:{Name:'{\"text\":\"Test Sword\",\"color\":\"#af4ebe\",\"italic\":false}'},Enchantments:[{lvl:100,id:\"minecraft:sharpness\"}]}},{Slot:100b,id:\"minecraft:diamond_boots\",Count:1b,tag:{Damage:0,Enchantments:[{lvl:4,id:\"minecraft:feather_falling\"}]}},{Slot:101b,id:\"minecraft:golden_leggings\",Count:1b,tag:{Damage:0,Enchantments:[{lvl:3,id:\"minecraft:unbreaking\"}]}},{Slot:102b,id:\"minecraft:iron_chestplate\",Count:1b,tag:{Damage:0,Enchantments:[{lvl:5,id:\"minecraft:protection\"}]}},{Slot:103b,id:\"minecraft:leather_helmet\",Count:1b,tag:{Damage:0,Enchantments:[{lvl:3,id:\"minecraft:respiration\"}]}}],foodTickTimer:0}";
        ConfigObject obj = codec.decode(ConfigContext.INSTANCE, snbt);

        String dump = codec.encodeToString(ConfigContext.INSTANCE, obj);
        Assertions.assertEquals(snbt, dump);

    }

    @Test
    public void testIndexed() {

        ConfigSection sec = new ConfigSection()
                .with("test", new ConfigList().append(10L).append(11L).append(13L));

        SNBTCodec codec = new SNBTCodec(false, true);
        String encoded = codec.encodeToString(ConfigContext.INSTANCE, sec);

        System.out.println(encoded);

        ConfigSection out = codec.decode(ConfigContext.INSTANCE, encoded).asSection();

        Assertions.assertEquals(sec, out);

    }

    @Test
    public void testEscaped() {
        SNBTCodec codec = new SNBTCodec()
                .useDoubleQuotes();

        String snbt = "{display:{Lore:[\"{\\\"text\\\":\\\"Test\\\",\\\"color\\\":\\\"#ffffff\\\",\\\"italic\\\":false}\"],Name:\"{\\\"text\\\":\\\"Test\\\",\\\"color\\\":\\\"#55ff55\\\",\\\"italic\\\":false}\"}}";
        ConfigObject obj = codec.decode(ConfigContext.INSTANCE, snbt);

        String dump = codec.encodeToString(ConfigContext.INSTANCE, obj);
        Assertions.assertEquals(snbt, dump);
    }

}
