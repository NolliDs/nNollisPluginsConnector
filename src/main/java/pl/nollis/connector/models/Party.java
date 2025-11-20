package pl.nollis.connector.models;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class Party {
    private final UUID leader;
    private final Set<UUID> members;
    private final int maxSize;
    private final Map<UUID, Long> pendingInvites;

    public Party(UUID leader, int maxSize) {
        this.leader = leader;
        this.members = new HashSet<>();
        this.members.add(leader);
        this.maxSize = maxSize;
        this.pendingInvites = new HashMap<>();
    }

    public UUID getLeader() {
        return leader;
    }

    public Set<UUID> getMembers() {
        return new HashSet<>(members);
    }

    public boolean isLeader(UUID playerId) {
        return leader.equals(playerId);
    }

    public boolean isMember(UUID playerId) {
        return members.contains(playerId);
    }

    public boolean isFull() {
        return members.size() >= maxSize;
    }

    public int getSize() {
        return members.size();
    }

    public int getMaxSize() {
        return maxSize;
    }

    public List<Player> getOnlineMembers() {
        List<Player> online = new ArrayList<>();
        for (UUID uuid : members) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                online.add(player);
            }
        }
        return online;
    }

    public Player getLeaderPlayer() {
        return Bukkit.getPlayer(leader);
    }

    public void addMember(UUID playerId) {
        if (!isFull()) {
            members.add(playerId);
            pendingInvites.remove(playerId);
        }
    }

    public void removeMember(UUID playerId) {
        members.remove(playerId);
        pendingInvites.remove(playerId);
    }

    public void invite(UUID target) {
        if (!isFull()) {
            pendingInvites.put(target, System.currentTimeMillis());
        }
    }

    public boolean hasInvite(UUID playerId) {
        return pendingInvites.containsKey(playerId);
    }

    public void removeInvite(UUID playerId) {
        pendingInvites.remove(playerId);
    }

    public void cleanExpiredInvites(long timeoutMillis) {
        long currentTime = System.currentTimeMillis();
        pendingInvites.entrySet().removeIf(entry ->
            currentTime - entry.getValue() > timeoutMillis
        );
    }

    public Set<UUID> getPendingInvites() {
        return new HashSet<>(pendingInvites.keySet());
    }
}
