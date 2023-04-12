package com.example.messenger_app_android.activities

import android.content.Intent
import android.content.IntentSender
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.example.messenger_app_android.R
import com.example.messenger_app_android.fragments.LoginWithEmailFragment
import com.example.messenger_app_android.utilities.Utilities
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private lateinit var signInRequest: BeginSignInRequest
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var onTapClient: SignInClient
    private lateinit var googleSignInCV: CardView
    private lateinit var auth: FirebaseAuth
    private lateinit var emailLogin: TextView
    private lateinit var loginWithEmailFrameLayout: FrameLayout

    val TAG = "!!!"
    private val REQ_ONE_TAP = 2
    private var showOneTapUI = true

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        googleSignInCV = findViewById(R.id.google_sign_in_cv)
        auth = Firebase.auth
        emailLogin = findViewById(R.id.sign_in_with_email_tv)
        loginWithEmailFrameLayout = findViewById(R.id.fragment_container)

        val utilities = Utilities()

        onTapClient = Identity.getSignInClient(this)
        signInRequest = BeginSignInRequest.builder()
            .setPasswordRequestOptions(
                BeginSignInRequest.PasswordRequestOptions.builder()
                    .setSupported(true)
                    .build()
            )
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(getString(R.string.client_id))
                    .setFilterByAuthorizedAccounts(true)
                    .build()
            )
            .setAutoSelectEnabled(true)
            .build()

        onTapClient.beginSignIn(signInRequest).addOnSuccessListener { result ->
            try {
                startIntentSenderForResult(
                    result.pendingIntent.intentSender,
                    REQ_ONE_TAP,
                    null,
                    0,
                    0,
                    0,
                    null
                )
            } catch (e: IntentSender.SendIntentException) {
                Log.e(TAG, "onCreate: $e")
            }

        }
            .addOnFailureListener { e ->
                Log.d(TAG, "Error while signing in", e)
            }

        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.client_id)).requestEmail().build()


//        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)
//
//        googleSignInCV.setOnClickListener {
//            signIn()
//        }
//
        emailLogin.setOnClickListener {
            utilities.loadFragment(LoginWithEmailFragment(), supportFragmentManager)
        }


    }

//    companion object {
//        const val RC_SIGN_IN = 1001
//
//    }

//    @Deprecated("Deprecated in Java")
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == RC_SIGN_IN) {
//            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
//            try {
//                val account = task.getResult(ApiException::class.java)
//                account.idToken?.let { firebaseAuthWithGoogle(it) }
//            } catch (e: ApiException) {
//                Log.d(TAG, "onActivityResult: Google sign in failed $e")
//            }
//        }
//    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQ_ONE_TAP -> {
                try {
                    val credential = onTapClient.getSignInCredentialFromIntent(data)
                    val idToken = credential.googleIdToken
                    val username = credential.id
                    val password = credential.password

                    when {
                        idToken != null -> {
                            firebaseAuthWithGoogle(idToken)

                            Log.d(TAG, "onActivityResult: $idToken")
                        }
                        password != null -> {
                            Log.d(TAG, "onActivityResult: Got password")
                        }
                        else -> {
                            Log.d(TAG, "onActivityResult: No ID token or password")
                        }
                    }

                } catch (e: ApiException) {
                    Log.e(TAG, "onActivityResult: $e")
                    when (e.statusCode) {
                        CommonStatusCodes.CANCELED -> {
                            showOneTapUI = false
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        updateUI(currentUser)
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

//    private fun signIn() {
//        val signInIntent = googleSignInClient.signInIntent
//        startActivityForResult(signInIntent, RC_SIGN_IN)
//    }


    private fun updateUI(user: FirebaseUser?) {
        user?.let {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity((intent))
        }
    }
}



