package net.vadamdev.jafarbot.utils;

import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author VadamDev
 * @since 17/05/2025
 */
public class LazyAccessor<T> implements Supplier<T> {
    public static <T> LazyAccessor<T> of(Supplier<T> supplier) {
        return new LazyAccessor<>(supplier.get());
    }

    private T value;

    public LazyAccessor(@Nullable T value) {
        this.value = value;
    }

    public LazyAccessor() {
        this(null);
    }

    public void ifPresent(Consumer<T> consumer) {
        if(value != null)
            consumer.accept(value);
    }

    @Nullable
    @Override
    public T get() {
        return value;
    }

    public void set(@Nullable T value) {
        this.value = value;
    }
}
