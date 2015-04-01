package com.rtweel.fragments;

import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.rtweel.R;
import com.rtweel.cache.App;
import com.rtweel.constant.Extras;
import com.rtweel.filechooser.FileAdapter;
import com.rtweel.filechooser.Line;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by root on 22.3.15.
 */
public class FileFragment extends BaseFragment {
    private File mCurrentPath;

    private FileAdapter mAdapter;

    private ListView list;

    private String mTweetText;

    private Pattern pattern = Pattern.compile("^.*\\.(png|gif|jpg|jpeg|bmp)$");
    private Matcher matcher;

    @Override
    public void onStart() {
        super.onStart();

        Bundle args = getArguments();
        if (args != null) {
            mTweetText = args.getString(Extras.TWEET_TEXT);
        }

        mCurrentPath = Environment.getExternalStorageDirectory();
        initialize(mCurrentPath);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_file, null);
        setTitle(getString(R.string.title_file));
        list = (ListView) v.findViewById(R.id.file_list);
        return v;
    }

    private void initialize(File startPath) {

        File[] paths = startPath.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (pathname.isDirectory())
                    return true;
                matcher = pattern.matcher(pathname.getPath());
                return matcher.matches();
            }
        });

        getActionBar().setTitle("Current Dir: " + startPath.getName());
        List<Line> pathList = new ArrayList<Line>();
        List<Line> fileList = new ArrayList<Line>();
        if (paths != null && paths.length > 0) {
            try {
                for (File path : paths) {
                    if (path.isDirectory())
                        pathList.add(new Line(path.getName(), "Folder", path
                                .getAbsolutePath()));
                    else {
                        fileList.add(new Line(path.getName(), "File Size: "
                                + path.length(), path.getAbsolutePath()));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            Collections.sort(pathList);
            Collections.sort(fileList);
            pathList.addAll(fileList);
        }
        if (!startPath.getName().contains("sdcard") && startPath.getParent() != null) {
            pathList.add(0,
                    new Line("..", "Parent Directory", startPath.getParent()));
        }
        mAdapter = new FileAdapter(getActivity(), R.layout.file, pathList);
        list.setAdapter(mAdapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Line line = mAdapter.getItem(position);
                if (line.getData().equalsIgnoreCase("folder")
                        || line.getData().equalsIgnoreCase("parent directory")) {
                    mCurrentPath = new File(line.getPath());
                    initialize(mCurrentPath);
                } else {
                    onFileClick(line);
                }

            }
        });

    }

    private void onFileClick(Line line) {
        final String path = line.getPath();
        new Thread(new Runnable() {

            @Override
            public void run() {
                File file = new File(Environment.getExternalStorageDirectory()
                        + App.PHOTO_PATH + ".jpg");
                FileOutputStream output = null;
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    output = new FileOutputStream(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                FileInputStream input = null;
                try {
                    input = new FileInputStream(new File(path));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                byte[] buf = new byte[1024];
                int lenght;
                try {
                    while ((lenght = input.read(buf)) > 0) {
                        output.write(buf, 0, lenght);
                    }
                    input.close();
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        SendTweetFragment fragment = new SendTweetFragment();
        Bundle args = new Bundle();
        args.putString(Extras.FILE_URI, line.getPath());
        args.putString(Extras.TWEET_TEXT, mTweetText);
        fragment.setArguments(args);
        getMainActivity().setMainFragment(fragment);
    }
}
