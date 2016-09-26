package ru.pes.observer.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.app.Activity;

import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import ru.pes.observer.R;
import ru.pes.observer.objects.Message;
import ru.pes.observer.objects.User;

/**
 * Экран входа с помощью почты и пароля
 */
public class LoginActivity extends Activity {

    /**
     * Fake login and password
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@ya.ru:hello", "bar@example.com:world"
    };
    /**
     * Sign in task
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private EditText mPassword2View;
    private CheckBox mPasswordCheckBox;
    private View mProgressView;
    private View mLoginFormView;
    //
    private String extra_message;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        preferences = getSharedPreferences(getString(R.string.app_preferences), MODE_PRIVATE);
        Intent intent = getIntent();
        if (!intent.hasExtra(getString(R.string.extra_message))) {
            startActivity(new Intent(this, StartActivity.class));
            finish();
        }
        extra_message = intent.getStringExtra(getResources().getString(R.string.extra_message));
        if (extra_message.equals(getResources().getString(R.string.register))) {
            mPassword2View = (EditText) findViewById(R.id.password2);
            mPassword2View.setVisibility(View.VISIBLE);
        }
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        mPasswordCheckBox = (CheckBox) findViewById(R.id.cbShowPass);
        mPasswordCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mPasswordView.setTransformationMethod(null); // Show symbols in password field
                    mPasswordView.setSelection(mPasswordView.length()); // Move carret to the end of string
                    if (extra_message.equalsIgnoreCase(getResources().getString(R.string.register))) {
                        mPassword2View.setTransformationMethod(null);
                        mPassword2View.setSelection(mPassword2View.length());
                    }
                } else {
                    mPasswordView.setTransformationMethod(PasswordTransformationMethod.getInstance()); // Hide symbols
                    mPasswordView.setSelection(mPasswordView.length());
                    if (extra_message.equalsIgnoreCase(getResources().getString(R.string.register))) {
                        mPassword2View.setTransformationMethod(PasswordTransformationMethod.getInstance());
                        mPassword2View.setSelection(mPassword2View.length());
                    }
                }
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    /**
     * Try to sign in
     * or show error mesage
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Save login and password, while trying to sign in;
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check password
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        if (extra_message.equalsIgnoreCase(getResources().getString(R.string.register))) {
            mPassword2View.setError(null);
            String password2 = mPassword2View.getText().toString();

            // Check password2
            if (!password.equals(password2)) {
                mPassword2View.setError(getString(R.string.error_incorrect_password2));
                focusView = mPassword2View;
                cancel = true;
            }
        }

        // Check email
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // Don't sign in, and focus first error field;
            focusView.requestFocus();
        } else {
            // Show progress bar and start sign in task;
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute(getApplicationContext());
        }
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4 && password.length() < 10;
    }

    /**
     * Show progress, hide login form
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Object, Void, Boolean> {
        private Socket client;
        private BufferedReader in;
        private PrintWriter out;
        private Context context;
        private final String mEmail;
        private final String mPassword;
        private static final String SERVER_ADDRESS = "109.123.160.7";
        private static final int PORT = 8081;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Object... params) {
            context = (Context) params[0];
            // Register the new account here.
            try {
                client = new Socket(SERVER_ADDRESS, PORT);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                out = new PrintWriter(client.getOutputStream(), true);
                Gson gson = new Gson();
                User user = new User();
                user.setName(mEmail);
                user.setPassword(mPassword.hashCode());
                String userJson = gson.toJson(user);
                Message message = new Message();
                message.setType(extra_message);
                message.setData(userJson);
                String json = gson.toJson(message);
                System.out.println(json);
                out.println(json);
                String answer;
                while ((answer = in.readLine()) != null) {
                    if (answer.equalsIgnoreCase("OK")) {
                        return true;
                    } else {
                        return false;
                    }
                }
            } catch (IOException e) {
                Log.e("IO_ERROR", "Can't create socket");
            }

            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                Intent intent = new Intent(context, MainActivity.class);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(getString(R.string.login_key), mEmailView.getText().toString());
                editor.commit();
                startActivity(intent);
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

