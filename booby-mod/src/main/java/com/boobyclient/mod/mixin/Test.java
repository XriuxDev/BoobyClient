package com.boobyclient.mod.mixin;
import net.minecraft.client.Keyboard;
import net.minecraft.client.input.KeyInput;
public class Test {
    public static void main(String[] args) throws Exception {
        for (java.lang.reflect.Method m : Keyboard.class.getDeclaredMethods()) {
            if (m.getName().equals("onKey")) {
                System.out.println(m);
            }
        }
    }
}
