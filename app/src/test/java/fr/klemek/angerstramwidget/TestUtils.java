package fr.klemek.angerstramwidget;

import android.util.Log;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.when;

final class TestUtils {

    private TestUtils(){

    }

    public static void prepareTests(){
        System.out.println("Running test");

        PowerMockito.mockStatic(Log.class);

        // Log warnings to the console
        when(Log.w(anyString(), anyString())).thenAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                System.out.println("[Warning][" + args[0] + "] " + args[1]);
                return null;
            }
        });
        when(Log.i(anyString(), anyString())).thenAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                System.out.println("[Info][" + args[0] + "] " + args[1]);
                return null;
            }
        });
        when(Log.e(anyString(), anyString())).thenAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                System.out.println("[Error][" + args[0] + "] " + args[1]);
                return null;
            }
        });
        when(Log.e(anyString(), anyString(), any(Throwable.class))).thenAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                System.out.println("[Error][" + args[0] + "] " + args[1]);
                ((Throwable)args[2]).printStackTrace();
                return null;
            }
        });
        when(Log.v(anyString(), anyString())).thenAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                System.out.println("[Verbose][" + args[0] + "] " + args[1]);
                return null;
            }
        });
        when(Log.d(anyString(), anyString())).thenAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation){
                Object[] args = invocation.getArguments();
                System.out.println("[Debug][" + args[0] + "] " + args[1]);
                return null;
            }
        });
    }
}
