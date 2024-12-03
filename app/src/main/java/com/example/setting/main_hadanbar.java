package com.example.setting;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class main_hadanbar extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;

    Fragment home;
    Fragment indoor;
    Fragment window;
    Fragment setting;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_hadanbar);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        home = new HomeFragment();
        indoor = new IndoorFragment();
        window = new WindowFragment();
        setting = new SettingFragment();


        // 처음 앱이 시작될 때 HomeFragment를 표시합니다.
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, home)
                    .commit();
        }



        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                int itemId = item.getItemId();
                Fragment fragment = null;

                if(itemId == R.id.i_onlyhome){
                    fragment = home;


                }else if(itemId == R.id.i_onlycamera){
                    fragment = indoor;


                }else if(itemId == R.id.i_onlywindow){
                    fragment = window;


                }else if(itemId == R.id.i_onlysetting){
                    fragment = setting;


                }

                return loadFragment(fragment) ;
            }
            boolean loadFragment(Fragment fragment){
                if(fragment != null){
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, fragment)
                            .commit();
                    return true;
                }else{
                    return false;
                }
            }
        });


    }

}