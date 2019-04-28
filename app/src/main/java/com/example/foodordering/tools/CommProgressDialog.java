package com.example.foodordering.tools;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.view.Gravity;
import android.view.KeyEvent;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.foodordering.R;

/**
 * @author xch  2018-03-20
 */
public class CommProgressDialog extends Dialog {
    private Context context = null;
    private int anim=0;
    private static CommProgressDialog commProgressDialog = null;

    public CommProgressDialog(Context context){
        super(context);
        this.context = context;
    }

    public CommProgressDialog(Context context, int theme, int anim) {
        super(context, theme);
        this.anim=anim;
    }

    public static CommProgressDialog createDialog(Context context, int anim){
        commProgressDialog = new CommProgressDialog(context, R.style.CommProgressDialog ,anim);
        commProgressDialog.setContentView(R.layout.comm_progress_dialog);
        commProgressDialog.setCanceledOnTouchOutside(false);
        commProgressDialog.getWindow().getAttributes().gravity = Gravity.CENTER;

        return commProgressDialog;
    }



    public void onWindowFocusChanged(boolean hasFocus){

        if (commProgressDialog == null){
            return;
        }

        ImageView imageView = (ImageView) commProgressDialog.findViewById(R.id.iv_loading);
        if(anim!=0) {
            imageView.setBackgroundResource(anim);
        }
        AnimationDrawable animationDrawable = (AnimationDrawable) imageView.getBackground();
        animationDrawable.start();
    }

    /**
     * 设置标题
     * @param strTitle
     * @return
     */
    public CommProgressDialog setTitile(String strTitle){
        return commProgressDialog;
    }

    /**
     * 设置提示内容
     * @param strMessage
     * @return
     */
    public CommProgressDialog setMessage(String strMessage){
        TextView tvMsg = (TextView)commProgressDialog.findViewById(R.id.tv_loading_msg);

        if (tvMsg != null){
            tvMsg.setText(strMessage);
        }

        return commProgressDialog;
    }

    /**屏蔽返回键**/
    @Override
    public boolean onKeyDown(int keyCode,KeyEvent event){
        if(keyCode== KeyEvent.KEYCODE_BACK)
            return true;
        return super.onKeyDown(keyCode, event);
    }
}
