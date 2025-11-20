package pl.nollis.connector.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.nollis.connector.NollisPluginsConnector;
import pl.nollis.connector.api.PartyAPI;
import pl.nollis.connector.models.Party;

import java.util.UUID;

public class PartyCommand implements CommandExecutor {

    private final NollisPluginsConnector plugin;
    private final PartyAPI partyAPI;

    public PartyCommand(NollisPluginsConnector plugin) {
        this.plugin = plugin;
        this.partyAPI = plugin.getPartyAPI();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create":
                handleCreate(player);
                break;
            case "invite":
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /party invite <player>");
                    return true;
                }
                handleInvite(player, args[1]);
                break;
            case "accept":
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /party accept <leader>");
                    return true;
                }
                handleAccept(player, args[1]);
                break;
            case "kick":
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /party kick <player>");
                    return true;
                }
                handleKick(player, args[1]);
                break;
            case "leave":
                handleLeave(player);
                break;
            case "list":
                handleList(player);
                break;
            case "disband":
                handleDisband(player);
                break;
            default:
                sendHelp(player);
        }

        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage("§e§l=== Party Commands ===");
        player.sendMessage("§e/party create §7- Create a party");
        player.sendMessage("§e/party invite <player> §7- Invite player");
        player.sendMessage("§e/party accept <leader> §7- Accept invite");
        player.sendMessage("§e/party kick <player> §7- Kick player");
        player.sendMessage("§e/party leave §7- Leave party");
        player.sendMessage("§e/party list §7- List members");
        player.sendMessage("§e/party disband §7- Disband party");
    }

    private void handleCreate(Player player) {
        if (partyAPI.isInParty(player)) {
            player.sendMessage(getMessage("already-in-party"));
            return;
        }

        Party party = partyAPI.createParty(player);
        if (party != null) {
            player.sendMessage(getMessage("created"));
        }
    }

    private void handleInvite(Player leader, String targetName) {
        if (!partyAPI.isPartyLeader(leader)) {
            leader.sendMessage(getMessage("not-leader"));
            return;
        }

        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            leader.sendMessage("§cPlayer not found!");
            return;
        }

        if (target.equals(leader)) {
            leader.sendMessage(getMessage("cannot-invite-self"));
            return;
        }

        Party party = partyAPI.getParty(leader);
        if (party.isFull()) {
            leader.sendMessage(getMessage("party-full"));
            return;
        }

        if (partyAPI.isInParty(target)) {
            String msg = getMessage("target-in-party");
            msg = msg.replace("%player%", target.getName());
            leader.sendMessage(msg);
            return;
        }

        if (party.hasInvite(target.getUniqueId())) {
            String msg = getMessage("already-invited");
            msg = msg.replace("%player%", target.getName());
            leader.sendMessage(msg);
            return;
        }

        if (partyAPI.invitePlayer(leader, target)) {
            String sentMsg = getMessage("invite-sent");
            sentMsg = sentMsg.replace("%player%", target.getName());
            leader.sendMessage(sentMsg);

            String receivedMsg = getMessage("invite-received");
            receivedMsg = receivedMsg.replace("%leader%", leader.getName());
            target.sendMessage(receivedMsg);
        }
    }

    private void handleAccept(Player player, String leaderName) {
        if (partyAPI.isInParty(player)) {
            player.sendMessage(getMessage("already-in-party"));
            return;
        }

        Player leader = Bukkit.getPlayer(leaderName);
        if (leader == null) {
            player.sendMessage("§cPlayer not found!");
            return;
        }

        Party party = partyAPI.getParty(leader);
        if (party == null) {
            player.sendMessage("§cThat player doesn't have a party!");
            return;
        }

        if (!party.hasInvite(player.getUniqueId())) {
            String msg = getMessage("no-invite");
            msg = msg.replace("%leader%", leader.getName());
            player.sendMessage(msg);
            return;
        }

        if (party.isFull()) {
            player.sendMessage(getMessage("party-full"));
            party.removeInvite(player.getUniqueId());
            return;
        }

        if (partyAPI.acceptInvite(player, leader)) {
            String joinedMsg = getMessage("joined");
            joinedMsg = joinedMsg.replace("%player%", player.getName());
            partyAPI.notifyPartyMembers(party, joinedMsg);

            String youJoinedMsg = getMessage("you-joined");
            youJoinedMsg = youJoinedMsg.replace("%leader%", leader.getName());
            player.sendMessage(youJoinedMsg);
        }
    }

    private void handleKick(Player leader, String targetName) {
        if (!partyAPI.isPartyLeader(leader)) {
            leader.sendMessage(getMessage("not-leader"));
            return;
        }

        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            leader.sendMessage("§cPlayer not found!");
            return;
        }

        if (target.equals(leader)) {
            leader.sendMessage(getMessage("cannot-kick-self"));
            return;
        }

        Party party = partyAPI.getParty(leader);
        if (!party.isMember(target.getUniqueId())) {
            String msg = getMessage("not-in-your-party");
            msg = msg.replace("%player%", target.getName());
            leader.sendMessage(msg);
            return;
        }

        if (partyAPI.kickPlayer(leader, target)) {
            String kickedMsg = getMessage("kicked");
            kickedMsg = kickedMsg.replace("%player%", target.getName());
            partyAPI.notifyPartyMembers(party, kickedMsg);

            String youKickedMsg = getMessage("you-kicked");
            target.sendMessage(youKickedMsg);
        }
    }

    private void handleLeave(Player player) {
        if (!partyAPI.isInParty(player)) {
            player.sendMessage(getMessage("not-in-party"));
            return;
        }

        Party party = partyAPI.getParty(player);

        if (partyAPI.leaveParty(player)) {
            if (party != null && !partyAPI.isPartyLeader(player)) {
                String leftMsg = getMessage("left");
                leftMsg = leftMsg.replace("%player%", player.getName());
                partyAPI.notifyPartyMembers(party, leftMsg);
            }

            player.sendMessage(getMessage("you-left"));
        }
    }

    private void handleList(Player player) {
        if (!partyAPI.isInParty(player)) {
            player.sendMessage(getMessage("not-in-party"));
            return;
        }

        Party party = partyAPI.getParty(player);
        if (party == null) {
            return;
        }

        player.sendMessage(getMessage("list-header"));

        for (UUID memberUUID : party.getMembers()) {
            Player member = Bukkit.getPlayer(memberUUID);
            if (member != null) {
                if (party.isLeader(memberUUID)) {
                    String msg = getMessage("list-leader");
                    msg = msg.replace("%player%", member.getName());
                    player.sendMessage(msg);
                } else {
                    String msg = getMessage("list-member");
                    msg = msg.replace("%player%", member.getName());
                    player.sendMessage(msg);
                }
            }
        }
    }

    private void handleDisband(Player player) {
        if (!partyAPI.isPartyLeader(player)) {
            player.sendMessage(getMessage("not-leader"));
            return;
        }

        Party party = partyAPI.getParty(player);
        if (party != null) {
            partyAPI.notifyPartyMembers(party, getMessage("disbanded"));
            partyAPI.disbandParty(player);
        }
    }

    private String getMessage(String key) {
        return plugin.getConfig().getString("party.messages." + key, "§cMessage not found: " + key);
    }
}
