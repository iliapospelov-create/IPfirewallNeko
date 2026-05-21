package org.nekoblock.installedapps;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
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
import java.util.List;

@CapacitorPlugin(name = "InstalledApps")
public class InstalledAppsPlugin extends Plugin {

    @PluginMethod
    public void getInstalledApps(PluginCall call) {
        PackageManager pm = getContext().getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        JSArray apps = new JSArray();

        for (ApplicationInfo info : packages) {
            try {
                // Показываем только приложения установленные пользователем
                // (у них есть installer или они не помечены как системные)
                boolean isSystem = (info.flags & ApplicationInfo.FLAG_SYSTEM) != 0
                                && (info.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0;

                String installer = null;
                try {
                    installer = pm.getInstallerPackageName(info.packageName);
                } catch (Exception ignored) {}

                // Пропускаем если системное И нет installer
                if (isSystem && installer == null) continue;

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
                    app.put("icon", Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP));
                } catch (Exception e) {
                    app.put("icon", "");
                }
                apps.put(app);
            } catch (Exception e) {}
        }

        JSObject result = new JSObject();
        result.put("apps", apps);
        call.resolve(result);
    }
}
