package app.sleepscreen.tv;
import android.accessibilityservice.AccessibilityService;
import android.graphics.PixelFormat;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.TextView;
import android.view.View;
import android.content.Context;
import android.content.SharedPreferences;
import java.util.Locale;
public class SleepService extends AccessibilityService {
 private static SleepService instance; private WindowManager wm; private TextView badge; private View dim, filter; private final Handler h=new Handler(Looper.getMainLooper());
 static boolean running(){return instance!=null;} static boolean sleep(){return instance!=null&&instance.performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN);}
 static void refresh(){if(instance!=null)instance.update();}
 protected void onServiceConnected(){instance=this;wm=(WindowManager)getSystemService(Context.WINDOW_SERVICE);update();}
 private void remove(View v){if(v!=null)try{wm.removeView(v);}catch(Exception ignored){}}
 private WindowManager.LayoutParams params(int w,int h,int gravity){WindowManager.LayoutParams p=new WindowManager.LayoutParams(w,h,WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE|WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,PixelFormat.TRANSLUCENT);p.gravity=gravity; return p;}
 void update(){
  if(wm==null)return;
  SharedPreferences p=getSharedPreferences("overlay",0);
  int d=p.getInt("dim",0);
  int warm=p.getInt("filter",0);
  if(warm>0){int color=Color.argb(warm*150/100,80,35,0);if(filter==null){filter=new View(this);filter.setBackgroundColor(color);wm.addView(filter,params(-1,-1,Gravity.TOP|Gravity.START));}else filter.setBackgroundColor(color);}else if(filter!=null){remove(filter);filter=null;}
  if(d>0){
   if(dim==null){dim=new View(this);dim.setBackgroundColor(Color.argb(d*255/100,0,0,0));wm.addView(dim,params(-1,-1,Gravity.TOP|Gravity.START));}
   else dim.setBackgroundColor(Color.argb(d*255/100,0,0,0));
  }else if(dim!=null){remove(dim);dim=null;}
  boolean show=p.getBoolean("show",false)&&(Timer.end(this)>0||Timer.paused(this));
  if(show){
   int[] g={Gravity.TOP|Gravity.START,Gravity.TOP|Gravity.END,Gravity.BOTTOM|Gravity.START,Gravity.BOTTOM|Gravity.END,Gravity.CENTER};
   WindowManager.LayoutParams lp=params(-2,-2,g[Math.max(0,Math.min(4,p.getInt("position",1)))]);lp.x=Ui.dp(this,24);lp.y=Ui.dp(this,24);
   if(badge==null){badge=Ui.text(this,16,Color.WHITE);badge.setPadding(Ui.dp(this,12),Ui.dp(this,6),Ui.dp(this,12),Ui.dp(this,6));badge.setBackground(Ui.pill(this,Color.BLACK));badge.setAlpha(p.getInt("opacity",80)/100f);wm.addView(badge,lp);h.removeCallbacks(tick);h.post(tick);}
   else {badge.setAlpha(p.getInt("opacity",80)/100f);wm.updateViewLayout(badge,lp);}
  }else if(badge!=null){h.removeCallbacks(tick);remove(badge);badge=null;}
 }
 private final Runnable tick=new Runnable(){public void run(){long seconds=(Timer.remaining(SleepService.this)+999)/1000;if(seconds<=0){update();return;}if(badge!=null)badge.setText((Timer.paused(SleepService.this)?"Paused ":"")+String.format(Locale.getDefault(),"%d:%02d:%02d",seconds/3600,(seconds/60)%60,seconds%60));h.postDelayed(this,1000);}};
 public void onAccessibilityEvent(AccessibilityEvent e){} public void onInterrupt(){} public void onDestroy(){h.removeCallbacks(tick);remove(badge);remove(dim);remove(filter);instance=null;super.onDestroy();}
}