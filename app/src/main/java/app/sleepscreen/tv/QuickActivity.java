package app.sleepscreen.tv;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class QuickActivity extends Activity {
 private TextView timerRow,cancelRow,brightnessRow,pauseRow,filterRow; private int choice;
 @Override protected void onCreate(Bundle state){super.onCreate(state);
  if(!getSharedPreferences("setup",0).getBoolean("shown",false)){getSharedPreferences("setup",0).edit().putBoolean("shown",true).apply();startActivity(new Intent(this,SetupActivity.class));}
  LinearLayout panel=new LinearLayout(this);panel.setOrientation(LinearLayout.VERTICAL);panel.setPadding(Ui.dp(this,20),Ui.dp(this,12),Ui.dp(this,20),Ui.dp(this,12));panel.setBackground(getDrawable(R.drawable.glass_panel));
  TextView hint=Ui.text(this,14,0xffdddddd);hint.setText("Up/Down: choose   Left/Right: adjust   OK: select");panel.addView(hint);
  timerRow=Ui.row(panel,440);timerRow.setOnKeyListener((v,key,event)->{if(key==KeyEvent.KEYCODE_DPAD_LEFT||key==KeyEvent.KEYCODE_DPAD_RIGHT){if(event.getAction()==KeyEvent.ACTION_DOWN)step(key==KeyEvent.KEYCODE_DPAD_LEFT?-1:1);return true;}return false;});timerRow.setOnClickListener(v->finish());
  brightnessRow=Ui.row(panel,440);brightnessRow.setOnKeyListener((v,key,event)->{if(key==KeyEvent.KEYCODE_DPAD_LEFT||key==KeyEvent.KEYCODE_DPAD_RIGHT){if(event.getAction()==KeyEvent.ACTION_DOWN)brightness(key==KeyEvent.KEYCODE_DPAD_LEFT?-1:1);return true;}return false;});brightnessRow.setOnClickListener(v->brightness(-1));
  filterRow=Ui.row(panel,440);filterRow.setOnKeyListener((v,key,event)->{if(key==KeyEvent.KEYCODE_DPAD_LEFT||key==KeyEvent.KEYCODE_DPAD_RIGHT){if(event.getAction()==KeyEvent.ACTION_DOWN){SharedPreferences prefs=getSharedPreferences("overlay",0);int n=Math.max(0,Math.min(100,prefs.getInt("filter",0)+(key==KeyEvent.KEYCODE_DPAD_RIGHT?5:-5)));prefs.edit().putInt("filter",n).apply();SleepService.refresh();show();}return true;}return false;});
  TextView custom=Ui.row(panel,440);custom.setText("Custom timer");custom.setOnClickListener(v->RemotePicker.time(this,"Custom sleep timer",60,values->{int n=values[0]*60+values[1];if(n>0)Timer.start(this,n);finish();}));
  pauseRow=Ui.row(panel,440);pauseRow.setOnClickListener(v->{if(Timer.paused(this))Timer.resume(this);else Timer.pause(this);show();});
  cancelRow=Ui.row(panel,440);cancelRow.setText("Cancel timer");cancelRow.setOnClickListener(v->{Timer.cancel(this);show();});
  TextView settings=Ui.row(panel,440);settings.setText("Settings");settings.setOnClickListener(v->startActivity(new Intent(this,OverlaySettingsActivity.class)));
  TextView setup=Ui.row(panel,440);setup.setText("Setup and remote button");setup.setOnClickListener(v->startActivity(new Intent(this,SetupActivity.class)));
  TextView update=Ui.row(panel,440);update.setText("Check for updates");update.setOnClickListener(v->startActivity(new Intent(this,UpdateActivity.class)));UpdateCheck.check(this,r->{if(r!=null)update.setText("Update available: "+r[0]);});
  TextView close=Ui.row(panel,440);close.setText("Close");close.setOnClickListener(v->finish());
  FrameLayout root=new FrameLayout(this);FrameLayout.LayoutParams lp=new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT,Gravity.BOTTOM|Gravity.END);lp.setMargins(0,0,Ui.dp(this,36),Ui.dp(this,36));android.widget.ScrollView sc=new android.widget.ScrollView(this); sc.setFillViewport(false); sc.addView(panel); root.addView(sc,lp);setContentView(root);timerRow.requestFocus(); }
 @Override protected void onResume(){super.onResume();choice=currentChoice();show();}
 private int currentChoice(){long end=Timer.end(this);if(end==0)return 0;long left=(end-System.currentTimeMillis()+59999)/60000;for(int i=0;i<Timer.MINUTES.length;i++)if(Timer.MINUTES[i]>=left)return i+1;return Timer.MINUTES.length;}
 private void step(int d){choice=Math.max(0,Math.min(Timer.MINUTES.length,choice+d));if(choice==0)Timer.cancel(this);else Timer.start(this,Timer.MINUTES[choice-1]);show();}
 private void brightness(int d){SharedPreferences p=getSharedPreferences("overlay",0);int dim=p.getInt("dim",0);dim=Math.max(0,Math.min(95,dim-d*5));p.edit().putInt("dim",dim).apply();SleepService.refresh();show();}
 private void show(){long end=Timer.end(this);if(!Timer.canSleep())timerRow.setText("Timer service is off");else if(Timer.paused(this))timerRow.setText("Timer: Paused ("+((Timer.remaining(this)+59999)/60000)+" min)");else if(end==0)timerRow.setText("Timer: Off   ◀ ▶");else timerRow.setText("Timer: "+((end-System.currentTimeMillis()+59999)/60000)+" min   ◀ ▶");brightnessRow.setText("Brightness: "+(100-getSharedPreferences("overlay",0).getInt("dim",0))+"%   ◀ ▶");filterRow.setText("Blue light filter: "+getSharedPreferences("overlay",0).getInt("filter",0)+"%   ◀ ▶");boolean active=end!=0||Timer.paused(this);pauseRow.setVisibility(active?View.VISIBLE:View.GONE);pauseRow.setText(Timer.paused(this)?"Resume timer":"Pause timer");cancelRow.setVisibility(active?View.VISIBLE:View.GONE);}
}