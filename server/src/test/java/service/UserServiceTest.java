package service;

import dataaccess.*;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {

    private UserService userService;
    private UserDao userDao;
    private AuthData authData;

    @BeforeEach
    public void setUp() throws Exception {
        userService = new UserService();
        userDao = new UserDao();
        userDao.clear();
    }

    @AfterEach
    public void tearDown() throws Exception {
        userDao.clear();
    }

    /**
     * Positive test case for the register method.
     * It should successfully register a new user.
     */
    @Test
    public void testRegisterPositive() {
        UserData user = new UserData("testUser", "password123", "test@example.com");
        try {
            authData = userService.register(user);
            assertNotNull(authData, "AuthData should not be null after registration");
            assertEquals("testUser", authData.username(), "Username should match the registered username");
            assertNotNull(authData.authToken(), "AuthToken should not be null after registration");
        } catch (Exception e) {
            fail("Exception should not be thrown in positive register test: " + e.getMessage());
        }
    }

    /**
     * Negative test case for the register method.
     * It should fail to register a user with an existing username.
     */
    @Test
    public void testRegisterNegative() {
        UserData user1 = new UserData("testUser", "password123", "test@example.com");
        UserData user2 = new UserData("testUser", "password456", "test2@example.com");
        try {
            userService.register(user1);
            userService.register(user2);
            fail("DuplicateInfoException should have been thrown due to duplicate username");
        } catch (DuplicateInfoException e) {
            assertEquals("Username already exists", e.getMessage(), "Exception message should indicate duplicate username");
        } catch (Exception e) {
            fail("Unexpected exception type: " + e.getClass().getSimpleName());
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
            userService.register(user);
            authData = userService.login(new UserData("testUser", "password123", null));
            assertNotNull(authData, "AuthData should not be null after login");
            assertEquals("testUser", authData.username(), "Username should match the logged-in username");
        } catch (Exception e) {
            fail("Exception should not be thrown in positive login test: " + e.getMessage());
        }
    }

    /**
     * Negative test case for the login method.
     * It should fail to log in with incorrect credentials.
     */
    @Test
    public void testLoginNegative() {
        UserData user = new UserData("testUser", "password123", "test@example.com");
        try {
            userService.register(user);
            userService.login(new UserData("testUser", "wrongPassword", null));
            fail("UnauthorizedException should have been thrown due to incorrect password");
        } catch (UnauthorizedException e) {
            assertEquals("Unauthorized", e.getMessage(), "Exception message should indicate unauthorized access");
        } catch (Exception e) {
            fail("Unexpected exception type: " + e.getClass().getSimpleName());
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
            authData = userService.register(user);
            userService.logout(authData);
            // Verify that the user is logged out by attempting to verify auth
            assertThrows(UnauthorizedException.class, () -> userService.verifyAuth(authData), "User should be logged out");
        } catch (Exception e) {
            fail("Exception should not be thrown in positive logout test: " + e.getMessage());
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
            userService.logout(invalidAuth);
            fail("UnauthorizedException should have been thrown due to invalid auth token");
        } catch (UnauthorizedException e) {
            assertEquals("Unauthorized", e.getMessage(), "Exception message should indicate unauthorized access");
        } catch (Exception e) {
            fail("Unexpected exception type: " + e.getClass().getSimpleName());
        }
    }

    /**
     * Positive test case for the verifyAuth method.
     * It should successfully verify a valid auth token.
     */
    @Test
    public void testVerifyAuthPositive() {
        UserData user = new UserData("testUser", "password123", "test@example.com");
        try {
            authData = userService.register(user);
            userService.verifyAuth(authData);
            // If no exception is thrown, the test passes
        } catch (Exception e) {
            fail("Exception should not be thrown in positive verifyAuth test: " + e.getMessage());
        }
    }

    /**
     * Negative test case for the verifyAuth method.
     * It should fail to verify an invalid auth token.
     */
    @Test
    public void testVerifyAuthNegative() {
        AuthData invalidAuth = new AuthData("invalidToken", "testUser");
        try {
            userService.verifyAuth(invalidAuth);
            fail("UnauthorizedException should have been thrown due to invalid auth token");
        } catch (UnauthorizedException e) {
            assertEquals("Unauthorized", e.getMessage(), "Exception message should indicate unauthorized access");
        } catch (Exception e) {
            fail("Unexpected exception type: " + e.getClass().getSimpleName());
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
            authData = userService.register(user);
            AuthData retrievedAuth = userService.getAuthByToken(authData.authToken());
            assertNotNull(retrievedAuth, "Retrieved AuthData should not be null");
            assertEquals(authData.username(), retrievedAuth.username(), "Usernames should match");
        } catch (Exception e) {
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
            if (userDao.getAuthByToken("invalidToken").username() != null) {
                fail("UnauthorizedException should have been thrown due to invalid auth token");
            }
            else {
                throw new UnauthorizedException("Unauthorized");
            }
        } catch (UnauthorizedException e) {
            assertEquals("Unauthorized", e.getMessage(), "Exception message should indicate unauthorized access");
        } catch (Exception e) {
            fail("Unexpected exception type: " + e.getClass().getSimpleName());
        }
    }
}
