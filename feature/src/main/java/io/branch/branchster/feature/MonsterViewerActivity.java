package io.branch.branchster.feature;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import fragment.InfoFragment;
import io.branch.indexing.BranchUniversalObject;
import io.branch.referral.Branch;
import io.branch.referral.SharingHelper;
import io.branch.referral.util.LinkProperties;
import io.branch.referral.util.ShareSheetStyle;
import util.MonsterImageView;
import util.MonsterPreferences;

public class MonsterViewerActivity extends Activity implements InfoFragment.OnFragmentInteractionListener {

    private static String TAG = MonsterViewerActivity.class.getSimpleName();
    public static final String MY_MONSTER_OBJ_KEY = "my_monster_obj_key";

    MonsterImageView monsterImageView_;
    BranchUniversalObject myMonsterObject_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monster_viewer);
        initUI();
    }

    private void initUI() {
        monsterImageView_ = findViewById(R.id.monster_img_view);
        if (Branch.isAutoDeepLinkLaunch(this)) {
            MonsterPreferences pref = MonsterPreferences.getInstance(this);
            myMonsterObject_ = BranchUniversalObject.getReferredBranchUniversalObject();
            pref.saveMonster(myMonsterObject_);
        } else {
            myMonsterObject_ = getIntent().getParcelableExtra(MY_MONSTER_OBJ_KEY);
        }

        if (myMonsterObject_ != null) {
            String monsterName = getString(R.string.monster_name);
            if (!TextUtils.isEmpty(myMonsterObject_.getTitle())) {
                monsterName = myMonsterObject_.getTitle();
            } else if (myMonsterObject_.getMetadata().containsKey("monster_name")) {
                monsterName = myMonsterObject_.getMetadata().get("monster_name");
            }
            ((TextView) findViewById(R.id.txtName)).setText(monsterName);

            String description = MonsterPreferences.getInstance(this).getMonsterDescription();
            if (!TextUtils.isEmpty(myMonsterObject_.getDescription())) {
                description = myMonsterObject_.getDescription();
            }
            ((TextView) findViewById(R.id.txtDescription)).setText(description);

            // set my monster image
            monsterImageView_.setMonster(myMonsterObject_);
        } else {
            Log.e(TAG, "Monster is null. Unable to view monster");
        }

        // Change monster
        findViewById(R.id.cmdChange).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), MonsterCreatorActivity.class);
                startActivity(i);
                finish();
            }
        });

        // More info
        findViewById(R.id.infoButton).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                InfoFragment infoFragment = InfoFragment.newInstance();
                ft.replace(R.id.container, infoFragment).addToBackStack("info_container").commit();
            }
        });

        //Share monster
        findViewById(R.id.share_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                shareMyMonster();
            }
        });

        View fullAppInstallButton = findViewById(R.id.app_installer_btn);
        if (Branch.isInstantApp(this)) {
            fullAppInstallButton.setVisibility(View.VISIBLE);
            fullAppInstallButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Branch.showInstallPrompt(MonsterViewerActivity.this, 1001);
                }
            });
        } else {
            fullAppInstallButton.setVisibility(View.GONE);
        }
    }

    /**
     * Method to share my custom monster with sharing with Branch Share sheet
     */
    private void shareMyMonster() {
        LinkProperties linkProperties = new LinkProperties()
                .addTag("myMonsterTag1")
                //.setAlias("myCustomMonsterLink") // In case you need to white label your link
                .setFeature("myMonsterSharefeature1")
                .setStage("1")
                .addControlParameter("$android_deeplink_path", "monster/view/");

        String monsterName = myMonsterObject_.getTitle();
        String shareTitle = "Check out my Branchster named " + monsterName;
        String shareMessage = "I just created this Branchster named " + monsterName + " in the Branch Monster Factory.\n\nSee it here:\n";
        String copyUrlMessage = "Save " + monsterName + " url";
        String copiedUrlMessage = "Added " + monsterName + " url to clipboard";

        ShareSheetStyle shareSheetStyle = new ShareSheetStyle(MonsterViewerActivity.this, shareTitle, shareMessage)
                .setCopyUrlStyle(getResources().getDrawable(android.R.drawable.ic_menu_send), copyUrlMessage, copiedUrlMessage)
                .setMoreOptionStyle(getResources().getDrawable(android.R.drawable.ic_menu_search), "More options")
                .addPreferredSharingOption(SharingHelper.SHARE_WITH.FACEBOOK)
                .addPreferredSharingOption(SharingHelper.SHARE_WITH.EMAIL)
                .addPreferredSharingOption(SharingHelper.SHARE_WITH.MESSAGE)
                .addPreferredSharingOption(SharingHelper.SHARE_WITH.TWITTER);

        myMonsterObject_.showShareSheet(MonsterViewerActivity.this, linkProperties, shareSheetStyle, null);
    }


    @Override
    public void onBackPressed() {
        FragmentManager fm = getFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            fm.popBackStack();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("Exit")
                    .setMessage("Are you sure you want to exit?")
                    .setNegativeButton(android.R.string.no, null)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }).create().show();
        }
    }


    @Override
    public void onFragmentInteraction() {
        //no-op
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        initUI();
    }
}