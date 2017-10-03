package com.comp30022.arrrrr;

import android.app.Activity;
import android.support.test.annotation.UiThreadTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.support.test.runner.lifecycle.Stage;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import java.util.Collection;

import static org.junit.Assert.*;

/**
 * Testing for email login which shows an example of
 * how to mock Firebase components and simulate expected behaviours.
 * This class uses Mockito and Espresso!
 *
 * @author Dafu Ai
 */

@RunWith(AndroidJUnit4.class)
public class EmailLoginTest {

    /**
     * You will need a ActivityTestRule for testing involving any activity.
     * See https://developer.android.com/reference/android/support/test/rule/ActivityTestRule.html
     * for more details
     */
    @Rule
    public ActivityTestRule<EmailLoginActivity> mEmailLoginActivityRule =
            new ActivityTestRule<>(EmailLoginActivity.class);

    /**
     * Test the case where a sign in process succeeds
     */
    @Test
    @UiThreadTest
    public void testSignInSuccess() throws InterruptedException {
        // Retrieve listener
        EmailLoginActivity emailLoginActivity = mEmailLoginActivityRule.getActivity();
        OnCompleteListener listener = emailLoginActivity.getOnSignInCompleteListener();

        // Mock a result task
        Task task = Mockito.mock(Task.class);
        // Make sure the task indicates success
        Mockito.when(task.isSuccessful()).thenReturn(true);

        // Mock an Auth object
        FirebaseAuth auth = MockUserDatabase.mockFirebaseAuth();

        // Replace mAuth with a fake one
        emailLoginActivity.setAuth(auth);
        // Simulates onComplete event
        listener.onComplete(task);

        // Retrieve the current resumed activity (which should be MainViewActivity)
        Activity currentActivity = null;
        Collection<Activity> activities = ActivityLifecycleMonitorRegistry
                .getInstance()
                .getActivitiesInStage(Stage.RESUMED);

        if (activities.iterator().hasNext()){
            currentActivity = activities.iterator().next();
        }

        // Check that the main view activity has been started after login success
        assertEquals(currentActivity.getClass(), MainViewActivity.class);
    }

    /**
     * Test the case where a sign in process fails
     */
    @Test
    @UiThreadTest
    public void testSignInFailure() {
        // Retrieve listener
        EmailLoginActivity emailLoginActivity = mEmailLoginActivityRule.getActivity();
        OnCompleteListener listener = emailLoginActivity.getOnSignInCompleteListener();

        // Mock a result task
        Task task = Mockito.mock(Task.class);
        // Make sure the task indicates failure
        Mockito.when(task.isSuccessful()).thenReturn(false);
        Mockito.when(task.getException()).thenReturn(new Exception("test exception"));

        // Simulates onComplete event
        listener.onComplete(task);

        // Check the text of textview has been changed to failure message
        TextView mStatusTextView = (TextView)emailLoginActivity.findViewById(R.id.status);
        assertEquals(mStatusTextView.getText(),
                            emailLoginActivity.getResources().getString(R.string.auth_failed));
    }
}
