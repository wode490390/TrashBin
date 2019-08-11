package cn.wode490390.nukkit.trashbin;

import cn.nukkit.Player;
import cn.nukkit.inventory.CustomInventory;
import cn.nukkit.inventory.InventoryType;
import cn.nukkit.network.protocol.ContainerOpenPacket;
import cn.nukkit.network.protocol.RemoveEntityPacket;

public class TrashBinInventory extends CustomInventory {

    private final long id;

    public TrashBinInventory(long id) {
        super(null, InventoryType.CHEST);
        this.id = id;
    }

    @Override
    public void onOpen(Player player) {
        ContainerOpenPacket pk = new ContainerOpenPacket();
        pk.windowId = player.getWindowId(this);
        pk.entityId = this.id;
        player.dataPacket(pk);
    }

    @Override
    public void onClose(Player player) {
        super.onClose(player);

        RemoveEntityPacket pk = new RemoveEntityPacket();
        pk.eid = this.id;
        player.dataPacket(pk);
    }
}
