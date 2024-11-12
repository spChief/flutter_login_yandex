package ru.spchief.flutter_login_yandex;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.yandex.authsdk.YandexAuthException;
import com.yandex.authsdk.YandexAuthLoginOptions;
import com.yandex.authsdk.YandexAuthOptions;
import com.yandex.authsdk.YandexAuthSdk;
import com.yandex.authsdk.YandexAuthToken;

import java.util.HashMap;
import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;

/**
 * FlutterLoginYandexPlugin
 */
public class FlutterLoginYandexPlugin implements FlutterPlugin, MethodCallHandler, ActivityAware {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private MethodChannel channel;
  private YandexAuthSdk sdk;
  private ActivityPluginBinding activityPluginBinding;
  private Delegate delegate;

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "flutter_login_yandex");
    channel.setMethodCallHandler(this);
    Context context = flutterPluginBinding.getApplicationContext();
    sdk = new YandexAuthSdk(context, new YandexAuthOptions(context, true));
    delegate = new Delegate(context, sdk);
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    if (call.method.equals("signIn")) {
      delegate.signIn(result);
    } else {
      result.notImplemented();
    }
  }

  private void dispose() {
    delegate = null;
    channel.setMethodCallHandler(null);
    channel = null;
  }

  private void attachToActivity(ActivityPluginBinding activityPluginBinding) {
    this.activityPluginBinding = activityPluginBinding;
    activityPluginBinding.addActivityResultListener(delegate);
    delegate.setActivity(activityPluginBinding.getActivity());
  }

  private void disposeActivity() {
    activityPluginBinding.removeActivityResultListener(delegate);
    delegate.setActivity(null);
    activityPluginBinding = null;
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    dispose();
  }

  @Override
  public void onAttachedToActivity(ActivityPluginBinding activityPluginBinding) {
    attachToActivity(activityPluginBinding);
  }

  @Override
  public void onDetachedFromActivityForConfigChanges() {
    disposeActivity();
  }

  @Override
  public void onReattachedToActivityForConfigChanges(ActivityPluginBinding activityPluginBinding) {
    attachToActivity(activityPluginBinding);
  }

  @Override
  public void onDetachedFromActivity() {
    disposeActivity();
  }

  public static class Delegate implements PluginRegistry.ActivityResultListener {

    private static final int REQUEST_LOGIN_SDK = 52500;

    private static final String ERROR_REASON_SIGN_IN_FAILED = "sign_in_failed";

    private final Context context;
    private Activity activity;
    private YandexAuthSdk sdk;

    private PendingOperation pendingOperation;

    public Delegate(Context context, YandexAuthSdk sdk) {
      this.context = context;
      this.sdk = sdk;
    }

    public void setActivity(Activity activity) {
      this.activity = activity;
    }

    // Only access activity with this method.
    public Activity getActivity() {
      return activity;
    }

    private void checkAndSetPendingOperation(String method, Result result) {
      checkAndSetPendingOperation(method, result, null);
    }

    private void checkAndSetPendingOperation(String method, Result result, Object data) {
      if (pendingOperation != null) {
        throw new IllegalStateException(
          "Concurrent operations detected: " + pendingOperation.method + ", " + method);
      }
      pendingOperation = new PendingOperation(method, result, data);
    }

    public void signIn(Result result) {
      if (getActivity() == null) {
        throw new IllegalStateException("signIn needs a foreground activity");
      }
      checkAndSetPendingOperation("signIn", result);

      final YandexAuthLoginOptions.Builder options = new YandexAuthLoginOptions.Builder();
      options.setLoginType(LoginType.WEBVIEW);
      final Intent intent = sdk.createLoginIntent(options.build());

      getActivity().startActivityForResult(intent, REQUEST_LOGIN_SDK);
    }

    private void onSignInResult(YandexAuthToken token) {
      Map<String, Object> response = new HashMap<>();
      response.put("token", token.getValue());
      response.put("expiresIn", token.expiresIn());
      finishWithSuccess(response);
    }

    private void finishWithSuccess(Object data) {
      pendingOperation.result.success(data);
      pendingOperation = null;
    }

    private void finishWithError(String errorCode, String errorMessage) {
      pendingOperation.result.error(errorCode, errorMessage, null);
      pendingOperation = null;
    }

    private static class PendingOperation {
      final String method;
      final Result result;
      final Object data;

      PendingOperation(String method, Result result, Object data) {
        this.method = method;
        this.result = result;
        this.data = data;
      }
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
      if (pendingOperation == null) {
        return false;
      }
      switch (requestCode) {
        case REQUEST_LOGIN_SDK:
          try {
            final YandexAuthToken yandexAuthToken = sdk.extractToken(resultCode, data);
            if (yandexAuthToken != null) {
              // Success auth
              onSignInResult(yandexAuthToken);
            } else {
              finishWithError(ERROR_REASON_SIGN_IN_FAILED, "Signin failed - no token");
            }
          } catch (YandexAuthException e) {
            // Process error
            finishWithError(ERROR_REASON_SIGN_IN_FAILED, "Signin failed");
          }
          return true;
        default:
          return false;
      }
    }
  }
}
