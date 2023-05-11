package com.example.messenger_app_android.activities


import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.example.messenger_app_android.R
import com.example.messenger_app_android.fragments.LoginWithEmailFragment
import com.example.messenger_app_android.utilities.Utilities
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.makeramen.roundedimageview.RoundedTransformationBuilder
import com.squareup.picasso.Picasso

class LoginActivity : AppCompatActivity() {

    private lateinit var signInRequest: BeginSignInRequest
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var onTapClient: SignInClient
    private lateinit var googleSignInCV: CardView
    private lateinit var auth: FirebaseAuth
    private lateinit var emailLogin: TextView
    private lateinit var loginWithEmailFrameLayout: FrameLayout
    private lateinit var backgroundView: ImageView
    private lateinit var loginPhoto: ImageView

    val TAG = "!!!"
    private val REQ_ONE_TAP = 2
    private var showOneTapUI = true
    private val PICK_IMAGE_REQUEST = 1
    private val READ_EXTERNAL_STORAGE_REQUEST_CODE = 123

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val viewBackgroundUrl =
            "https://image.winudf.com/v2/image/Y29tLmNvZGVGYWN0b3J5LndhV2FsbHBhcGVyc19zY3JlZW5fMV8xNTMwNTY4MzE5XzA2Nw/screen-1.jpg?fakeurl=1&type=.webp"
        val utilities = Utilities()

        googleSignInCV = findViewById(R.id.google_sign_in_cv)
        auth = Firebase.auth

        emailLogin = findViewById(R.id.sign_in_with_email_tv)
        loginWithEmailFrameLayout = findViewById(R.id.fragment_container)
        backgroundView = findViewById(R.id.background_iv)
        loginPhoto = findViewById(R.id.login_photo_iv)

        Picasso.get().load(viewBackgroundUrl).fit().centerCrop().into(backgroundView)


        loginPhoto.setOnClickListener {
            openImagePicker()
        }

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

        emailLogin.setOnClickListener {
            utilities.loadFragment(LoginWithEmailFragment(), supportFragmentManager)
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            val imageUri = data?.data
            handleSelectedImage(imageUri)
        }
        when (requestCode) {
            REQ_ONE_TAP -> {
                try {
                    val credential = onTapClient.getSignInCredentialFromIntent(data)
                    val idToken = credential.googleIdToken
                    val username = credential.id
                    val password = credential.password
                    val photoUri = credential.profilePictureUri

                    when {
                        idToken != null -> {
                            firebaseAuthWithGoogle(idToken)

                            Log.d(TAG, "onActivityResult: $idToken")
                        }
                        password != null -> {
                            Log.d(TAG, "onActivityResult: Got password")
                        }
                        photoUri != null -> {
                            Log.d(TAG, "onActivityResult: Got photo")
                        }
                        else -> {
                            Log.d(TAG, "onActivityResult: No ID token or password or photo")
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
        val photoUri = auth.currentUser?.photoUrl
        updateUI(currentUser, photoUri)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "firebaseAuthWithGoogle: Success logged in")
                val user = auth.currentUser
                val photoUri = user?.photoUrl
                updateUI(user, photoUri)

            } else {
                Log.d(TAG, "firebaseAuthWithGoogle: FAILED")
                updateUI(null, null)
            }
        }
    }


    private fun updateUI(user: FirebaseUser?, photoUri: Uri?) {
        user?.let {
            val intent = Intent(this, HomeActivity::class.java)
            intent.putExtra("photoUri", photoUri)
            startActivity((intent))
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    private fun handleSelectedImage(imageUri: Uri?) {
        imageUri?.let { uri ->
            val storageRef = FirebaseStorage.getInstance().reference
            val imageFileName = "images/profile_pictures/${auth.currentUser?.uid}.jpg"
            val imageRef = storageRef.child(imageFileName)

            imageRef.putFile(uri).addOnSuccessListener { taskSnapshot ->
                Log.d(TAG, "handleSelectedImage: ${taskSnapshot.metadata?.path}")
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    Picasso.get().load(uri).fit().transform(
                        RoundedTransformationBuilder()
                            .cornerRadius(100f)
                            .oval(true)
                            .build()
                    ).into(loginPhoto)
                }
            }.addOnFailureListener { e ->
                Log.e(TAG, "handleSelectedImage: $e")
            }
        }
    }
}



