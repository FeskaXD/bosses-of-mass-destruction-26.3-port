package net.barribob.maelstrom.mixin;

import com.mojang.brigadier.arguments.ArgumentType;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.core.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ArgumentTypeInfos.class)
public interface ArgumentTypeAccessor {
    @Invoker("register")
    static <A extends ArgumentType<?>, T extends ArgumentTypeInfo.Template<A>> ArgumentTypeInfo<A, T> register(Registry<ArgumentTypeInfo<?, ?>> registry, String id, Class<? extends A> clazz, ArgumentTypeInfo<A, T> serializer) {
        throw new AssertionError();
    }
}
