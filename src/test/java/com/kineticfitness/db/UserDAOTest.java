package com.kineticfitness.db;

import com.kineticfitness.model.FitnessLevel;
import com.kineticfitness.model.User;
import com.kineticfitness.util.PasswordUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Accounts: registration, login, password change and lookups, against a throwaway
 * in-memory database so the suite never touches {@code kineticfitness.db}.
 */
class UserDAOTest {

    private final UserDAO userDAO = new UserDAO();

    @BeforeEach
    void setUp() {
        DatabaseConnection.configure("jdbc:sqlite::memory:");
    }

    @AfterEach
    void tearDown() {
        DatabaseConnection.reset();
    }

    private User registerAlice() {
        return userDAO.register("alice", "Secret123!", "alice@example.com", "0400000001");
    }

    // ---- register ----------------------------------------------------------------

    @Test
    @DisplayName("register returns the new user with the details given")
    void registerReturnsUser() {
        User user = registerAlice();

        assertNotNull(user);
        assertEquals("alice", user.getUsername());
        assertEquals("alice@example.com", user.getEmail());
        assertEquals("0400000001", user.getPhoneNumber());
        assertEquals(FitnessLevel.BEGINNER, user.getFitnessLevel());
    }

    @Test
    @DisplayName("register stores a hash, never the raw password")
    void registerStoresHashedPassword() {
        User user = registerAlice();

        assertNotEquals("Secret123!", user.getPasswordHash());
        assertTrue(PasswordUtil.verify("Secret123!", user.getPasswordHash()));

        User stored = userDAO.findByUsername("alice");
        assertNotEquals("Secret123!", stored.getPasswordHash());
        assertTrue(PasswordUtil.verify("Secret123!", stored.getPasswordHash()));
    }

    @Test
    @DisplayName("register rejects a duplicate username")
    void registerRejectsDuplicateUsername() {
        registerAlice();

        assertNull(userDAO.register("alice", "Other456!", "other@example.com", "0400000002"));
    }

    @Test
    @DisplayName("register rejects a duplicate email")
    void registerRejectsDuplicateEmail() {
        registerAlice();

        assertNull(userDAO.register("bob", "Other456!", "alice@example.com", "0400000002"));
    }

    @Test
    @DisplayName("register rejects a duplicate phone number")
    void registerRejectsDuplicatePhone() {
        registerAlice();

        assertNull(userDAO.register("bob", "Other456!", "bob@example.com", "0400000001"));
    }

    @Test
    @DisplayName("a rejected registration does not create the account")
    void rejectedRegistrationCreatesNothing() {
        registerAlice();
        userDAO.register("bob", "Other456!", "alice@example.com", "0400000002");

        assertFalse(userDAO.usernameExists("bob"));
    }

    // ---- exists checks -----------------------------------------------------------

    @Test
    @DisplayName("usernameExists, emailExists and phoneExists find a registered user")
    void existsChecksFindRegisteredUser() {
        registerAlice();

        assertTrue(userDAO.usernameExists("alice"));
        assertTrue(userDAO.emailExists("alice@example.com"));
        assertTrue(userDAO.phoneExists("0400000001"));
    }

    @Test
    @DisplayName("exists checks are false for values nobody has used")
    void existsChecksFalseForUnknownValues() {
        registerAlice();

        assertFalse(userDAO.usernameExists("nobody"));
        assertFalse(userDAO.emailExists("nobody@example.com"));
        assertFalse(userDAO.phoneExists("0499999999"));
    }

    // ---- authenticate ------------------------------------------------------------

    @Test
    @DisplayName("authenticate accepts the right password with a username")
    void authenticateByUsername() {
        registerAlice();

        User user = userDAO.authenticate("alice", "Secret123!");

        assertNotNull(user);
        assertEquals("alice", user.getUsername());
    }

    @Test
    @DisplayName("authenticate accepts the right password with an email")
    void authenticateByEmail() {
        registerAlice();

        User user = userDAO.authenticate("alice@example.com", "Secret123!");

        assertNotNull(user);
        assertEquals("alice", user.getUsername());
    }

    @Test
    @DisplayName("authenticate rejects a wrong password")
    void authenticateWrongPassword() {
        registerAlice();

        assertNull(userDAO.authenticate("alice", "wrong"));
    }

    @Test
    @DisplayName("authenticate rejects an unknown user")
    void authenticateUnknownUser() {
        assertNull(userDAO.authenticate("ghost", "Secret123!"));
    }

    @Test
    @DisplayName("a user saved without a password cannot log in")
    void userWithoutPasswordCannotAuthenticate() {
        userDAO.save(new User("nopass", FitnessLevel.BEGINNER, 25, 180, 75));

        assertNull(userDAO.authenticate("nopass", ""));
        assertNull(userDAO.authenticate("nopass", "anything"));
    }

    // ---- save / find -------------------------------------------------------------

    @Test
    @DisplayName("save then findByUsername returns the same body details")
    void saveRoundTrip() {
        userDAO.save(new User("carol", FitnessLevel.INTERMEDIATE, 31, 168.5, 62.25));

        User found = userDAO.findByUsername("carol");

        assertNotNull(found);
        assertEquals(FitnessLevel.INTERMEDIATE, found.getFitnessLevel());
        assertEquals(31, found.getAge());
        assertEquals(168.5, found.getHeightCm(), 0.001);
        assertEquals(62.25, found.getWeightKg(), 0.001);
    }

    @Test
    @DisplayName("saving an existing username updates it instead of adding a second row")
    void saveUpdatesExistingUser() {
        userDAO.save(new User("carol", FitnessLevel.BEGINNER, 30, 170, 70));
        userDAO.save(new User("carol", FitnessLevel.ADVANCED, 31, 171, 68));

        User found = userDAO.findByUsername("carol");

        assertEquals(FitnessLevel.ADVANCED, found.getFitnessLevel());
        assertEquals(31, found.getAge());
        assertEquals(171, found.getHeightCm(), 0.001);
        assertEquals(68, found.getWeightKg(), 0.001);
    }

    @Test
    @DisplayName("saving body details does not overwrite the account password")
    void saveKeepsPassword() {
        registerAlice();
        User alice = userDAO.findByUsername("alice");
        alice.setAge(28);
        alice.setFitnessLevel(FitnessLevel.INTERMEDIATE);
        userDAO.save(alice);

        assertNotNull(userDAO.authenticate("alice", "Secret123!"));
        assertEquals(28, userDAO.findByUsername("alice").getAge());
    }

    @Test
    @DisplayName("findByUsername returns null for an unknown user")
    void findByUsernameUnknown() {
        assertNull(userDAO.findByUsername("ghost"));
    }

    @Test
    @DisplayName("findFirst returns null when there are no users")
    void findFirstEmpty() {
        assertNull(userDAO.findFirst());
    }

    @Test
    @DisplayName("findFirst returns the earliest user created")
    void findFirstReturnsEarliest() {
        userDAO.save(new User("first", FitnessLevel.BEGINNER, 20, 170, 60));
        userDAO.save(new User("second", FitnessLevel.BEGINNER, 21, 171, 61));

        assertEquals("first", userDAO.findFirst().getUsername());
    }

    // ---- getEmail ----------------------------------------------------------------

    @Test
    @DisplayName("getEmail returns the account email, or null for an unknown user")
    void getEmail() {
        registerAlice();

        assertEquals("alice@example.com", userDAO.getEmail("alice"));
        assertNull(userDAO.getEmail("ghost"));
    }

    // ---- updatePassword ----------------------------------------------------------

    @Test
    @DisplayName("updatePassword replaces the password: the new one works and the old one does not")
    void updatePasswordChangesLogin() {
        registerAlice();

        boolean updated = userDAO.updatePassword("alice", PasswordUtil.hash("NewSecret456!"));

        assertTrue(updated);
        assertNotNull(userDAO.authenticate("alice", "NewSecret456!"));
        assertNull(userDAO.authenticate("alice", "Secret123!"));
    }

    @Test
    @DisplayName("updatePassword returns false for an unknown user")
    void updatePasswordUnknownUser() {
        assertFalse(userDAO.updatePassword("ghost", PasswordUtil.hash("x")));
    }
}