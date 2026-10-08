package ir.kamranvahdati.lawoffice;
import static org.junit.Assert.*;
import android.app.*;
import android.content.*;
import android.view.*;
import android.widget.*;
import android.graphics.Bitmap;
import java.io.*;
import java.util.*;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
@RunWith(AndroidJUnit4.class)
public class WelcomeEntryTest {
 @Test public void entryPreservesProfileRoutesRegistrationAndHonorsLock()throws Exception{
  Instrumentation ins=InstrumentationRegistry.getInstrumentation();Context c=ins.getTargetContext();
  SharedPreferences p=c.getSharedPreferences("office_profile",0);Map<String,?> saved=p.getAll();
  try{for(String theme:new String[]{AppTheme.LIGHT,AppTheme.DARK}){
   p.edit().putString("theme_id",theme).putBoolean("lock_enabled",false).putBoolean("profile_complete",false).putString("name","نام موجود").commit();
   MainActivity a=(MainActivity)ins.startActivitySync(new Intent(c,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));ins.waitForIdleSync();
   try{
    ins.runOnMainSync(()->{assertTrue(a.welcomeScreen);assertEquals(View.GONE,a.root.getChildAt(0).getVisibility());assertNotNull(find(a.page,"ورود به اپلیکیشن"));assertNotNull(find(a.page,"ثبت‌نام وکیل"));a.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);});ins.waitForIdleSync();
    Thread.sleep(200);Bitmap bitmap=ins.getUiAutomation().takeScreenshot();assertNotNull(bitmap);File dir=new File(c.getExternalFilesDir(null),"qa/welcome");assertTrue(dir.isDirectory()||dir.mkdirs());try(FileOutputStream out=new FileOutputStream(new File(dir,theme+".png"))){assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG,100,out));}finally{bitmap.recycle();}
    ins.runOnMainSync(()->{
     find(a.page,"ورود به اپلیکیشن").performClick();assertTrue(a.registrationGate);assertEquals(View.GONE,a.root.getChildAt(0).getVisibility());assertEquals("نام موجود",p.getString("name",""));
     a.onBackPressed();assertTrue(a.welcomeScreen);
     p.edit().putBoolean("profile_complete",true).putString("name","وکیل آزمون").putString("professional_body","کانون وکلای دادگستری").putString("province","تهران").putString("city","تهران").putString("national_id","0013540831").putString("phone","09120000001").commit();
     find(a.page,"ثبت‌نام وکیل").performClick();assertTrue(a.welcomeScreen);assertEquals("وکیل آزمون",p.getString("name",""));
     find(a.page,"ورود به اپلیکیشن").performClick();assertFalse(a.welcomeScreen);assertFalse(a.registrationGate);assertEquals(View.VISIBLE,a.root.getChildAt(0).getVisibility());
     a.welcome();p.edit().putBoolean("lock_enabled",true).putBoolean("biometric_enabled",false).commit();find(a.page,"ورود به اپلیکیشن").performClick();assertEquals(View.INVISIBLE,a.root.getVisibility());assertFalse(a.authenticated);
    });ins.waitForIdleSync();
   }finally{ins.runOnMainSync(a::finish);ins.waitForIdleSync();}
  }}finally{
   SharedPreferences.Editor e=p.edit().clear();for(Map.Entry<String,?> x:saved.entrySet()){Object v=x.getValue();if(v instanceof String)e.putString(x.getKey(),(String)v);else if(v instanceof Boolean)e.putBoolean(x.getKey(),(Boolean)v);else if(v instanceof Integer)e.putInt(x.getKey(),(Integer)v);else if(v instanceof Long)e.putLong(x.getKey(),(Long)v);else if(v instanceof Float)e.putFloat(x.getKey(),(Float)v);else if(v instanceof Set)e.putStringSet(x.getKey(),(Set<String>)v);}e.commit();
  }
 }
 static TextView find(View v,String text){if(v instanceof TextView&&text.contentEquals(((TextView)v).getText()))return (TextView)v;if(v instanceof ViewGroup)for(int i=0;i<((ViewGroup)v).getChildCount();i++){TextView r=find(((ViewGroup)v).getChildAt(i),text);if(r!=null)return r;}return null;}
}
