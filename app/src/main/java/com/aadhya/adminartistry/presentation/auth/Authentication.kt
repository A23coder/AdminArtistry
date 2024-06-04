package com.aadhya.adminartistry.presentation.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.aadhya.adminartistry.databinding.ActivityAuthenticationBinding
import com.aadhya.adminartistry.presentation.admin.Admin
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
    private lateinit var number: String

    private var isLogging: Boolean = false
    private var userMobile: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityAuthenticationBinding.inflate(layoutInflater)
        setContentView(_binding.root)
        FirebaseApp.initializeApp(this)

        userMobile = ""
        number = ""
        mAuth = FirebaseAuth.getInstance()

        _binding.btnGetOtp.setOnClickListener {

            if (_binding.idEdtPhoneNumber.text.isEmpty()) {
                Toast.makeText(this , "Please enter a phone number.." , Toast.LENGTH_SHORT).show()
            } else {
                number = "+91${_binding.idEdtPhoneNumber.text}"
                println("Number is $number")

                if (number == "+919081069042" || number == "+917573838402" || number=="+919274929291") {
                    _binding.layoutEnterOtp.visibility = View.GONE
                    _binding.layoutSetOtp.visibility = View.VISIBLE
                    sendCode(number)
                    Toast.makeText(this , "Otp sent Successfully.." , Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this , "You are not Admin.." , Toast.LENGTH_SHORT).show()
                }
            }
        }

        _binding.btnVerifyOtp.setOnClickListener {
            val otp = _binding.otpTake.otp.toString()
            Log.d("OTP" , "OTP IS $otp")
            if (otp.isEmpty()) {
                Toast.makeText(this , "Please enter OTP" , Toast.LENGTH_SHORT).show()
            } else {
                verifyCode(otp)
            }
        }
    }

    private fun sendCode(number: String) {
        sendVerificationCode(number)
    }

    private fun verifyCode(otp: String) {
        val credential = PhoneAuthProvider.getCredential(verificationId , otp)
        signInWithCredential(credential)
    }

    private fun sendVerificationCode(phone: String) {
        val options = PhoneAuthOptions.newBuilder(mAuth).setPhoneNumber(phone)
            .setTimeout(120L , TimeUnit.SECONDS).setActivity(this).setCallbacks(mCallback).build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private val mCallback = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onCodeSent(
            verificationId: String ,
            token: PhoneAuthProvider.ForceResendingToken ,
        ) {
            super.onCodeSent(verificationId , token)
            this@Authentication.verificationId = verificationId
        }

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            val code = credential.smsCode
            if (code != null) {
                _binding.otpTake.otpEditText?.setText(code)
                verifyCode(code)
            }
        }

        override fun onVerificationFailed(e: FirebaseException) {
            Toast.makeText(this@Authentication , e.message , Toast.LENGTH_SHORT).show()
        }
    }

    private fun signInWithCredential(credential: PhoneAuthCredential) {
        mAuth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                userMobile = number
                isLogging = true

                val sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE)
                val myEdit = sharedPreferences.edit()
                myEdit.putString("userMobile", userMobile)
                myEdit.putBoolean("isLogging", isLogging)
                myEdit.apply()

                val intent = Intent(this@Authentication , Admin::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this , "Invalid OTP. Please try again." , Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val sh = getSharedPreferences("MySharedPref", MODE_PRIVATE)
        userMobile = sh.getString("userMobile", "")
        val isLoggedIn = sh.getBoolean("isLogging", false)

        if (isLoggedIn) {
            val intent = Intent(this@Authentication, Admin::class.java)
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this , "Enter Otp for accessing Admin App.." , Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPause() {
        super.onPause()
        val sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE)
        val myEdit = sharedPreferences.edit()
        myEdit.putString("userMobile", userMobile)
        myEdit.putBoolean("isLogging", isLogging)
        myEdit.apply()
    }
}
