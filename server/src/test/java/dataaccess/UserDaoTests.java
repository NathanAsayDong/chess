package dataaccess;

import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class UserDaoTests {

    private UserDao userDao;

    @BeforeEach
    public void setUp() throws Exception {
        try {
            userDao = new SqlUserDao();
            userDao.clear();
        } catch (DataAccessException e) {
            fail("Exception should not be thrown in setup: " + e.getMessage());
        }
    }

    @AfterEach
    public void tearDown() throws Exception {
        userDao.clear();
    }

    /**
     * Positive test case for the createUser method.
     * It should successfully create a new user.
     */
    @Test
    public void testCreateUserPositive() {
        UserData user = new UserData("testUser", "password123", "test@example.com");
        try {
            AuthData authData = userDao.createUser(user);
            assertNotNull(authData, "AuthData should not be null after user creation");
            assertEquals("testUser", authData.username(), "Username should match the created user");
            assertNotNull(authData.authToken(), "AuthToken should not be null after user creation");
        } catch (DataAccessException e) {
            fail("Exception should not be thrown in positive createUser test: " + e.getMessage());
        }
    }

    /**
     * Negative test case for the createUser method.
     * It should fail to create a user with an existing username.
     */
    @Test
    public void testCreateUserNegative() {
        UserData user1 = new UserData("testUser", "password123", "test@example.com");
        UserData user2 = new UserData("testUser", "password456", "test2@example.com");
        try {
            userDao.createUser(user1);
            userDao.createUser(user2);
            fail("DataAccessException should have been thrown due to duplicate username");
        } catch (DataAccessException e) {
            assertEquals("unable to update database: INSERT INTO UserData (username, email, password) VALUES (?, ?, ?), Duplicate entry 'testUser' for key 'userdata.username'", e.getMessage(), "Exception message should indicate duplicate username");
        }
    }

    /**
     * Positive test case for the login method.
     * It should successfully log in a registered user.
     */
    @Test
    public void testLoginPositive() {
        UserData user = new UserData("testUser", "password123", "test@example.com");
        try {
            userDao.createUser(user);
            AuthData authData = userDao.login(user);
            assertNotNull(authData, "AuthData should not be null after login");
            assertEquals("testUser", authData.username(), "Username should match the logged-in user");
            assertNotNull(authData.authToken(), "AuthToken should not be null after login");
        } catch (DataAccessException e) {
            fail("Exception should not be thrown in positive login test: " + e.getMessage());
        }
    }

    /**
     * Negative test case for the login method.
     * It should fail to log in with incorrect password.
     */
    @Test
    public void testLoginNegative() {
        UserData user = new UserData("testUser", "password123", "test@example.com");
        UserData wrongPasswordUser = new UserData("testUser", "wrongPassword", "test@example.com");
        try {
            AuthData auth1 =userDao.createUser(user);
            AuthData auth2 = userDao.login(wrongPasswordUser);
            assertNotEquals(auth1.authToken(), auth2.authToken(), "AuthTokens should not match for incorrect password");
        } catch (DataAccessException e) {
            assertEquals("Incorrect password", e.getMessage(), "Exception message should indicate incorrect password");
        }
    }

    /**
     * Positive test case for the logout method.
     * It should successfully log out a logged-in user.
     */
    @Test
    public void testLogoutPositive() {
        UserData user = new UserData("testUser", "password123", "test@example.com");
        try {
            AuthData authData = userDao.createUser(user);
            userDao.logout(authData);
            userDao.getAuthByToken(authData.authToken());
        } catch (DataAccessException e) {
            assertEquals("Invalid token", e.getMessage(), "Exception message should indicate invalid token");
        }
    }

    /**
     * Negative test case for the logout method.
     * It should fail to log out with an invalid auth token.
     */
    @Test
    public void testLogoutNegative() {
        AuthData invalidAuth = new AuthData("invalidToken", "testUser");
        try {
            userDao.logout(invalidAuth);
            fail("DataAccessException should have been thrown due to invalid auth token");
        } catch (DataAccessException e) {
            assertEquals("Invalid token", e.getMessage(), "Exception message should indicate invalid auth token");
        }
    }

    /**
     * Positive test case for the clear method.
     * It should successfully clear all user data.
     */
    @Test
    public void testClearPositive() {
        try {
            UserData user = new UserData("testUser", "password123", "test@example.com");
            userDao.createUser(user);
            userDao.clear();
            userDao.login(user);
        } catch (DataAccessException e) {
            assertEquals("Invalid token", e.getMessage(), "Exception message should indicate invalid token");
        }
    }

    /**
     * Positive test case for the getAuthByToken method.
     * It should successfully retrieve auth data with a valid token.
     */
    @Test
    public void testGetAuthByTokenPositive() {
        UserData user = new UserData("testUser", "password123", "test@example.com");
        try {
            AuthData authData = userDao.createUser(user);
            AuthData retrievedAuth = userDao.getAuthByToken(authData.authToken());
            assertNotNull(retrievedAuth, "Retrieved AuthData should not be null");
            assertEquals(authData.username(), retrievedAuth.username(), "Usernames should match");
            assertEquals(authData.authToken(), retrievedAuth.authToken(), "AuthTokens should match");
        } catch (DataAccessException e) {
            fail("Exception should not be thrown in positive getAuthByToken test: " + e.getMessage());
        }
    }

    /**
     * Negative test case for the getAuthByToken method.
     * It should fail to retrieve auth data with an invalid token.
     */
    @Test
    public void testGetAuthByTokenNegative() {
        try {
            AuthData auth = userDao.getAuthByToken("invalidToken");
            assertEquals(null, auth.username(), "AuthData should be null for invalid token");
        } catch (DataAccessException e) {
            assertEquals("Invalid token", e.getMessage(), "Exception message should indicate invalid auth token");
        }
    }

    /**
     * Positive test case for the getUserDataByUserData method.
     * It should successfully retrieve user data for an existing user.
     */
    @Test
    public void testGetUserDataByUserDataPositive() {
        UserData user = new UserData("testUser", "password123", "test@example.com");
        try {
            userDao.createUser(user);
            UserData retrievedUser = userDao.getUserDataByUserData(user);
            assertNotNull(retrievedUser, "Retrieved UserData should not be null");
            assertEquals(user.username(), retrievedUser.username(), "Usernames should match");
            assertEquals(user.email(), retrievedUser.email(), "Emails should match");
        } catch (DataAccessException e) {
            fail("Exception should not be thrown in positive getUserDataByUserData test: " + e.getMessage());
        }
    }

    /**
     * Negative test case for the getUserDataByUserData method.
     * It should fail to retrieve user data for a non-existent user.
     */
    @Test
    public void testGetUserDataByUserDataNegative() {
        UserData nonExistentUser = new UserData("nonExistentUser", "password123", "nonexistent@example.com");
        try {
            UserData userData = userDao.getUserDataByUserData(nonExistentUser);
            assertEquals(null, userData, "UserData should be null for non-existent user");
        } catch (DataAccessException e) {
            assertEquals("User does not exist", e.getMessage(), "Exception message should indicate non-existent user");
        }
    }
}

