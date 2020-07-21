package com.sample.mobileotp_email_password_verification;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
//F1:11:AE:7E:06:01:DE:AE:DA:25:CF:1E:6E:68:0A:65:03:8A:B2:27

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    /************************validate and save data**************************/
    String user;
    TextView domain;
    EditText regUsername, regPassword, regMobileNumber;

    /************************validate email and password**************************/
    EditText loginEmail, loginPassword;

    /************************verify mobile no by otp**************************/
    String verificationCodeBySystem;
    EditText mobileNumberEnterByUser, verifyOTP;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        regUsername = findViewById(R.id.reg_email);
        regPassword = findViewById(R.id.reg_password);
        regMobileNumber = findViewById(R.id.reg_phone);
        domain = findViewById(R.id.domain);

        loginEmail = findViewById(R.id.login_email);
        loginPassword = findViewById(R.id.login_password);

        mobileNumberEnterByUser = findViewById(R.id.verify_phone);
        verifyOTP = findViewById(R.id.verify_otp);
        progressBar = findViewById(R.id.progressbar);
        progressBar.setVisibility(View.INVISIBLE);
    }

    /************************Validate and save data**************************/
    private Boolean validateUsername() {
        String val = regUsername.getText().toString();
        String pattern = "\\A\\w{4,20}\\z";
        if (val.isEmpty()) {
            regUsername.setError("Field cannot be empty");
            return false;
        } else if (!val.matches(pattern)) {
            regUsername.setError("Space is not allowed");
            return false;
        } else {
            regUsername.setError(null);
            return true;
        }
    }

    private Boolean validatePassword() {
        String val = regPassword.getText().toString();
        String pattern = "^" +
                "(?=.*[a-zA-Z])" + //any letter
                "(?=.*[@#$%^&+])" +//atleast one special character
                "(?=.*[\\S+$])" +// no white space
                ".{4,}" +//atleast 4 characters
                "$";
        if (val.isEmpty()) {
            regPassword.setError("Field cannot be empty");
            return false;
        } else if (!val.matches(pattern)) {
            regPassword.setError("Password is too weak");
            return false;
        } else {
            regPassword.setError(null);
            return true;
        }
    }

    private Boolean validateMobileNumber() {
        String val = regMobileNumber.getText().toString();
        String pattern = "(0/91)?[7-9][0-9]{9}";
        if (val.isEmpty()) {
            regMobileNumber.setError("Field cannot be empty");
            return false;
        } else if (!val.matches(pattern)) {
            regMobileNumber.setError("Invalid mobile number");
            return false;
        } else {
            regMobileNumber.setError(null);
            return true;
        }
    }

    public void buttonSave(View view) {
        if (!validateUsername() | !validatePassword() | !validateMobileNumber()) {
            return;
        } else {
            user =  regUsername.getText().toString();
            FirebaseDatabase.getInstance().getReference(user).child("Username").setValue(regUsername.getText().toString());
            FirebaseDatabase.getInstance().getReference(user).child("Email").setValue(regUsername.getText().toString() + domain.getText().toString());
            FirebaseDatabase.getInstance().getReference(user).child("Password").setValue(regPassword.getText().toString());
            FirebaseDatabase.getInstance().getReference(user).child("MobileNumber").setValue(regMobileNumber.getText().toString());
            Toast.makeText(this, "Save data sucessfully", Toast.LENGTH_SHORT).show();
            clearData();
        }
    }

    private void clearData() {
        regUsername.setText("");
        regMobileNumber.setText("");
        regPassword.setText("");
    }

    /************************validate username and password**************************/

    private Boolean validateLoginEmail() {
        String val = loginEmail.getText().toString();
        String pattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        if (val.isEmpty()) {
            loginEmail.setError("Field cannot be empty");
            return false;
        } else if (!val.matches(pattern)) {
            loginEmail.setError("Invalid Email");
            return false;
        } else {
            loginEmail.setError(null);
            return true;
        }
    }

    private Boolean validateLoginPassword() {
        String val = loginPassword.getText().toString();
        String pattern = "^" +
                "(?=.*[a-zA-Z])" + //any letter
                "(?=.*[@#$%^&+])" +//atleast one special character
                "(?=.*[\\S+$])" +// no white space
                ".{4,}" +//atleast 4 characters
                "$";
        if (val.isEmpty()) {
            loginPassword.setError("Field cannot be empty");
            return false;
        } else if (!val.matches(pattern)) {
            loginPassword.setError("Password is too weak");
            return false;
        } else {
            loginPassword.setError(null);
            return true;
        }
    }

    private void clearLoginData() {
        loginEmail.setText("");
        loginPassword.setText("");
    }

    public void buttonLogin(View view) {
        if (!validateLoginEmail() | !validateLoginPassword()) {
            return;
        } else {
            isUser();
            clearLoginData();
        }
    }

    private void isUser() {
        final String userEnteredUsername = loginEmail.getText().toString().trim();
        final String userEnteredPassword = loginPassword.getText().toString().trim();

        Query checkUser = FirebaseDatabase.getInstance().getReference(user).orderByChild("Username").equalTo(userEnteredUsername);

        checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    loginEmail.setError(null);
                    String passwordFromDB = dataSnapshot.child(userEnteredUsername).child("Password").getValue(String.class);

                    if(passwordFromDB.equals(userEnteredPassword))
                    {   loginPassword.setError(null);
                        Toast.makeText(MainActivity.this, "Successfully login", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        loginPassword.setError("Wrong Password");
                        loginPassword.requestFocus();
                    }
                }else {
                    loginEmail.setError("No such User exist");
                    loginEmail.requestFocus();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Database Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /************************verify mobile no by otp**************************/

    public void buttonGenerateOTP(View view) {
        final String mobileNumber = mobileNumberEnterByUser.getText().toString();
        sendVerificationCodeToUser(mobileNumber);
    }

    private void sendVerificationCodeToUser(String mobileNumber) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+91" + mobileNumber, // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,     // Unit of timeout
                TaskExecutors.MAIN_THREAD, // Activity (for callback binding)
                mCallbacks);         // OnVerificationStateChangedCallbacks
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        //other device
        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            verificationCodeBySystem = s;
        }

        //same device
        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            String code = phoneAuthCredential.getSmsCode();
            if (code != null) {
                progressBar.setVisibility(View.VISIBLE);
                verifyCode(code);
            }
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    };

    private void verifyCode(String codeByUser) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationCodeBySystem, codeByUser);
        signInTheUserByCredential(credential);
    }

    private void signInTheUserByCredential(PhoneAuthCredential credential) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Successfully Verified", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "signInTheUserByCredential :" + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void buttonVerifyOTP(View view) {
        String code = verifyOTP.getText().toString();
        if (code.isEmpty() || code.length() < 6) {
            mobileNumberEnterByUser.setError("Wrong OTP...");
            mobileNumberEnterByUser.requestFocus();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        verifyCode(code);
    }

}
