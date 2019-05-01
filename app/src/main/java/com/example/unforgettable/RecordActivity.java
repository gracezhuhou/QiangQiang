package com.example.unforgettable;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;

import org.litepal.LitePal;

import java.util.ArrayList;

public class RecordActivity extends Fragment {
    // 前端相关变量
    private Button submitButton;
    private EditText sourceInput;
    private EditText authorInput;
    private EditText headingInput;
    private Button typeButton;
    private Button cameraButton;
    private Button soundButton;
    private Button starButton;
    private EditText contentInput;


    // 数据库相关变量
    private Dbhelper dbhelper = new Dbhelper();
    private String source;  // 来源
    private String author;  // 作者
    private String heading; // 正面 标题
    private String content; // 背面 内容
    private boolean like = false;   // 收藏
    private String tab;     // 标签

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        View view = inflater.inflate(R.layout.activity_record, container, false);
        LitePal.initialize(this.getActivity());   // 初始化数据库

        //初始化SDK
        // 5cc9274e为申请的 APPID
        SpeechUtility.createUtility(this.getActivity(), SpeechConstant.APPID +"=5cc9274e");


        //设置id
        submitButton = view.findViewById(R.id.submitButton);
        sourceInput = view.findViewById(R.id.sourceInput);
        authorInput = view.findViewById(R.id.authorInput);
        headingInput = view.findViewById(R.id.headingInput);
        typeButton = view.findViewById(R.id.typeButton);
        cameraButton = view.findViewById(R.id.cameraButton);
        soundButton = view.findViewById(R.id.soundButton);
        starButton = view.findViewById(R.id.starButton);
        contentInput = view.findViewById(R.id.contentInput);

        return view;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            //now invisible to user
            Log.v("记录界面", "页面隐藏");
        } else {
            //now visible to user
            Log.v("记录界面", "刷新页面");
        }
    }


    //确认键监听
    //控件的点击事件写在onActivityCreated中
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // 提交按钮监听
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getInput(); //获取用户输入内容

                // TODO: 选择标签
                tab = "英语"; //暂时

                dbhelper.addCard(source, author, heading, content, like, tab);  //添加记录
                // TODO: 清空页面
                sourceInput.setText("");
                authorInput.setText("");
                headingInput.setText("");
                contentInput.setText("");
            }
        });
        // 收藏按钮响应
        starButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                // TODO: 改收藏按键颜色状态  @大冬瓜 @母后
                String starText = (String)starButton.getText();
                if (starText.equals("❤")) {
                    Drawable drawable = getResources().getDrawable(R.drawable.ic_star_yel);
                    // 这一步必须要做,否则不会显示.
                    drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                    starButton.setCompoundDrawables(null, null, drawable, null);
                    starButton.setText("已收藏"); //暂时
                    starButton.setTextColor(Color.argb(0, 0, 255, 0));
                    like = true;
                }
                else {
                    Drawable drawable = getResources().getDrawable(R.drawable.ic_star_black);
                    // 这一步必须要做,否则不会显示.
                    drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                    starButton.setCompoundDrawables(null, null, drawable, null);
                    starButton.setText("❤"); //暂时
                    starButton.setTextColor(Color.argb(0, 0, 255, 0));
                    like = false;
                }
            }
        });

        soundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initSpeech(getActivity());
            }
        });
    }

    public void initSpeech(final Context context) {
        //1.创建RecognizerDialog对象
        RecognizerDialog mDialog = new RecognizerDialog(context, null);
        //2.设置accent、language等参数
        mDialog.setParameter(SpeechConstant.LANGUAGE, "zh_cn");//语种，这里可以有zh_cn和en_us
        mDialog.setParameter(SpeechConstant.ACCENT, "mandarin");//设置口音，这里设置的是汉语普通话
        mDialog.setParameter(SpeechConstant.TEXT_ENCODING, "utf-8");//设置编码类型
        //3.设置回调接口
        mDialog.setListener(new RecognizerDialogListener() {
            @Override
            public void onResult(RecognizerResult recognizerResult, boolean isLast) {
                if (!isLast) {
                    //解析语音
                    //返回的result为识别后的汉字,直接赋值到TextView上即可
                    String result = parseVoice(recognizerResult.getResultString());
                    content = content + result;
                }
            }

            @Override
            public void onError(SpeechError speechError) {
                Log.e("返回的错误码", speechError.getErrorCode() + "");
            }
        });
        //4.显示dialog，接收语音输入
        mDialog.show();
    }

    /**
     * 解析语音json
     */
    public String parseVoice(String resultString) {
        Gson gson = new Gson();
        Voice voiceBean = gson.fromJson(resultString, Voice.class);

        StringBuffer sb = new StringBuffer();
        ArrayList<Voice.WSBean> ws = voiceBean.ws;
        for (Voice.WSBean wsBean : ws) {
            String word = wsBean.cw.get(0).w;
            sb.append(word);
        }
        return sb.toString();
    }


    /**
     * 语音对象封装
     */
    public class Voice {

        public ArrayList<WSBean> ws;

        public class WSBean {
            public ArrayList<CWBean> cw;
        }

        public class CWBean {
            public String w;
        }
    }


    //获取用户输入内容
    private void getInput(){
        source = sourceInput.getText().toString();
        author = authorInput.getText().toString();
        heading = headingInput.getText().toString();
        content = contentInput.getText().toString();
    }

}
