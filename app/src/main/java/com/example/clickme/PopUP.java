package com.example.clickme;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class PopUP extends LinearLayout {
    CameraActivity activity;
    Boolean pop_up_opened=false;
    View view;
    int align_left = RelativeLayout.ALIGN_LEFT;
    int align_right = RelativeLayout.ALIGN_RIGHT;
    int align_top = RelativeLayout.ALIGN_TOP;
    int align_bottom = RelativeLayout.ALIGN_BOTTOM;
    int left_of = RelativeLayout.LEFT_OF;
    int right_of = RelativeLayout.RIGHT_OF;
    int above = RelativeLayout.ABOVE;
    int below = RelativeLayout.BELOW;
    int align_parent_left = RelativeLayout.ALIGN_PARENT_LEFT;
    int align_parent_right = RelativeLayout.ALIGN_PARENT_RIGHT;
    int align_parent_top = RelativeLayout.ALIGN_PARENT_TOP;
    int align_parent_bottom = RelativeLayout.ALIGN_PARENT_BOTTOM;

    public PopUP(Context context) {
        super(context);
        activity= (CameraActivity) this.getContext();
        this.setOrientation(VERTICAL);
        if(pop_up_opened){
            closePopUp();
            return;
        }
        Toast.makeText(context,"welcome",Toast.LENGTH_SHORT).show();
        view = activity.findViewById(R.id.popup_container);
        RelativeLayout.LayoutParams layoutParams =
                (RelativeLayout.LayoutParams) view.getLayoutParams();

        layoutParams.addRule(align_parent_right);
        layoutParams.addRule(below, R.id.linearicons);
        layoutParams.addRule(above, 0);
        layoutParams.addRule(align_parent_top, 0);
        layoutParams.addRule(align_bottom,R.id.pop_up);
        view.setBackgroundColor(Color.BLUE);
//        view.setTranslationX(4.0f);
//        view.setTranslationY(0.0f);

        view.setFocusable(true);
        setFocusableInTouchMode(true);
        pop_up_opened=true;
        view.setLayoutParams(layoutParams);
    }

    public void closePopUp(){
        if(pop_up_opened) {
            ViewGroup view = activity.findViewById(R.id.popup_container);
            view.removeAllViews();
            pop_up_opened=false;
        }

    }
}
