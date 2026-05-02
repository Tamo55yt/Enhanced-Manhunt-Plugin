package com.example.manhunt.message;

import com.example.manhunt.api.MPlatform;
import com.example.manhunt.api.MPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class MessageManager {

    private final MPlatform platform;
    private final Map<String, Map<String, String>> messages;

    public MessageManager(@NotNull MPlatform platform) {
        this.platform = platform;
        this.messages = new HashMap<>();
        loadMessages();
        translateAll();
    }

    private String translate(String text) {
        if (text == null) return null;
        char[] b = text.toCharArray();
        for (int i = 0; i < b.length - 1; i++) {
            if (b[i] == '&' && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(b[i+1]) > -1) {
                b[i] = '§';
                b[i+1] = Character.toLowerCase(b[i+1]);
            }
        }
        return new String(b);
    }

    private void translateAll() {
        for (Map<String, String> langMap : messages.values()) {
            for (Map.Entry<String, String> entry : langMap.entrySet()) {
                entry.setValue(translate(entry.getValue()));
            }
        }
    }

    private void loadMessages() {
        // --- TÜRKÇE (tr_tr) ---
        Map<String, String> tr = new HashMap<>();
        tr.put("runner_selected", "&6[Manhunt] &e%s &aartık Runner!");
        tr.put("game_started", "&6[Manhunt] &aOyun başladı! %s");
        tr.put("game_started_kits", "Kitler dağıtıldı.");
        tr.put("game_started_no_kits", "Envanterler korundu.");
        tr.put("tracking_active", "&6[Manhunt] &ePusulalar artık Runner'ı gösteriyor!");
        tr.put("runner_timeout", "&cRunner dönmedi. Avcılar kazandı!");
        tr.put("logout_countdown_actionbar", "&cRunner'ın dönmesine son %d saniye!");
        tr.put("no_permission", "&cYetkiniz yok!");
        tr.put("only_player", "Sadece oyuncular kullanabilir.");
        tr.put("usage_track", "&6Kullanım: &e/manhunt track <avcıAdı>");
        tr.put("hunter_not_found", "&cAvcı bulunamadı.");
        tr.put("tracking_hunter", "&aTakip ediliyor: &e%s");
        tr.put("only_runner_track", "&cSadece Runner kullanabilir!");
        tr.put("runner_compass_usage", "&bTakip için: &f/manhunt track <isim>");
        tr.put("hunter_different_world", "&cAvcı farklı bir dünyada!");
        tr.put("compass_pointing_runner", "&aPusula Runner'ı gösteriyor.");
        tr.put("portal_link_lost", "&c⚠ Portal bağlantısı koptu!");
        tr.put("elytra_mace_reduce", "&eElytra ile Mace hasarını azalttın!");
        tr.put("runner_winner_title", "&b&lRUNNER KAZANDI");
        tr.put("hunter_winner_title", "&c&lAVCILAR KAZANDI");
        tr.put("hardcore_enabled", "&6[Manhunt] &cHardcore Mod AKTİF!");
        tr.put("hardcore_disabled", "&6[Manhunt] &aHardcore Mod Devre Dışı!");
        tr.put("hunter_became_spectator", "&c[Manhunt] &e%s &7öldü ve izleyici oldu!");
        tr.put("usage_setrunner", "&6Kullanım: &e/manhunt setrunner <ad>");
        tr.put("kit_saved", "&aKit '%s' kaydedildi.");
        tr.put("kit_not_found", "&cKit '%s' bulunamadı.");
        tr.put("kit_applied", "&aKit %s uygulandı.");
        tr.put("health_set", "&6[Manhunt] &e%s &arolü için kalp sayısı &e%d &aolarak ayarlandı.");
        tr.put("respawn_set", "&6[Manhunt] &eAvcı doğma mesafesi: &a%d-%d &eblok olarak ayarlandı.");
        tr.put("usage_setrespawn", "&6Kullanım: &e/manhunt setrespawn <min> <max>");
        tr.put("cleanup_toggled", "&6[Manhunt] &eEşya temizleme: %s");
        tr.put("grace_period_start", "&6[Manhunt] &eBaşlangıç koruması başladı! Süre: &a%d saniye");
        tr.put("grace_period_end", "&6[Manhunt] &cKoruma bitti! Avcılar artık serbest!");
        tr.put("grace_period_countdown", "&6[Manhunt] &eKorumadan çıkışa son &c%d &esaniye!");
        tr.put("usage_sethealth", "&6Kullanım: &e/manhunt sethealth <runner|hunter> <kalpSayısı>");
        tr.put("runner_returned", "&aRunner geri döndü!");
        tr.put("game_already_running", "&cOyun zaten devam ediyor!");
        tr.put("help_header", "&7&m-----&r &6&lMANHUNT YARDIM &7&m-----");
        tr.put("help_setrunner", "&e/manhunt setrunner <ad> &7- Runner seçer.");
        tr.put("help_hardcore", "&e/manhunt hardcore <true|false> &7- Tek can modunu ayarlar.");
        tr.put("help_setkit", "&e/manhunt setkit <role> &7- Mevcut envanteri kit yapar.");
        tr.put("help_start", "&e/manhunt start &7- Oyunu başlatır.");
        tr.put("help_stop", "&e/manhunt stop &7- Oyunu bitirir.");
        tr.put("help_random", "&e/manhunt random &7- Rastgele biriyle başlatır.");
        tr.put("help_hidden", "&e/manhunt hidden &7- Runner gizli başlar.");
        tr.put("help_track", "&e/manhunt track <ad> &7- (Sadece Runner) Avcı takip eder.");
        tr.put("countdown_3", "&6[Manhunt] &cOyun 3 saniye içinde başlıyor!");
        tr.put("countdown_2", "&6[Manhunt] &eOyun 2 saniye içinde başlıyor!");
        tr.put("countdown_1", "&6[Manhunt] &bOyun 1 saniye içinde başlıyor!");
        tr.put("countdown_start", "&6[Manhunt] &a&lOYUN BAŞLADI! İYİ ŞANSLAR!");
        tr.put("countdown_start_title", "BAŞLA!");
        tr.put("ffa_tracking_target", "&6[FFA] &aTakip ediliyor: &e%s");
        messages.put("tr_tr", tr);

        // --- ENGLISH (en_us) ---
        Map<String, String> en = new HashMap<>();
        en.put("runner_selected", "&6[Manhunt] &e%s &ais now the Runner!");
        en.put("game_started", "&6[Manhunt] &aGame started! %s");
        en.put("game_started_kits", "Kits applied.");
        en.put("game_started_no_kits", "Inventories kept.");
        en.put("tracking_active", "&6[Manhunt] &eCompasses now point to the Runner!");
        en.put("runner_timeout", "&cRunner didn't return. Hunters win!");
        en.put("logout_countdown_actionbar", "&cRunner must return in %d seconds!");
        en.put("no_permission", "&cNo permission!");
        en.put("only_player", "Only players can use this.");
        en.put("usage_track", "&6Usage: &e/manhunt track <hunterName>");
        en.put("hunter_not_found", "&cHunter not found.");
        en.put("tracking_hunter", "&aTracking: &e%s");
        en.put("only_runner_track", "&cOnly Runner can use this!");
        en.put("runner_compass_usage", "&bTo track: &f/manhunt track <name>");
        en.put("hunter_different_world", "&cHunter is in a different world!");
        en.put("compass_pointing_runner", "&aCompass pointing to Runner.");
        en.put("portal_link_lost", "&c⚠ Portal link lost!");
        en.put("elytra_mace_reduce", "&eYou reduced Mace damage with Elytra!");
        en.put("runner_winner_title", "&b&lRUNNER WINS");
        en.put("hunter_winner_title", "&c&lHUNTERS WIN");
        en.put("hardcore_enabled", "&6[Manhunt] &cHardcore Mode ACTIVE!");
        en.put("hardcore_disabled", "&6[Manhunt] &aHardcore Mode Disabled!");
        en.put("hunter_became_spectator", "&c[Manhunt] &e%s &7died and became a spectator!");
        en.put("usage_setrunner", "&6Usage: &e/manhunt setrunner <name>");
        en.put("kit_saved", "&aKit '%s' saved.");
        en.put("kit_not_found", "&cKit '%s' not found.");
        en.put("kit_applied", "&aKit %s applied.");
        en.put("runner_returned", "&aRunner returned!");
        en.put("game_already_running", "&cGame is already running!");
        en.put("help_header", "&7&m-----&r &6&lMANHUNT HELP &7&m-----");
        en.put("help_setrunner", "&e/manhunt setrunner <name> &7- Sets the Runner.");
        en.put("help_hardcore", "&e/manhunt hardcore <true|false> &7- Sets hardcore mode.");
        en.put("help_setkit", "&e/manhunt setkit <role> &7- Saves inventory as kit.");
        en.put("help_start", "&e/manhunt start &7- Starts the game.");
        en.put("help_stop", "&e/manhunt stop &7- Stops the game.");
        en.put("help_random", "&e/manhunt random &7- Random runner start.");
        en.put("help_hidden", "&e/manhunt hidden &7- Hidden runner start.");
        en.put("help_track", "&e/manhunt track <name> &7- (Runner only) Track hunter.");
        en.put("scoreboard_title", "&6&lMANHUNT");
        en.put("scoreboard_time", "&fTime: &e%s");
        en.put("scoreboard_runner", "&fRunner: &b%s");
        en.put("scoreboard_health", "&fHealth: &c%s ❤");
        en.put("scoreboard_distance", "&fDistance: &a%dm");
        en.put("scoreboard_world", "&fWorld: &7%s");
        en.put("scoreboard_role", "&fRole: %s");
        en.put("role_runner", "&bRUNNER");
        en.put("role_hunter", "&eHUNTER");
        en.put("role_spectator", "&7SPECTATOR");
        en.put("compass_mode_change", "&6[Manhunt] &eCompass Mode: &b%s");
        en.put("compass_mode_runner", "Track Runner");
        en.put("compass_mode_teammate", "Track Teammate");
        en.put("compass_mode_portal", "Last Portal");
        en.put("spectator_teleport_runner", "&aTeleported to Runner!");
        en.put("advancement_hunter", "&6[HUNTER] &e%s &7made an advancement: &a%s");
        en.put("gui_scenarios", "&eScenarios");
        en.put("gui_classes", "&bClasses");
        en.put("scenario_normal", "&fNormal Manhunt");
        en.put("scenario_lava", "&cLava Rises");
        en.put("scenario_random", "&6Random Drops");
        en.put("scenario_swap", "&dSwap Places");
        en.put("class_scout", "&aScout");
        en.put("class_tank", "&cTank");
        en.put("class_trapper", "&6Trapper");
        en.put("scenario_selected", "&6[Manhunt] &eScenario selected: &b%s");
        en.put("class_selected", "&6[Manhunt] &eYour class: &b%s");
        en.put("countdown_3", "&6[Manhunt] &cGame starts in 3 seconds!");
        en.put("countdown_2", "&6[Manhunt] &eGame starts in 2 seconds!");
        en.put("countdown_1", "&6[Manhunt] &bGame starts in 1 second!");
        en.put("countdown_start", "&6[Manhunt] &a&lGAME STARTED! GOOD LUCK!");
        en.put("countdown_start_title", "START!");
        en.put("ffa_tracking_target", "&6[FFA] &aTracking: &e%s");
        messages.put("en_us", en);
        messages.put("default", en);

        // --- Omitted other languages for brevity in core, can be added later ---
        messages.put("tr_tr", tr);
    }

    @NotNull
    public String getRawMessage(@Nullable MPlayer player, @NotNull String key) {
        String langCode = "en_us";
        // Locale detection is platform specific, but MPlayer can be extended or we use default
        Map<String, String> map = messages.get(langCode);
        if (map == null) map = messages.get("default");
        return map.getOrDefault(key, "Message not found: " + key);
    }

    public void sendMessage(@NotNull MPlayer player, @NotNull String key, Object... args) {
        String msg = getRawMessage(player, key);
        if (args.length > 0) {
            try { msg = String.format(msg, args); } catch (Exception ignored) {}
        }
        player.sendMessage(msg);
    }

    public void broadcast(@NotNull String key, Object... args) {
        for (MPlayer p : platform.getOnlinePlayers()) sendMessage(p, key, args);
    }

    public void broadcastRaw(@NotNull String rawMessage) {
        String msg = translate(rawMessage);
        for (MPlayer p : platform.getOnlinePlayers()) p.sendMessage(msg);
    }

    public void sendTitle(@NotNull MPlayer player, @NotNull String key, int fadeIn, int stay, int fadeOut) {
        String text = getRawMessage(player, key);
        player.sendTitle(text, "", fadeIn, stay, fadeOut);
    }

    public void sendActionBar(@NotNull MPlayer player, @NotNull String key, Object... args) {
        String msg = getRawMessage(player, key);
        if (args.length > 0) {
            try { msg = String.format(msg, args); } catch (Exception ignored) {}
        }
        player.sendActionBar(msg);
    }
}
