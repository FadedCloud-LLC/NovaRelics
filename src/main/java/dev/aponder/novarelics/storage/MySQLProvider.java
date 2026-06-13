package dev.aponder.novarelics.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.storage.model.PlayerData;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.*;
import java.util.Arrays;
import java.util.UUID;
import java.util.logging.Level;

public class MySQLProvider implements StorageProvider {

    private final NovaRelics plugin;
    private HikariDataSource dataSource;

    public MySQLProvider(NovaRelics plugin) {
        this.plugin = plugin;
    }

    @Override
    public void connect() throws Exception {
        FileConfiguration cfg = plugin.getConfigManager().getMainConfig();
        String host = cfg.getString("storage.mysql.host", "localhost");
        int port = cfg.getInt("storage.mysql.port", 3306);
        String database = cfg.getString("storage.mysql.database", "novarelics");
        String username = cfg.getString("storage.mysql.username", "root");
        String password = cfg.getString("storage.mysql.password", "");
        int poolSize = cfg.getInt("storage.mysql.pool-size", 10);

        HikariConfig hcfg = new HikariConfig();
        hcfg.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database +
                "?useSSL=false&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=utf8");
        hcfg.setUsername(username);
        hcfg.setPassword(password);
        hcfg.setMaximumPoolSize(poolSize);
        hcfg.setConnectionTimeout(cfg.getLong("storage.mysql.connection-timeout", 30000));
        hcfg.setIdleTimeout(cfg.getLong("storage.mysql.idle-timeout", 600000));
        hcfg.setMaxLifetime(cfg.getLong("storage.mysql.max-lifetime", 1800000));
        hcfg.setPoolName("NovaRelics-MySQL");
        hcfg.addDataSourceProperty("cachePrepStmts", "true");
        hcfg.addDataSourceProperty("prepStmtCacheSize", "250");
        hcfg.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hcfg.addDataSourceProperty("useServerPrepStmts", "true");

        dataSource = new HikariDataSource(hcfg);
        createTables();
    }

    @Override
    public void disconnect() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    @Override
    public void createTables() {
        try (Connection c = dataSource.getConnection(); Statement s = c.createStatement()) {
            s.executeUpdate(
                "CREATE TABLE IF NOT EXISTS nr_player_data (" +
                "  uuid VARCHAR(36) PRIMARY KEY," +
                "  discovered_relics TEXT DEFAULT ''," +
                "  last_seen BIGINT DEFAULT 0" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;"
            );
            s.executeUpdate(
                "CREATE TABLE IF NOT EXISTS nr_cooldowns (" +
                "  uuid VARCHAR(36) NOT NULL," +
                "  cooldown_key VARCHAR(128) NOT NULL," +
                "  expires_at BIGINT NOT NULL," +
                "  PRIMARY KEY (uuid, cooldown_key)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;"
            );
            s.executeUpdate(
                "CREATE TABLE IF NOT EXISTS nr_charges (" +
                "  uuid VARCHAR(36) NOT NULL," +
                "  item_uuid VARCHAR(36) NOT NULL," +
                "  relic_id VARCHAR(64) NOT NULL," +
                "  charges INT DEFAULT 0," +
                "  PRIMARY KEY (uuid, item_uuid)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;"
            );
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to create MySQL tables", e);
        }
    }

    @Override
    public PlayerData loadPlayer(UUID uuid) {
        String sql = "SELECT discovered_relics, last_seen FROM nr_player_data WHERE uuid = ?";
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            PlayerData data = new PlayerData(uuid);
            if (rs.next()) {
                String discovered = rs.getString("discovered_relics");
                if (discovered != null && !discovered.isEmpty()) {
                    Arrays.stream(discovered.split(","))
                            .filter(s -> !s.isBlank())
                            .forEach(data::addDiscovered);
                }
                data.setLastSeen(rs.getLong("last_seen"));
            }
            return data;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load player data for " + uuid, e);
            return new PlayerData(uuid);
        }
    }

    @Override
    public void savePlayer(PlayerData data) {
        String sql =
            "INSERT INTO nr_player_data (uuid, discovered_relics, last_seen) VALUES (?,?,?) " +
            "ON DUPLICATE KEY UPDATE discovered_relics=VALUES(discovered_relics), last_seen=VALUES(last_seen)";
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, data.getUuid().toString());
            ps.setString(2, String.join(",", data.getDiscoveredRelics()));
            ps.setLong(3, data.getLastSeen());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save player data for " + data.getUuid(), e);
        }
    }

    @Override
    public long getCooldownExpiry(UUID uuid, String cooldownKey) {
        String sql = "SELECT expires_at FROM nr_cooldowns WHERE uuid = ? AND cooldown_key = ?";
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, cooldownKey);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getLong("expires_at");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to get cooldown", e);
        }
        return 0L;
    }

    @Override
    public void setCooldown(UUID uuid, String cooldownKey, long expiresAt) {
        String sql =
            "INSERT INTO nr_cooldowns (uuid, cooldown_key, expires_at) VALUES (?,?,?) " +
            "ON DUPLICATE KEY UPDATE expires_at=VALUES(expires_at)";
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, cooldownKey);
            ps.setLong(3, expiresAt);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to set cooldown", e);
        }
    }

    @Override
    public void removeCooldown(UUID uuid, String cooldownKey) {
        String sql = "DELETE FROM nr_cooldowns WHERE uuid = ? AND cooldown_key = ?";
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, cooldownKey);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to remove cooldown", e);
        }
    }

    @Override
    public int getCharges(UUID uuid, String itemUuid, String relicId) {
        String sql = "SELECT charges FROM nr_charges WHERE uuid = ? AND item_uuid = ?";
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, itemUuid);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("charges");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to get charges", e);
        }
        return -1;
    }

    @Override
    public void setCharges(UUID uuid, String itemUuid, String relicId, int charges) {
        String sql =
            "INSERT INTO nr_charges (uuid, item_uuid, relic_id, charges) VALUES (?,?,?,?) " +
            "ON DUPLICATE KEY UPDATE charges=VALUES(charges)";
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, itemUuid);
            ps.setString(3, relicId);
            ps.setInt(4, charges);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to set charges", e);
        }
    }
}
