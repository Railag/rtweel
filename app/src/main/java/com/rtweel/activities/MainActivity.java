package com.rtweel.activities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.rtweel.R;
import com.rtweel.fragments.LoginFragment;
import com.rtweel.fragments.ProfileFragment;
import com.rtweel.fragments.SendTweetFragment;
import com.rtweel.fragments.SettingsFragment;
import com.rtweel.fragments.TimelineFragment;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends ActionBarActivity {

    private FragmentManager mFragmentManager;

    private Fragment mCurrentFragment;

    private ProgressBar mLoadingBar;

    private DrawerLayout mDrawerLayout;

    private ListView mDrawerList;

    private ArrayList<String> mDrawerItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mFragmentManager = getFragmentManager();
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        initDrawer();

        mLoadingBar = (ProgressBar) findViewById(R.id.loading);

        setMainFragment(new LoginFragment());

    }

    private void initDrawer() {
        mDrawerItems = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.drawer_items)));
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_item, mDrawerItems));
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch(position) {
                    case 0:
                        setMainFragment(new ProfileFragment());
                        break;
                    case 1:
                        setMainFragment(new TimelineFragment());
                        break;
                    case 2:
                        setMainFragment(new SendTweetFragment());
                        break;
                    case 3:
                        setMainFragment(new SettingsFragment());
                        break;
                }

                mDrawerLayout.closeDrawers();
            }
        });
    }

    public void setMainFragment(final Fragment fragment) {

        final FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();

        if( !(fragment instanceof LoginFragment) )
            fragmentTransaction.addToBackStack(null);

        fragmentTransaction.replace(R.id.main_frame, fragment).commit();

        mCurrentFragment = fragment;

    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

//        else if (requestCode == REQUEST_FILE_SELECT) {
//            if (resultCode == RESULT_OK) {
//                Uri file = Uri.fromFile(new File(data
//                        .getStringExtra(Extras.FILE_URI)));
//                Log.i("DEBUG", file.toString());
//                Bitmap bitmap = BitmapFactory.decodeFile(data
//                        .getStringExtra(Extras.FILE_URI));
//                Log.i("DEBUG", data.getStringExtra(Extras.FILE_URI));
//                mTweetPicture.setImageBitmap(bitmap);
//            } else if (resultCode == RESULT_CANCELED) {
//                Toast.makeText(getApplicationContext(), "File choosing failed",
//                        Toast.LENGTH_LONG).show();
//            }
//        }
//        super.onActivityResult(requestCode, resultCode, data);
//    }

    @Override
    public void onBackPressed() {
            if(mFragmentManager.getBackStackEntryCount() == 1)
                super.onBackPressed();
            else
                mFragmentManager.popBackStackImmediate();
    }

    public ProgressBar getLoadingBar() {
        return mLoadingBar;
    }

    public boolean isLoggedIn() {
        return mCurrentFragment != null;
    }
}
