package com.whf.messagerelayer.service;

import android.app.IntentService;
import android.content.Intent;

import com.whf.messagerelayer.bean.Contact;
import com.whf.messagerelayer.confing.Constant;
import com.whf.messagerelayer.utils.EmailRelayerManager;
import com.whf.messagerelayer.utils.NativeDataManager;
import com.whf.messagerelayer.utils.SmsRelayerManager;
import com.whf.messagerelayer.utils.db.DataBaseManager;

import java.util.ArrayList;
import java.util.Set;

public class SmsService extends IntentService {

    private NativeDataManager mNativeDataManager;
    private DataBaseManager mDataBaseManager;

    public SmsService() {
        super("SmsService");
    }

    public SmsService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mNativeDataManager = new NativeDataManager(this);
        mDataBaseManager = new DataBaseManager(this);

        String mobile = intent.getStringExtra(Constant.EXTRA_MESSAGE_MOBILE);
        String content = intent.getStringExtra(Constant.EXTRA_MESSAGE_CONTENT);
        Set<String> keySet = mNativeDataManager.getKeywordSet();
        ArrayList<Contact> contactList = mDataBaseManager.getAllContact();
        //无转发规则
        if (keySet.size() == 0 && contactList.size() == 0) {
            relayMessage(mobile, content);
        } else if (keySet.size() != 0 && contactList.size() == 0) {//仅有关键字规则
            for (String key : keySet) {
                if (content.contains(key)) {
                    relayMessage(mobile, content);
                    break;
                }
            }
        } else if (keySet.size() == 0 && contactList.size() != 0) {//仅有手机号规则
            for (Contact contact : contactList) {
                if (contact.getContactNum().equals(mobile)) {
                    relayMessage(mobile, content);
                    break;
                }
            }
        } else {//两种规则共存
            out:
            for (Contact contact : contactList) {
                if (contact.getContactNum().equals(mobile)) {
                    for (String key : keySet) {
                        if (content.contains(key)) {
                            relayMessage(mobile, content);
                            break out;
                        }
                    }
                }
            }
        }
    }

    private void relayMessage(String mobile, String content) {
        String suffix = mNativeDataManager.getContentSuffix();
        String prefix = mNativeDataManager.getContentPrefix();
        if (suffix != null) {
            content = content + suffix;
        }
        if (prefix != null) {
            content = prefix + content;
        }
        if (mNativeDataManager.getSmsRelay()) {
            if (mobile == null || (mobile = mobile.trim()).length() == 0) {
                SmsRelayerManager.relaySms(mNativeDataManager, content);
            } else {
                SmsRelayerManager.relaySms(mNativeDataManager, content);
            }
        }
        if (mNativeDataManager.getEmailRelay()) {
            if (mobile == null || (mobile = mobile.trim()).length() == 0) {
                EmailRelayerManager.relayEmail(mNativeDataManager, content);
            } else {
                EmailRelayerManager.relayEmail(mNativeDataManager, content);
            }
        }
    }

    @Override
    public void onDestroy() {
        mDataBaseManager.closeHelper();
        super.onDestroy();
    }
}
