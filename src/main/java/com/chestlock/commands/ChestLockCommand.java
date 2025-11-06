package com.chestlock.commands;

import com.chestlock.ChestLock;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Main command handler for /chestlock
 */
public class ChestLockCommand implements CommandExecutor {

    private final ChestLock plugin;

    public ChestLockCommand(ChestLock plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "help":
                sendHelp(sender);
                return true;

            case "reload":
                if (!sender.hasPermission("chestlock.admin")) {
                    sender.sendMessage(plugin.getMessage("no-permission"));
                    return true;
                }
                plugin.reloadConfiguration();
                sender.sendMessage(plugin.getMessage("prefix") + " §a설정이 리로드되었습니다!");
                return true;

            case "version":
            case "about":
                sender.sendMessage("§8[§6ChestLock§8] §fv1.0.0");
                sender.sendMessage("§7종속성 없는 블록 보호 플러그인");
                return true;

            default:
                sendHelp(sender);
                return true;
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§8§m                                    ");
        sender.sendMessage("§6§lChestLock §7- 도움말");
        sender.sendMessage("");
        sender.sendMessage("§e/chestlock help §7- 도움말 표시");
        sender.sendMessage("§e/chestlock reload §7- 설정 리로드 §c(관리자)");
        sender.sendMessage("§e/chestlock about §7- 플러그인 정보");
        sender.sendMessage("");
        sender.sendMessage("§7블록을 잠그려면:");
        sender.sendMessage("§e스니킹(Shift) + 우클릭 §7- 잠금 메뉴 열기");
        sender.sendMessage("§8§m                                    ");
    }
}
