package com.example.cheat;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class CheatClient implements ClientModInitializer {

    private static final KeyBinding.Category CATEGORY =
            KeyBinding.Category.create(Identifier.of("cheatmod", "main"));

    private record Bind(KeyBinding key, Consumer<MinecraftClient> action) {}
    private final List<Bind> binds = new ArrayList<>();

    private boolean god, nightVision, speed;

    @Override
    public void onInitializeClient() {
        bind("creative", GLFW.GLFW_KEY_G, c -> cmd(c, "gamemode creative"));

        bind("god", GLFW.GLFW_KEY_H, c -> {
            god = !god;
            if (god) {
                cmd(c, "effect give @s minecraft:resistance infinite 4 true");
                cmd(c, "effect give @s minecraft:regeneration infinite 4 true");
                cmd(c, "effect give @s minecraft:saturation infinite 0 true");
            } else {
                cmd(c, "effect clear @s minecraft:resistance");
                cmd(c, "effect clear @s minecraft:regeneration");
                cmd(c, "effect clear @s minecraft:saturation");
            }
            msg(c, "Modo deus: " + (god ? "ON" : "OFF"));
        });

        bind("nightvision", GLFW.GLFW_KEY_J, c -> {
            nightVision = !nightVision;
            cmd(c, nightVision
                    ? "effect give @s minecraft:night_vision infinite 0 true"
                    : "effect clear @s minecraft:night_vision");
            msg(c, "Visão noturna: " + (nightVision ? "ON" : "OFF"));
        });

        bind("day", GLFW.GLFW_KEY_K, c -> {
            cmd(c, "time set day");
            cmd(c, "weather clear");
        });

        bind("clearitems", GLFW.GLFW_KEY_L, c -> cmd(c, "kill @e[type=minecraft:item]"));

        bind("speed", GLFW.GLFW_KEY_N, c -> {
            speed = !speed;
            if (speed) {
                cmd(c, "effect give @s minecraft:speed infinite 4 true");
                cmd(c, "effect give @s minecraft:jump_boost infinite 3 true");
            } else {
                cmd(c, "effect clear @s minecraft:speed");
                cmd(c, "effect clear @s minecraft:jump_boost");
            }
            msg(c, "Velocidade/pulo: " + (speed ? "ON" : "OFF"));
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;
            for (Bind b : binds) {
                while (b.key().wasPressed()) b.action().accept(client);
            }
        });
    }

    private void bind(String name, int glfwKey, Consumer<MinecraftClient> action) {
        KeyBinding kb = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.cheatmod." + name, InputUtil.Type.KEYSYM, glfwKey, CATEGORY));
        binds.add(new Bind(kb, action));
    }

    /** Envia um comando (sem a barra). Precisa de permissão de op. */
    private static void cmd(MinecraftClient c, String command) {
        if (c.player != null) c.player.networkHandler.sendChatCommand(command);
    }

    private static void msg(MinecraftClient c, String text) {
        if (c.player != null) c.player.sendMessage(Text.literal("[Cheat] " + text), true);
    }
}
