package ru.pes.observer.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import ru.pes.observer.R;

public class StartActivity extends Activity {
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        intent = new Intent(this, LoginActivity.class);
    }

    public void doSignIn(View view) {
        intent.putExtra(getResources().getString(R.string.extra_message), getResources().getString(R.string.login)); // Sign in view
        startActivity(intent);
        finish();
    }

    public void doSignUp(View view) {
        intent.putExtra(getResources().getString(R.string.extra_message), getResources().getString(R.string.register)); // Registration view
        startActivity(intent);
        finish();
    }
}
