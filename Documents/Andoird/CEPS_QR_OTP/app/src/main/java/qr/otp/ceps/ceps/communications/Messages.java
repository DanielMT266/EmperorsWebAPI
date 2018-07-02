package qr.otp.ceps.ceps.communications;

import android.util.Base64;
import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;

import qr.otp.ceps.ceps.Variables;
import services.MyFirebaseMessagingService;

/**
 * Created by Johnie on 2017/05/18.
 */

public class Messages {

    static boolean check = false;

    public boolean sendMes() {
        return sendMes(Variables.mes);
    }

    public boolean sendMes(String Mes) {
        try {

            if (Variables._serverCon == null) {
                Variables._serverCon = new ServerConnection();
            }

            Variables._serverCon.connectToServer();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            FirebaseCrash.logcat(Log.ERROR, MyFirebaseMessagingService.TAG, "Error sending request");
            FirebaseCrash.report(e);
            return false;
            // return check;

        }


        Variables.mes = Base64.encodeToString(Mes.getBytes(), Base64.DEFAULT);
        Variables.int_State = 2;

        while (true) {

            if (Variables.serverOut.equals(Variables.MessageState.MSG_SUCCESSFUL)) {
                check = true;
                break;
            } else if (Variables.serverOut.equals(Variables.MessageState.MSG_FAILED)) {
                check = false;
                break;
            } else if (Variables.serverOut.equals(Variables.MessageState.MSG_IO_ERROR)) {
                check = false;
                break;
            }
        }

        Variables._serverCon.close();
        return check;
    }

    public enum MessagStates {
        MS_RECEIVED, MS_TRANSMITTED, MS_WAITING_RESPONSE, MS_RESPONSE_RECEIVED, MS_FAILED
    }
}
