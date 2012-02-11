package org.eiennohito.kotonoha.android.test;

import android.test.ActivityInstrumentationTestCase2;
import org.eiennohito.kotonoha.android.activities.HelloAndroidActivity;

public class HelloAndroidActivityTest extends ActivityInstrumentationTestCase2<HelloAndroidActivity> {

    public HelloAndroidActivityTest() {
        super("org.eiennohito.kotonoha.android", HelloAndroidActivity.class);
    }

    public void testActivity() {
        HelloAndroidActivity activity = getActivity();
        assertNotNull(activity);
    }
}

