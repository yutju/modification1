package com.example.myapplication2222;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private Button actionButton; // 로그인/로그아웃 버튼
    private Button deleteAccountButton; // 회원 탈퇴 버튼

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        actionButton = findViewById(R.id.login_button); // 로그인 버튼
        deleteAccountButton = findViewById(R.id.delete_account_button); // 회원 탈퇴 버튼

        // 회원가입 텍스트 클릭 리스너 추가
        TextView signupText = findViewById(R.id.signup_text);
        signupText.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });

        updateButtonForAuthState(); // 로그인 상태에 따라 버튼 설정

        actionButton.setOnClickListener(v -> {
            if (mAuth.getCurrentUser() != null) {
                logoutUser(); // 로그아웃 시도
            } else {
                loginUser(); // 로그인 시도
            }
        });

        // 회원 탈퇴 버튼 클릭 리스너 추가
        deleteAccountButton.setOnClickListener(v -> deleteUserAccount());
    }

    private void deleteUserAccount() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // 비밀번호를 재인증하기 위해 이메일과 비밀번호를 입력하도록 요청합니다.
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "이메일과 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            // 재인증을 수행합니다.
            AuthCredential credential = EmailAuthProvider.getCredential(email, password);
            user.reauthenticate(credential).addOnCompleteListener(reauthTask -> {
                if (reauthTask.isSuccessful()) {
                    // 재인증에 성공한 경우 계정을 삭제합니다.
                    user.delete().addOnCompleteListener(deleteTask -> {
                        if (deleteTask.isSuccessful()) {
                            // 계정 삭제 성공
                            Log.d("LoginActivity", "계정 삭제 성공");
                            Toast.makeText(LoginActivity.this, "계정이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                            // 로그인 화면으로 이동
                            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // 계정 삭제 실패
                            Log.e("LoginActivity", "계정 삭제 실패: " + deleteTask.getException().getMessage());
                            Toast.makeText(LoginActivity.this, "계정 삭제에 실패했습니다: " + deleteTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    // 재인증 실패
                    Log.e("LoginActivity", "재인증 실패: " + reauthTask.getException().getMessage());
                    Toast.makeText(LoginActivity.this, "재인증에 실패했습니다: " + reauthTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void loginUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "이메일과 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null && user.isEmailVerified()) {
                            Log.d("LoginActivity", "로그인 성공: " + user.getEmail());
                            // 로그인 성공 후 메인 화면으로 이동
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(this, "이메일 인증을 완료해주세요.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e("LoginActivity", "로그인 실패: " + task.getException().getMessage());
                        Toast.makeText(this, "로그인 실패: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void logoutUser() {
        mAuth.signOut();
        Toast.makeText(this, "로그아웃되었습니다.", Toast.LENGTH_SHORT).show();
        updateButtonForAuthState(); // 로그아웃 후 버튼 업데이트
    }

    private void updateButtonForAuthState() {
        // 로그인 상태에 따라 버튼 텍스트 변경
        if (mAuth.getCurrentUser() != null) {
            actionButton.setText("로그아웃");
        } else {
            actionButton.setText("로그인");
        }
    }
}
