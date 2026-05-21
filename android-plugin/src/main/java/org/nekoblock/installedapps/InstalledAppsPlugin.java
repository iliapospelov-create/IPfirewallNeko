package org.nekoblock.installedapps;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;package org.nekoblock.installedapps;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.Base64;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import java.io.ByteArrayOutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@CapacitorPlugin(name = "InstalledApps")
public class InstalledAppsPlugin extends Plugin {

    @PluginMethod
    public void getInstalledApps(PluginCall call) {
        PackageManager pm = getContext().getPackageManager();

        // Берём все приложения у которых есть иконка в лаунчере
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> launcherApps = pm.queryIntentActivities(intent, 0);

        Set<String> seen = new HashSet<>();
        JSArray apps = new JSArray();

        for (ResolveInfo info : launcherApps) {
            String pkg = info.activityInfo.packageName;
            if (seen.contains(pkg)) continue;
            seen.add(pkg);

            try {
                JSObject app = new JSObject();
                app.put("packageName", pkg);
                app.put("name", info.loadLabel(pm).toString());

                try {
                    Drawable icon = info.loadIcon(pm);
                    Bitmap bitmap = Bitmap.createBitmap(48, 48, Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(bitmap);
                    icon.setBounds(0, 0, 48, 48);
                    icon.draw(canvas);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 85, baos);
                    app.put("icon", Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP));
                } catch (Exception e) {
                    app.put("icon", "");
                }

                apps.put(app);
            } catch (Exception e) {
                // пропускаем
            }
        }

        JSObject result = new JSObject();
        result.put("apps", apps);
        call.resolve(result);
    }
}
import android.graphics.drawable.Drawable;
import android.util.Base64;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import java.io.ByteArrayOutputStream;
import java.util.List;

@CapacitorPlugin(name = "InstalledApps")
public class InstalledAppsPlugin extends Plugin {

    @PluginMethod
    public void getInstalledApps(PluginCall call) {
        PackageManager pm = getContext().getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        JSArray apps = new JSArray();

        for (ApplicationInfo info : packages) {
            if ((info.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                if ((info.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0) {
                    continue;
                }
            }

            try {
                JSObject app = new JSObject();
                app.put("packageName", info.packageName);
                app.put("name", pm.getApplicationLabel(info).toString());

                try {
                    Drawable icon = pm.getApplicationIcon(info.packageName);
                    Bitmap bitmap = Bitmap.createBitmap(48, 48, Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(bitmap);
                    icon.setBounds(0, 0, 48, 48);
                    icon.draw(canvas);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 85, baos);
                    String iconBase64 = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP);
                    app.put("icon", iconBase64);
                } catch (Exception e) {
                    app.put("icon", "");
                }

                apps.put(app);
            } catch (Exception e) {
                // пропускаем
            }
        }

        JSObject result = new JSObject();
        result.put("apps", apps);
        call.resolve(result);
    }
}
