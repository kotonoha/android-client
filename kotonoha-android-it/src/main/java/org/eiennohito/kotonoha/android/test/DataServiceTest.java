package org.eiennohito.kotonoha.android.test;

import android.content.Intent;
import android.test.ServiceTestCase;
import org.eiennohito.kotonoha.DataService;

/**
 * @author eiennohito
 * @since 27.09.2010
 */
public class DataServiceTest extends ServiceTestCase<DataService> {

    private static String TAG = "Kotonoha.Test";
    public DataServiceTest() {
        super(DataService.class);
    }

    @Override
    protected void setUp() throws Exception {
        //super.setUp();    //To change body of overridden methods use File | Settings | File Templates.
        startService(new Intent(getContext(), DataService.class));
    }


    public void testListWords() {
        boolean val = getService().preloadWords();
        assertTrue(val);
    }
}
