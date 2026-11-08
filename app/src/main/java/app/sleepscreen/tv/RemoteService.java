package app.sleepscreen.tv;
import android.accessibilityservice.AccessibilityService;import android.view.KeyEvent;import android.view.accessibility.AccessibilityEvent;import android.content.Intent;import android.content.SharedPreferences;
public class RemoteService extends AccessibilityService {
 protected boolean onKeyEvent(KeyEvent e){SharedPreferences p=getSharedPreferences("mapping",0);int key=p.getInt("key",0);if(key==0||e.getKeyCode()!=key||e.getScanCode()!=p.getInt("scan",-1))return false;if(e.getAction()==KeyEvent.ACTION_DOWN&&e.getRepeatCount()==0){Intent i=new Intent(this,QuickActivity.class);i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);try{startActivity(i);}catch(Exception ignored){}}return true;}
 public void onAccessibilityEvent(AccessibilityEvent e){} public void onInterrupt(){}
}