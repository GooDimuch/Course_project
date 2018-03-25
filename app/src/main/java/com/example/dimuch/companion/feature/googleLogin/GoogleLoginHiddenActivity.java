package com.example.dimuch.companion.feature.googleLogin;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import com.example.dimuch.companion.data.model.Profile;
import com.example.dimuch.companion.feature.main.MainActivity;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import javax.inject.Inject;
import timber.log.Timber;

public class GoogleLoginHiddenActivity extends AppCompatActivity
    implements GoogleApiClient.OnConnectionFailedListener {

  public static final String EXTRA_CLIENT_ID = "EXTRA_CLIENT_ID";
  private static final int RC_SIGN_IN = 1000;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    GoogleSignInOptions gso =
        new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestId()
            .requestProfile()
            .requestIdToken(getIntent().getStringExtra(EXTRA_CLIENT_ID))
            .requestEmail()
            .build();

    GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this, this)
        .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
        .build();

    Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
    startActivityForResult(signInIntent, RC_SIGN_IN);
  }

  @Override public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    Throwable throwable = new Throwable(connectionResult.getErrorMessage());
    Timber.e("onConnectionFailed " + throwable);
    finish();
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == RC_SIGN_IN) {
      GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
      handleSignInResult(result);
    }
  }

  private void handleSignInResult(GoogleSignInResult result) {
    if (result.isSuccess()) {
      GoogleSignInAccount acct = result.getSignInAccount();
      SocialUser user = new SocialUser();
      user.userId = acct.getId();
      user.accessToken = acct.getIdToken();
      user.photoUrl = acct.getPhotoUrl() != null ? acct.getPhotoUrl().toString() : "";
      Profile profile = user.profile;
      profile.email = acct.getEmail();
      profile.name = acct.getDisplayName();
      profile.firstName = acct.getGivenName();
      profile.lastName = acct.getFamilyName();
      profile.photoUrl = user.photoUrl;
      Timber.e("handleSignInResult : " +user.toString());

      startActivity(new Intent(getApplicationContext(), MainActivity.class));

    } else {
      Throwable throwable = new Throwable(result.getStatus().getStatusMessage());
      Timber.e("handleSignInResult " + throwable);

    }

    finish();
  }
}
