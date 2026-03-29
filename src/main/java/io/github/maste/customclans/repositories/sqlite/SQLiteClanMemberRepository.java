package io.github.maste.customclans.repositories.sqlite;

import io.github.maste.customclans.models.ClanMember;
import io.github.maste.customclans.models.PlayerClanSnapshot;
import io.github.maste.customclans.repositories.ClanMemberRepository;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class SQLiteClanMemberRepository implements ClanMemberRepository {

    private final SQLiteDatabase database;

    public SQLiteClanMemberRepository(SQLiteDatabase database) {
        this.database = database;
    }

    @Override
    public CompletableFuture<Optional<ClanMember>> findByPlayerUuid(UUID playerUuid) {
        return database.supplyAsync(() -> {
            try (var connection = database.openConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "SELECT * FROM clan_members WHERE player_uuid = ?"
                 )) {
                statement.setString(1, playerUuid.toString());
                try (ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next() ? Optional.of(SQLiteMapper.mapClanMember(resultSet)) : Optional.empty();
                }
            }
        });
    }

    @Override
    public CompletableFuture<Optional<PlayerClanSnapshot>> findSnapshotByPlayerUuid(UUID playerUuid) {
        return database.supplyAsync(() -> {
            try (var connection = database.openConnection();
                 PreparedStatement statement = connection.prepareStatement("""
                         SELECT c.id, c.name, c.tag, c.tag_color, c.president_uuid, m.role
                         FROM clan_members m
                         JOIN clans c ON c.id = m.clan_id
                         WHERE m.player_uuid = ?
                         """)) {
                statement.setString(1, playerUuid.toString());
                try (ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next() ? Optional.of(SQLiteMapper.mapSnapshot(resultSet)) : Optional.empty();
                }
            }
        });
    }

    @Override
    public CompletableFuture<List<ClanMember>> findByClanId(long clanId) {
        return database.supplyAsync(() -> {
            try (var connection = database.openConnection();
                 PreparedStatement statement = connection.prepareStatement("""
                         SELECT * FROM clan_members
                         WHERE clan_id = ?
                         ORDER BY CASE WHEN role = 'PRESIDENT' THEN 0 ELSE 1 END, lower(last_known_name)
                         """)) {
                statement.setLong(1, clanId);
                List<ClanMember> members = new ArrayList<>();
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        members.add(SQLiteMapper.mapClanMember(resultSet));
                    }
                }
                return members;
            }
        });
    }

    @Override
    public CompletableFuture<Integer> countByClanId(long clanId) {
        return database.supplyAsync(() -> {
            try (var connection = database.openConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "SELECT COUNT(*) AS total FROM clan_members WHERE clan_id = ?"
                 )) {
                statement.setLong(1, clanId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next() ? resultSet.getInt("total") : 0;
                }
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> removeMember(long clanId, UUID playerUuid) {
        return database.supplyAsync(() -> {
            try (var connection = database.openConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "DELETE FROM clan_members WHERE clan_id = ? AND player_uuid = ?"
                 )) {
                statement.setLong(1, clanId);
                statement.setString(2, playerUuid.toString());
                return statement.executeUpdate() > 0;
            }
        });
    }

    @Override
    public CompletableFuture<Void> updateLastKnownName(UUID playerUuid, String lastKnownName) {
        return database.runAsync(() -> {
            try (var connection = database.openConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "UPDATE clan_members SET last_known_name = ? WHERE player_uuid = ?"
                 )) {
                statement.setString(1, lastKnownName);
                statement.setString(2, playerUuid.toString());
                statement.executeUpdate();
            }
        });
    }
}
