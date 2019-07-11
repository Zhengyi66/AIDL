// IReceiveMsgListener.aidl
package com.aidl.service;
import com.aidl.service.Msg;
// Declare any non-default types here with import statements

interface IReceiveMsgListener {
   void onReceive(in Msg msg);
}
