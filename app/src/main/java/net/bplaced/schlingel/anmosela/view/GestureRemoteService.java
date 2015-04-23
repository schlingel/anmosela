package net.bplaced.schlingel.anmosela.view;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import de.dfki.ccaal.gestures.IGestureRecognitionListener;
import de.dfki.ccaal.gestures.IGestureRecognitionService;
import de.dfki.ccaal.gestures.classifier.Distribution;

/**
 * Created by zombie on 23.04.15.
 */
public class GestureRemoteService extends Service {
    private static final String TAG = GestureRemoteService.class.getSimpleName();
    private static final String TRAININGS_SET = "GestureRemoteTrainingsSet";

    IGestureRecognitionService recognitionService;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            recognitionService = IGestureRecognitionService.Stub.asInterface(service);
            try {
                recognitionService.startClassificationMode(TRAININGS_SET);
                recognitionService.registerListener(IGestureRecognitionListener.Stub.asInterface(gestureListenerStub));

                Log.i(TAG, "recogination service setup succeeded!");
            } catch (RemoteException e1) {
                Log.i(TAG, "recogination service setup failed!");

                e1.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            recognitionService = null;
        }
    };

    IBinder gestureListenerStub = new IGestureRecognitionListener.Stub() {
        @Override
        public void onGestureLearned(String gestureName) throws RemoteException {
            Log.i(TAG, String.format("Gesture %s learned", gestureName));
        }

        @Override
        public void onTrainingSetDeleted(String trainingSet) throws RemoteException {
            Log.i(TAG, String.format("Training set %s deleted", trainingSet));
        }

        @Override
        public void onGestureRecognized(final Distribution distribution) throws RemoteException {
            Log.i(TAG, String.format("Gesture %s recognized (%f)", distribution.getBestMatch(), distribution.getBestDistance()));

            if(distribution.getBestDistance() < 3) {
                Log.i(TAG, String.format("Got only %f as distance. ignoring ...", distribution.getBestDistance()));
                return;
            }

            switch (distribution.getBestMatch()) {
                case RemoteCommands.NEXT:
                    onNext();
                    break;
                case RemoteCommands.PREV:
                    onPrev();
                    break;
                case RemoteCommands.PLAY:
                    onPlay();
                    break;
                case RemoteCommands.PAUSE:
                    onPause();
                    break;
            }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Started service!");

        handleIntent(intent);

        if(recognitionService == null)  {
            Intent bindIntent = new Intent("de.dfki.ccaal.gestures.GESTURE_RECOGNIZER");
            bindService(bindIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        }

        return super.onStartCommand(intent, flags, startId);
    }



    private void handleIntent(Intent i) {
        String action = i.getAction();

        Log.i(TAG, "Got action " + action);

        switch (action) {
            case RemoteCommands.STARTUP:
                break;
            case RemoteCommands.NEXT:
                onNext();
                break;
            case RemoteCommands.PREV:
                onPrev();
                break;
            case RemoteCommands.PLAY:
                onPlay();
                break;
            case RemoteCommands.PAUSE:
                onPause();
                break;
            case RemoteCommands.STOP_LEARNING:
                onStopLearning();
                break;
            case RemoteCommands.START_LEARNING:
                onStartLearning(i.getStringExtra("gesture"));
                break;
        }
    }

    private void onNext() {
        sendMediaButtonKeyPress(KeyEvent.KEYCODE_MEDIA_NEXT);
        Log.i(TAG, "Send Next media button stuff");
    }

    private void onPrev() {
        sendMediaButtonKeyPress(KeyEvent.KEYCODE_MEDIA_PREVIOUS);
        sendMediaButtonKeyPress(KeyEvent.KEYCODE_MEDIA_PREVIOUS);
        Log.i(TAG, "Send prev media button stuff");
    }


    private void onPause() {
        sendMediaButtonKeyPress(KeyEvent.KEYCODE_MEDIA_PAUSE);
        Log.i(TAG, "Send pause media button stuff");
    }

    private void onPlay() {
        sendMediaButtonKeyPress(KeyEvent.KEYCODE_MEDIA_PLAY);
        Log.i(TAG, "Send play media button stuff");
    }

    private void sendMediaButtonKeyPress(int eventCode) {
        Intent i = new Intent(Intent.ACTION_MEDIA_BUTTON);
        i.putExtra(Intent.EXTRA_KEY_EVENT,new KeyEvent(KeyEvent.ACTION_DOWN, eventCode));
        sendOrderedBroadcast(i, null);

        i = new Intent(Intent.ACTION_MEDIA_BUTTON);
        i.putExtra(Intent.EXTRA_KEY_EVENT,new KeyEvent(KeyEvent.ACTION_UP, eventCode));
        sendOrderedBroadcast(i, null);
    }

    public void onStopLearning() {
        try {
            recognitionService.stopLearnMode();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void onStartLearning(String gesture) {
        try {
            recognitionService.startLearnMode(TRAININGS_SET, gesture);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        try {
            recognitionService.unregisterListener(IGestureRecognitionListener.Stub.asInterface(gestureListenerStub));
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        recognitionService = null;
        unbindService(serviceConnection);

        Log.i(TAG, "Goodby service!");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new Binder();
    }
}
