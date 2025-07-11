package com.example.whatsapp_share;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import java.io.File;
import java.util.ArrayList;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.embedding.engine.plugins.FlutterPlugin.FlutterPluginBinding;


/**
 * WhatsappShare
 */
public class WhatsappShareImproved implements FlutterPlugin, MethodChannel.MethodCallHandler, ActivityAware {

    private Context context;
    private MethodChannel channel;
    private Activity activity;

    public WhatsappShareImproved() {
    }

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {
        context = binding.getApplicationContext();
        channel = new MethodChannel(binding.getBinaryMessenger(), "whatsapp_share_improved");
        channel.setMethodCallHandler(this);
    }

    @Override
    public void onDetachedFromEngine(FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
        channel = null;
    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        activity = binding.getActivity();
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
        activity = binding.getActivity();
    }

    @Override
    public void onDetachedFromActivity() {
        activity = null;
    }

    @Override
    public void onMethodCall(MethodCall call, MethodChannel.Result result) {
        if (call.method.equals("shareFile")) {
            shareFile(call, result);
        } else if (call.method.equals("share")) {
            share(call, result);
        } else if (call.method.equals("isInstalled")) {
            isInstalled(call, result);
        } else {
            result.notImplemented();
        }
    }

    private boolean isPackageInstalled(String packageName, PackageManager packageManager) {
        try {
            packageManager.getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private void isInstalled(MethodCall call, MethodChannel.Result result) {
        try {
            String packageName = call.argument("package");

            if (packageName == null || packageName.isEmpty()) {
                Log.println(Log.ERROR, "", "FlutterShare Error: Package name null or empty");
                result.error("FlutterShare:Package name cannot be null or empty", null, null);
                return;
            }

            PackageManager pm = context.getPackageManager();
            boolean isInstalled = isPackageInstalled(packageName, pm);
            result.success(isInstalled);
        } catch (Exception ex) {
            Log.println(Log.ERROR, "", "FlutterShare: Error");
            result.error(ex.getMessage(), null, null);
        }
    }

    private void share(MethodCall call, MethodChannel.Result result) {
        try {
            String title = call.argument("title");
            String text = call.argument("text");
            String linkUrl = call.argument("linkUrl");
            String chooserTitle = call.argument("chooserTitle");
            String phone = call.argument("phone");
            String packageName = call.argument("package");

            if (title == null || title.isEmpty()) {
                Log.println(Log.ERROR, "", "FlutterShare Error: Title null or empty");
                result.error("FlutterShare: Title cannot be null or empty", null, null);
                return;
            } else if (phone == null || phone.isEmpty()) {
                Log.println(Log.ERROR, "", "FlutterShare Error: phone null or empty");
                result.error("FlutterShare: phone cannot be null or empty", null, null);
                return;
            } else if (packageName == null || packageName.isEmpty()) {
                Log.println(Log.ERROR, "", "FlutterShare Error: Package name null or empty");
                result.error("FlutterShare:Package name cannot be null or empty", null, null);
                return;
            }

            ArrayList<String> extraTextList = new ArrayList<>();

            if (text != null && !text.isEmpty()) {
                extraTextList.add(text);
            }
            if (linkUrl != null && !linkUrl.isEmpty()) {
                extraTextList.add(linkUrl);
            }

            String extraText = "";

            if (!extraTextList.isEmpty()) {
                extraText = TextUtils.join("\n\n", extraTextList);
            }

            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.setPackage(packageName);
            intent.putExtra("jid", phone + "@s.whatsapp.net");
            intent.putExtra(Intent.EXTRA_SUBJECT, title);
            intent.putExtra(Intent.EXTRA_TEXT, extraText);

            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            activity.startActivity(intent);

            result.success(true);
        } catch (Exception ex) {
            Log.println(Log.ERROR, "", "FlutterShare: Error");
            result.error(ex.getMessage(), null, null);
        }
    }

    private void shareFile(MethodCall call, MethodChannel.Result result) {
        ArrayList<String> filePaths = new ArrayList<String>();
        ArrayList<Uri> files = new ArrayList<Uri>();
        try {
            String title = call.argument("title");
            String text = call.argument("text");
            filePaths = call.argument("filePath");
            String chooserTitle = call.argument("chooserTitle");
            String phone = call.argument("phone");
            String packageName = call.argument("package");

            if (filePaths == null || filePaths.isEmpty()) {
                Log.println(Log.ERROR, "", "FlutterShare: ShareLocalFile Error: filePath null or empty");
                result.error("FlutterShare: FilePath cannot be null or empty", null, null);
                return;
            } else if (phone == null || phone.isEmpty()) {
                Log.println(Log.ERROR, "", "FlutterShare Error: phone null or empty");
                result.error("FlutterShare: phone cannot be null or empty", null, null);
                return;
            } else if (packageName == null || packageName.isEmpty()) {
                Log.println(Log.ERROR, "", "FlutterShare Error: Package name null or empty");
                result.error("FlutterShare:Package name cannot be null or empty", null, null);
                return;
            }

            for (int i = 0; i < filePaths.size(); i++) {
                File file = new File(filePaths.get(i));
                Uri fileUri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", file);
                files.add(fileUri);
            }

            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(Intent.ACTION_SEND_MULTIPLE);
            intent.setType("*/*");
            intent.setPackage(packageName);
            intent.putExtra("jid", phone + "@s.whatsapp.net");
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            activity.startActivity(intent);

            result.success(true);
        } catch (Exception ex) {
            result.error(ex.getMessage(), null, null);
            Log.println(Log.ERROR, "", "FlutterShare: Error");
        }
    }
}
