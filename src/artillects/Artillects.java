package artillects;

import java.io.File;
import java.util.Arrays;

import net.minecraft.block.Block;
import net.minecraft.command.ICommandManager;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.NetLoginHandler;
import net.minecraft.network.packet.NetHandler;
import net.minecraft.network.packet.Packet1Login;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatMessageComponent;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import artillects.block.BlockDecoration;
import artillects.block.BlockSymbol;
import artillects.block.teleporter.BlockGlyph;
import artillects.block.teleporter.BlockTeleporterAnchor;
import artillects.block.teleporter.ItemBlockMetadata;
import artillects.block.teleporter.TileEntityTeleporterAnchor;
import artillects.commands.CommandTool;
import artillects.hive.ArtillectEntityType;
import artillects.hive.BlockHiveComplexCore;
import artillects.hive.HiveComplexManager;
import artillects.hive.TileEntityHiveComplexCore;
import artillects.hive.worldgen.HiveComplexGenerator;
import artillects.item.ItemArtillectSpawner;
import artillects.item.ItemBuildingGenerator;
import artillects.item.ItemParts;
import artillects.item.ItemParts.Part;
import artillects.item.ItemSchematicCreator;
import artillects.item.weapons.ItemPlasmaLauncher;
import artillects.item.weapons.ItemWeaponBattery;
import artillects.network.PacketEntity;
import artillects.network.PacketHandler;
import artillects.network.PacketTile;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.Metadata;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.IConnectionHandler;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.relauncher.Side;

@Mod(modid = Artillects.ID, name = Artillects.NAME, version = Artillects.VERSION, useMetadata = true)
@NetworkMod(channels = { Artillects.CHANNEL }, clientSideRequired = true, serverSideRequired = false, packetHandler = PacketHandler.class, connectionHandler = Artillects.class)
public class Artillects implements IConnectionHandler
{
    // @Mod Prerequisites
    public static final String MAJOR_VERSION = "@MAJOR@";
    public static final String MINOR_VERSION = "@MINOR@";
    public static final String REVIS_VERSION = "@REVIS@";
    public static final String BUILD_VERSION = "@BUILD@";
    public static final String VERSION = MAJOR_VERSION + "." + MINOR_VERSION + "." + REVIS_VERSION + "." + BUILD_VERSION;

    // @Mod
    public static final String ID = "Artillects";
    public static final String NAME = "Artillects";

    @SidedProxy(clientSide = "artillects.client.ClientProxy", serverSide = "artillects.CommonProxy")
    public static CommonProxy proxy;

    public static final String CHANNEL = "Artillects";

    public static final Configuration CONFIGURATION = new Configuration(new File(Loader.instance().getConfigDir(), "Dark/Artillects.cfg"));

    private static final String[] LANGUAGES_SUPPORTED = new String[] { "en_US" };

    // Domain and prefix
    public static final String DOMAIN = "artillects";
    public static final String PREFIX = DOMAIN + ":";

    // File paths
    public static final String RESOURCE_DIRECTORY_NO_SLASH = "assets/" + DOMAIN + "/";
    public static final String RESOURCE_DIRECTORY = "/" + RESOURCE_DIRECTORY_NO_SLASH;
    public static final String LANGUAGE_PATH = RESOURCE_DIRECTORY + "lang/";
    public static final String SOUND_PATH = RESOURCE_DIRECTORY + "audio/";

    public static final String TEXTURE_DIRECTORY = "textures/";
    public static final String BLOCK_DIRECTORY = TEXTURE_DIRECTORY + "blocks/";
    public static final String ITEM_DIRECTORY = TEXTURE_DIRECTORY + "items/";
    public static final String MODEL_DIRECTORY = TEXTURE_DIRECTORY + "models/";
    public static final String GUI_DIRECTORY = TEXTURE_DIRECTORY + "gui/";

    /* START IDS */
    public static int BLOCK_ID_PRE = 3856;
    public static int ITEM_ID_PREFIX = 15966;

    /** Packet Types */
    public static final PacketTile PACKET_TILE = new PacketTile();
    public static final PacketEntity PACKET_ENTITY = new PacketEntity();

    @Instance(Artillects.ID)
    public static Artillects instance;

    @Metadata(Artillects.ID)
    public static ModMetadata meta;

    public static Block blockGlyph;
    public static Block blockWall1;
    public static Block blockWall2;
    public static Block blockLight;
    public static Block blockHiveTeleporterNode;

    public static Block blockSymbol;
    public static Block blockHiveCore;

    public static Item itemArtillectSpawner;
    public static Item itemParts;
    public static Item itemBuilding;
    public static Item itemSchematicCreator;
    public static Item weaponPlasmaLauncher;
    public static Item plasmaBattery;

    public static boolean enableHiveComplexGenerator = true;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        MinecraftForge.EVENT_BUS.register(this);

        // Load meta
        meta.modId = ID;
        meta.name = NAME;
        meta.description = "Alien in nature, it is unknown how these Artillects came to exist. What is do know is that they seem to be focused on stripping the planet of its resources...";
        meta.url = "www.universalelectricity.com/artillects";

        meta.logoFile = TEXTURE_DIRECTORY + "Artillects_Banner.png";
        meta.version = VERSION;
        meta.authorList = Arrays.asList(new String[] { "Archadia", "DarkGuardsman", "Calclavia", "Hangcow" });
        meta.credits = "Please see the website.";
        meta.autogenerated = false;

        // Register event handlers
        HiveComplexManager.instance();
        NetworkRegistry.instance().registerGuiHandler(this, Artillects.proxy);

        proxy.preInit();
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        // Register blocks and tiles
        CONFIGURATION.load();
        // Settings
        enableHiveComplexGenerator = CONFIGURATION.get("HiveComplex", "EnableWorldGen", true).getBoolean(true);
        // Item & block ids
        itemArtillectSpawner = new ItemArtillectSpawner();
        itemParts = new ItemParts();
        itemBuilding = new ItemBuildingGenerator();
        itemSchematicCreator = new ItemSchematicCreator();
        weaponPlasmaLauncher = new ItemPlasmaLauncher();
        plasmaBattery = new ItemWeaponBattery("plasmaBattery", 20);

        blockSymbol = new BlockSymbol();

        blockWall1 = new BlockDecoration("decorWall1");
        blockWall2 = new BlockDecoration("decorWall2");
        blockLight = new BlockDecoration("decorLight").setLightValue(1F);

        blockGlyph = new BlockGlyph();

        // Teleporter Blocks
        blockHiveTeleporterNode = new BlockTeleporterAnchor();
        blockHiveCore = new BlockHiveComplexCore();

        CONFIGURATION.save();

        ArtillectsTab.itemStack = new ItemStack(blockSymbol);

        System.out.println(NAME + ": Loaded languages: " + loadLanguages(LANGUAGE_PATH, LANGUAGES_SUPPORTED));

        // Register entities
        for (ArtillectEntityType artillect : ArtillectEntityType.values())
        {
            artillect.register();
        }

        GameRegistry.addRecipe(new ItemStack(plasmaBattery, 1), new Object[] { "X", Character.valueOf('X'), Block.glowStone });

        GameRegistry.registerBlock(blockWall1, "blockWall1");
        GameRegistry.registerBlock(blockWall2, "blockWall2");
        GameRegistry.registerBlock(blockLight, "blockLight");
        GameRegistry.registerBlock(blockHiveTeleporterNode, "blockHiveTeleporterNode");
        GameRegistry.registerBlock(blockSymbol, ItemBlockMetadata.class, "blockSymbol");
        GameRegistry.registerBlock(blockGlyph, ItemBlockMetadata.class, "blockGlyph");
        GameRegistry.registerBlock(blockHiveCore, ItemBlockMetadata.class, "blockHiveCore");
        GameRegistry.registerTileEntity(TileEntityTeleporterAnchor.class, "tileHiveTeleporterAnchor");
        GameRegistry.registerTileEntity(TileEntityHiveComplexCore.class, "tileHiveComplexCore");
        if (Artillects.enableHiveComplexGenerator)
        {
            GameRegistry.registerWorldGenerator(new HiveComplexGenerator());
        }
        proxy.init();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        /* Load Artillect Recipes */
        // Worker
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemArtillectSpawner, 1, ArtillectEntityType.WORKER.ordinal()), "G G", "GCG", "G G", 'G', itemParts, 'C', new ItemStack(itemParts, 1, Part.CIRCUITS_T1.ordinal())));

        // Fabriactor
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemArtillectSpawner, 1, ArtillectEntityType.FABRICATOR.ordinal()), "GCG", "GGG", "GCG", 'G', itemParts, 'C', new ItemStack(itemParts, 1, Part.CIRCUITS_T1.ordinal())));

        // Demolisher
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemArtillectSpawner, 1, ArtillectEntityType.DEMOLISHER.ordinal()), "C C", "GGG", "G G", 'G', itemParts, 'C', new ItemStack(itemParts, 1, Part.CIRCUITS_T1.ordinal())));

        // Seeker
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemArtillectSpawner, 1, ArtillectEntityType.SEEKER.ordinal()), "G G", "GGG", " C ", 'G', itemParts, 'C', new ItemStack(itemParts, 1, Part.CIRCUITS_T1.ordinal())));

        /* Load Recipe Item Recipes */
        // Metal Plate
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemParts, 4, Part.METAL_PLATE.ordinal()), "II ", "II ", 'I', Item.ingotIron));

        // Metal Gear
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemParts, 4, Part.GEARS.ordinal()), "G G", " G ", "G G", 'G', Item.ingotGold));

        // Circuit 1
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemParts, 1, Part.CIRCUITS_T1.ordinal()), "III", "IPI", "III", 'P', new ItemStack(itemParts, 1, ItemParts.Part.METAL_PLATE.ordinal()), 'I', Item.ingotGold));
        GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(itemParts, 1, Part.CIRCUITS_T1.ordinal()), new ItemStack(itemParts, 1, ItemParts.Part.CIRCUITS_MELTED_T1.ordinal()), new ItemStack(itemParts, 1, ItemParts.Part.CIRCUITS_MELTED_T1.ordinal())));
        // Circuit 2
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemParts, 1, Part.CIRCUITS_T2.ordinal()), "III", "IPI", "III", 'P', new ItemStack(itemParts, 1, ItemParts.Part.CIRCUITS_T1.ordinal()), 'I', Item.ingotGold));
        GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(itemParts, 1, Part.CIRCUITS_T2.ordinal()), new ItemStack(itemParts, 1, ItemParts.Part.CIRCUITS_MELTED_T2.ordinal()), new ItemStack(itemParts, 1, ItemParts.Part.CIRCUITS_MELTED_T2.ordinal())));
        // Circuit 3
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemParts, 1, Part.CIRCUITS_T3.ordinal()), "III", "IPI", "III", 'P', new ItemStack(itemParts, 1, ItemParts.Part.CIRCUITS_T2.ordinal()), 'I', new ItemStack(itemParts, 1, ItemParts.Part.CIRCUITS_T1.ordinal())));
        GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(itemParts, 1, Part.CIRCUITS_T3.ordinal()), new ItemStack(itemParts, 1, ItemParts.Part.CIRCUITS_MELTED_T3.ordinal()), new ItemStack(itemParts, 1, ItemParts.Part.CIRCUITS_MELTED_T3.ordinal())));

        // Wall 1
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockWall1, 1), "PGP", "G G", "PGP", 'P', new ItemStack(itemParts, 1, ItemParts.Part.METAL_PLATE.ordinal()), 'G', new ItemStack(itemParts, 1, ItemParts.Part.GEARS.ordinal())));
        // Wall 2
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockWall1, 1), "GPG", "P P", "GPG", 'P', new ItemStack(itemParts, 1, ItemParts.Part.METAL_PLATE.ordinal()), 'G', new ItemStack(itemParts, 1, ItemParts.Part.GEARS.ordinal())));

        proxy.postInit();
    }

    /** Loads all the language files for a mod. This supports the loading of "child" language files
     * for sub-languages to be loaded all from one file instead of creating multiple of them. An
     * example of this usage would be different Spanish sub-translations (es_MX, es_YU).
     * 
     * @param languagePath - The path to the mod's language file folder.
     * @param languageSupported - The languages supported. E.g: new String[]{"en_US", "en_AU",
     * "en_UK"}
     * @return The amount of language files loaded successfully. */
    public static int loadLanguages(String languagePath, String[] languageSupported)
    {
        int languages = 0;

        /** Load all languages. */
        for (String language : languageSupported)
        {
            LanguageRegistry.instance().loadLocalization(languagePath + language + ".properties", language, false);

            if (LanguageRegistry.instance().getStringLocalization("children", language) != "")
            {
                try
                {
                    String[] children = LanguageRegistry.instance().getStringLocalization("children", language).split(",");

                    for (String child : children)
                    {
                        if (child != "" || child != null)
                        {
                            LanguageRegistry.instance().loadLocalization(languagePath + language + ".properties", child, false);
                            languages++;
                        }
                    }
                }
                catch (Exception e)
                {
                    FMLLog.severe("Failed to load a child language file.");
                    e.printStackTrace();
                }
            }

            languages++;
        }

        return languages;
    }

    /** Gets the local text of your translation based on the given key. This will look through your
     * mod's translation file that was previously registered. Make sure you enter the full name
     * 
     * @param key - e.g tile.block.name
     * @return The translated string or the default English translation if none was found. */
    public static String getLocal(String key)
    {
        String text = null;

        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
        {
            text = LanguageRegistry.instance().getStringLocalization(key);
        }

        if (text == null || text == "")
        {
            text = LanguageRegistry.instance().getStringLocalization(key, "en_US");
        }

        return text;
    }

    public static int nextBlockID()
    {
        int id = BLOCK_ID_PRE;

        while (id > 255 && id < (Block.blocksList.length - 1))
        {
            Block block = Block.blocksList[id];
            if (block == null)
            {
                break;
            }
            id++;
        }
        BLOCK_ID_PRE = id + 1;
        return id;
    }

    public static int nextItemID()
    {
        int id = ITEM_ID_PREFIX;

        while (id > 255 && id < (Item.itemsList.length - 1))
        {
            Item item = Item.itemsList[id];
            if (item == null)
            {
                break;
            }
            id++;
        }
        ITEM_ID_PREFIX = id + 1;
        return id;
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event)
    {
        ICommandManager commandManager = FMLCommonHandler.instance().getMinecraftServerInstance().getCommandManager();
        ServerCommandManager serverCommandManager = ((ServerCommandManager) commandManager);
        serverCommandManager.registerCommand(new CommandTool());
    }

    @Override
    public void playerLoggedIn(Player player, NetHandler netHandler, INetworkManager manager)
    {
        if (player != null && !((EntityPlayer) player).worldObj.isRemote)
        {
            ((EntityPlayer) player).sendChatToPlayer(ChatMessageComponent.createFromText("[Artillect] Visit http://wiki.universalelectricity.com/artillects for documentation on Artillects."));
            ((EntityPlayer) player).sendChatToPlayer(ChatMessageComponent.createFromText("[Artillect] For more updated versions of the mod, visit http://www.calclavia.com:8080/job/Artillects/"));
        }
    }

    @Override
    public String connectionReceived(NetLoginHandler netHandler, INetworkManager manager)
    {
        return null;
    }

    @Override
    public void connectionOpened(NetHandler netClientHandler, String server, int port, INetworkManager manager)
    {
    }

    @Override
    public void connectionOpened(NetHandler netClientHandler, MinecraftServer server, INetworkManager manager)
    {
    }

    @Override
    public void connectionClosed(INetworkManager manager)
    {
    }

    @Override
    public void clientLoggedIn(NetHandler clientHandler, INetworkManager manager, Packet1Login login)
    {
    }

}
