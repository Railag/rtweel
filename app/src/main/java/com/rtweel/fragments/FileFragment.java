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
import com.rtweel.Const;
import com.rtweel.filechooser.FileAdapter;
import com.rtweel.filechooser.FileItem;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by firrael on 22.3.15.
 */
public class FileFragment extends BaseFragment {
    private File mCurrentPath;

    private FileAdapter mAdapter;

    private ListView list;

    private String mTweetText;
    private long mReplyId;

    private Pattern pattern = Pattern.compile("^.*\\.(png|gif|jpg|jpeg|bmp)$");
    private Matcher matcher;

    @Override
    public void onStart() {
        super.onStart();

        Bundle args = getArguments();
        if (args != null) {
            mTweetText = args.getString(Const.TWEET_TEXT);
            mReplyId = args.getLong(Const.REPLY_ID);
        }

        mCurrentPath = Environment.getExternalStorageDirectory();
        initialize(mCurrentPath);
    }

    @Override
    protected String getTitle() {
        return getString(R.string.title_file);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_file, null);
        list = (ListView) v.findViewById(R.id.file_list);
        setRetainInstance(true);
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

        getActionBar().setTitle(getString(R.string.file_current_dir) + ":" + startPath.getName());
        List<FileItem> pathList = new ArrayList<FileItem>();
        List<FileItem> fileList = new ArrayList<FileItem>();
        if (paths != null && paths.length > 0) {
            try {
                for (File path : paths) {
                    if (path.isDirectory())
                        pathList.add(new FileItem(path.getName(), getString(R.string.file_folder), path
                                .getAbsolutePath()));
                    else {
                        fileList.add(new FileItem(path.getName(), getString(R.string.file_size) + ":"
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
                    new FileItem("..", getString(R.string.file_parent_directory), startPath.getParent()));
        }

        mAdapter = new FileAdapter(getActivity(), pathList);
        list.setAdapter(mAdapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FileItem line = mAdapter.getItem(position);
                if (line.getDetails().equalsIgnoreCase("folder")
                        || line.getDetails().equalsIgnoreCase("parent directory")) {
                    mCurrentPath = new File(line.getPath());
                    getLoadingBar().setVisibility(View.VISIBLE);
                    initialize(mCurrentPath);
                } else {
                    onFileClick(line);
                }

            }
        });

        getLoadingBar().setVisibility(View.GONE);
    }

    private void onFileClick(FileItem line) {
        SendTweetFragment fragment = new SendTweetFragment();
        Bundle args = new Bundle();
        args.putString(Const.FILE_URI, line.getPath());
        args.putString(Const.TWEET_TEXT, mTweetText);
        args.putLong(Const.REPLY_ID, mReplyId);
        fragment.setArguments(args);
        getMainActivity().setMainFragment(fragment);
    }
}
