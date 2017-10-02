package com.comp30022.arrrrr;

import com.comp30022.arrrrr.database.UserManagement;
import com.comp30022.arrrrr.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Provides mocking functionality for User and UserManagement
 *
 * @author Dafu Ai
 */

public class MockUserDatabase {
    // Fake data
    public static final String currentUserUID = UUID.randomUUID().toString();
    public static final String currentUserEmail = UUID.randomUUID().toString() + "@gmail.com";

    /**
     * Mock a fake {@link FirebaseAuth} object.
     * @return containing the current {@link FirebaseUser}
     */
    public static FirebaseAuth mockFirebaseAuth() {
        FirebaseAuth auth = Mockito.mock(FirebaseAuth.class);
        FirebaseUser firebaseUser = Mockito.mock(FirebaseUser.class);

        Mockito.when(firebaseUser.getUid()).thenReturn(currentUserUID);
        Mockito.when(auth.getCurrentUser()).thenReturn(firebaseUser);
        return auth;
    }

    /**
     * Mock a fake {@link UserManagement} object.
     * @return containing the current user, friend users, admin users
     */
    public static UserManagement mockUserManagement() {
        UserManagement userManagement = Mockito.mock(UserManagement.class);
        User currentUser = mockUser(currentUserUID, currentUserEmail);

        Mockito.when(userManagement.getCurrentUser()).thenReturn(currentUser);

        List<User> friendUsers = mockRandomUsers(2);
        Mockito.when(userManagement.getFriendList()).thenReturn(friendUsers);

        List<User> adminUsers = mockRandomUsers(2);
        Mockito.when(userManagement.getAdminList()).thenReturn(adminUsers);

        List<User> allUsers = new ArrayList<>();
        allUsers.add(currentUser);
        allUsers.addAll(friendUsers);
        allUsers.addAll(adminUsers);

        return userManagement;
    }

    /**
     * Mock a list of friends
     */
    public static List<User> mockRandomUsers(int quantity) {
        List<User> users = new ArrayList<>();

        for (int i = 0; i<quantity; i++) {
            users.add(mockRandomUser());
        }

        return users;
    }

    /**
     * Mock a random user
     */
    public static User mockRandomUser() {
        String uid = UUID.randomUUID().toString();
        User user = mockUser(UUID.randomUUID().toString(), uid+"@gmail.com");
        return user;
    }

    /**
     * Mock a user by given data
     */
    public static User mockUser(String uid, String email) {
        User user = Mockito.mock(User.class);

        Mockito.when(user.getUid()).thenReturn(uid);
        Mockito.when(user.getEmail()).thenReturn(email);

        return user;
    }
}
