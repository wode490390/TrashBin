package cn.wode490390.nukkit.trashbin;

import cn.nukkit.Player;
import cn.nukkit.inventory.CustomInventory;
import cn.nukkit.inventory.FakeBlockMenu;
import cn.nukkit.inventory.InventoryType;
import cn.nukkit.level.Position;
import cn.nukkit.math.Vector3;

@Deprecated
public class TrashBinV1Inventory extends CustomInventory {

    public TrashBinV1Inventory(Position position) {
        super(null, InventoryType.CHEST);
        this.holder = new FakeBlockMenu(this, position);
    }

    @Override
    public void onClose(Player player) {
        super.onClose(player);
        player.getLevel().sendBlocks(new Player[]{player}, new Vector3[]{(Vector3) this.holder});
    }
}
