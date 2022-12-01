package com.example.ramen_cooking;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {
    private FloatingActionButton fabReset; // RESET 버튼
    private TextView tvMinute, tvSecond, tvMillis; // 분, 초, 밀리초
    private TextView ramen_Rc; // 스낵면 조리법
    private Button order_Bt; // ORDER 버튼
    private Button inq_Status; // INQUIRY 버튼
    private boolean isPlaying; // (isPlaying 변수 값) 반전시키기
    private long orderTime; // ORDER 버튼 누른 시점 (혹은 RESTART 누른 시점)
    private long elapsedTime; // 최신측정시간 (ORDER 버튼 누른 이후 현재까지 경과시간)
    private long totalElapsedTime; // 전체측정시간 (PAUSE 버튼 누를 때 elapsedTime 누적)
    private long updateTime; // 화면에 표시할 시간
    private int millis; // 화면에 표시할 밀리초
    private int sec; // 화면에 표시할 초
    private int min; // 화면에 표시할 분
    private int endTime1 = 4; // 타이머 종료 시간 : 4분
    private int endTime2 = 30; // 타이머 종료 시간 : 30초

    // final Handler 선언 및 생성
    final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // findViewById(리소스 Id)를 이용하여 바인딩하기
        fabReset = findViewById(R.id.fabReset);
        order_Bt = findViewById(R.id.order_Ramen);
        inq_Status = findViewById(R.id.inq_Status);
        ramen_Rc = findViewById(R.id.ramen_Recipe);
        tvMillis = findViewById(R.id.tvMillis);
        tvSecond = findViewById(R.id.tvSecond);
        tvMinute = findViewById(R.id.tvMinute);

        // INQUIRY 이벤트 처리
        inq_Status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inquiry();

            }

        });

        // RESET 이벤트 처리
        fabReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reset();

            }

        });

        // ORDER 이벤트 처리
        order_Bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isPlaying = !isPlaying; // 타이머 : ORDER 버튼 이벤트 처리
                // 한 번 클릭 시
                if (isPlaying) {
                    order(); // ORDER 기능 활성화

                    // 두 번 클릭 시
                } else {
                    pause(); // PAUSE 기능 활성화

                }

            }

        });

    }

    // INQUIRY 기능
    @SuppressLint("SetTextI18n")
    private void inquiry() {
        // INQUIRY 버튼 클릭 시 : 라면의 조리 상태 및 순서를 즉시 알려줌.
        handler.removeCallbacks(timeUpdater); // timerUpdater 핸들러 중지
        order_Bt.setText("START");
        // 스낵면의 조리 상태와 순서를 토스트 메시지로 표시
        Toast inquiryMessage = Toast.makeText(getApplicationContext(),
                "스낵면의 조리 상태는 위와 같습니다.", Toast.LENGTH_SHORT);
        inquiryMessage.show();

    }

    // RESET 기능
    @SuppressLint("SetTextI18n")
    private void reset() {
        // RESET 버튼 클릭 시 : PAUSE => RESTART
        order_Bt.setText("RESTART");
        ramen_Rc.setText("조리 시작");
        // RESET 버튼 클릭 시 : 모든 값 초기화
        totalElapsedTime = 0L;
        millis = 0;
        sec = 0;
        min = 0;
        isPlaying = false;
        handler.removeCallbacks(timeUpdater);
        handler.post(new Runnable() {
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {
                tvMinute.setText("00");
                tvSecond.setText(".00");
                tvMillis.setText(".00");

            }

        });

    }

    // ORDER 기능
    @SuppressLint("SetTextI18n")
    private void order() {
        // order_Bt 버튼 클릭 시 : order -> pause
        order_Bt.setText("PAUSE");
        // 부팅 이후 경과시간 ms
        orderTime = SystemClock.uptimeMillis();
        // UI 업데이트 시작
        handler.post(timeUpdater);

    }

    // 1. 물 넣기
    @SuppressLint("SetTextI18n")
    private void fillWithWater() {
        ramen_Rc.setText("1. 냄비에 물 500ml를 넣으세요.");

    }

    // 2. 물 끓이기
    private void boilWithWater() {
        ramen_Rc.setText("2. 물을 2분 동안 끓이세요.");

    }

    // 경과 시간 측정 및 UI 업데이트
    Runnable timeUpdater = new Runnable() {
        @SuppressLint({"DefaultLocale", "SetTextI18n"})
        @Override
        public void run() {
            // 주문 시작 또는 재시작 후 시간이 얼마나 경과했는지 측정
            elapsedTime = SystemClock.uptimeMillis() - orderTime;

            // 마지막 누적측정시간에 최신측정시간을 더해서 화면에 표시할 시간을 계산
            updateTime = totalElapsedTime + elapsedTime;

            // 1. millis 표시
            millis = (int) (updateTime % 1000) / 10; // 0.01초까지만 표시
            tvMillis.setText(String.format(".%02d", millis));

            // 2. second 표시
            sec = (int) (updateTime % 3600000 % 60000) / 1000;
            tvSecond.setText(String.format(".%02d", sec));

            // 3. minute 표시
            min = (int) (updateTime % 3600000) / 60000;
            tvMinute.setText(String.format("%02d", min));

            // 시간 재측정 및 UI 업데이트 요청. 무한루프 개념
            handler.post(this);

            if (min == 0 && sec == 3) { // 라면 주문 후 3초가 지나면
                // 1. 물 넣기
                fillWithWater();

            }

            if (min == 0 && sec == 10) { // 라면 주문 후 10초가 되면
                // 2. 물 끓이기
                boilWithWater();

            }

            if (min == 2 && sec == 10) { // 타이머 시간이 2분 10초가 되면
                // 3. 분말스프 넣기
                fillPowderSoup();

            }

            if (min == 2 && sec == 15) { // 타이머 시간이 2분 15초가 되면
                // 4. 면 넣기
                fillNoodle();

            }

            if (min == 2 && sec == 25) { // 타이머 시간이 2분 25초가 되면
                // 5. 김치, 파, 계란 등 원하는 재료 넣기
                fillWithAnything();

            }

            if (min == 2 && sec == 30) { // 타이머 시간이 2분 30초가 되면
                // 6. 라면을 더 끓이기
                ramen_Rc.setText("6. 라면을 2분 동안 더 끓이세요.");

            }

            // 스낵면 조리 타이머 종료 시간 = 4분 30초
            if (min == endTime1 && sec == endTime2) {
                handler.removeCallbacks(timeUpdater); // timerUpdater 핸들러 중지
                tvMillis.setText(".00");
                order_Bt.setText("RESTART");

                final Toast maintainMessage = Toast.makeText(getBaseContext(),
                        "스낵면 조리가 끝났습니다. 맛있게 드세요~~~", Toast.LENGTH_SHORT);

                maintainMessage.show();

                // 토스트 메세지가 Activity 강제 종료 이후에 매우 짧게 나타나므로,
                // 카운트다운타이머를 이용하여 토스트 메세지 시간을 연장함.
                new CountDownTimer(10000, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        maintainMessage.show();
                    }

                    @Override
                    public void onFinish() {
                        maintainMessage.show();
                    }
                }.start();
                // 스낵면 완성
                finish(); // 타이머는 바로 Activity 강제 종료

            }

        }

    };

    // 3. 분말스프 넣기
    private void fillPowderSoup() {
        ramen_Rc.setText("3. 먼저, 분말스프를 넣으세요.");

    }

    // 4. 면 넣기
    private void fillNoodle() {
        ramen_Rc.setText("4. 지금 면을 추가로 넣으세요.");

    }

    // 5. 김치, 파, 계란 등 원하는 재료 넣기
    private void fillWithAnything() {
        ramen_Rc.setText("5. 원하는 재료를 더 넣으세요.");

    }

    // PAUSE 기능
    @SuppressLint("SetTextI18n")
    private void pause() {
        order_Bt.setText("START");
        // 전체측정시간 누적
        totalElapsedTime += elapsedTime;
        // timerUpdater 핸들러 중지
        handler.removeCallbacks(timeUpdater);

    }

}
