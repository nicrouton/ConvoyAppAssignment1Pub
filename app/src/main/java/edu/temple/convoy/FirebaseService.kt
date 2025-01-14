package edu.temple.convoy

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import edu.temple.convoy.CustomDialogFragment.Companion.TAG
import org.json.JSONObject

class FirebaseService :  FirebaseMessagingService() {





    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")

            val payload = remoteMessage.data["payload"]



            // Check if message contains a data payload.


            val jsonData = JSONObject(payload)

            // Broadcast the JSON object
            val intent = Intent("YOUR_ACTION_NAME")
            intent.putExtra("JSON_DATA", jsonData.toString())
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)




            if (/* Check if data needs to be processed by long running job */ true) {
                // For long-running tasks (10 seconds or more) use WorkManager.
                //scheduleJob()
            } else {
                // Handle message within 10 seconds
                //handleNow()
            }
        }
    }

    override fun onNewToken(token: String) {
        // Send broadcast indicating token refresh
        val intent = Intent("TOKEN_REFRESHED")
        intent.putExtra("TOKEN", token)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)

        Log.d("new token", "FCM service received new token")

    }
}