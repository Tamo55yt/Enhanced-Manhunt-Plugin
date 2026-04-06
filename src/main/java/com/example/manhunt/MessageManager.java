package com.example.manhunt;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class MessageManager {

    private final Main plugin;
    private final Map<String, Map<String, String>> messages;

    public MessageManager(Main plugin) {
        this.plugin = plugin;
        this.messages = new HashMap<>();
        loadMessages();
    }

    private void loadMessages() {
        // Türkçe (tr_tr)
        Map<String, String> tr = new HashMap<>();
        tr.put("runner_selected", "§6[Manhunt] §e%s §aartık Runner! Diğer tüm oyuncular Avcı oldu.");
        tr.put("you_are_hunter", "§aArtık bir Avcısın! Pusulan Runner'ı gösterecek.");
        tr.put("kit_saved", "§aKit '%s' başarıyla kaydedildi.");
        tr.put("kit_not_found", "§cKit '%s' config'de bulunamadı! Oyuncuya eşya verilmedi.");
        tr.put("kit_applied", "§a%s kiti envanterine uygulandı.");
        tr.put("game_started", "§6[Manhunt] §aOyun başladı! %s");
        tr.put("game_started_kits", "Kitler dağıtıldı.");
        tr.put("game_started_no_kits", "Envanterler korundu.");
        tr.put("tracking_active", "§6[Manhunt] §e10 dakika doldu! Pusulalar artık Runner'ı gösteriyor!");
        tr.put("runner_left", "§cRunner oyundan ayrıldı! Geri dönmesi için %d saniye var...");
        tr.put("runner_returned", "§aRunner geri döndü! Oyun devam ediyor.");
        tr.put("runner_timeout", "§cRunner 60 saniye içinde dönmedi. Avcılar kazandı!");
        tr.put("compass_updated", "§aRunner hedefi güncellendi! Mesafe: %d blok");
        tr.put("tracking_inactive", "§cTakip henüz aktif değil! %d saniye kaldı.");
        tr.put("runner_not_found", "§cRunner bulunamadı!");
        tr.put("portal_link_lost", "§c⚠ Portal Link Lost! Runner bir portalı yok etti!");
        tr.put("elytra_mace_reduce", "§eElytra sayesinde havadan Mace darbesini %%20 azalttın!");
        tr.put("mace_damage_reduced", "§cRunner Elytra kullandığı için Mace hasarı azaldı!");
        tr.put("runner_winner_title", "RUNNER WINNER");
        tr.put("hunter_winner_title", "HUNTER WINNER");
        tr.put("no_permission", "§cYetkiniz yok!");
        tr.put("only_player", "Bu komutu sadece oyuncular kullanabilir.");
        tr.put("usage_setrunner", "§6Kullanım: §e/manhunt setrunner <oyuncuAdı>");
        tr.put("player_not_found", "§cOyuncu bulunamadı veya çevrimdışı.");
        tr.put("usage_setkit", "§6Kullanım: §e/manhunt setkit <runner|hunter>");
        tr.put("invalid_role", "§cGeçersiz rol! Lütfen 'runner' veya 'hunter' kullanın.");
        tr.put("not_enough_players", "§cBu komutu kullanmak için en az 2 oyuncu çevrimiçi olmalı!");
        tr.put("random_runner_selected", "§6[Manhunt] §eRunner rastgele seçildi: §a%s");
        tr.put("runner_identity_hidden", "§7Kimliğiniz gizli, kimse bilmiyor!");
        tr.put("hunter_identity_hidden", "§7Runner kimliği gizli! Dikkatli ol.");
        tr.put("game_already_started", "§cZaten aktif bir oyun var! Önce mevcut oyunu bitirin.");
        // Yeni yardım menüsü anahtarları
        tr.put("help_header", "§6=== Manhunt Komutları ===");
        tr.put("help_setrunner", "§e/manhunt setrunner <oyuncu> §7- Runner'ı belirler.");
        tr.put("help_setkit", "§e/manhunt setkit <runner|hunter> §7- Mevcut envanterini kit olarak kaydeder.");
        tr.put("help_start", "§e/manhunt start §7- Oyunu başlatır (kitleri dağıtır).");
        tr.put("help_random", "§e/manhunt random §7- Rastgele runner seçer ve oyunu başlatır (kitler dağıtılır).");
        tr.put("help_hidden", "§e/manhunt hidden §7- Gizli runner seçer, kit dağıtmaz, 10 dk pusula gizli.");
        tr.put("logout_countdown_actionbar", "§cRunner'ın dönmesine son %d saniye!");
        messages.put("tr_tr", tr);

        // İngilizce (en_us)
        Map<String, String> en = new HashMap<>();
        en.put("runner_selected", "§6[Manhunt] §e%s §ais now the Runner! All other players are Hunters.");
        en.put("you_are_hunter", "§aYou are now a Hunter! Your compass will point to the Runner.");
        en.put("kit_saved", "§aKit '%s' successfully saved.");
        en.put("kit_not_found", "§cKit '%s' not found in config! No items given.");
        en.put("kit_applied", "§a%s kit applied to your inventory.");
        en.put("game_started", "§6[Manhunt] §aGame started! %s");
        en.put("game_started_kits", "Kits have been distributed.");
        en.put("game_started_no_kits", "Inventories preserved.");
        en.put("tracking_active", "§6[Manhunt] §e10 minutes have passed! Compasses now track the Runner!");
        en.put("runner_left", "§cRunner left the game! %d seconds to return...");
        en.put("runner_returned", "§aRunner returned! Game continues.");
        en.put("runner_timeout", "§cRunner did not return within 60 seconds. Hunters win!");
        en.put("compass_updated", "§aRunner target updated! Distance: %d blocks");
        en.put("tracking_inactive", "§cTracking not yet active! %d seconds remaining.");
        en.put("runner_not_found", "§cRunner not found!");
        en.put("portal_link_lost", "§c⚠ Portal Link Lost! The Runner destroyed a portal!");
        en.put("elytra_mace_reduce", "§eElytra reduced Mace damage by 20%%!");
        en.put("mace_damage_reduced", "§cMace damage reduced because Runner is using Elytra!");
        en.put("runner_winner_title", "RUNNER WINNER");
        en.put("hunter_winner_title", "HUNTER WINNER");
        en.put("no_permission", "§cYou don't have permission!");
        en.put("only_player", "Only players can use this command.");
        en.put("usage_setrunner", "§6Usage: §e/manhunt setrunner <playerName>");
        en.put("player_not_found", "§cPlayer not found or offline.");
        en.put("usage_setkit", "§6Usage: §e/manhunt setkit <runner|hunter>");
        en.put("invalid_role", "§cInvalid role! Use 'runner' or 'hunter'.");
        en.put("not_enough_players", "§cAt least 2 players must be online to use this command!");
        en.put("random_runner_selected", "§6[Manhunt] §eRandom runner selected: §a%s");
        en.put("runner_identity_hidden", "§7Your identity is hidden, nobody knows!");
        en.put("hunter_identity_hidden", "§7Runner identity is hidden! Be careful.");
        en.put("game_already_started", "§cA game is already active! End it first.");
        en.put("help_header", "§6=== Manhunt Commands ===");
        en.put("help_setrunner", "§e/manhunt setrunner <player> §7- Set the Runner.");
        en.put("help_setkit", "§e/manhunt setkit <runner|hunter> §7- Save current inventory as a kit.");
        en.put("help_start", "§e/manhunt start §7- Start the game (distribute kits).");
        en.put("help_random", "§e/manhunt random §7- Random runner, start with kits.");
        en.put("help_hidden", "§e/manhunt hidden §7- Hidden runner, no kits, 10min delay.");
        en.put("logout_countdown_actionbar", "§cRunner returning in %d seconds!");
        messages.put("en_us", en);

        // İspanyolca (es_es)
        Map<String, String> es = new HashMap<>();
        es.put("runner_selected", "§6[Manhunt] §e%s §aes ahora el Corredor! El resto son Cazadores.");
        es.put("you_are_hunter", "§aAhora eres un Cazador! Tu brújula apuntará al Corredor.");
        es.put("kit_saved", "§aKit '%s' guardado correctamente.");
        es.put("kit_not_found", "§cKit '%s' no encontrado en configuración. No se dieron objetos.");
        es.put("kit_applied", "§aKit de %s aplicado a tu inventario.");
        es.put("game_started", "§6[Manhunt] §a¡Juego iniciado! %s");
        es.put("game_started_kits", "Kits distribuidos.");
        es.put("game_started_no_kits", "Inventarios preservados.");
        es.put("tracking_active", "§6[Manhunt] §e¡Han pasado 10 minutos! ¡Las brújulas ahora siguen al Corredor!");
        es.put("runner_left", "§c¡El Corredor abandonó el juego! %d segundos para regresar...");
        es.put("runner_returned", "§a¡El Corredor regresó! El juego continúa.");
        es.put("runner_timeout", "§cEl Corredor no regresó en 60 segundos. ¡Los Cazadores ganan!");
        es.put("compass_updated", "§a¡Objetivo del Corredor actualizado! Distancia: %d bloques");
        es.put("tracking_inactive", "§c¡Seguimiento aún no activo! %d segundos restantes.");
        es.put("runner_not_found", "§c¡Corredor no encontrado!");
        es.put("portal_link_lost", "§c⚠ ¡Enlace del portal perdido! El Corredor destruyó un portal.");
        es.put("elytra_mace_reduce", "§e¡El Elytra redujo el daño del Maza en un 20%%!");
        es.put("mace_damage_reduced", "§c¡Daño de Maza reducido porque el Corredor usa Elytra!");
        es.put("runner_winner_title", "EL CORREDOR GANA");
        es.put("hunter_winner_title", "LOS CAZADORES GANAN");
        es.put("no_permission", "§c¡No tienes permiso!");
        es.put("only_player", "Solo los jugadores pueden usar este comando.");
        es.put("usage_setrunner", "§6Uso: §e/manhunt setrunner <nombreJugador>");
        es.put("player_not_found", "§cJugador no encontrado o desconectado.");
        es.put("usage_setkit", "§6Uso: §e/manhunt setkit <runner|hunter>");
        es.put("invalid_role", "§c¡Rol inválido! Usa 'runner' o 'hunter'.");
        es.put("not_enough_players", "§c¡Debe haber al menos 2 jugadores en línea para usar este comando!");
        es.put("random_runner_selected", "§6[Manhunt] §eCorredor aleatorio seleccionado: §a%s");
        es.put("runner_identity_hidden", "§7Tu identidad está oculta, ¡nadie lo sabe!");
        es.put("hunter_identity_hidden", "§7¡La identidad del Corredor está oculta! Ten cuidado.");
        es.put("game_already_started", "§c¡Ya hay un juego activo! Termínalo primero.");
        es.put("help_header", "§6=== Comandos de Manhunt ===");
        es.put("help_setrunner", "§e/manhunt setrunner <jugador> §7- Establece el Corredor.");
        es.put("help_setkit", "§e/manhunt setkit <runner|hunter> §7- Guarda el inventario actual como kit.");
        es.put("help_start", "§e/manhunt start §7- Inicia el juego (distribuye kits).");
        es.put("help_random", "§e/manhunt random §7- Corredor aleatorio, inicia con kits.");
        es.put("help_hidden", "§e/manhunt hidden §7- Corredor oculto, sin kits, retraso 10 min.");
        es.put("logout_countdown_actionbar", "§c¡El Corredor regresa en %d segundos!");
        messages.put("es_es", es);

        // Varsayılan olarak İngilizce kullan
        messages.put("default", en);
    }

    public String getMessage(Player player, String key) {
        String locale = player.getLocale().toLowerCase().replace("_", "");
        String langCode = null;
        if (locale.startsWith("tr")) langCode = "tr_tr";
        else if (locale.startsWith("es")) langCode = "es_es";
        else langCode = "en_us";

        Map<String, String> langMap = messages.getOrDefault(langCode, messages.get("default"));
        return langMap.getOrDefault(key, "Message not found: " + key);
    }

    public String getMessage(String key, Object... args) {
        Map<String, String> defaultMap = messages.get("default");
        String msg = defaultMap.getOrDefault(key, "Message not found: " + key);
        return String.format(msg, args);
    }

    public void sendMessage(Player player, String key, Object... args) {
        String msg = getMessage(player, key);
        if (args.length > 0) {
            msg = String.format(msg, args);
        }
        player.sendMessage(msg);
    }

    public void broadcast(String key, Object... args) {
        for (Player online : plugin.getServer().getOnlinePlayers()) {
            sendMessage(online, key, args);
        }
    }

    public void sendTitle(Player player, String key, int fadeIn, int stay, int fadeOut) {
        String title = getMessage(player, key);
        player.sendTitle(title, "", fadeIn, stay, fadeOut);
    }

    public void sendActionBar(Player player, String key, Object... args) {
        String msg = getMessage(player, key);
        if (args.length > 0) {
            msg = String.format(msg, args);
        }
        player.sendActionBar(msg);
    }
}