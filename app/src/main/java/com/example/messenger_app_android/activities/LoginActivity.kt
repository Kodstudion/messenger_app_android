package com.example.messenger_app_android.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.example.messenger_app_android.R
import com.example.messenger_app_android.fragments.LoginWithEmailFragment
import com.example.messenger_app_android.utilities.Utilities
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_home.*

class LoginActivity : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleSignInCV: CardView
    private lateinit var auth: FirebaseAuth
    private lateinit var emailLogin: TextView
    private lateinit var loginWithEmailFrameLayout: FrameLayout

    val TAG = "!!!"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        googleSignInCV = findViewById(R.id.google_sign_in_cv)
        auth = Firebase.auth
        emailLogin = findViewById(R.id.sign_in_with_email_tv)
        loginWithEmailFrameLayout = findViewById(R.id.fragment_container)

        val utilities = Utilities()
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.SHA1_key)).requestEmail().build()

        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)

        googleSignInCV.setOnClickListener {
            signIn()
        }

        emailLogin.setOnClickListener {
          utilities.loadFragment(LoginWithEmailFragment(), supportFragmentManager)
        }


    }


    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }


    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                account.idToken?.let { firebaseAuthWithGoogle(it) }
            } catch (e: ApiException) {
                Log.d(TAG, "onActivityResult: Google sign in failed $e")
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "firebaseAuthWithGoogle: Success logged in")
                val user = auth.currentUser
                updateUI(user)
            } else {
                Log.d(TAG, "firebaseAuthWithGoogle: FAILED")
                updateUI(null)
            }
        }
    }

    private fun updateUI(user: FirebaseUser?) {
        user?.let {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity((intent))
        }
    }

    companion object {
        const val RC_SIGN_IN = 1001

    }

}



