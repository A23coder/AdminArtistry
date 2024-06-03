package com.aadhya.adminartistry.presentation.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.aadhya.adminartistry.presentation.admin.Admin
import com.aadhya.adminartistry.databinding.ActivityAuthenticationBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class Authentication : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var _binding: ActivityAuthenticationBinding
    private lateinit var verificationId: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityAuthenticationBinding.inflate(layoutInflater)
        setContentView(_binding.root)
        FirebaseApp.initializeApp(this)

        _binding.btnGetOtp.setOnClickListener {
            _binding.layoutEnterOtp.visibility = View.GONE
            _binding.layoutSetOtp.visibility = View.VISIBLE
        }
        mAuth = FirebaseAuth.getInstance()

//        _binding.idBtnGetOtp.setOnClickListener {
//            if (_binding.idEdtPhoneNumber.text.isEmpty()) {
//                Toast.makeText(this , "Please enter phone number" , Toast.LENGTH_SHORT).show()
//            } else {
//                val phone = "+91${_binding.idEdtPhoneNumber.text}"
//                sendVerificationCode(phone)
//            }
//        }

//        _binding.idBtnVerify.setOnClickListener {
//            if (_binding.idEdtOtp.text.isEmpty()) {
//                Toast.makeText(this , "Please enter OTP" , Toast.LENGTH_SHORT).show()
//            } else {
//                verifyCode(_binding.idEdtOtp.text.toString())
//            }
//        }
    }

    private fun verifyCode(edtOtp: String) {
        val credential = PhoneAuthProvider.getCredential(verificationId , edtOtp)
        signInWithCredential(credential)
    }

    private fun sendVerificationCode(phone: String) {
        val options = PhoneAuthOptions.newBuilder(mAuth).setPhoneNumber(phone)
            .setTimeout(60L , TimeUnit.SECONDS).setActivity(this).setCallbacks(mCallback).build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private val mCallback = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onCodeSent(p0: String , p1: PhoneAuthProvider.ForceResendingToken) {
            super.onCodeSent(p0 , p1)
            verificationId = p0
        }


        override fun onVerificationCompleted(p0: PhoneAuthCredential) {
            val code = p0.smsCode
            if (code != null) {
//                val edtOtp = _binding.idEdtOt
//                edtOtp.setText(code.toString())
//                _binding.idEdtOtp.setText(code.toString())
                verifyCode(code)
            }
        }

        override fun onVerificationFailed(p0: FirebaseException) {
            Toast.makeText(this@Authentication , "${p0.message}" , Toast.LENGTH_SHORT).show()
        }

    }

    private fun signInWithCredential(credential: PhoneAuthCredential) {
        mAuth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val intent = Intent(this@Authentication , Admin::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(
                    this , task.exception?.message.toString() , Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}