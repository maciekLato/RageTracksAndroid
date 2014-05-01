package com.macieklato.ragetracks.widget;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.FacebookDialog;
import com.facebook.widget.LoginButton;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;
import com.macieklato.ragetracks.R;
import com.macieklato.ragetracks.model.Song;

public class FacebookFragment extends Fragment {

	public static final String TAG = "FacebookFragment";
	public static final int SHARE_REQUEST = 123;

	private Session.StatusCallback callback = new Session.StatusCallback() {
		@Override
		public void call(Session session, SessionState state,
				Exception exception) {
			onSessionStateChange(session, state, exception);
		}
	};

	private UiLifecycleHelper uiHelper;
	private LoginButton authButton;
	private Song postSong;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		uiHelper = new UiLifecycleHelper(getActivity(), callback);
		uiHelper.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.login_fragment, container, false);
		authButton = (LoginButton) view.findViewById(R.id.authButton);
		authButton.setPublishPermissions("publish_actions");
		authButton.setFragment(this);

		return view;
	}

	private void onSessionStateChange(Session session, SessionState state,
			Exception exception) {
		if (state.isOpened()) {
			Log.i(TAG, "Logged in...");
			if (postSong != null) {
				share(postSong);
				postSong = null;
			}
		} else if (state.isClosed()) {
			Log.i(TAG, "Logged out...");
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		uiHelper.onResume();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		uiHelper.onActivityResult(requestCode, resultCode, data,
				new FacebookDialog.Callback() {
					@Override
					public void onError(FacebookDialog.PendingCall pendingCall,
							Exception error, Bundle data) {
						Log.e(TAG, String.format("Error: %s", error.toString()));
					}

					@Override
					public void onComplete(
							FacebookDialog.PendingCall pendingCall, Bundle data) {
						Log.i(TAG, "Success!");
					}
				});
	}

	@Override
	public void onPause() {
		super.onPause();
		uiHelper.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		uiHelper.onDestroy();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		uiHelper.onSaveInstanceState(outState);
	}

	public void share(Song s) {
		if (s == null)
			return;
		Session session = Session.getActiveSession();
		if (session == null || !session.isOpened()) {
			authButton.performClick();
			postSong = s;
		} else {
			if (FacebookDialog.canPresentShareDialog(getActivity()
					.getApplicationContext(),
					FacebookDialog.ShareDialogFeature.SHARE_DIALOG)) {
				// Publish the post using the Share Dialog
				FacebookDialog shareDialog = new FacebookDialog.ShareDialogBuilder(
						getActivity()).setLink(s.getRageTracksUrl())
						.setPicture(s.getThumbnailUrl())
						.setCaption(s.getArtist() + " - " + s.getTitle())
						.setRequestCode(SHARE_REQUEST).build();
				uiHelper.trackPendingDialogCall(shareDialog.present());

			} else {
				// Fallback. For example, publish the post using the Feed Dialog
				publishFeedDialog(s);
			}
		}
	}

	private void publishFeedDialog(final Song s) {
		Bundle params = new Bundle();
		params.putString("caption", s.getArtist() + " - " + s.getTitle());
		params.putString("link", s.getRageTracksUrl());
		params.putString("picture", s.getThumbnailUrl());

		WebDialog feedDialog = (new WebDialog.FeedDialogBuilder(getActivity(),
				Session.getActiveSession(), params)).setOnCompleteListener(
				new OnCompleteListener() {

					@Override
					public void onComplete(Bundle values,
							FacebookException error) {
						if (error == null) {
							// When the story is posted, echo the success
							// and the post Id.
							final String postId = values.getString("post_id");
							if (postId != null) {
								Toast.makeText(
										getActivity(),
										"Posted song: " + s.getTitle() + " - "
												+ s.getArtist(),
										Toast.LENGTH_SHORT).show();
							} else {
								// User clicked the Cancel button
								Toast.makeText(
										getActivity().getApplicationContext(),
										"Publish cancelled", Toast.LENGTH_SHORT)
										.show();
							}
						} else if (error instanceof FacebookOperationCanceledException) {
							// User clicked the "x" button
							Toast.makeText(
									getActivity().getApplicationContext(),
									"Publish cancelled", Toast.LENGTH_SHORT)
									.show();
						} else {
							// Generic, ex: network error
							Toast.makeText(
									getActivity().getApplicationContext(),
									"Error posting song", Toast.LENGTH_SHORT)
									.show();
						}
					}
				}).build();
		feedDialog.show();
	}
}
