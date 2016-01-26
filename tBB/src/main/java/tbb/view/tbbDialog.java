package tbb.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import blackbox.tinyblackbox.R;

/**
 * Created by Anabela on 04/01/2016.
 */
public class tbbDialog extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.tbbdialog);

        Button yes = (Button) findViewById(R.id.button2);
        Button later = (Button) findViewById(R.id.button);

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("debug", "yes onclick");
                Intent intent = new Intent(getApplicationContext(),tbbEditor.class);
                startActivity(intent);
                finish();
            }
        });

        later.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


}
