package study.acodexm;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import study.acodexm.utils.LOG;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import acodexm.panorama.R;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class WelcomeActivity extends Activity implements ActivityCompat.OnRequestPermissionsResultCallback {
    private static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
    private static final String TAG = WelcomeActivity.class.getSimpleName();
    private final String CAMERA = Manifest.permission.CAMERA;
    private final String STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    @BindView(R.id.right_btn)
    ImageView rightBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //if this activity is opened from MainActivity than it means user want to exit application
        if (getIntent().getBooleanExtra("EXIT", false)) {
            finish();
            return;
        }
        setContentView(R.layout.welcome_activity);
        ButterKnife.bind(this);

        //if permissions are granted go straight to MainActivity
        if (checkAndRequestPermissions()) {
            openMainActivity();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkAndRequestPermissions()) {
            //show button (to start MainActivity) if permissions are granted
            rightBtn.setVisibility(View.VISIBLE);
        }
    }

    @OnClick(R.id.right_btn)
    void onStartClickListener() {
        openMainActivity();
    }

    private void openMainActivity() {
        Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
        startActivity(intent);
    }

    /**
     * method checks if permissions are granted, if they are not then it request them to be granted
     *
     * @return
     */
    private boolean checkAndRequestPermissions() {
        int cameraPerm = ContextCompat.checkSelfPermission(this, CAMERA);
        int storagePerm = ContextCompat.checkSelfPermission(this, STORAGE);
        List<String> neededPerms = new ArrayList<>();
        if (storagePerm != PackageManager.PERMISSION_GRANTED) {
            neededPerms.add(STORAGE);
        }
        if (cameraPerm != PackageManager.PERMISSION_GRANTED) {
            neededPerms.add(CAMERA);
        }
        if (!neededPerms.isEmpty()) {
            ActivityCompat.requestPermissions(this, neededPerms.toArray(
                    new String[neededPerms.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    /**
     * method handles permission request.
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        LOG.d(TAG, "Permission callback called-------");
        switch (requestCode) {
            case REQUEST_ID_MULTIPLE_PERMISSIONS: {
                Map<String, Integer> perms = new HashMap<>();
                perms.put(CAMERA, PackageManager.PERMISSION_GRANTED);
                perms.put(STORAGE, PackageManager.PERMISSION_GRANTED);
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);
                    if (perms.get(CAMERA) == PackageManager.PERMISSION_GRANTED
                            && perms.get(STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        LOG.d(TAG, "Camera and Read&Write services permission granted");
                        rightBtn.setVisibility(View.VISIBLE);
                    } else {
                        LOG.d(TAG, "Some permissions are not granted ask again ");
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, CAMERA) ||
                                ActivityCompat.shouldShowRequestPermissionRationale(this, STORAGE)) {
                            showDialogOK(getString(R.string.dialog_rw_perms_required), (dialog, which) -> {
                                switch (which) {
                                    case DialogInterface.BUTTON_POSITIVE:
                                        checkAndRequestPermissions();
                                        break;
                                    case DialogInterface.BUTTON_NEGATIVE:
                                        break;
                                    default:
                                        break;
                                }
                            });
                        } else {
                            Toast.makeText(this, R.string.msg_go_to_settings, Toast.LENGTH_LONG).show();
                        }
                    }
                }
                break;
            }
            default:
                break;
        }

    }

    /**
     * method builds dialog
     *
     * @param message
     * @param okListener
     */
    private void showDialogOK(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton(R.string.dialog_ok, okListener)
                .setNegativeButton(R.string.dialog_cancel, okListener)
                .create()
                .show();
    }
}
