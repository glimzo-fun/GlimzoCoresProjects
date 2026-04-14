package me.pikashrey.glimzocore.utilities.item;
import me.pikashrey.glimzocore.utilities.chat.CC;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import java.util.Arrays;
import java.util.List;
public final class BookUtil {
    private BookUtil() {}
    public static ItemStack createNickBook() {
        ItemStack book = new ItemStack(Material.BOOK_AND_QUILL);
        BookMeta meta = (BookMeta) book.getItemMeta();
        meta.setTitle("Set Nickname");
        meta.setAuthor("GlimzoCore");
        meta.addPage(CC.translate(
                "&8Write your desired nickname on this page, then close the book to apply it.\n\n"
                + "&7Rules:\n&7- 3-16 characters\n&7- Letters, digits, underscores\n"
                + "&7- No impersonation\n\n&6Write below:"));
        book.setItemMeta(meta);
        return book;
    }
}
