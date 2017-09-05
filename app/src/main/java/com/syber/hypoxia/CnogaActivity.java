package com.syber.hypoxia;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.cnoga.singular.mobile.sdk.bean.DetailsBean;
import com.cnoga.singular.mobile.sdk.constants.CodeConstant;
import com.cnoga.singular.mobile.sdk.device.CnogaDeviceManager;
import com.cnoga.singular.mobile.sdk.device.DeviceStatus;
import com.cnoga.singular.mobile.sdk.device.IOnUpdateDeviceConnectionListener;
import com.cnoga.singular.mobile.sdk.measurement.CnogaMeasurementManager;
import com.cnoga.singular.mobile.sdk.measurement.IMeasurementListener;
import com.cnoga.singular.mobile.sdk.passport.CnogaPassportManager;
import com.cnoga.singular.mobile.sdk.passport.PassportKeys;
import com.cnoga.singular.mobile.sdk.profile.CnogaProfileManager;
import com.syber.base.BaseActivity;
import com.syber.base.BaseViewHolder;
import com.syber.hypoxia.helo.BTManager;
import com.syber.hypoxia.helo.BleFlow;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class CnogaActivity extends BaseActivity implements BTManager.RequestListener, IOnUpdateDeviceConnectionListener, IMeasurementListener {
    private ViewHolder viewHolder;
    private CnogaMeasurementManager mCnogaMeasurementManager;

    private BTManager btManager = BTManager.instance;
    private CnogaDeviceManager mBluetoothLeManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cnoga);
        viewHolder = new ViewHolder(findViewById(R.id.view_holder));
        initAppBar();
        mBluetoothLeManager = CnogaDeviceManager.getInstance(this);
        mBluetoothLeManager.regOnUpdateDeviceConnectionListener(this);

        new LoginTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    private void start() {

        mCnogaMeasurementManager = CnogaMeasurementManager.getInstance(this);
        mCnogaMeasurementManager.setMeasurementListener(this);

        btManager.setRequestListener(this);
        btManager.startCnoga(this, new BleFlow() {
            @Override
            public void onStart() {
                mBluetoothLeManager.connect(btManager.getBlutoothGatt().getDevice().getAddress());
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {

            }

            @Override
            protected void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (btManager.handleEnableResult(requestCode, resultCode, data)) {
            start();
        } else {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        btManager.stop();
    }

    @Override
    public void onRequestConfirm(int request, Intent data) {
    }

    @Override
    public void onUpdateDeviceStatus(int i) {
    }

    @Override
    public void onDataAvailable(int i) {
        postUpdateProgress(i);
    }

    @Override
    public void onConnectionStatusChanged(int i) {
    }

    @Override
    public void onDeviceStatusChanged(DeviceStatus deviceStatus) {
    }

    @Override
    public void onMeasurementFinished() {
    }

    @Override
    public void onUploadFinished(int i, String s) {
        finish();
    }

    @Override
    public void onMeasurementInterrupted(int i) {
    }

    private void postUpdateProgress(int count) {
        final HashMap<Integer, Object> paramMap = mCnogaMeasurementManager.getMeasurementParamData(count);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isFinishing()) return;
                viewHolder.updateProgress(paramMap);
            }
        });
    }

    private class ViewHolder extends BaseViewHolder {
        TextView info, progressInfo;

        public ViewHolder(View view) {
            super(view);
            info = get(R.id.info);
            progressInfo = get(R.id.progress_info);
        }

        private void updateProgress(HashMap<Integer, Object> paramMap) {
            if (null == paramMap) return;
            StringBuilder sb = new StringBuilder();
            Iterator<Map.Entry<Integer, Object>> it = paramMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Integer, Object> entity = it.next();
                sb.append(String.format("key: %s\tvalue:%s", "" + entity.getKey(), "" + entity.getValue()));
            }
            progressInfo.setText(sb.toString());
        }

    }

    private class LoginTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            CnogaPassportManager.getInstance(getApplicationContext()).setSubscriptionKey("a9395b47b62c4ff2b3fd930129e244e5");
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            HashMap<String, Object> results = CnogaPassportManager.getInstance(CnogaActivity.this).login("liangtiangang@syberos.com",
                    "00000000p",
                    "GenericChina",
                    getDeviceIMEI());
            if (null != results) {
                int code = (int) results.get(PassportKeys.PASSPORT_CODE);
                Log.d("oo", "login:\t" + code);
                boolean suc = code == CodeConstant.SUCCESS;
                if (suc) {
                    DetailsBean bean = (DetailsBean) results.get(PassportKeys.LOGIN_BEAN);

                    CnogaProfileManager.getInstance(getApplicationContext()).init("liangtiangang@syberos.com",
                            "00000000p",
                            bean.getId(),
                            "GenericChina",
                            results.get(PassportKeys.LOGIN_TOKEN_AUTH).toString(),
                            results.get(PassportKeys.LOGIN_TOKEN_EXPIRE).toString());
                }
                return suc;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (isFinishing()) return;
            if (aBoolean.booleanValue()) {

                start();
            } else {
                finish();
            }
        }

        private String getDeviceIMEI() {
            String device = null;
            try {
                TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                device = tm.getDeviceId();
            } catch (Exception e) {
                Log.e(TAG, "getDeviceIMEI   " + e.toString());
            }
            if (device == null) {
                device = String.valueOf(System.nanoTime());
            }

            return device;
        }
    }

}
