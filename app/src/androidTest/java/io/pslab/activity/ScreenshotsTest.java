package io.pslab.activity;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.notNullValue;

import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import androidx.test.ext.junit.rules.ActivityScenarioRule;

import tools.fastlane.screengrab.Screengrab;
import tools.fastlane.screengrab.UiAutomatorScreenshotStrategy;
import tools.fastlane.screengrab.locale.LocaleTestRule;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiScrollable;
import androidx.test.uiautomator.UiSelector;
import androidx.test.uiautomator.Until;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class ScreenshotsTest {
    private static final int TIMEOUT = 10000;
    private static final String APP_PACKAGE_NAME = "io.pslab";
    UiDevice mDevice;

    @ClassRule
    public static final LocaleTestRule localeTestRule = new LocaleTestRule();

    @Rule
    public ActivityScenarioRule<MainActivity> mActivityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Rule
    public GrantPermissionRule mGrantPermissionRule =
            GrantPermissionRule.grant(
                    "android.permission.ACCESS_FINE_LOCATION",
                    "android.permission.RECORD_AUDIO",
                    "android.permission.WRITE_EXTERNAL_STORAGE");

    @Before
    public void setUp() {
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        mDevice = UiDevice.getInstance(instrumentation);
        mDevice.pressHome();

        final String launcherPackage = getLauncherPackageName();
        assertThat(launcherPackage, notNullValue());

        Context context = getApplicationContext();
        final Intent intent = context.getPackageManager().getLaunchIntentForPackage(APP_PACKAGE_NAME);
        assert intent != null;
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
        Screengrab.setDefaultScreenshotStrategy(new UiAutomatorScreenshotStrategy());

        mDevice.wait(Until.hasObject(By.pkg(APP_PACKAGE_NAME).depth(0)), TIMEOUT);
    }

    @Test
    public void testTakeScreenshot() throws UiObjectNotFoundException {
        mDevice.wait(Until.hasObject(By.text("Instruments")), TIMEOUT);
        Screengrab.screenshot("dashboard");
        UiObject openDrawer = mDevice.findObject(new UiSelector().description("open_drawer"));
        openDrawer.click();

        mDevice.wait(Until.hasObject(By.text("Not Connected")), TIMEOUT);
        Screengrab.screenshot("drawer");

        UiScrollable navRecyclerView = new UiScrollable(new UiSelector().resourceId(APP_PACKAGE_NAME + ":id/nav_instruments"));
        UiObject item = navRecyclerView.getChild(new UiSelector().text("Instruments"));
        item.click();

        UiScrollable applicationsRecyclerView = new UiScrollable(new UiSelector().resourceId(APP_PACKAGE_NAME + ":id/applications_recycler_view"));

        applicationsRecyclerView.scrollTextIntoView("ACCELEROMETER");
        onView(withText("ACCELEROMETER")).perform(click());
        mDevice.wait(Until.hasObject(By.text("Accelerometer")), TIMEOUT);
        Screengrab.screenshot("instrument_accelerometer_view");

        mDevice.pressBack();

        applicationsRecyclerView.scrollTextIntoView("BAROMETER");
        onView(withText("BAROMETER")).perform(click());
        mDevice.wait(Until.hasObject(By.text("Barometer")), TIMEOUT);
        Screengrab.screenshot("instrument_barometer_view");

        mDevice.pressBack();

        applicationsRecyclerView.scrollTextIntoView("MULTIMETER");
        onView(withText("MULTIMETER")).perform(click());
        mDevice.wait(Until.hasObject(By.text("Multimeter")), TIMEOUT);
        Screengrab.screenshot("instrument_multimeter_view");

        mDevice.pressBack();

        applicationsRecyclerView.scrollTextIntoView("WAVE GENERATOR");
        onView(withText("WAVE GENERATOR")).perform(click());
        mDevice.wait(Until.hasObject(By.text("Wave Generator")), TIMEOUT);
        Screengrab.screenshot("instrument_wave_generator");

        mDevice.pressBack();

        applicationsRecyclerView.scrollTextIntoView("OSCILLOSCOPE");
        onView(withText("OSCILLOSCOPE")).perform(click());
        mDevice.wait(Until.hasObject(By.text("Oscilloscope")), TIMEOUT);
        mDevice.swipe(500, 0, 500, 1500, 10);
        mDevice.wait(Until.hasObject(By.text("400")), TIMEOUT);
        Screengrab.screenshot("oscilloscope_channel_view");
    }

    private String getLauncherPackageName() {
        final Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);

        PackageManager pm = getApplicationContext().getPackageManager();
        ResolveInfo resolveInfo = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        assert resolveInfo != null;
        return resolveInfo.activityInfo.packageName;
    }
}
