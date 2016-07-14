package com.nuvoton.nuplayer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.view.LayoutInflaterCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.nuvoton.socketmanager.ReadConfigure;

public class TutorialActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ReadConfigure configure = ReadConfigure.getInstance(this);
        setContentView(R.layout.activity_tutorial);

        ViewPager viewPager = (ViewPager) findViewById(R.id.tutorialPager);
        viewPager.setAdapter(new CustomPagerAdapter(this));
    }

    public class CustomPagerAdapter extends PagerAdapter{
        private  Context mContext;

        public CustomPagerAdapter(Context context){
            mContext = context;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            CustomPagerEnum customPagerEnum = CustomPagerEnum.values()[position];
            return mContext.getString(customPagerEnum.getTitleResId());
        }

        @Override
        public int getCount() {
            return CustomPagerEnum.values().length;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            CustomPagerEnum customPagerEnum = CustomPagerEnum.values()[position];
            LayoutInflater inflater = LayoutInflater.from(mContext);
            ViewGroup layout = (ViewGroup) inflater.inflate(customPagerEnum.getLayoutResId(), container, false);
            container.addView(layout);
            if (position == 4){
                Button button = (Button) findViewById(R.id.exit_tutorial);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                       finish();
                    }
                });
            }
            return layout;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }


    }

    public enum CustomPagerEnum{

        PLATFORM(R.string.pager_platform, R.layout.pager_platform),
        SELECT(R.string.pager_select, R.layout.pager_select),
        STREAMING(R.string.pager_streaming, R.layout.pager_streaming),
        FILES(R.string.pager_files, R.layout.pager_files),
        SETTINGS(R.string.pager_settings, R.layout.pager_settings);

        private int mTitleResId, mLayoutResId;
        CustomPagerEnum(int titleResId, int layoutResId){
            mTitleResId = titleResId;
            mLayoutResId = layoutResId;
        }
        public int getTitleResId(){
            return mTitleResId;
        }
        public int getLayoutResId(){
            return mLayoutResId;
        }
    }


}
