package com.adbc.sdk.reward.test;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.adbc.sdk.greenp.v2.AdbcReward;
import com.adbc.sdk.greenp.v2.OfferwallBuilder;

import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class MainActivity extends FragmentActivity implements View.OnClickListener {

    private String appUserId = "someUser13";

    private OfferwallBuilder builder;

    private EditText titleText;
    private EditText editText;
    private Button initBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        titleText = findViewById(R.id.title);
        editText = findViewById(R.id.user_id);
        initBtn = findViewById(R.id.btn_init);

        initOfferwall();

        initBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String userId = editText.getText().toString();

                if(TextUtils.isEmpty(userId)) {
                    Toast.makeText(MainActivity.this, "app_uid 값을 입력해주세요", Toast.LENGTH_SHORT).show();
                } else {
                    appUserId = userId;
                    initOfferwall();
                }
            }
        });

        (findViewById(R.id.btn)).setOnClickListener(this);
        (findViewById(R.id.btn2)).setOnClickListener(this);
        (findViewById(R.id.btn3)).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        if(builder == null) {
            return;
        }

        String title = titleText.getText().toString();
        if(TextUtils.isEmpty(title))
            title = "greenP Offerwall";

        builder.setTitle(title); // 오퍼월 화면 타이틀영역 제목 ( 미 입력 시 '무료 충전소' )
        builder.setReferrer(appUserId); // 광고 참여 시 포스트백으로 전달 될 리퍼러 ( 매체 개별 활용값 )

        switch (view.getId()) {

            case R.id.btn:
                builder.showOfferwall(MainActivity.this);
                break;

            case R.id.btn2:
                builder.showOfferwallWithoutNewsFeed(MainActivity.this);
                break;

            case R.id.btn3:

                builder.requestOfferwallFragment(this, true, new OfferwallBuilder.OnRequestFragmentListener() {
                    @Override
                    public void onResult(boolean b, String s, Fragment fragment) {
                        if(b) {
                            loadFragment(fragment);
                        } else {
                            Toast.makeText(MainActivity.this, s, Toast.LENGTH_LONG).show();
                        }
                    }
                });

                break;
        }
    }

    public void loadFragment(Fragment fragment) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                changeCurFragment(fragment);
            }
        });
    }

    protected void changeCurFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container, fragment, fragment.getTag());
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commitAllowingStateLoss();
    }

    private void initOfferwall() {

        /**
         * @params
         *   - Context context
         *   - String appCode ( 발급받은 매체 코드 )
         *   - String userId ( 유저 구분을 위한 ID 값 - 매체 임의 생성 )
         *   - OnAdbcRewardListener initListener
         * */
        AdbcReward.init(getApplicationContext(), "ZBhFaS5kxE", appUserId, new AdbcReward.OnAdbcRewardListener() {
            @Override
            public void onResult(boolean result, String msg, OfferwallBuilder offerwallBuilder) {

                if(result) {
                    Toast.makeText(getBaseContext(), "SDK가 초기화 되었습니다.", Toast.LENGTH_LONG).show();
                    builder = offerwallBuilder;
                } else {
                    Toast.makeText(getBaseContext(), "SDK가 초기화 되지 않았습니다.", Toast.LENGTH_LONG).show();
                    Log.e("tag", msg);
                }
            }
        });
    }

    /** 암호화 된 유저 ID 생성 예제 */
    private String encId() {

        //byte[] bytes = (appUserId + ("adid")).getBytes();
        byte[] bytes = appUserId.getBytes();

        Checksum crc = new CRC32();
        crc.update(bytes, 0, bytes.length);
        return String.format("%08x", crc.getValue());
    }
}
