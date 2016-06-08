package com.syber.hypoxia;


import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.soundcloud.android.crop.Crop;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;
import com.syber.base.BaseFragment;
import com.syber.base.data.DataRequester;
import com.syber.base.data.EmptyResponse;
import com.syber.hypoxia.data.IRequester;
import com.syber.hypoxia.data.SignInResponse;
import com.syber.hypoxia.data.User;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 */
public class UpdateUserInfoFragment extends BaseFragment implements View.OnClickListener, DatePickerDialog.OnDateSetListener, StringPickerFragment.OnItemClickedListener {
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
    private Calendar calendar = Calendar.getInstance(Locale.CHINA);
    private Bus bus = new Bus();
    private boolean editable = false;
    private ImageView userImage;
    private TextView userSex, userBirthday, userHeight, userWeight;
    private EditText userName;
    private File file;
    private SignInResponse.UserInfoExt update = new SignInResponse.UserInfoExt();
    private ProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        bus.register(this);
        calendar.add(Calendar.YEAR, -40);
        SignInResponse.UserInfoExt ext = User.getUserInfoExt();
        update.user_id = ext.user_id;
        update.fullname = ext.fullname;
        update.sex = ext.sex;
        update.sexstring = ext.sexstring;
        update.birthday = ext.birthday;
        update.height = ext.height;
        update.weight = ext.weight;
        editable = getActivity().getIntent().getBooleanExtra(UpdateUserInfoActivity.KEY_EDIT, false);
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
                updateEdit();
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
                file = new File(Environment.getExternalStorageDirectory(), String.valueOf(System.currentTimeMillis()) + ".jpg");
                Crop.of(data.getData(), Uri.fromFile(file)).asSquare().withMaxSize(120, 120).start(getActivity(), this);
            }
        } else if (Crop.REQUEST_CROP == requestCode) {
            if (Activity.RESULT_OK == resultCode) {
                Picasso.with(getActivity()).load(file).into(userImage);
            } else {
                file = null;
            }
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        userImage = get(R.id.user_image);
        userName = get(R.id.user_name_text);
        userSex = get(R.id.user_sex_text);
        userBirthday = get(R.id.user_birthday_text);
        userHeight = get(R.id.user_height_text);
        userWeight = get(R.id.user_weight_text);
        get(R.id.user_image_container).setOnClickListener(this);
        get(R.id.user_sex_container).setOnClickListener(this);
        get(R.id.user_birthday_container).setOnClickListener(this);
        get(R.id.user_height_container).setOnClickListener(this);
        get(R.id.user_weight_container).setOnClickListener(this);
        userName.setText(update.fullname);
        userSex.setText(update.sexstring);
        if (!TextUtils.isEmpty(update.birthday)) {
            userBirthday.setText(sdf.format(Long.parseLong(update.birthday)));
        }
        userHeight.setText(update.height);
        userWeight.setText(update.weight);
        Picasso.with(getActivity()).load(DataRequester.SERVER + "user/getavatar?id=" + update.user_id).into(userImage);
        updateEdit();
    }

    private void updateEdit() {
        userName.setEnabled(editable);
        userHeight.setEnabled(editable);
        userWeight.setEnabled(editable);
    }

    @Override
    public void onClick(View v) {
        if (!editable) return;
        int id = v.getId();
        if (R.id.user_image_container == id) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT).setType("image/*");
            startActivityForResult(intent, Crop.REQUEST_PICK);
        } else if (R.id.user_birthday_container == id) {
            DatePickerFragment.from(this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show(
                    getFragmentManager().beginTransaction().addToBackStack("date_picker_fragment"),
                    "date_picker_fragment");
        } else if (R.id.user_sex_container == id) {
            StringPickerFragment.from(this, "性别", StringPickerFragment.SEX).show(getFragmentManager().beginTransaction().addToBackStack("sex_picker"),
                    "sex_picker");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        bus.unregister(this);
    }

    private void attemptUpload() {
        if (checkName() && checkSex() && checkBirthday() && checkHeight() && checkWeight()) {
            if (null == progressDialog) {
                progressDialog = ProgressDialog.show(getActivity(), null, "正在保存", true, false);
            } else {
                progressDialog.show();
            }
            if (null != file) {
                IRequester.getInstance().uploadImage(bus, file);
            } else {
                IRequester.getInstance().updateUserInfo(bus, update);
            }
        }
    }

    @Subscribe
    public void withResponse(SignInResponse event) {
        if (null == getView() || getActivity().isFinishing()) return;
        progressDialog.dismiss();
        showToast("修改" + (event.isSuccess() ? "成功" : "失败"));
        if (event.isSuccess()) {
            User.saveUser(event);
            getActivity().finish();
        }
    }

    @Subscribe
    public void withImage(EmptyResponse event) {
        if (null == getView() || getActivity().isFinishing()) return;
        if (event.isSuccess()) {
            file = null;
            attemptUpload();
        } else {
            progressDialog.dismiss();
            showToast("头像上传失败:" + event.error);
        }
    }

    private boolean checkName() {
        boolean result = false;
        if (userName.getText().length() == 0) {
            showToast("请输入您的名字");
            userName.requestFocus();
        } else {
            result = true;
            update.fullname = userName.getText().toString();
        }
        return result;
    }

    private boolean checkSex() {
        boolean result = false;
        if (userSex.getText().length() == 0) {
            showToast("请选择您的性别");
        } else {
            result = true;
        }
        return result;
    }


    private boolean checkBirthday() {
        boolean result = false;
        if (userBirthday.getText().length() == 0) {
            showToast("请选择您的出生日期");
        } else {
            result = true;
        }
        return result;
    }

    private boolean checkHeight() {
        boolean result = false;
        if (userHeight.getText().length() == 0) {
            showToast("请输入您的身高");
        } else {
            update.height = userHeight.getText().toString();
            result = true;
        }
        return result;
    }


    private boolean checkWeight() {
        boolean result = false;
        if (userWeight.getText().length() == 0) {
            showToast("请输入您的体重");
        } else {
            update.weight = userWeight.getText().toString();
            result = true;
        }
        return result;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        calendar.set(year, monthOfYear, dayOfMonth);
        String text = sdf.format(calendar.getTime());
        update.birthday = text;
        userBirthday.setText(text);
    }

    @Override
    public void onItemClicked(int which, String[] array) {
        update.sex = String.valueOf(which + 1);
        userSex.setText(array[which]);
    }
}
