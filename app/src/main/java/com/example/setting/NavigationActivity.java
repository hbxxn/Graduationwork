package com.example.setting;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

public class NavigationActivity extends AppCompatActivity {

    private ViewPager sliderViewPager;
    private LinearLayout dotIndicator;
    private ViewPagerAdapter viewPagerAdapter;
    private Button backBtn, nextBtn;
    private TextView[] dots;

    private ViewPager.OnPageChangeListener viewPagerListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

        @Override
        public void onPageSelected(int position) {
            setDotIndicator(position);

            if (position > 0) {
                backBtn.setVisibility(View.VISIBLE);
            } else {
                backBtn.setVisibility(View.INVISIBLE);
            }

            if (position == 3) {
                nextBtn.setText("시작하기");
            } else {
                nextBtn.setText("다음");
            }

            // Update tutorial page status in the User object (Myapp)
            Myapp myApp = (Myapp) getApplicationContext();
            User currentUser = myApp.getCurrentUser();
            if (currentUser != null) {
                currentUser.setTutorialPage(position);  // 현재 튜토리얼 페이지 저장
                myApp.setCurrentUser(currentUser);      // myApp에 다시 저장
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_navigation);

        backBtn = findViewById(R.id.Back);
        nextBtn = findViewById(R.id.Next);

        backBtn.setOnClickListener(view -> {
            if (getItem(-1) >= 0) {
                sliderViewPager.setCurrentItem(getItem(-1), true);
            }
        });

        nextBtn.setOnClickListener(view -> {
            if (getItem(1) < 4) {
                sliderViewPager.setCurrentItem(getItem(1), true);
            } else {
                // 튜토리얼 완료 상태를 User 객체와 SharedPreferences에 저장
                Myapp myApp = (Myapp) getApplicationContext();
                User currentUser = myApp.getCurrentUser();
                if (currentUser != null) {
                    currentUser.setTutorialCompleted(true);  // 튜토리얼 완료 상태 업데이트
                    myApp.setCurrentUser(currentUser);

                    // 튜토리얼 완료 상태를 SharedPreferences에 저장
                    String userId = currentUser.getUserId();
                    PreferenceManager.saveTutorialCompleted(this, userId, true);
                }

                // 메인 액티비티로 이동
                Intent intent = new Intent(NavigationActivity.this, main_hadanbar.class);
                startActivity(intent);
                finish();
            }
        });

        sliderViewPager = findViewById(R.id.slideViewPager);
        dotIndicator = findViewById(R.id.dotIndicator);

        viewPagerAdapter = new ViewPagerAdapter(this);
        sliderViewPager.setAdapter(viewPagerAdapter);
        sliderViewPager.addOnPageChangeListener(viewPagerListener);

        setDotIndicator(0);

        // 튜토리얼 페이지를 User 객체에서 불러와서 초기화
        Myapp myApp = (Myapp) getApplicationContext();
        User currentUser = myApp.getCurrentUser();
        if (currentUser != null) {
            sliderViewPager.setCurrentItem(currentUser.getTutorialPage());  // 마지막 페이지로 이동
        }
    }

    public void setDotIndicator(int position) {
        dots = new TextView[4];
        dotIndicator.removeAllViews();

        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226", Html.FROM_HTML_MODE_LEGACY));
            dots[i].setTextSize(35);
            dots[i].setTextColor(getResources().getColor(R.color.grey, getApplicationContext().getTheme()));
            dotIndicator.addView(dots[i]);
        }
        dots[position].setTextColor(getResources().getColor(R.color.main_purple, getApplicationContext().getTheme()));
    }

    private int getItem(int i) {
        return sliderViewPager.getCurrentItem() + i;
    }
}
