package me.pikashrey.glimzocore.menu.menu;
import me.pikashrey.glimzocore.GlimzoCore;
import org.bukkit.entity.Player;
public abstract class SwitchableMenu extends GlimzoMenu {
    protected int currentPage = 0;
    public SwitchableMenu(GlimzoCore plugin, Player player, String title, int size) {
        super(plugin, player, title, size);
    }
    public void nextPage()  { currentPage++; open(); }
    public void prevPage()  { if (currentPage > 0) { currentPage--; open(); } }
    public int  getPage()   { return currentPage; }
}
