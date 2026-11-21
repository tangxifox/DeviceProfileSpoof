package com.deviceprofile.spoof;
import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.window.OnBackInvokedCallback;
import android.window.OnBackInvokedDispatcher;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.animation.DecelerateInterpolator;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Bundle;
import android.os.Build;
import android.graphics.Insets;
import android.text.InputType;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import io.github.libxposed.service.XposedService;

public final class SettingsActivity extends Activity {
  private final EditText[] fields=new EditText[Config.KEYS.length];
  private SharedPreferences prefs;
  private TextView status;
  private Button saveButton;
  private FrameLayout root;
  private TextView snackbar;
  private int onTint,barColor,green,greenInk,greenMark;
  private FrameLayout content;
  private LinearLayout editor,footer,scopePage,scopeRows;
  private View bottomNav,navDivider;
  private TextView scopeSummary;
  private boolean scopeOpen;
  private OnBackInvokedCallback scopeBack;
  private ScrollView overviewScroll,editorScroll;
  private TextView connectionTitle,connectionDetail,apiLine,scopeDetail,profileDetail;
  private TextView[] navLabels=new TextView[2];
  private Icon[] navIcons=new Icon[2];
  private View statusCard, statusIcon;
  private int tab;
  private boolean connected;
  private Bridge bridge;
  private boolean initialized;
  private final Runnable bindingChanged=()->runOnUiThread(this::refreshBinding);
  private int surface,card,primary,onPrimary,text,muted,outline,tint,error;
  private int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}
  @Override public void onCreate(Bundle state){
    boolean dark=(getResources().getConfiguration().uiMode&Configuration.UI_MODE_NIGHT_MASK)==Configuration.UI_MODE_NIGHT_YES;
    setTheme(getResources().getIdentifier(dark?"Theme.Material.NoActionBar":"Theme.Material.Light.NoActionBar","style","android"));
    super.onCreate(state);
    surface=dark?0xff151515:0xfff5f5f5;
    card=dark?0xff232323:0xffffffff;
    barColor=surface;
    primary=dark?0xffe6e6e6:0xff161616;
    onPrimary=dark?0xff151515:0xffffffff;
    text=dark?0xfff4f4f4:0xff090909;
    muted=dark?0xffb1b1b1:0xff646464;
    outline=dark?0xff525252:0xffdcdcdc;
    tint=dark?0xff323232:0xffeeeeee;
    onTint=text;
    green=dark?0xff163729:0xffdcf9e6;
    greenInk=dark?0xffe0f9e8:0xff08190e;
    greenMark=dark?0xff50d582:0xff2fc75f;
    error=dark?0xffffb4ab:0xffa02323;
    if(Build.VERSION.SDK_INT>=30){
      getWindow().setDecorFitsSystemWindows(false);
      getWindow().setStatusBarColor(Color.TRANSPARENT);
      getWindow().setNavigationBarColor(Color.TRANSPARENT);
    }else{
      getWindow().setStatusBarColor(surface);getWindow().setNavigationBarColor(barColor);
      if(!dark)getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
    }
    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE|WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    bridge=(Bridge)getApplication();
    root=new FrameLayout(this); root.setBackgroundColor(surface); setContentView(root);
    if(Build.VERSION.SDK_INT>=30){
      root.setOnApplyWindowInsetsListener((view,insets)->{
        Insets bars=insets.getInsets(WindowInsets.Type.systemBars()|WindowInsets.Type.displayCutout());
        Insets keyboard=insets.getInsets(WindowInsets.Type.ime());
        view.setPadding(bars.left,bars.top,bars.right,Math.max(bars.bottom,keyboard.bottom));
        return WindowInsets.CONSUMED;
      });
      root.post(()->{
        WindowInsetsController controller=root.getWindowInsetsController();
        if(controller!=null){
          int flags=WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS|WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS;
          controller.setSystemBarsAppearance(dark?0:flags,flags);
        }
      });
      root.requestApplyInsets();
    }
    LinearLayout layout=column(); root.addView(layout,new FrameLayout.LayoutParams(-1,-1));
    content=new FrameLayout(this);layout.addView(content,new LinearLayout.LayoutParams(-1,0,1));
    overviewScroll=new ScrollView(this); overviewScroll.setFillViewport(true); overviewScroll.setVerticalScrollBarEnabled(false);
    content.addView(overviewScroll,new FrameLayout.LayoutParams(-1,-1));
    LinearLayout overview=column(); overview.setPadding(dp(14),dp(64),dp(14),dp(18)); overviewScroll.addView(overview);
    title(overview,"机型伪装");
    FrameLayout hero=new FrameLayout(this); statusCard=hero; hero.setBackground(bg(tint,20,0)); hero.setClipToOutline(true); add(overview,hero,18);
    Icon mark=new Icon(2,greenMark); statusIcon=mark;
    FrameLayout.LayoutParams markParams=new FrameLayout.LayoutParams(dp(112),dp(112),Gravity.BOTTOM|Gravity.RIGHT);
    markParams.rightMargin=-dp(21); markParams.bottomMargin=-dp(28); hero.addView(mark,markParams);
    LinearLayout summary=column(); summary.setPadding(dp(18),dp(17),dp(76),dp(14)); hero.addView(summary,new FrameLayout.LayoutParams(-1,-2));
    connectionTitle=label("等待连接",26,text); connectionTitle.setTypeface(Typeface.create("sans-serif-medium",0)); add(summary,connectionTitle,0);
    connectionDetail=label("LSPosed",16,text); add(summary,connectionDetail,2);
    apiLine=label("API 102",17,text); add(summary,apiLine,16);
    LinearLayout details=column();details.setBackground(bg(card,20,0));details.setPadding(0,dp(7),0,dp(7));add(overview,details,14);
    detail(details,"应用版本","1.0.1");
    detail(details,"系统设备",android.os.Build.MANUFACTURER+" "+android.os.Build.MODEL);
    profileDetail=detail(details,"已保存的伪装机型",Config.DEFAULTS[2]);
    scopeDetail=detail(details,"应用作用域   ›","点按查看已选应用");
    View scopeRow=(View)scopeDetail.getParent();
    scopeRow.setFocusable(true);
    scopeRow.setBackground(new RippleDrawable(ColorStateList.valueOf(0x22080808),null,bg(Color.WHITE,0,0)));
    scopeRow.setOnClickListener(v->showScope());
    LinearLayout shortcut=column();shortcut.setBackground(bg(card,20,0));add(overview,shortcut,14);
    detail(shortcut,"设备参数   ›","编辑制造商、品牌、型号、产品名称和设备代号"); shortcut.setOnClickListener(v->showTab(1));
    editor=column();content.addView(editor,new FrameLayout.LayoutParams(-1,-1));
    editorScroll=new ScrollView(this); editorScroll.setFillViewport(true); editorScroll.setVerticalScrollBarEnabled(false);
    editor.addView(editorScroll,new LinearLayout.LayoutParams(-1,0,1));
    LinearLayout page=column(); page.setPadding(dp(14),dp(44),dp(14),dp(20)); page.setFocusableInTouchMode(true); editorScroll.addView(page); page.requestFocus();
    title(page,"设备参数");
    LinearLayout heading=new LinearLayout(this); heading.setGravity(Gravity.CENTER_VERTICAL); heading.setPadding(dp(6),0,dp(2),0); add(page,heading,12);
    TextView section=label("设备配置",18,text); section.setTypeface(Typeface.create("sans-serif-medium",0)); heading.addView(section,new LinearLayout.LayoutParams(0,-2,1));
    Button reset=button("恢复预设",false); heading.addView(reset,new LinearLayout.LayoutParams(-2,-2));
    LinearLayout form=column(); add(page,form,4);
    for(int i=0;i<fields.length;i++){
      int index=i;
      LinearLayout row=column(); row.setPadding(dp(16),dp(8),dp(16),dp(6)); row.setBackground(segment(index,card,0));
      TextView fieldLabel=label(Config.LABELS[i]+"  ·  "+Config.key(i),12,muted); fieldLabel.setTypeface(Typeface.create("sans-serif-medium",0)); add(row,fieldLabel,0);
      EditText input=new EditText(this); fields[i]=input; input.setId(3000+i); input.setSingleLine();
      input.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS); input.setImeOptions(i==fields.length-1?6:5);
      input.setTextSize(17); input.setTextColor(text); input.setPadding(0,0,0,0); input.setMinHeight(dp(32)); input.setMinimumHeight(dp(32)); input.setBackground(null); input.setText(Config.DEFAULTS[i]);
      input.setOnFocusChangeListener((view,focus)->{row.setBackground(segment(index,focus?tint:card,focus?primary:0)); fieldLabel.setTextColor(focus?onTint:muted);});
      fieldLabel.setLabelFor(input.getId()); row.setOnClickListener(v->{input.requestFocus(); ((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).showSoftInput(input,InputMethodManager.SHOW_IMPLICIT);});
      add(row,input,0); add(form,row,i==0?0:3);
    }
    footer=column(); footer.setBackgroundColor(barColor); footer.setPadding(dp(16),dp(8),dp(16),dp(9)); editor.addView(footer,new LinearLayout.LayoutParams(-1,-2));
    saveButton=button("保存设备参数",true); add(footer,saveButton,0);
    status=label("保存后重新打开目标应用",12,muted); status.setGravity(Gravity.CENTER); add(footer,status,5);
    saveButton.setOnClickListener(v->save(page));
    reset.setOnClickListener(v->{for(int i=0;i<fields.length;i++){fields[i].setText(Config.DEFAULTS[i]);fields[i].setError(null);}snack("已填入小米 14 预设，保存后应用");});
    View rule=new View(this);rule.setBackgroundColor(outline);layout.addView(rule,new LinearLayout.LayoutParams(-1,1));
    LinearLayout nav=new LinearLayout(this);nav.setBackgroundColor(surface);layout.addView(nav,new LinearLayout.LayoutParams(-1,dp(60)));
    bottomNav=nav;navDivider=rule;
    navItem(nav,0,"概览");navItem(nav,1,"设置");
    showTab(state==null?0:state.getInt("tab"));
    initialized=state!=null;
    bridge.listeners.add(bindingChanged);
    refreshBinding();
    if(state!=null && state.getBoolean("scopeOpen"))showScope();
  }
  @Override protected void onSaveInstanceState(Bundle state){state.putInt("tab",tab);state.putBoolean("scopeOpen",scopeOpen);super.onSaveInstanceState(state);}
  private void title(LinearLayout parent,String value){TextView t=label(value,36,text);t.setPadding(dp(14),0,dp(10),0);add(parent,t,0);}
  private TextView detail(LinearLayout parent,String title,String value){LinearLayout row=column();row.setPadding(dp(18),dp(10),dp(18),dp(13));
    TextView name=label(title,18,text);name.setTypeface(Typeface.create("sans-serif-medium",0));add(row,name,0);
    TextView val=label(value,15,muted);add(row,val,4);add(parent,row,0);return val;}
  private void navItem(LinearLayout nav,int index,String name){LinearLayout item=column();item.setGravity(Gravity.CENTER);nav.addView(item,new LinearLayout.LayoutParams(0,-1,1));
    item.setContentDescription(name); item.setFocusable(true);item.setBackground(new RippleDrawable(ColorStateList.valueOf(0x22080808),null,bg(0xffffffff,0,0)));
    Icon icon=new Icon(index,muted);navIcons[index]=icon;item.addView(icon,new LinearLayout.LayoutParams(dp(25),dp(25)));
    TextView title=label(name,12,muted);navLabels[index]=title;LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-2,-2);lp.topMargin=dp(3);item.addView(title,lp);
    item.setOnClickListener(v->showTab(index));}
  private void showTab(int index){tab=index;overviewScroll.setVisibility(index==0?View.VISIBLE:View.GONE);editor.setVisibility(index==1?View.VISIBLE:View.GONE);
    if(index==0)((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(content.getWindowToken(),0);
    for(int i=0;i<navLabels.length;i++){boolean selected=index==i;navLabels[i].setTextColor(selected?text:muted);navLabels[i].setTypeface(Typeface.create("sans-serif",selected?Typeface.BOLD:Typeface.NORMAL));navIcons[i].shade=selected?text:muted;navIcons[i].invalidate();((View)navLabels[i].getParent()).setSelected(selected);}}
  @Override protected void onResume(){super.onResume();if(bridge!=null)refreshBinding();}
  private void showScope(){
    if(scopeOpen)return;
    scopeOpen=true;
    scopePage=column();scopePage.setBackgroundColor(surface);content.addView(scopePage,new FrameLayout.LayoutParams(-1,-1));
    overviewScroll.setVisibility(View.GONE);editor.setVisibility(View.GONE);bottomNav.setVisibility(View.GONE);navDivider.setVisibility(View.GONE);
    LinearLayout toolbar=new LinearLayout(this);toolbar.setGravity(Gravity.CENTER_VERTICAL);toolbar.setPadding(dp(12),dp(6),dp(16),dp(6));add(scopePage,toolbar,0);
    Button back=button("‹",false);back.setTextSize(28);back.setPadding(0,0,0,0);back.setContentDescription("返回概览");toolbar.addView(back,new LinearLayout.LayoutParams(dp(48),dp(48)));
    TextView title=label("应用作用域",22,text);title.setTypeface(Typeface.create("sans-serif-medium",0));
    LinearLayout.LayoutParams tp=new LinearLayout.LayoutParams(-2,-2);tp.leftMargin=dp(12);toolbar.addView(title,tp);back.setOnClickListener(v->closeScope());
    scopeSummary=label("",14,muted);scopeSummary.setPadding(dp(20),dp(6),dp(20),dp(12));add(scopePage,scopeSummary,0);
    ScrollView scroll=new ScrollView(this);scopePage.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
    scopeRows=column();scopeRows.setPadding(dp(14),0,dp(14),dp(16));scroll.addView(scopeRows);
    if(Build.VERSION.SDK_INT>=33){scopeBack=this::closeScope;getOnBackInvokedDispatcher().registerOnBackInvokedCallback(OnBackInvokedDispatcher.PRIORITY_DEFAULT,scopeBack);}
    renderScope();
  }
  private void closeScope(){
    if(!scopeOpen)return;
    if(Build.VERSION.SDK_INT>=33 && scopeBack!=null){getOnBackInvokedDispatcher().unregisterOnBackInvokedCallback(scopeBack);scopeBack=null;}
    scopeOpen=false;content.removeView(scopePage);scopePage=null;scopeRows=null;scopeSummary=null;
    bottomNav.setVisibility(View.VISIBLE);navDivider.setVisibility(View.VISIBLE);showTab(0);refreshBinding();
  }
  @Override public void onBackPressed(){if(scopeOpen)closeScope();else super.onBackPressed();}
  private void renderScope(){
    scopeRows.removeAllViews();
    XposedService service=bridge.service;
    if(service==null){scopeSummary.setText("尚未连接 LSPosed，请确认模块已启用。");return;}
    final List<String> packages;
    try{packages=new ArrayList<>(service.getScope());}catch(RuntimeException e){scopeSummary.setText("暂时无法读取作用域，请返回后重试。");return;}
    packages.removeAll(Collections.singleton(null));
    Collections.sort(packages,String.CASE_INSENSITIVE_ORDER);
    scopeSummary.setText(packages.isEmpty()?"目前没有勾选应用 · 在 LSPosed 中添加":"已勾选 "+packages.size()+" 个应用 · 在 LSPosed 中管理");
    PackageManager pm=getPackageManager();
    for(String pkg:packages){
      String name=pkg;
      Drawable icon=pm.getDefaultActivityIcon();
      try{
        ApplicationInfo info=pm.getApplicationInfo("system".equals(pkg)?"android":pkg,0);
        CharSequence appName=info.loadLabel(pm);
        if(appName!=null && appName.length()>0)name=appName.toString();
        Drawable loaded=info.loadIcon(pm);if(loaded!=null)icon=loaded;
      }catch(PackageManager.NameNotFoundException | RuntimeException ignored){}
      if("system".equals(pkg)||"android".equals(pkg))name="系统框架";
      LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(14),dp(12),dp(14),dp(12));row.setMinimumHeight(dp(70));row.setBackground(bg(card,16,0));
      ImageView image=new ImageView(this);image.setImageDrawable(icon);image.setScaleType(ImageView.ScaleType.FIT_CENTER);image.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);row.addView(image,new LinearLayout.LayoutParams(dp(44),dp(44)));
      LinearLayout labels=column();LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1);lp.leftMargin=dp(14);row.addView(labels,lp);
      TextView nameView=label(name,16,text);nameView.setTypeface(Typeface.create("sans-serif-medium",0));add(labels,nameView,0);
      if(!name.equals(pkg))add(labels,label(pkg,13,muted),3);
      add(scopeRows,row,scopeRows.getChildCount()==0?0:6);
    }
  }
  @Override protected void onDestroy(){
    if(Build.VERSION.SDK_INT>=33 && scopeBack!=null)getOnBackInvokedDispatcher().unregisterOnBackInvokedCallback(scopeBack);
    bridge.listeners.remove(bindingChanged);super.onDestroy();
  }
  private void refreshBinding(){
    if(isDestroyed())return;
    XposedService service=bridge.service;
    prefs=null;
    int api=0; String framework="LSPosed"; String scope="点按查看已选应用";
    if(service!=null){
      try{
        api=service.getApiVersion();
        if(api>=102){
          prefs=service.getRemotePreferences(Config.FILE);
          if(!initialized){
            SharedPreferences local=getSharedPreferences(Config.FILE,MODE_PRIVATE);
            for(int i=0;i<fields.length;i++){
              String val=prefs.getString(Config.KEYS[i],local.getString(Config.KEYS[i],Config.DEFAULTS[i]));
              fields[i].setText(Config.valid(val)?val:Config.DEFAULTS[i]);
            }
            initialized=true;
          }
          try{framework=service.getFrameworkVersion()+" ("+service.getFrameworkVersionCode()+")";}catch(RuntimeException ignored){}
          try{scope="已选 "+service.getScope().size()+" 个应用 · 点按查看";}catch(RuntimeException ignored){}
          message("保存后，请彻底退出并重新打开目标应用。",false);
        }
      }catch(RuntimeException e){prefs=null;}
    }
    connected=prefs!=null;
    statusCard.setBackground(bg(connected?green:tint,20,0));statusIcon.setVisibility(connected?View.VISIBLE:View.INVISIBLE);
    connectionTitle.setText(connected?"已连接":"未连接");connectionDetail.setText(framework);apiLine.setText(api>0?"API "+api:"需要 API 102");
    for(TextView v:new TextView[]{connectionTitle,connectionDetail,apiLine})v.setTextColor(connected?greenInk:text);
    scopeDetail.setText(scope);
    if(scopeOpen)renderScope();
    if(connected)profileDetail.setText(prefs.getString(Config.KEYS[2],Config.DEFAULTS[2]));
    saveButton.setEnabled(prefs!=null);saveButton.setAlpha(prefs==null?.45f:1f);
    if(prefs==null){message("请在 LSPosed 启用本模块；若刚升级，请重启目标应用。",true);}
  }
  private void save(View page){
    if(prefs==null)return;
    SharedPreferences.Editor edit=prefs.edit();
    for(int i=0;i<fields.length;i++){
      String value=fields[i].getText().toString().trim();
      if(!Config.valid(value)){fields[i].requestFocus();fields[i].setError("请输入非空值，最多 91 个 UTF-8 字节");return;}
      edit.putString(Config.KEYS[i],value);
    }
    try{if(!edit.commit()){message("保存失败，请重试。",true);return;}}catch(RuntimeException e){message("与 LSPosed 的连接已断开，请重试。",true);return;}
    ((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(page.getWindowToken(),0); page.requestFocus();
    profileDetail.setText(fields[2].getText().toString().trim());
    message("已保存。彻底退出并重新打开目标应用后生效。",false); snack("设备参数已保存");
  }
  private void message(String v,boolean bad){status.setTextColor(bad?error:muted);status.setText(v);}
  private TextView label(String s,int size,int shade){TextView v=new TextView(this);v.setText(s);v.setTextSize(size);v.setTextColor(shade);return v;}
  private GradientDrawable bg(int fill,int radius,int stroke){GradientDrawable d=new GradientDrawable();d.setColor(fill);d.setCornerRadius(dp(radius));if(stroke!=0)d.setStroke(dp(1),stroke);return d;}
  private void add(LinearLayout parent,View child,int top){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.topMargin=dp(top);parent.addView(child,p);}
  private LinearLayout column(){LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);return box;}
  private int system(String group,int shade,int fallback){int id=getResources().getIdentifier("system_"+group+"_"+shade,"color","android");return id==0?fallback:getColor(id);}
  private GradientDrawable segment(int index,int fill,int stroke){
    GradientDrawable d=bg(fill,0,stroke);float top=dp(index==0?24:6),bottom=dp(index==fields.length-1?24:6);
    d.setCornerRadii(new float[]{top,top,top,top,bottom,bottom,bottom,bottom});return d;
  }
  private void snack(String value){
    if(snackbar!=null)root.removeView(snackbar);
    TextView note=label(value,14,surface);snackbar=note;note.setPadding(dp(16),dp(12),dp(16),dp(12));note.setBackground(bg(text,16,0));note.setElevation(dp(7));note.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
    FrameLayout.LayoutParams p=new FrameLayout.LayoutParams(-1,-2,Gravity.BOTTOM);p.setMargins(dp(16),0,dp(16),dp(106));root.addView(note,p);note.setAlpha(0); note.setTranslationY(dp(8));note.animate().alpha(1).translationY(0).setDuration(190).setInterpolator(new DecelerateInterpolator()).start();
    note.postDelayed(()->{if(snackbar==note){root.removeView(note);snackbar=null;}},3000);
  }
  private Button button(String title,boolean solid){
    Button b=new Button(this);b.setText(title);b.setAllCaps(false);b.setTextSize(14); b.setMinHeight(dp(48));b.setMinimumHeight(dp(48));b.setPadding(dp(16),0,dp(16),0);b.setTypeface(Typeface.create("sans-serif-medium",0));b.setTextColor(solid?onPrimary:onTint);b.setStateListAnimator(null);b.setBackgroundTintList(null);
    b.setBackground(new RippleDrawable(ColorStateList.valueOf(0x30ffffff),bg(solid?primary:tint,28,0),null));return b;
  }
  private final class Icon extends View {
    final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG); final int kind; int shade;
    Icon(int kind,int shade){super(SettingsActivity.this);this.kind=kind;this.shade=shade;setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}
    @Override protected void onDraw(Canvas c){super.onDraw(c);c.save();c.scale(getWidth()/24f,getHeight()/24f);paint.setColor(shade);
      if(kind==2){paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(2.1f);paint.setStrokeCap(Paint.Cap.ROUND);paint.setStrokeJoin(Paint.Join.ROUND);c.drawCircle(12,12,10.5f,paint);
        android.graphics.Path check=new android.graphics.Path();check.moveTo(7,12);check.lineTo(10.4f,15.4f);check.lineTo(17.5f,8.4f);c.drawPath(check,paint);
      }else if(kind==0){paint.setStyle(Paint.Style.FILL);android.graphics.Path house=new android.graphics.Path();house.moveTo(1,11);house.lineTo(12,2);house.lineTo(23,11);house.lineTo(20,11);house.lineTo(20,22);house.lineTo(14,22);house.lineTo(14,15);house.lineTo(10,15);house.lineTo(10,22);house.lineTo(4,22);house.lineTo(4,11);house.close();c.drawPath(house,paint);
      }else{paint.setStyle(Paint.Style.FILL);for(int i=0;i<8;i++){c.save();c.rotate(i*45,12,12);c.drawRoundRect(9.8f,1,14.2f,7,1,1,paint);c.restore();}c.drawCircle(12,12,8,paint);paint.setColor(surface);c.drawCircle(12,12,3,paint);}
      c.restore();}
  }
}
