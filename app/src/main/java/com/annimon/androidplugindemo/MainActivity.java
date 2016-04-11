package com.annimon.androidplugindemo;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import dalvik.system.DexClassLoader;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String CATEGORY_PLUGIN = "com.annimon.plugin.PLUGIN_APPLICATION";
    private static final String PLUGIN_PACKAGE = "com.annimon.plugin";

    private Toolbar toolbar;
    private TextView infoTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        infoTextView = (TextView) findViewById(R.id.info);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        int[] ids = {R.id.launch_plugin_activity, R.id.invoke_code, R.id.invoke_library};
        for (int id : ids) {
            findViewById(id).setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.launch_plugin_activity:
                launchPluginActivity();
                break;
            case R.id.invoke_code:
                invokeCode();
                break;
            case R.id.invoke_library:
                invokeLibrary();
                break;
        }
    }

    /*
     * Checks if plugin application is installed.
     */
    private boolean isPluginInstalled() {
        if (!PackageUtil.isApplicationInstalled(this, PLUGIN_PACKAGE)) {
            infoTextView.setText(R.string.plugin_not_installed);
            return false;
        }
        return true;
    }

    /*
     * Gets information about plugin application (label, icon, resources), then launch activity.
     */
    private void launchPluginActivity() {
        PackageManager pm = getApplicationContext().getPackageManager();

        // Search plugin applications
        Intent queryIntent = new Intent(Intent.ACTION_MAIN);
        queryIntent.addCategory(CATEGORY_PLUGIN);
        final List<ApplicationInfo> infos = PackageUtil.filterApplicationsByIntent(pm, queryIntent);
        if (infos.isEmpty()) {
            infoTextView.setText(R.string.plugin_not_installed);
            return;
        }
        // Get first app
        ApplicationInfo appInfo = infos.get(0);

        // Get simple information from ApplicationInfo
        StringBuilder info = new StringBuilder();
        info.append("Label: ").append(appInfo.loadLabel(pm)).append("\n");
        info.append("Package name: ").append(appInfo.packageName).append("\n");
        info.append("Source dir: ").append(appInfo.sourceDir).append("\n");
        info.append("Target SDK Version: ").append(appInfo.targetSdkVersion).append("\n");

        toolbar.setLogo(appInfo.loadIcon(pm));

        // Get resources
        Resources pluginResources = PackageUtil.getResources(pm, appInfo);
        if (pluginResources != null) {
            // Get string resource
            int plugin_text = pluginResources.getIdentifier("plugin_text", "string", appInfo.packageName);
            if (plugin_text > 0) {
                info.append("Plugin text: ").append(pluginResources.getString(plugin_text)).append("\n");
            }

            // Get color resource
            int colorPrimaryDark = pluginResources.getIdentifier("colorPrimaryDark", "color", appInfo.packageName);
            if (colorPrimaryDark > 0) {
                toolbar.setBackgroundColor(pluginResources.getColor(colorPrimaryDark));
            }
        }
        infoTextView.setText(info);

        // Start Activity
        Intent activityIntent = pm.getLaunchIntentForPackage(appInfo.packageName);
        startActivity(activityIntent);
    }

    /**
     * Invokes code from plugin application in separate ClassLoader.
     */
    private void invokeCode() {
        if (!isPluginInstalled()) return;

        Context pluginContext = PackageUtil.getPackageContext(this, PLUGIN_PACKAGE);
        if (pluginContext == null) return;
        ClassLoader classLoader = pluginContext.getClassLoader();

        StringBuilder info = new StringBuilder();
        try {
            // Invoke class via Reflection API
            Class<?> pluginClass = classLoader.loadClass(PLUGIN_PACKAGE + ".Plugin");

            Field goldenRatio = pluginClass.getDeclaredField("GOLDEN_RATIO");
            info.append("GOLDEN_RATIO: ").append(goldenRatio.getDouble(null)).append("\n");

            Method sum = pluginClass.getDeclaredMethod("sum", int.class, int.class);
            info.append("sum(42, 280): ").append(sum.invoke(null, 42, 280)).append("\n");

            Method reverse = pluginClass.getDeclaredMethod("reverse", String.class);
            info.append("reverse(\"abcdefgh\"): ").append(reverse.invoke(null, "abcdefgh"));
        } catch (Exception e) {
            info.append("Unable to retrieve data, ").append(e.toString());
        }
        infoTextView.setText(info);
    }

    /**
     * Extracts library.apk to internal storage, then invoke code.
     */
    private void invokeLibrary() {
        final String filename = "library.apk";
        String path = IOUtil.extractAssetToInternalStorage(this, filename);
        if (path == null) {
            infoTextView.setText("Unable to extract " + filename + " to internal storage");
            return;
        }

        StringBuilder info = new StringBuilder();
        try {
            DexClassLoader dexClassLoader = new DexClassLoader(
                    path, getFilesDir().getAbsolutePath(), null,
                    getClassLoader());
            final Class<?> infoClass = dexClassLoader.loadClass("plugin.Info");

            // Invoke default constructor
            Object defaultObject = infoClass.newInstance();
            Method getInfo = infoClass.getDeclaredMethod("getInfo");
            String defaultInfo = (String) getInfo.invoke(defaultObject);
            info.append("new Info().getInfo(): ").append(defaultInfo).append("\n");

            // Invoke constructor with argument
            Constructor constructor = infoClass.getDeclaredConstructor(String.class);
            Object testObject = constructor.newInstance("Test");
            String testInfo = (String) getInfo.invoke(testObject);
            info.append("new Info(\"Test\").getInfo(): ").append(testInfo);
        } catch (Exception e) {
            info.append("Unable to retrieve data, ").append(e.toString());
        }
        infoTextView.setText(info);
    }
}
