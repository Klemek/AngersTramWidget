package fr.klemek.angerstramwidget;

import android.util.Log;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.ZoneId;

import java.util.List;

import fr.klemek.angerstramwidget.utils.APIManager;
import fr.klemek.angerstramwidget.utils.Constants;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Log.class})
public class APIManagerTest {

    @BeforeClass
    public static void initClass() {
        TestUtils.prepareTests();
    }


    @Test
    public void testLoadList() {
        TimeList tl = APIManager.loadList(null, "BAMA", false);
        assertNotEquals(0, tl.size());
        Log.d(Constants.LOGGER_TAG, tl.getList().size()+" records");
        Log.d(Constants.LOGGER_TAG, tl.toString());

        int length = tl.getList().size();

        TimeList tl2 = new TimeList(tl.toString());
        assertNotEquals(0, tl2.getList().size());

        List<String> output = tl.toStringList();

        TimeList tl3 = APIManager.loadList(tl, "BAMA", false);

        assertEquals(length, tl.size());
        assertEquals(length, tl3.size());

        Log.d(Constants.LOGGER_TAG, tl.getList().get(0).toString() + " " + output.get(0));
        Log.d(Constants.LOGGER_TAG, tl.getList().get(1).toString() + " " + output.get(1));
        Log.d(Constants.LOGGER_TAG, tl.getList().get(2).toString() + " " + output.get(2));
    }

}
