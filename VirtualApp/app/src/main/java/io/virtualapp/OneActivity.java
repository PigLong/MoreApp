package io.virtualapp;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.os.VUserInfo;
import com.lody.virtual.os.VUserManager;
import com.lody.virtual.remote.InstallResult;
import java.util.Timer;
import java.util.TimerTask;
import io.virtualapp.home.LoadingActivity;
/*
 * Created by Zxl on 2017/10/26.
 */
public class OneActivity extends AppCompatActivity {
    String pg = "com.tencent.mm";
    PackageManager packageManager;
    TextView tv_go;
    int userId = 1;
    MyHandler handler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String app_Name = getResources().getString(R.string.app_name);
        userId = Integer.parseInt(app_Name.substring(2));

        tv_go = (TextView) this.findViewById(R.id.tv_go);

        if (handler == null){
            handler = new MyHandler();
        }
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                cancel();
                go();
            }
        },200,200);
    }
    void go(){
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        packageManager = getPackageManager();
                        try {
                            PackageInfo packageInfo = packageManager.getPackageInfo(pg,0);
                            boolean isInstalled = VirtualCore.get().isAppInstalledAsUser(0,pg);
                            Message message = new Message();
                            if (isInstalled){
                                handler.sendEmptyMessage(0);
                            }else {
                                VUserManager.get().createUser(userId+"", VUserInfo.FLAG_ADMIN);
                                InstallResult installResult = VirtualCore.get().installPackage(packageInfo.applicationInfo.publicSourceDir,0);
                                if (installResult.isSuccess){
                                    handler.sendEmptyMessage(0);
                                }else {
                                    Log.e("zz",installResult.error);
                                    message.obj = installResult.error;
                                    message.what = 1;
                                    handler.sendMessage(message);
                                }
                            }
                        } catch (Exception e) {
                            Message message = new Message();
                            message.obj = e.toString();
                            message.what = 1;
                            handler.sendMessage(message);
                            e.printStackTrace();
                        }
                    }
                }
        ).start();

    }

    class MyHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0:
                    LoadingActivity.launch(OneActivity.this,pg,0);
                    break;
                case 1:
                    tv_go.setText(msg.obj.toString());
                    break;
            }
        }
    }
}
