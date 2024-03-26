package com.example.opensourceprj;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

public class CustomDialog extends Dialog {
    private CustomDialogInterface customDialogInterface;

    public TextView txt_content;
    private Button btn_cancel;
    private Button btn_accept;

    // setDialogListener()로 클릭 시 발생 이벤트 override 가능
    interface CustomDialogInterface{
        void cancelClicked();
        void acceptClicked();
    }

    public void setDialogListener(CustomDialogInterface customDialogInterface){
        this.customDialogInterface = customDialogInterface;
    }

    public CustomDialog(@NonNull Context context, String contents, String txt_btn_cancel, String txt_btn_accept) {
        super(context);
        setContentView(R.layout.activity_custom_dialog);

        txt_content = findViewById(R.id.Text_content);
        txt_content.setText(contents);

        btn_cancel = findViewById(R.id.Dlg_btn_cancel);
        btn_cancel.setText(txt_btn_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(customDialogInterface != null) {
                    customDialogInterface.cancelClicked();
                }
                dismiss();
            }
        });

        btn_accept = findViewById(R.id.Dlg_btn_accept);
        btn_accept.setText(txt_btn_accept);
        btn_accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(customDialogInterface != null) {
                    customDialogInterface.acceptClicked();
                }
                dismiss();
            }
        });
    }
}
