package com.syber.hypoxia;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.soundcloud.android.crop.Crop;
import com.squareup.picasso.Picasso;
import com.syber.base.BaseFragment;

import java.io.File;


/**
 * A simple {@link Fragment} subclass.
 */
public class UpdateUserInfoFragment extends BaseFragment implements View.OnClickListener {
    private boolean editable = false;
    private ImageView userImage;
    private TextView userSex, userBirthday, userHeight, userWeight;
    private EditText userName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.edit_save, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.edit).setVisible(!editable);
        menu.findItem(R.id.save).setVisible(editable);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit:
                editable = true;
                userName.setEnabled(editable);
                getActivity().supportInvalidateOptionsMenu();
                return true;
            case R.id.save:
                attemptUpload();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_update_user_info, container, false);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (Crop.REQUEST_PICK == requestCode) {
            if (Activity.RESULT_OK == resultCode) {
                Crop.of(data.getData(),
                        Uri.fromFile(new File(Environment.getExternalStorageDirectory(),
                                String.valueOf(System.currentTimeMillis()) + ".jpg"))).asSquare().start(getActivity(), this);
            }
        } else if (Crop.REQUEST_CROP == requestCode) {
            if (Activity.RESULT_OK == resultCode) {
                Picasso.with(getActivity()).load((Uri) data.getParcelableExtra(MediaStore.EXTRA_OUTPUT)).into(userImage);
            }
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        userImage = get(R.id.user_image);
        userName = get(R.id.user_name_text);
        userSex = get(R.id.user_sex_text);
        userBirthday = get(R.id.user_sex_text);
        userHeight = get(R.id.user_height_text);
        userWeight = get(R.id.user_weight_text);
        get(R.id.user_image_container).setOnClickListener(this);
        get(R.id.user_sex_container).setOnClickListener(this);
        get(R.id.user_birthday_container).setOnClickListener(this);
        get(R.id.user_height_container).setOnClickListener(this);
        get(R.id.user_weight_container).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (R.id.user_image_container == id) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT).setType("image/*");
            startActivityForResult(intent, Crop.REQUEST_PICK);
        }
    }

    private void attemptUpload() {
        if (checkName()) {
        }
    }

    private boolean checkName() {
        boolean result = false;
        if (userName.getText().length() == 0) {
            showToast("请输入您的名字");
            userName.requestFocus();
        } else {
            result = true;
        }
        return result;
    }

}
