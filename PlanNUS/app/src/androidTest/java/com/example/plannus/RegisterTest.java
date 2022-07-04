package com.example.plannus;
import androidx.test.core.app.ActivityScenario;
import androidx.test.platform.app.InstrumentationRegistry;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.ComponentName;
import android.content.Context;
import android.view.View;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;

import com.example.plannus.Activities.ContentMainActivity;
import com.example.plannus.Activities.MainActivity;
import com.example.plannus.Activities.RegisterUser;

@RunWith(AndroidJUnit4.class)
@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RegisterTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    private String fullName = "DEADBEEF";
    private String age = "12";
    private String email = "deadbeef@gmail.com";
    private String password = "deadbeef1234";

    @Before
    public void setUp() throws Exception{
        Intents.init();
    }

    @Test
    public void B_clickOnRegisterButton() {
        intending(hasComponent(MainActivity.class.getName()));
        onView(withId(R.id.register)).perform(ViewActions.scrollTo(), ViewActions.click());
        intending(hasComponent(RegisterUser.class.getName()));
        fillInDetails();
        goBackToMainActivity();
    }

    public void fillInDetails() {
        onView(withId(R.id.password)).perform(ViewActions.typeText(password));
        onView(withId(R.id.age)).perform(ViewActions.typeText(age));
        onView(withId(R.id.email)).perform(ViewActions.typeText(email));
        onView(withId(R.id.fullName)).perform(ViewActions.typeText(fullName));
        onView(withId(R.id.registerUser)).perform(ViewActions.scrollTo(),ViewActions.click());
    }

    public void goBackToMainActivity() {
        onView(withId(R.id.password)).perform(ViewActions.pressBack());
        intending(hasComponent(MainActivity.class.getName()));
        //checkLoginPageDisplayed();
    }

    @Test
    public void A_isBackToMain() {
        onView(withId(R.id.register)).perform(ViewActions.scrollTo(), ViewActions.click());
        intending(hasComponent(RegisterUser.class.getName()));
        onView(withId(R.id.password)).perform(ViewActions.pressBack());
        intending(hasComponent(MainActivity.class.getName()));
        checkLoginPageDisplayed();
    }

    public void checkLoginPageDisplayed() {
        onView(withId(R.id.loginButton)).check(matches(isDisplayed()));
        onView(withId(R.id.emailAddress)).check(matches(isDisplayed()));
        onView(withId(R.id.passWord)).check(matches(isDisplayed()));
        onView(withId(R.id.register)).check(matches(isDisplayed()));
        onView(withId(R.id.imageView)).check(matches(isDisplayed()));
        onView(withId(R.id.imageView2)).check(matches(isDisplayed()));
        onView(withId(R.id.emailIcon)).check(matches(isDisplayed()));
        B_clickOnRegisterButton();
    }

    @Test
    public void C_AttemptToLoginNewUser() throws InterruptedException {
        onView(withId(R.id.emailAddress)).perform(ViewActions.typeText(email));
        onView(withId(R.id.passWord)).perform(ViewActions.typeText(password));
        onView(withId(R.id.loginButton)).perform(ViewActions.scrollTo(), ViewActions.click());
        intending(hasComponent(ContentMainActivity.class.getName()));
//        checkSuccessfulLoginOfNewUser();
        Thread.sleep(1000);
        onView(withId(R.id.welcomeBackMsg)).check(matches(withText("Welcome Back!")));
        onView(withId(R.id.hiName)).check(matches(withText("Hi " + fullName + " !")));
    }

    public void checkSuccessfulLoginOfNewUser() {
        intending(hasComponent(ContentMainActivity.class.getName()));
        onView(withId(R.id.welcomeBackMsg)).check(matches(withText("Welcome Back!")));
        onView(withId(R.id.hiName)).check(matches(withText("Hi " + fullName + " !")));
    }

    @After
    public void tearDown() throws Exception {
        Intents.release();
    }

}
