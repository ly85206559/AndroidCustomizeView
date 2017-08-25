package com.android.customview.activity;

import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.android.customview.R;
import com.android.customview.widget.MessageBubble;

/**
 * Created by Ben.li on 2017/8/18.
 */

public class MessageBubbleActivity extends AppCompatActivity {

    private MessageBubble messageBubble;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_message_bubble);
        messageBubble = (MessageBubble) findViewById(R.id.messageBubble);
        messageBubble.setText("99+");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void onClick(View view) {
        if (view.getId() == R.id.btn_reset) {
            messageBubble.reset();
        }
    }
}
