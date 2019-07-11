// IMsg.aidl
package com.aidl.service;
import com.aidl.service.IReceiveMsgListener;
import com.aidl.service.Msg;
// Declare any non-default types here with import statements

interface IMsgManager {
   void sendMsg(in Msg msg);
   void registerReceiveListener(IReceiveMsgListener receiveListener);
   void unregisterReceiveListener(IReceiveMsgListener receiveListener);
}
