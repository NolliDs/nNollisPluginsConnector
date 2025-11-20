package pl.nollis.connector.managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import pl.nollis.connector.NollisPluginsConnector;
import pl.nollis.connector.api.PartyAPI;
import pl.nollis.connector.models.Party;

import java.util.*;

public class PartyManager implements PartyAPI {
    private final NollisPluginsConnector plugin;
    private final Map<UUID, Party> parties; // leader UUID -> Party
    private final Map<UUID, UUID> playerToParty; // player UUID -> leader UUID
    private final int maxSize;
    private final long inviteTimeout;

    public PartyManager(NollisPluginsConnector plugin) {
        this.plugin = plugin;
        this.parties = new HashMap<>();
        this.playerToParty = new HashMap<>();
        this.maxSize = plugin.getConfig().getInt("party.max-size", 2);
        this.inviteTimeout = plugin.getConfig().getLong("party.invite-timeout", 30) * 1000; // Convert to milliseconds
    }

    @Override
    public Party getParty(Player player) {
        UUID leaderUUID = playerToParty.get(player.getUniqueId());
        if (leaderUUID == null) {
            return null;
        }
        return parties.get(leaderUUID);
    }

    @Override
    public boolean isInParty(Player player) {
        return playerToParty.containsKey(player.getUniqueId());
    }

    @Override
    public boolean isPartyLeader(Player player) {
        Party party = getParty(player);
        return party != null && party.isLeader(player.getUniqueId());
    }

    @Override
    public Set<UUID> getPartyMembers(Player player) {
        Party party = getParty(player);
        return party != null ? party.getMembers() : Collections.emptySet();
    }

    @Override
    public Player getPartyLeader(Player player) {
        Party party = getParty(player);
        return party != null ? party.getLeaderPlayer() : null;
    }

    @Override
    public Party createParty(Player leader) {
        if (isInParty(leader)) {
            return null;
        }

        Party party = new Party(leader.getUniqueId(), maxSize);
        parties.put(leader.getUniqueId(), party);
        playerToParty.put(leader.getUniqueId(), leader.getUniqueId());

        return party;
    }

    @Override
    public boolean disbandParty(Player leader) {
        Party party = parties.get(leader.getUniqueId());
        if (party == null) {
            return false;
        }

        // Remove all members from tracking
        for (UUID memberUUID : party.getMembers()) {
            playerToParty.remove(memberUUID);
        }

        // Remove party
        parties.remove(leader.getUniqueId());
        return true;
    }

    @Override
    public boolean invitePlayer(Player leader, Player target) {
        // Check if leader has party
        Party party = parties.get(leader.getUniqueId());
        if (party == null) {
            return false;
        }

        // Check if party is full
        if (party.isFull()) {
            return false;
        }

        // Check if target is already in a party
        if (isInParty(target)) {
            return false;
        }

        // Check if target already has invite
        if (party.hasInvite(target.getUniqueId())) {
            return false;
        }

        // Send invite
        party.invite(target.getUniqueId());
        return true;
    }

    @Override
    public boolean acceptInvite(Player player, Player leader) {
        Party party = parties.get(leader.getUniqueId());
        if (party == null) {
            return false;
        }

        // Check if player has invite
        if (!party.hasInvite(player.getUniqueId())) {
            return false;
        }

        // Check if player is already in a party
        if (isInParty(player)) {
            return false;
        }

        // Check if party is full
        if (party.isFull()) {
            party.removeInvite(player.getUniqueId());
            return false;
        }

        // Add player to party
        party.addMember(player.getUniqueId());
        playerToParty.put(player.getUniqueId(), leader.getUniqueId());

        return true;
    }

    @Override
    public boolean kickPlayer(Player leader, Player target) {
        Party party = parties.get(leader.getUniqueId());
        if (party == null) {
            return false;
        }

        // Check if target is in the party
        if (!party.isMember(target.getUniqueId())) {
            return false;
        }

        // Cannot kick leader
        if (party.isLeader(target.getUniqueId())) {
            return false;
        }

        // Remove player
        party.removeMember(target.getUniqueId());
        playerToParty.remove(target.getUniqueId());

        return true;
    }

    @Override
    public boolean leaveParty(Player player) {
        UUID leaderUUID = playerToParty.get(player.getUniqueId());
        if (leaderUUID == null) {
            return false;
        }

        Party party = parties.get(leaderUUID);
        if (party == null) {
            return false;
        }

        // If player is leader, disband party
        if (party.isLeader(player.getUniqueId())) {
            Player leaderPlayer = Bukkit.getPlayer(leaderUUID);
            if (leaderPlayer != null) {
                disbandParty(leaderPlayer);
            }
            return true;
        }

        // Remove player from party
        party.removeMember(player.getUniqueId());
        playerToParty.remove(player.getUniqueId());

        return true;
    }

    @Override
    public void notifyPartyMembers(Party party, String message) {
        for (Player member : party.getOnlineMembers()) {
            member.sendMessage(message);
        }
    }

    @Override
    public void teleportParty(Party party, Location location) {
        for (Player member : party.getOnlineMembers()) {
            member.teleport(location);
        }
    }

    /**
     * Start task to clean expired invites
     */
    public void startInviteCleanupTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Party party : parties.values()) {
                Set<UUID> expiredInvites = new HashSet<>();
                for (UUID invitedUUID : party.getPendingInvites()) {
                    party.cleanExpiredInvites(inviteTimeout);

                    // Notify players of expired invites
                    Player invitedPlayer = Bukkit.getPlayer(invitedUUID);
                    if (invitedPlayer != null && !party.hasInvite(invitedUUID)) {
                        String message = plugin.getConfig().getString("party.messages.invite-expired", "§cYour party invite has expired.");
                        message = message.replace("%leader%", party.getLeaderPlayer() != null ? party.getLeaderPlayer().getName() : "Unknown");
                        invitedPlayer.sendMessage(message);
                    }
                }
            }
        }, 20L, 20L); // Run every second
    }

    /**
     * Remove player from any party (for disconnect handling)
     */
    public void removePlayer(Player player) {
        if (isPartyLeader(player)) {
            // Disband party if leader leaves
            Party party = getParty(player);
            if (party != null) {
                notifyPartyMembers(party, plugin.getConfig().getString("party.messages.disbanded", "§cThe party has been disbanded."));
                disbandParty(player);
            }
        } else if (isInParty(player)) {
            // Remove member from party
            Party party = getParty(player);
            if (party != null) {
                String message = plugin.getConfig().getString("party.messages.left", "§e%player% §cleft the party.");
                message = message.replace("%player%", player.getName());
                notifyPartyMembers(party, message);
                leaveParty(player);
            }
        }
    }
}
