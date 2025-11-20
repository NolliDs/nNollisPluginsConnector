package pl.nollis.connector.api;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import pl.nollis.connector.models.Party;

import java.util.Set;
import java.util.UUID;

public interface PartyAPI {
    /**
     * Get the party of a player
     * @param player Player to check
     * @return Party or null if not in party
     */
    Party getParty(Player player);

    /**
     * Check if player is in a party
     * @param player Player to check
     * @return true if in party
     */
    boolean isInParty(Player player);

    /**
     * Check if player is party leader
     * @param player Player to check
     * @return true if leader
     */
    boolean isPartyLeader(Player player);

    /**
     * Get all party members UUIDs
     * @param player Player whose party to get
     * @return Set of member UUIDs or empty set
     */
    Set<UUID> getPartyMembers(Player player);

    /**
     * Get party leader
     * @param player Player whose party leader to get
     * @return Leader player or null
     */
    Player getPartyLeader(Player player);

    /**
     * Create a new party
     * @param leader Party leader
     * @return Created party or null if already in party
     */
    Party createParty(Player leader);

    /**
     * Disband a party
     * @param leader Party leader
     * @return true if disbanded
     */
    boolean disbandParty(Player leader);

    /**
     * Invite a player to party
     * @param leader Party leader
     * @param target Player to invite
     * @return true if invited successfully
     */
    boolean invitePlayer(Player leader, Player target);

    /**
     * Accept party invite
     * @param player Player accepting
     * @param leader Party leader
     * @return true if accepted
     */
    boolean acceptInvite(Player player, Player leader);

    /**
     * Kick player from party
     * @param leader Party leader
     * @param target Player to kick
     * @return true if kicked
     */
    boolean kickPlayer(Player leader, Player target);

    /**
     * Leave party
     * @param player Player leaving
     * @return true if left
     */
    boolean leaveParty(Player player);

    /**
     * Send message to all party members
     * @param party Party to notify
     * @param message Message to send
     */
    void notifyPartyMembers(Party party, String message);

    /**
     * Teleport entire party
     * @param party Party to teleport
     * @param location Target location
     */
    void teleportParty(Party party, Location location);
}
