package com.azesmwayreactnativeunity;

import android.os.Handler;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.infer.annotation.Assertions;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.unity3d.player.UnityPlayer;

import java.util.Map;

import javax.annotation.Nonnull;

//import FrameLayout;
import android.widget.FrameLayout;
//import ViewGroup
import android.view.ViewGroup;
//import Activity
import android.app.Activity;
//import Context
import android.content.Context;
//import LayoutInflater
import android.view.LayoutInflater;
import com.unity3d.player.IUnityPlayerLifecycleEvents;
//import windowManager
import android.view.WindowManager;


public class ReactNativeUnityViewManager extends SimpleViewManager<View> implements LifecycleEventListener, View.OnAttachStateChangeListener {
    public static final String REACT_CLASS = "ReactNativeUnityView";
    ReactApplicationContext context;
    static View view;
    static View subView;
    protected UnityPlayer unityPlayer;

    public ReactNativeUnityViewManager(ReactApplicationContext context) {
        super();
        this.context = context;
        context.addLifecycleEventListener(this);
    }
    @Override
    @NonNull
    public String getName() {
        return REACT_CLASS;
    }

   
  @Override
  @NonNull
  protected View createViewInstance(@Nonnull ThemedReactContext reactContext) {

      if(unityPlayer != null) {
          unityPlayer.windowFocusChanged(true);
          unityPlayer.requestFocus();
          unityPlayer.resume();
          return subView;
      }

        unityPlayer = new UnityPlayer(reactContext);



      Activity activity = reactContext.getCurrentActivity();
      int flag = activity.getWindow().getAttributes().flags;
      boolean fullScreen = false;
      if ((flag & WindowManager.LayoutParams.FLAG_FULLSCREEN) == WindowManager.LayoutParams.FLAG_FULLSCREEN) {
          fullScreen = true;
      }

      LayoutInflater inflater = (LayoutInflater) reactContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

      subView = inflater.inflate(R.layout.activity_sub, null);

      FrameLayout unityPlayerLayout = subView.findViewById(R.id.unity_player_layout);
      unityPlayerLayout.addView(unityPlayer.getView());

      if (!fullScreen) {
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
      }

      unityPlayer.windowFocusChanged(true);
      unityPlayer.requestFocus();
      unityPlayer.resume();

      return subView;
  }
  
  @Override
  public void receiveCommand(
      @NonNull View view,
      String commandType,
      @Nullable ReadableArray args) {
          Assertions.assertNotNull(view);
          Assertions.assertNotNull(args);
          switch (commandType) {
              case "postMessage":
                  assert args != null;
                  postMessage(args.getString(0), args.getString(1), args.getString(2));
                  return;
              case "unloadUnity":
                  unloadUnity();
                  return;
              case "pauseUnity":
                  unityPlayer.pause();
                  return;
              case "resumeUnity":
                  unityPlayer.resume();
                  return;
              case "didBecomeActive":
                  unityPlayer.windowFocusChanged(true);
                  unityPlayer.requestFocus();
                  unityPlayer.resume();
                  return;
              default:
                  throw new IllegalArgumentException(String.format(
                      "Unsupported command %s received by %s.",
                      commandType,
                      getClass().getSimpleName()));
    }
  }

  public Map getExportedCustomBubblingEventTypeConstants() {
    return MapBuilder.builder()
            .put("onUnityMessage", MapBuilder.of("phasedRegistrationNames", MapBuilder.of("bubbled", "onUnityMessage")))
            .put("onPlayerUnload", MapBuilder.of("phasedRegistrationNames", MapBuilder.of("bubbled", "onPlayerUnload")))
            .put("onPlayerQuit", MapBuilder.of("phasedRegistrationNames", MapBuilder.of("bubbled", "onPlayerQuit")))
            .build();
}

  public void unloadUnity() {
    if(unityPlayer != null){
        // unityPlayer.unload();
        unityPlayer.UnitySendMessage("GameManager", "DestroyAll", "android");
    }
  }

  public void postMessage(String gameObject, String methodName, String message) {
    if(unityPlayer != null){
        unityPlayer.UnitySendMessage(gameObject, methodName, message);
    }
  }

  public static void sendMessageToMobileApp(String message) {
    WritableMap data = Arguments.createMap();
    data.putString("message", message);
    ReactContext reactContext = (ReactContext) subView.getContext();
    reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(subView.getId(), "onUnityMessage", data);
  }

  @Override
  public void onHostResume() {
      if(unityPlayer != null){
          unityPlayer.resume();
          restoreUnityUserState();
      }
  }

  @Override
  public void onHostPause() {
      if(unityPlayer != null){
          unityPlayer.pause();
      }
  }

  @Override
  public void onHostDestroy() {
      if(unityPlayer != null){
          unityPlayer.quit();
      }
  }

  private void restoreUnityUserState() {
    if(unityPlayer != null){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (unityPlayer != null) {
                    unityPlayer.pause();
                }
            }
        }, 300);
    }
  }

  @Override
  public void onViewAttachedToWindow(View v) {
      restoreUnityUserState();
  }

  @Override
  public void onViewDetachedFromWindow(View v) {

  }

}
