package ir.kamranvahdati.lawoffice;

import static org.junit.Assert.*;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Never clears the live preferences, database, or Android Keystore alias. */
@RunWith(AndroidJUnit4.class)
public class DatabaseKeyConcurrencyTest {
    @Test public void concurrentFirstReadersAndReopenUseOneSecret() throws Exception {
        Context live=InstrumentationRegistry.getInstrumentation().getTargetContext();
        byte[] liveBefore=DatabaseKey.read(live);
        for(int round=0;round<3;round++){
            String namespace="key-concurrency-test-"+UUID.randomUUID();
            Context isolated=new ContextWrapper(live){
                @Override public SharedPreferences getSharedPreferences(String name,int mode){
                    return super.getSharedPreferences(namespace+"-"+name,mode);
                }
                @Override public File getDatabasePath(String name){
                    return new File(getCacheDir(),namespace+"-"+name);
                }
            };
            ExecutorService pool=Executors.newFixedThreadPool(16);
            CountDownLatch ready=new CountDownLatch(16),start=new CountDownLatch(1);
            List<Future<byte[]>> results=new ArrayList<>();
            try{
                for(int i=0;i<16;i++)results.add(pool.submit(()->{
                    ready.countDown();
                    if(!start.await(15,TimeUnit.SECONDS))throw new AssertionError("Start timeout");
                    return DatabaseKey.read(isolated);
                }));
                assertTrue(ready.await(15,TimeUnit.SECONDS));start.countDown();
                byte[] first=results.get(0).get(30,TimeUnit.SECONDS);
                assertEquals(32,first.length);
                for(Future<byte[]> result:results){
                    // Do not include cryptographic material in assertion output.
                    assertTrue("Concurrent callers must share one secret",Arrays.equals(first,result.get(30,TimeUnit.SECONDS)));
                }
                assertTrue("Persisted secret must reopen",Arrays.equals(first,DatabaseKey.read(isolated)));
                assertFalse("Fixture must not reuse live database secret",Arrays.equals(first,liveBefore));
            }finally{
                start.countDown();pool.shutdownNow();
                assertTrue(pool.awaitTermination(30,TimeUnit.SECONDS));
                live.deleteSharedPreferences(namespace+"-database_secret");
            }
        }
        assertTrue("Live key must remain unchanged",Arrays.equals(liveBefore,DatabaseKey.read(live)));
    }
}
