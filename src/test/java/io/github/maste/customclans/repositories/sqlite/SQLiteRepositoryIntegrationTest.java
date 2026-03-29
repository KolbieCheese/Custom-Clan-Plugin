package io.github.maste.customclans.repositories.sqlite;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.maste.customclans.models.ClanCreateResult;
import io.github.maste.customclans.models.ClanInvite;
import io.github.maste.customclans.models.InviteAcceptResult;
import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SQLiteRepositoryIntegrationTest {

    @TempDir
    Path tempDir;

    private SQLiteDatabase database;
    private SQLiteClanRepository clanRepository;
    private SQLiteClanMemberRepository clanMemberRepository;
    private SQLiteClanInviteRepository clanInviteRepository;

    @BeforeEach
    void setUp() throws Exception {
        database = new SQLiteDatabase(tempDir.resolve("clans.db"), java.util.logging.Logger.getLogger("test"));
        database.initialize();
        clanRepository = new SQLiteClanRepository(database);
        clanMemberRepository = new SQLiteClanMemberRepository(database);
        clanInviteRepository = new SQLiteClanInviteRepository(database);
    }

    @AfterEach
    void tearDown() {
        database.close();
    }

    @Test
    void createClanRejectsCaseInsensitiveDuplicateNames() {
        UUID president = UUID.randomUUID();
        ClanCreateResult created = clanRepository.createClan(
                president,
                "Alice",
                "Crimson Knights",
                "CK",
                "gold",
                Instant.now()
        ).join();

        assertEquals(ClanCreateResult.Status.CREATED, created.status());
        assertTrue(clanMemberRepository.findSnapshotByPlayerUuid(president).join().isPresent());

        ClanCreateResult duplicate = clanRepository.createClan(
                UUID.randomUUID(),
                "Bob",
                "crimson knights",
                "CK2",
                "red",
                Instant.now()
        ).join();

        assertEquals(ClanCreateResult.Status.NAME_TAKEN, duplicate.status());
    }

    @Test
    void acceptInviteAddsMemberAndDeletesInvite() {
        UUID president = UUID.randomUUID();
        UUID invited = UUID.randomUUID();

        ClanCreateResult created = clanRepository.createClan(
                president,
                "Alice",
                "Azure Guard",
                "AG",
                "blue",
                Instant.now()
        ).join();

        ClanInvite invite = new ClanInvite(
                created.clan().id(),
                invited,
                president,
                Instant.now().plusSeconds(300)
        );
        clanInviteRepository.createInvite(invite, Instant.now()).join();

        InviteAcceptResult accepted = clanInviteRepository.acceptInvite(
                invited,
                "Bob",
                20,
                Instant.now()
        ).join();

        assertEquals(InviteAcceptResult.Status.ACCEPTED, accepted.status());
        assertTrue(clanMemberRepository.findSnapshotByPlayerUuid(invited).join().isPresent());
        assertFalse(clanInviteRepository.findByInvitedPlayerUuid(invited).join().isPresent());
    }
}
