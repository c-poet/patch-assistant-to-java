package cn.cpoet.patch.assistant.control.menu;

import javafx.scene.control.MenuItem;

import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * @author CPoet
 */
public class MenuItemClaim {

    private final MenuItem item;
    private final Predicate<MenuItem> predicate;

    public MenuItemClaim(MenuItem item) {
        this(item, null);
    }

    public MenuItemClaim(MenuItem item, Predicate<MenuItem> predicate) {
        this.item = item;
        this.predicate = predicate;
    }

    public boolean isAccept() {
        return predicate == null || predicate.test(item);
    }

    public MenuItem getItem() {
        return item;
    }

    public static MenuItemClaim create(Supplier<MenuItem> factory) {
        return new MenuItemClaim(factory.get());
    }

    public static  MenuItemClaim create(Supplier<MenuItem> factory, Predicate<MenuItem> predicate) {
        return new MenuItemClaim(factory.get(), predicate);
    }
}
