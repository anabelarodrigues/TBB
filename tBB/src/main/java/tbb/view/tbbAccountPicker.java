package tbb.view;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.common.AccountPicker;

import tbb.core.CoreController;

/**
 * Created by Anabela on 06/01/2016.
 */
public class TBBAccountPicker extends Activity {

    private static final int PICK_ACCOUNT_REQUEST = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startActivityForResult(AccountPicker.newChooseAccountIntent(null, null, new String[]{"com.google"}, true, null, null, null, null), PICK_ACCOUNT_REQUEST);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == PICK_ACCOUNT_REQUEST) {

            if(resultCode == Activity.RESULT_OK){

                if (data != null && data.getExtras() != null) {

                    String accountName = data.getExtras().getString(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {

                        CoreController.sharedInstance().setAccount(accountName);


                    }
                }
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //startActivityForResult(AccountPicker.newChooseAccountIntent(null, null, new String[]{"com.google"}, true, null, null, null, null), PICK_ACCOUNT_REQUEST);

            }
        }
        finish();
    }
}
