package app.sleepscreen.tv;
import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

public class OverlaySettingsActivity extends Activity {
 SharedPreferences prefs; TextView show,pos,opacityLabel,dimLabel,filterLabel; String[] names={"Top left","Top right","Bottom left","Bottom right","Center"};
 protected void onCreate(Bundle b){
  super.onCreate(b); prefs=getSharedPreferences("overlay",0);
  LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(Ui.dp(this,48),Ui.dp(this,30),Ui.dp(this,48),Ui.dp(this,30));root.setBackgroundColor(0xff0b0d12);
  TextView title=Ui.text(this,32,Color.WHITE);title.setText("Timer display settings");root.addView(title);
  TextView help=Ui.text(this,16,0xffbbbbbb);help.setText("Use Left and Right to adjust sliders. Press Back to close.");root.addView(help);
  show=Ui.row(root,700);pos=Ui.row(root,700);
  show.setOnClickListener(v->{prefs.edit().putBoolean("show",!prefs.getBoolean("show",false)).apply();render();});
  pos.setOnClickListener(v->{prefs.edit().putInt("position",(prefs.getInt("position",1)+1)%5).apply();render();});
  opacityLabel=Ui.text(this,20,Color.WHITE);opacityLabel.setPadding(0,Ui.dp(this,20),0,0);root.addView(opacityLabel);
  SeekBar opacity=new SeekBar(this);opacity.setMax(8);opacity.setKeyProgressIncrement(1);opacity.setProgress((prefs.getInt("opacity",80)-20)/10);root.addView(opacity,new LinearLayout.LayoutParams(-1,Ui.dp(this,48)));
  dimLabel=Ui.text(this,20,Color.WHITE);dimLabel.setPadding(0,Ui.dp(this,12),0,0);root.addView(dimLabel);
  SeekBar dim=new SeekBar(this);dim.setMax(19);dim.setKeyProgressIncrement(1);dim.setProgress((95-prefs.getInt("dim",0))/5);root.addView(dim,new LinearLayout.LayoutParams(-1,Ui.dp(this,48)));
  opacity.setOnSeekBarChangeListener(listener("opacity",20));dim.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){ public void onProgressChanged(SeekBar bar,int value,boolean fromUser){if(fromUser){prefs.edit().putInt("dim",95-value*5).apply();render();}} public void onStartTrackingTouch(SeekBar bar){} public void onStopTrackingTouch(SeekBar bar){} });
  filterLabel=Ui.text(this,20,Color.WHITE);filterLabel.setPadding(0,Ui.dp(this,12),0,0);root.addView(filterLabel);
  SeekBar filter=new SeekBar(this);filter.setMax(20);filter.setKeyProgressIncrement(1);filter.setProgress(prefs.getInt("filter",0)/5);root.addView(filter,new LinearLayout.LayoutParams(-1,Ui.dp(this,48)));
  filter.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onProgressChanged(SeekBar bar,int value,boolean fromUser){if(fromUser){prefs.edit().putInt("filter",value*5).apply();render();}}public void onStartTrackingTouch(SeekBar bar){}public void onStopTrackingTouch(SeekBar bar){}});
  ScrollView sc=new ScrollView(this);sc.setFillViewport(true);sc.addView(root);setContentView(sc);render();show.requestFocus();
 }
 SeekBar.OnSeekBarChangeListener listener(String key,int base){return new SeekBar.OnSeekBarChangeListener(){public void onProgressChanged(SeekBar bar,int value,boolean fromUser){if(fromUser){prefs.edit().putInt(key,base+value*10).apply();render();}}public void onStartTrackingTouch(SeekBar bar){}public void onStopTrackingTouch(SeekBar bar){}};}
 void render(){show.setText("Countdown overlay: "+(prefs.getBoolean("show",false)?"On":"Off"));pos.setText("Position: "+names[Math.max(0,Math.min(4,prefs.getInt("position",1)))]);opacityLabel.setText("Overlay opacity: "+prefs.getInt("opacity",80)+"%");dimLabel.setText("Brightness: "+(100-prefs.getInt("dim",0))+"%");if(filterLabel!=null)filterLabel.setText("Blue light filter: "+prefs.getInt("filter",0)+"%");SleepService.refresh();}
}