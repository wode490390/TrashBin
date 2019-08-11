package cn.wode490390.nukkit.trashbin;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockSignPost;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.BlockEntitySign;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.item.EntityMinecartChest;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.inventory.InventoryType;
import cn.nukkit.level.GlobalBlockPalette;
import cn.nukkit.level.Position;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.AddEntityPacket;
import cn.nukkit.network.protocol.BlockEntityDataPacket;
import cn.nukkit.network.protocol.UpdateBlockPacket;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import com.google.common.collect.Maps;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.Map;

public class TrashBin extends PluginBase implements Listener {

    private static final Map<Player, Integer> cd = Maps.newHashMap();

    private static String title;

    @Override
    public void onEnable() {
        this.getServer().getCommandMap().register("trashbin", new TrashBinCommand(this));
        this.saveDefaultConfig();
        Config config = this.getConfig();
        String node = "inventory-title";
        String title;
        try {
            title = config.getString(node, "&4&lTrash Bin");
        } catch (Exception ex) {
            title = "&4&lTrash Bin";
            this.logLoadException(node, ex);
        }
        TrashBin.title = TextFormat.colorize(title);
        node = "enable-sign";
        boolean sign;
        try {
            sign = config.getBoolean(node, true);
        } catch (Exception ex) {
            sign = true;
            this.logLoadException(node, ex);
        }
        if (sign) {
            this.getServer().getPluginManager().registerEvents(this, this);
        }
        try {
            new MetricsLite(this);
        } catch (Exception ignore) {

        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Block block = event.getBlock();
        if (block instanceof BlockSignPost) {
            BlockEntity tile = block.getLevel().getBlockEntity(block);
            if (tile instanceof BlockEntitySign) {
                BlockEntitySign sign = (BlockEntitySign) tile;
                if (TextFormat.clean(sign.getText()[0]).equalsIgnoreCase("[TrashBin]")) {
                    open(event.getPlayer());
                }
            }
        }
    }

    public static void open(Player player) {
        Integer next = cd.get(player);
        int tick = Server.getInstance().getTick();
        if (next != null && next >= tick) {
            return;
        }
        cd.put(player, tick + 20);
        openV2(player);
    }

    public static void openV2(Player player) {
        long id = Entity.entityCount++;
        AddEntityPacket fakeEntity = new AddEntityPacket();
        fakeEntity.entityUniqueId = id;
        fakeEntity.entityRuntimeId = id;
        fakeEntity.type = EntityMinecartChest.NETWORK_ID;
        fakeEntity.x = (float) player.x;
        fakeEntity.y = (float) player.y;
        fakeEntity.z = (float) player.z;
        fakeEntity.metadata
                .putString(Entity.DATA_NAMETAG, title)
                .putByte(Entity.DATA_CONTAINER_TYPE, 10)
                .putInt(Entity.DATA_CONTAINER_BASE_SIZE, InventoryType.CHEST.getDefaultSize());
        player.dataPacket(fakeEntity);

        player.addWindow(new TrashBinInventory(id));
    }

    @Deprecated
    public static void openV1(Player player) {
        UpdateBlockPacket fakeBlock = new UpdateBlockPacket();
        fakeBlock.x = player.getFloorX();
        fakeBlock.y = player.getFloorY();
        fakeBlock.z = player.getFloorZ();
        fakeBlock.blockRuntimeId = GlobalBlockPalette.getOrCreateRuntimeId(Block.CHEST, 0);
        player.dataPacket(fakeBlock);

        BlockEntityDataPacket fakeTile = new BlockEntityDataPacket();
        fakeTile.x = fakeBlock.x;
        fakeTile.y = fakeBlock.y;
        fakeTile.z = fakeBlock.z;
        try {
            fakeTile.namedTag = NBTIO.write(new CompoundTag()
                    .putString("id", BlockEntity.CHEST)
                    .putInt("x", fakeTile.x)
                    .putInt("y", fakeTile.y)
                    .putInt("z", fakeTile.z)
                    .putBoolean("isMovable", true)
                    .putString("CustomName", title),
            ByteOrder.LITTLE_ENDIAN, true);
        } catch (IOException ignore) {
            fakeTile.namedTag = new byte[0];
        }
        player.dataPacket(fakeTile);

        player.addWindow(new TrashBinV1Inventory(new Position(fakeTile.x, fakeTile.y, fakeTile.z)));
    }

    private void logLoadException(String node, Exception ex) {
        this.getLogger().alert("An error occurred while reading the configuration '" + node + "'. Use the default value.", ex);
    }
}
