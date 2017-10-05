package com.comp30022.arrrrr;

import android.app.Activity;
import android.support.test.annotation.UiThreadTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.support.test.runner.lifecycle.Stage;
import android.support.v4.app.FragmentTransaction;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;


import org.junit.Rule;
import org.mockito.Mockito;

import java.util.Collection;

import static org.junit.Assert.assertEquals;

/**
 * This class will test the functionality of registration
 *
 * @author Zijie Shen
 */

@RunWith(AndroidJUnit4.class)
public class RegisterTest{

    /**
     * You will need a ActivityTestRule for testing involving any activity.
     * See https://developer.android.com/reference/android/support/test/rule/ActivityTestRule.html
     * for more details
     */
    @Rule
    public ActivityTestRule<RegisterActivity> mRegisterActivityRule =
            new ActivityTestRule<>(RegisterActivity.class);

    /**
     * Test the case where a register process succeeds
     */
    @Test
    @UiThreadTest
    public void testRegisterSuccess() throws InterruptedException {
        // Retrieve listener
        RegisterActivity registerActivity = mRegisterActivityRule.getActivity();
        OnCompleteListener onRegisterlistener = registerActivity.getOnRegisterCompleteListener();

        // Mock a result task
        Task task = Mockito.mock(Task.class);
        // Make sure the task indicates success
        Mockito.when(task.isSuccessful()).thenReturn(true);

        // Mock an Auth object
        FirebaseAuth auth = MockUserDatabase.mockFirebaseAuth();

        // Replace mAuth with a fake one
        registerActivity.setAuth(auth);
        // Simulates onComplete event
        onRegisterlistener.onComplete(task);

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
     * Test the case where register process fails
     */
    @Test
    @UiThreadTest
    public void testRegisterFailure() {
        // Retrieve listener
        RegisterActivity registerActivity = mRegisterActivityRule.getActivity();
        OnCompleteListener onRegisterlistener = registerActivity.getOnRegisterCompleteListener();

        // Mock a result task
        Task task = Mockito.mock(Task.class);
        // Make sure the task indicates failure
        Mockito.when(task.isSuccessful()).thenReturn(false);
        Mockito.when(task.getException()).thenReturn(new Exception("test exception"));

        // Simulates onComplete event
        onRegisterlistener.onComplete(task);

        // Check the text of textview has been changed to failure message
    }

}