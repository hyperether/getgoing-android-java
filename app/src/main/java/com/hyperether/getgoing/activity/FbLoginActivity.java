package com.hyperether.getgoing.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.hyperether.getgoing.R;
import com.hyperether.getgoing.util.Constants;

public class FbLoginActivity extends Activity {

    LoginButton loginButton;
    CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fb_login);

        boolean logOut = getIntent().getBooleanExtra("logout", false);

        if (isLoggedIn() && logOut) {
            return;
        } else if (isLoggedIn()) {
            startGetGoingActivity();
        }

        callbackManager = CallbackManager.Factory.create();
        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions("email");

        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
                startGetGoingActivity();
            }

            @Override
            public void onCancel() {
                // App code
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
            }
        });
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (callbackManager == null) {
            callbackManager = CallbackManager.Factory.create();
        }

        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (isLoggedIn()) {
            Intent intent = new Intent(FbLoginActivity.this, GetGoingActivity.class);
            startActivityForResult(intent, Constants.RESULT_REQUESTED);
        }
    }

    /**
     * This method check if user is logged in by fb
     */
    public boolean isLoggedIn() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        return accessToken != null;
    }

    /**
     * This method starts getgoing activity
     */
    private void startGetGoingActivity() {
        Intent intent = new Intent(FbLoginActivity.this, GetGoingActivity.class);
        startActivity(intent);
        finish();
    }
}
