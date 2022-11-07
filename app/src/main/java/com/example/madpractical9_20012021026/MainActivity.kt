package com.example.madpractical9_20012021026

import android.Manifest
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Telephony
import android.telephony.SmsManager
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.madpractical9_20012021026.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val SMS_PERMISSION_CODE = 1000
    private lateinit var lv: ListView
    private lateinit var al: ArrayList<SMSView>
    private lateinit var smsReceiver: SMSBroadcastReceiver
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lv = binding.listview
        al = ArrayList()

        if (checkRequestPermission()) {
            loadSMSInbox()
        }

        smsReceiver = SMSBroadcastReceiver()
        registerReceiver(smsReceiver, IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION))
        smsReceiver.setListner(ListenerImplement())
        binding.sendButton.setOnClickListener {
            val phone = binding.phoneno.text.toString()
            val msg = binding.message.text.toString()
            sendSms(phone, msg)
            val builder: AlertDialog.Builder = AlertDialog.Builder(this@MainActivity)
            builder.setTitle("Sent SMS")
            builder.setMessage("SMS is sent.\nPhone No : $phone \n\n Message : $msg")
            builder.setCancelable(true)
            builder.setPositiveButton("OK", null);
            builder.show()
        }

    }

    private fun sendSms(sPhoneNo: String?, sMsg: String?) {
        if (!checkRequestPermission()) {
            return
        }
        val smsmanager = SmsManager.getDefault()
        if (smsmanager != null) {
            smsmanager.sendTextMessage(sPhoneNo, null, sMsg, null, null)
        }
    }

    inner class ListenerImplement : SMSBroadcastReceiver.Listner {
        override fun onTextReceived(sPhoneNo: String?, sMsg: String?) {
            val builder: AlertDialog.Builder = AlertDialog.Builder(this@MainActivity)
            builder.setTitle("New SMS Received")
            builder.setMessage("From : $sPhoneNo\n\n Message : $sMsg")
            builder.setCancelable(true)
            builder.setPositiveButton("OK", null);
            builder.show()
            loadSMSInbox()
        }
    }

    private val isSMSReadPermission: Boolean
        get() = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED
    private val isSMSWritePermission: Boolean
        get() = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED

    private fun requestSMSPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_SMS))

        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.READ_SMS,
                Manifest.permission.SEND_SMS,
                Manifest.permission.RECEIVE_SMS
            ),
            SMS_PERMISSION_CODE
        )
    }

    private fun checkRequestPermission(): Boolean {
        return if (!isSMSReadPermission || !isSMSWritePermission) {
            requestSMSPermission()
            false
        } else true
    }

    private fun loadSMSInbox() {
        if (!checkRequestPermission()) return
        val uriSMS = Uri.parse("content://sms/inbox")
        val c = contentResolver.query(uriSMS, null, null, null, null)
        al.clear()
        while (c!!.moveToNext()) {
            al.add(SMSView(c.getString(2), c.getString(12)))
        }
        lv.adapter = SMSViewAdapter(this, al)

    }

    override fun onDestroy() {
        unregisterReceiver(smsReceiver)
        super.onDestroy()
    }
}