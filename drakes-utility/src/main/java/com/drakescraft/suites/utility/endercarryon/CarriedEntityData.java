package com.drakescraft.suites.utility.endercarryon;

import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;

/** Keeps large snapshots out of NBT strings without changing their contents. */
public final class CarriedEntityData {

    private CarriedEntityData() {}

    public static void write(PersistentDataContainer container, NamespacedKey key, String snapshot) {
        // NBT strings use modified UTF-8, whose length is an unsigned short.
        int length = 0;
        for (int i = 0; i < snapshot.length(); i++) {
            char c = snapshot.charAt(i);
            length += c >= 1 && c <= 127 ? 1 : c <= 2047 ? 2 : 3;
            if (length > 65535) {
                try {
                    ByteBuffer encoded = StandardCharsets.UTF_8.newEncoder().encode(CharBuffer.wrap(snapshot));
                    byte[] bytes = new byte[encoded.remaining()];
                    encoded.get(bytes);
                    container.set(key, PersistentDataType.BYTE_ARRAY, bytes);
                } catch (CharacterCodingException ex) {
                    throw new IllegalArgumentException("Invalid carried entity encoding", ex);
                }
                return;
            }
        }
        // Retain the legacy representation for snapshots that already fit.
        container.set(key, PersistentDataType.STRING, snapshot);
    }

    public static boolean has(PersistentDataContainer container, NamespacedKey key) {
        return container.has(key, PersistentDataType.STRING) || container.has(key, PersistentDataType.BYTE_ARRAY);
    }

    public static String read(PersistentDataContainer container, NamespacedKey key) {
        if (container.has(key, PersistentDataType.STRING)) {
            return container.get(key, PersistentDataType.STRING);
        }
        byte[] bytes = container.get(key, PersistentDataType.BYTE_ARRAY);
        if (bytes == null) {
            return null;
        }
        try {
            return StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(bytes)).toString();
        } catch (CharacterCodingException ex) {
            throw new IllegalArgumentException("Invalid carried entity encoding", ex);
        }
    }
}
