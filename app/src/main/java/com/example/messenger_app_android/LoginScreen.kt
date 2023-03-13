package com.example.messenger_app_android
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.cardview.widget.CardView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginScreen : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleSignInCV: CardView
    private lateinit var auth: FirebaseAuth
    val TAG = "!!!"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_screen)

        googleSignInCV = findViewById(R.id.google_sign_in_cv)
        auth = Firebase.auth

        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.SHA1_key))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this,googleSignInOptions)
        
        googleSignInCV.setOnClickListener {
           signIn()
        }
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivity(intent)
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
            val intent = Intent(applicationContext, MainActivity::class.java)
            intent.putExtra(EXTRA_NAME, user.displayName)
            startActivity((intent))
        }
    }

    companion object {
        const val RC_SIGN_IN = 1001
        const val EXTRA_NAME = "EXTRA NAME"
    }

    override fun onStart() {
        super.onStart()
        if (GoogleSignIn.getLastSignedInAccount(this) != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}



