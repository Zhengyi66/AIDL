package com.aidl.client;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.aidl.service.IMsgManager;
import com.aidl.service.IReceiveMsgListener;
import com.aidl.service.Msg;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity {

    private IMsgManager myBinder;//定义AIDL
    private ListView mListView;
    private TextView mSendCountTv, mReceiveCountTv;
    private List<Msg> mMsgs = new ArrayList<>();
    private ListAdapter mAdapter;
    private IMsgManager mIMsgManager;
    private Msg mMsg;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    mAdapter.notifyDataSetChanged();
                    mListView.smoothScrollToPosition(mMsgs.size() - 1);
                    mReceiveCount++;
                    mReceiveCountTv.setText("收到返回次数：" + mReceiveCount);
                    break;
                case 2:
                    mSendCountTv.setText("发送消息次数：" + mSendCount);
                    break;
            }
        }
    };
    EditText mEditText;
    private int mSendCount = 0, mReceiveCount = 0;
    ScheduledExecutorService service;
    ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            myBinder = IMsgManager.Stub.asInterface(iBinder);
            IMsgManager msgManager = IMsgManager.Stub.asInterface(iBinder);
            mIMsgManager = msgManager;
            try {
                //链接到死亡代理
                mIMsgManager.asBinder().linkToDeath(mDeathRecipient, 0);
                //注册监听
                mIMsgManager.registerReceiveListener(mReceiveMsgListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mListView = (ListView) findViewById(R.id.listview);
        mEditText = (EditText) findViewById(R.id.edit_text);
        mSendCountTv = (TextView) findViewById(R.id.send_count_tv);
        mReceiveCountTv = (TextView) findViewById(R.id.receive_count_tv);

        mAdapter = new ListAdapter(this, mMsgs);
        mListView.setAdapter(mAdapter);
        mMsg = new Msg("");

        Intent intent = new Intent();
        //跨进程通信需要使用action启动
        intent.setAction("com.aidl.service.MyService");

        //android5.0之后，如果servicer不在同一个App的包中，需要设置service所在程序的包名
        intent.setPackage("com.aidl.service");
        //开启Service
        bindService(intent, mConnection, BIND_AUTO_CREATE);

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String msg = mEditText.getText().toString().trim();
                    if (TextUtils.isEmpty(msg)) {
                        Toast.makeText(MainActivity.this, "消息不能为空", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    mMsg.setMsg(msg);
                    myBinder.sendMsg(mMsg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });

        findViewById(R.id.btn_loop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (service == null){
                    service = Executors.newScheduledThreadPool(2);
                    service.scheduleAtFixedRate(mRunnable, 1, 1, TimeUnit.SECONDS);
                }
            }
        });
    }

    //回调监听
    private IReceiveMsgListener mReceiveMsgListener = new IReceiveMsgListener.Stub() {

        @Override
        public void onReceive(Msg msg) throws RemoteException {
            msg.setTime(System.currentTimeMillis());
            if (mMsgs.size() > 100) {
                mMsgs.clear();
            }
            mMsgs.add(msg);
            mHandler.sendEmptyMessage(1);
        }
    };

    private IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        /**
         * 当承载IBinder的进程消失时接收回调的接口
         */
        @Override
        public void binderDied() {
            if (null == mIMsgManager) {
                return;
            }
            mIMsgManager.asBinder().unlinkToDeath(mDeathRecipient, 0);
            mIMsgManager = null;
            //在这里重新绑定远程服务
        }
    };

    @Override
    protected void onDestroy() {
        if (null != mIMsgManager && mIMsgManager.asBinder().isBinderAlive()) {
            try {
                mIMsgManager.unregisterReceiveListener(mReceiveMsgListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        unbindService(mConnection);
        super.onDestroy();
    }

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            mSendCount++;
            mHandler.sendEmptyMessage(2);
            try {
                mMsg.setMsg(mSendCount + "只羊");
                myBinder.sendMsg(mMsg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    };
}
