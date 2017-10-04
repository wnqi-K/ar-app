package com.comp30022.arrrrr;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.test.runner.AndroidJUnit4;

import com.comp30022.arrrrr.database.UserManagement;
import com.comp30022.arrrrr.models.User;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;

/**
 * Test for user profile information.
 *
 * @author Dafu Ai
 * // TODO: Add co-author here
 * @author ____
 */

@RunWith(MockitoJUnitRunner.class)
public class UserProfileTest {
    @Mock
    private Context mMockContext;

    private UserManagement userManagement;

    @Before
    public void setUp() throws Exception {
        userManagement = MockUserDatabase.mockUserManagement();

        User user1 = MockUserDatabase.mockUser("user_no_profile_image", "user_no_profile_image@gmail.com");
        User user2 = MockUserDatabase.mockUser("user_has_profile_image", "user_has_profile_image@gmail.com",
                null, null, null, null, null, "wrong_url");

        // Users with different profile image status
        userManagement.addUser(user1);
        userManagement.addUser(user2);
    }

    /**
     * Test when user has no profile image.
     */
    @Test
    public void testNoProfileImage() {

        String uid = "user_no_profile_image";
        Bitmap img = userManagement.getUserProfileImage(uid, mMockContext);
        assertEquals(img, BitmapFactory.decodeResource(mMockContext.getResources(),
                R.drawable.portrait_photo));
    }

    /**
     * Test when user has profile image.
     */
    @Test
    public void testExistProfileImage() {
        UserManagement userManagement = MockUserDatabase.mockUserManagement();
        String uid = "user_has_profile_image";
        Bitmap img = userManagement.getUserProfileImage(uid, mMockContext);
        assertEquals(img, null);
    }
}
