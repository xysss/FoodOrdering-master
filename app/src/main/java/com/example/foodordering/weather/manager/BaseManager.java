package com.example.foodordering.weather.manager;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.example.foodordering.weather.util.Constant;


/**
 * Created by xch on 2018/3/10.
 */
public abstract class BaseManager {

    protected Context _context;

    public BaseManager(Context context) {
        _context = context;
    }

    protected void sendEmptyMessage(Handler handler, int what) {
        if (handler != null)
            handler.sendEmptyMessage(what);
    }

    protected void sendMessage(Handler handler, Object value) {
        sendMessage(handler, value, true);
    }

    protected void sendMessage(Handler handler, Object value, boolean isSuccess) {
        Message message = new Message();
        if (isSuccess && value != null) {
            message.what = Constant.MSG_SUCCESS;
            message.obj = value;
        } else {
            message.what = Constant.MSG_ERROR;
        }
        handler.sendMessage(message);
    }

}
