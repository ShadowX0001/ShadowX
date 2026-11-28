package dev.shadowx.launcher;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.OpenableColumns;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/** ShadowX Launcher home. The game is started only after the player presses Play. */
public final class LauncherActivity extends Activity {
    static final String AFTER = "dev.shadowx.launcher.AFTER";
    static final String MESSAGE = "dev.shadowx.launcher.MESSAGE";
    private LauncherHome home;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        Intent intent = getIntent();
        final int after = intent == null ? 0 : intent.getIntExtra(AFTER, 0);
        Uri file = handedFile(intent);
        if (after != 0) {
            showRestarting(intent.getStringExtra(MESSAGE));
            new Thread(() -> {
                for (int i = 0; i < 100 && new File("/proc/" + after).exists(); i++) {
                    try { Thread.sleep(50); } catch (InterruptedException e) { break; }
                }
                runOnUiThread(this::play);
            }, "shadowx-restart").start();
        } else if (file != null) {
            AlertDialog copying = new AlertDialog.Builder(this,
                    android.R.style.Theme_DeviceDefault_Dialog_Alert)
                    .setMessage("Importing file into ShadowX…").setCancelable(false).show();
            receive(this, file, () -> {
                copying.dismiss();
                showHome();
            });
        } else if (StartupRecovery.interrupted(this)) {
            startActivity(new Intent(this, DiagnosticsActivity.class)
                    .putExtra(StartupRecovery.RECOVERY, true));
            finish();
        } else {
            showHome();
        }
    }

    private void showHome() {
        home = new LauncherHome(this);
        setContentView(home);
        hideBars();
    }

    private void play() {
        Intent game = new Intent(this, GameActivity.class);
        game.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(game);
        overridePendingTransition(0, 0);
        finish();
    }

    @SuppressWarnings("deprecation")
    private void hideBars() {
        View decor = getWindow().getDecorView();
        if (Build.VERSION.SDK_INT >= 30) {
            getWindow().setDecorFitsSystemWindows(false);
            WindowInsetsController bars = decor.getWindowInsetsController();
            if (bars != null) {
                bars.hide(WindowInsets.Type.statusBars());
                bars.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
    }

    @Override public void onBackPressed() {
        if (home != null && home.page != 0) {
            home.page = 0;
            home.invalidate();
        } else {
            super.onBackPressed();
        }
    }

    private void showRestarting(String message) {
        setContentView(new RestartView(this,
                message != null && !message.isEmpty() ? message : "Starting ShadowX…",
                restartLook(this)));
        hideBars();
    }

    private static int[] restartLook(Context context) {
        int[] look = {0xFF10051F, 0xFF1E1A2B, 0xFF5A5468, 0xFF9C22FF,
                0xFFF5F2FC, 0xFFA39BBF, 0xFF2C2839};
        File file = new File(context.getFilesDir(), "restart-look");
        try (FileInputStream in = new FileInputStream(file)) {
            byte[] bytes = new byte[256];
            int n = in.read(bytes);
            String[] parts = new String(bytes, 0, Math.max(n, 0), "UTF-8").trim().split(" ");
            if (parts.length == look.length) {
                int[] read = new int[look.length];
                for (int i = 0; i < read.length; i++) read[i] = (int) Long.parseLong(parts[i], 16);
                return read;
            }
        } catch (Exception ignored) { }
        return look;
    }

    /** Touch-friendly ShadowX home screen, drawn natively to avoid a WebView dependency. */
    private final class LauncherHome extends View {
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF r = new RectF();
        private final float density;
        private int page = 0;
        private float playTop, playBottom, navTop;
        private float downX, downY;
        private final int bg = 0xFF0A0613, panel = 0xFF171024, panel2 = 0xFF211333;
        private final int purple = 0xFF9C35FF, bright = 0xFFE8D7FF, muted = 0xFFAA9BBE;

        LauncherHome(Context c) {
            super(c);
            density = getResources().getDisplayMetrics().density;
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        private float d(float v) { return v * density; }
        private void box(Canvas c, float x, float y, float w, float h, float radius, int color) {
            p.setStyle(Paint.Style.FILL); p.setColor(color);
            r.set(x,y,x+w,y+h); c.drawRoundRect(r,radius,radius,p);
        }
        private void outline(Canvas c,float x,float y,float w,float h,float radius,int color) {
            p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(d(1)); p.setColor(color);
            r.set(x,y,x+w,y+h); c.drawRoundRect(r,radius,radius,p); p.setStyle(Paint.Style.FILL);
        }
        private void txt(Canvas c,String s,float x,float y,float size,int color,boolean bold) {
            p.setStyle(Paint.Style.FILL); p.setColor(color); p.setTextSize(d(size));
            p.setTypeface(Typeface.create("sans-serif",bold?Typeface.BOLD:Typeface.NORMAL));
            p.setTextAlign(Paint.Align.LEFT); c.drawText(s,x,y,p);
        }
        private void center(Canvas c,String s,float x,float y,float size,int color,boolean bold) {
            p.setTextSize(d(size)); p.setTypeface(Typeface.create("sans-serif",bold?Typeface.BOLD:Typeface.NORMAL));
            p.setTextAlign(Paint.Align.CENTER); p.setColor(color); c.drawText(s,x,y,p);
            p.setTextAlign(Paint.Align.LEFT);
        }
        private void crown(Canvas c,float cx,float cy,float size) {
            p.setColor(0xFFCE9AFF); p.setStyle(Paint.Style.FILL);
            android.graphics.Path path=new android.graphics.Path();
            path.moveTo(cx-size*.5f,cy+size*.2f); path.lineTo(cx-size*.42f,cy-size*.3f);
            path.lineTo(cx-size*.15f,cy-size*.03f); path.lineTo(cx,cy-size*.52f);
            path.lineTo(cx+size*.16f,cy-size*.03f); path.lineTo(cx+size*.43f,cy-size*.3f);
            path.lineTo(cx+size*.5f,cy+size*.2f); path.close(); c.drawPath(path,p);
            box(c,cx-size*.48f,cy+size*.2f,size*.96f,size*.13f,size*.04f,0xFF8D20EF);
        }
        @Override protected void onDraw(Canvas raw) {
            super.onDraw(raw);
            Canvas c=raw; float w=getWidth(),h=getHeight(),u=density;
            c.drawColor(bg);
            p.setShader(new android.graphics.LinearGradient(0,0,w,h,0xFF21003F,0xFF0A0613,android.graphics.Shader.TileMode.CLAMP));
            c.drawRect(0,0,w,h,p); p.setShader(null);
            float pad=d(22), top=d(30);
            // Brand header
            crown(c,pad+d(18),top+d(18),d(31));
            txt(c,"SHADOWX",pad+d(45),top+d(16),22*u,bright,true);
            txt(c,"LAUNCHER",pad+d(47),top+d(34),9*u,purple,true);
            box(c,w-d(82),top-d(1),d(60),d(29),d(15),panel2);
            center(c,"ONLINE",w-d(52),top+d(18),9*u,0xFF9DF58B,true);
            float y=top+d(62);
            if(page==0) {
                txt(c,"YOUR WORLD.",pad,y+d(12),12*u,muted,true);
                txt(c,"YOUR ADVENTURE.",pad,y+d(42),24*u,bright,true);
                float heroY=y+d(62), heroH=Math.min(d(210),h*.28f);
                box(c,pad,heroY,w-pad*2,heroH,d(22),panel);
                outline(c,pad,heroY,w-pad*2,heroH,d(22),0xFF54227A);
                // abstract block-world landscape
                box(c,pad+d(18),heroY+d(22),w-pad*2-d(36),heroH-d(44),d(16),0xFF24113D);
                float ground=heroY+heroH-d(58);
                box(c,pad+d(20),ground,w-pad*2-d(40),d(38),d(7),0xFF3C2054);
                box(c,pad+d(42),ground-d(24),d(52),d(24),d(5),0xFF552A79);
                box(c,pad+d(94),ground-d(45),d(52),d(45),d(5),0xFF7136A5);
                box(c,pad+d(146),ground-d(18),d(52),d(18),d(5),0xFF552A79);
                txt(c,"JAVA EDITION",pad+d(35),heroY+d(49),10*u,0xFFE2C9FF,true);
                txt(c,"Ready when you are.",pad+d(35),heroY+d(70),16*u,bright,true);
                txt(c,"Manage versions, mods and controls.",pad+d(35),heroY+d(88),10*u,muted,false);
                playTop=heroY+heroH+d(18); playBottom=playTop+d(58);
                box(c,pad,playTop,w-pad*2,d(58),d(17),0xFF8D25E8);
                center(c,"▶    PLAY MINECRAFT",w/2,playTop+d(36),16*u,0xFFFFFFFF,true);
                float cardsY=playBottom+d(18), gap=d(10), cardW=(w-pad*2-gap)/2;
                box(c,pad,cardsY,cardW,d(74),d(15),panel);
                box(c,pad+cardW+gap,cardsY,cardW,d(74),d(15),panel);
                txt(c,"▦  INSTANCES",pad+d(13),cardsY+d(28),11*u,bright,true);
                txt(c,"Game profiles",pad+d(13),cardsY+d(49),9*u,muted,false);
                txt(c,"✦  MODS",pad+cardW+gap+d(13),cardsY+d(28),11*u,bright,true);
                txt(c,"Customize gameplay",pad+cardW+gap+d(13),cardsY+d(49),9*u,muted,false);
                float infoY=cardsY+d(91);
                txt(c,"SHADOWX ENGINE",pad,infoY,9*u,purple,true);
                txt(c,"Touch controls  •  Controller ready",pad,infoY+d(19),10*u,muted,false);
                navTop=h-d(65);
            } else {
                String title=page==1?"INSTANCES":page==2?"MODS & PACKS":page==3?"SETTINGS":"ABOUT SHADOWX";
                txt(c,title,pad,y+d(28),23*u,bright,true);
                box(c,pad,y+d(50),w-pad*2,d(142),d(18),panel);
                txt(c,page==1?"Game profiles":page==2?"Your add-ons":page==3?"Launcher preferences":"Built for your adventures",pad+d(18),y+d(83),15*u,bright,true);
                txt(c,page==1?"Instance management is being integrated.":page==2?"Native mod support is available in the game.":page==3?"More launcher controls are coming in updates.":"ShadowX Launcher",pad+d(18),y+d(110),10*u,muted,false);
                txt(c,page==1?"Use Play to open the installed game.":page==2?"Import compatible files from Android share.":page==3?"Open Diagnostics to inspect startup issues.":"Powered by the Starling engine.",pad+d(18),y+d(130),10*u,muted,false);
                if(page==3) {
                    box(c,pad,y+d(210),w-pad*2,d(50),d(14),panel2);
                    txt(c,"RUN DIAGNOSTICS",pad+d(17),y+d(240),12*u,bright,true);
                }
                navTop=h-d(65);
            }
            box(c,0,navTop,w,h-navTop,0,panel);
            String[] labels={"HOME","INSTANCES","MODS","SETTINGS"};
            String[] symbols={"⌂","▦","✦","⚙"};
            for(int i=0;i<4;i++) {
                float cx=w*(i+.5f)/4f;
                int col=page==i?0xFFC88AFF:muted;
                center(c,symbols[i],cx,navTop+d(25),19*u,col,true);
                center(c,labels[i],cx,navTop+d(45),8*u,col,true);
            }
        }
        @Override public boolean onTouchEvent(MotionEvent e) {
            float x=e.getX(),y=e.getY();
            if(e.getAction()==MotionEvent.ACTION_DOWN){downX=x;downY=y;return true;}
            if(e.getAction()==MotionEvent.ACTION_UP) {
                float w=getWidth(),h=getHeight(),pad=d(22);
                if(page==0 && downY>=playTop-d(8) && downY<=playBottom+d(8)) { play(); return true; }
                if(page==3 && downY> d(230) && downY<d(310)) {
                    startActivity(new Intent(LauncherActivity.this,DiagnosticsActivity.class)); return true;
                }
                if(y>=navTop) {
                    int selected=Math.min(3,Math.max(0,(int)(x/(w/4f))));
                    page=selected; invalidate(); return true;
                }
                if(page!=0 && downY<d(100)) { page=0; invalidate(); return true; }
                if(page==0 && downY>h-d(160)) { page=x<w/2?1:2; invalidate(); return true; }
                performClick(); return true;
            }
            return true;
        }
        @Override public boolean performClick(){super.performClick();return true;}
    }

    /** Restart transition retains the game's dark look while a new process starts. */
    private static final class RestartView extends View {
        private final String message; private final int[] look;
        private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG); private final RectF rect=new RectF();
        private final long start=SystemClock.uptimeMillis();
        RestartView(Context c,String m,int[] l){super(c);message=m;look=l;}
        private void rounded(Canvas c,float x,float y,float w,float h,float radius,int color){
            paint.setStyle(Paint.Style.FILL);paint.setColor(color);rect.set(x,y,x+w,y+h);c.drawRoundRect(rect,radius,radius,paint);
        }
        @Override protected void onDraw(Canvas c){
            float w=getWidth(),h=getHeight(),cx=w/2; c.drawColor(look[0]);
            paint.setColor(look[3]);paint.setTextAlign(Paint.Align.CENTER);
            paint.setTypeface(Typeface.create("sans-serif",Typeface.BOLD));paint.setTextSize(30);
            c.drawText("SHADOWX",cx,h*.43f,paint);
            paint.setTextSize(13);paint.setColor(look[5]);c.drawText("MINECRAFT  •  ANDROID EDITION",cx,h*.43f+28,paint);
            float bw=Math.min(w*.55f,300),bx=cx-bw/2,by=h*.55f;
            rounded(c,bx,by,bw,6,3,look[6]);
            float phase=.5f-.5f*(float)Math.cos((SystemClock.uptimeMillis()-start)/1000*Math.PI);
            rounded(c,bx+(bw*.7f)*phase,by,bw*.3f,6,3,look[3]);
            paint.setTextSize(12);paint.setColor(look[5]);c.drawText(message,cx,by+32,paint);
            postInvalidateOnAnimation();
        }
    }

    @SuppressWarnings("deprecation")
    private static Uri handedFile(Intent intent) {
        if(intent==null)return null;
        if(Intent.ACTION_VIEW.equals(intent.getAction()))return intent.getData();
        if(Intent.ACTION_SEND.equals(intent.getAction()))return intent.getParcelableExtra(Intent.EXTRA_STREAM);
        return null;
    }
    static void receive(final Activity activity,final Uri file,final Runnable done) {
        final File inbox=new File(activity.getFilesDir(),"import-inbox");
        final String name=displayName(activity,file);
        new Thread(() -> {
            inbox.mkdirs();File part=new File(inbox,name+".part");
            try(InputStream in=activity.getContentResolver().openInputStream(file);
                OutputStream out=new FileOutputStream(part)){
                if(in==null)throw new java.io.IOException("nothing to read");
                byte[] buffer=new byte[1<<16];for(int n;(n=in.read(buffer))>0;)out.write(buffer,0,n);
            }catch(Exception e){part.delete();android.util.Log.w("shadowx","could not receive "+name,e);}
            if(part.exists()&&!part.renameTo(new File(inbox,name)))part.delete();
            if(done!=null)activity.runOnUiThread(done);
        },"shadowx-receive").start();
    }
    private static String displayName(Context context,Uri file) {
        String name=null;
        try(Cursor cursor=context.getContentResolver().query(file,new String[]{OpenableColumns.DISPLAY_NAME},null,null,null)){
            if(cursor!=null&&cursor.moveToFirst())name=cursor.getString(0);
        }catch(Exception ignored){}
        if(name==null)name=file.getLastPathSegment();
        if(name==null||name.isEmpty())name="imported";
        name=name.replaceAll("[/\\\\:*?\"<>|]","_");
        return name.endsWith(".part")?name+"_":name;
    }
}
