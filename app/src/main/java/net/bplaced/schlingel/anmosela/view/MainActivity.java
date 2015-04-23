package net.bplaced.schlingel.anmosela.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;

import de.dfki.android.gesture.R;

public class MainActivity extends Activity implements RadioGroup.OnCheckedChangeListener, View.OnClickListener {
    private RadioGroup rgGestures;
    private Button btnStop;
    private Button btnStart;
    private TextView txtVwStatus;
    private String selectedPattern = RemoteCommands.NEXT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        txtVwStatus = (TextView)findViewById(R.id.txtVwStatus);
        rgGestures = (RadioGroup)findViewById(R.id.rgGestures);
        btnStart = (Button)findViewById(R.id.btnStart);
        btnStop = (Button)findViewById(R.id.btnStop);

        rgGestures.setOnCheckedChangeListener(this);
        btnStart.setOnClickListener(this);
        btnStop.setOnClickListener(this);

        startService();

    }

    private void startService() {
        Intent i = new Intent();
        i.setAction("commands.RemoteStartup");
        startService(i);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        stopRecording();

        switch (checkedId) {
            case R.id.rbNext:
                selectedPattern = RemoteCommands.NEXT;
                break;
            case R.id.rbPrevious:
                selectedPattern = RemoteCommands.PREV;
                break;
            case R.id.rbPause:
                selectedPattern = RemoteCommands.PAUSE;
                break;
            case R.id.rbPlay:
                selectedPattern = RemoteCommands.PLAY;
                break;
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btnStart) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void startRecording() {
        txtVwStatus.setText("Recording gesture");
        Intent i = new Intent(RemoteCommands.START_LEARNING);
        i.putExtra("gesture", selectedPattern);

        startService(i);
    }

    private void stopRecording() {
        txtVwStatus.setText("Stopped gesture recording");
        startService(new Intent(RemoteCommands.STOP_LEARNING));
    }
}
