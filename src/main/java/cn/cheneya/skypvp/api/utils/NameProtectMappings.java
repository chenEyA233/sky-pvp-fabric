package cn.cheneya.skypvp.api.utils;

import java.security.MessageDigest;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Keeps track of the current name protect mappings and contains functions for replacement.
 */
public class NameProtectMappings {
    private String username;
    private String replacement;

    private Map<String, String> friendMappings = new HashMap<>();
    private Set<String> otherPlayerMappings = new HashSet<>();

    private Map<String, String> currentMappings = new HashMap<>();
    private boolean protectOtherPlayers = false; // 默认不保护其他玩家名称

    /**
     * Updates the mappings with new information.
     */
    public void update(String username, String replacement, Map<String, String> friendMappings, List<String> otherPlayers) {
        update(username, replacement, friendMappings, otherPlayers, false);
    }

    /**
     * Updates the mappings with new information.
     * @param protectOtherPlayers 是否保护其他玩家的名称
     */
    public void update(String username, String replacement, Map<String, String> friendMappings,
                      List<String> otherPlayers, boolean protectOtherPlayers) {
        boolean shouldUpdate = !username.equals(this.username) ||
                              !replacement.equals(this.replacement) ||
                              !this.friendMappings.equals(friendMappings) ||
                              otherPlayers.stream().anyMatch(name -> !this.otherPlayerMappings.contains(name)) ||
                              friendMappings.size() != this.friendMappings.size() ||
                              otherPlayers.size() != this.otherPlayerMappings.size() ||
                              protectOtherPlayers != this.protectOtherPlayers;

        if (!shouldUpdate) {
            return;
        }

        this.username = username;
        this.replacement = replacement;
        this.friendMappings = new HashMap<>(friendMappings);
        this.otherPlayerMappings = new HashSet<>(otherPlayers);
        this.protectOtherPlayers = protectOtherPlayers;

        // Build new mappings
        Map<String, String> newMappings = new HashMap<>();

        // Add username mapping
        newMappings.put(username, replacement);

        // Add friend mappings
        newMappings.putAll(friendMappings);

        // Add other player mappings only if protectOtherPlayers is true
        if (protectOtherPlayers) {
            for (String playerName : otherPlayers) {
                if (playerName.length() >= 3 && playerName.length() <= 20) {
                    Random rng = getEntropySourceFrom(playerName);
                    newMappings.put(playerName, generateRandomUsername(16, rng));
                }
            }
        }

        this.currentMappings = newMappings;
    }

    /**
     * Replaces all occurrences of protected names in the given text.
     */
    public String replace(String text) {
        if (text == null || text.isEmpty() || currentMappings.isEmpty()) {
            return text;
        }

        String result = text;

        for (Map.Entry<String, String> entry : currentMappings.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }

        return result;
    }

    /**
     * Generates a random username with the given length.
     */
    private String generateRandomUsername(int length, Random random) {
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(chars.length());
            sb.append(chars.charAt(index));
        }

        return sb.toString();
    }

    /**
     * Creates a deterministic random source from a player name.
     */
    private Random getEntropySourceFrom(String playerName) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(playerName.getBytes());
            long seed = ByteBuffer.wrap(hash).getLong();
            return new Random(seed);
        } catch (Exception e) {
            return new Random(playerName.hashCode());
        }
    }
}
