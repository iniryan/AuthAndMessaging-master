package com.example.authandmessaging

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var REQ_CODE: Int = 3
    private var googleSignInClient: GoogleSignInClient? = null
    private var mAuth: FirebaseAuth? = null
    private var mAuthStateListener: FirebaseAuth.AuthStateListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Set Visibility prof_section
        prof_section.visibility = View.GONE

        //Get Firebase Instance
        mAuth = FirebaseAuth.getInstance()

        //Set Firebase Auth Listener
        mAuthStateListener = FirebaseAuth.AuthStateListener() {
            fun onAuthStateChange(firebaseAuth: FirebaseAuth){}
        }

        //Get Instance id
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if(!task.isSuccessful){
                    Log.w(ContentValues.TAG, "getInstanceId Failed", task.exception)
                }

                //Get Instance id token
                val token = task.result?.token

                //Log
                Log.d(ContentValues.TAG, "Google Token : "+ token)
                Toast.makeText(baseContext, token, Toast.LENGTH_SHORT).show()
            })

        val signInOptions : GoogleSignInOptions = GoogleSignInOptions.Builder(
            GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail().build()

        googleSignInClient = GoogleSignIn.getClient(this, signInOptions)

        btn_signout.setOnClickListener {signOut()}
        btn_login.setOnClickListener{signIn()}
    }

    private fun signOut(){
        mAuth?.signOut()

        googleSignInClient?.signOut()?.addOnCompleteListener(this) {
            updateUI(false)
        }
    }

    private fun signIn(){
        val signInIntent = googleSignInClient?.signInIntent
        startActivityForResult(signInIntent, REQ_CODE)
    }

    private fun updateUI (isLogin: Boolean){
        if(isLogin){
            prof_section.visibility= View.VISIBLE
            btn_login.visibility = View.GONE
        }
        else {
            prof_section.visibility = View.GONE
            btn_login.visibility = View.VISIBLE
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQ_CODE) {
            val result = GoogleSignIn.getSignedInAccountFromIntent(data)
            try{
                val account = result.getResult(ApiException::class.java)
                handleResult(account!!)
            }
            catch (e: ApiException){
                Log.d("SIGN IN", "Signin Failed", e)
            }
        }
    }

    private fun handleResult(result : GoogleSignInAccount){
        val credential = GoogleAuthProvider.getCredential(result.idToken, null)

        mAuth!!.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if(task.isSuccessful) {
                    txt_nama.text = result.displayName
                    txt_email.text = result.email

                    Glide.with(this )
                        .load(result.photoUrl.toString())
                        .into(image_profile)

                    updateUI(true )
                }
                else{
                    updateUI(false)
                }
            }
    }
}
