package com.example.basicandroidmqttclient;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.ArrayAdapter;

import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "com.example.basicandroidmqttclient.MESSAGE";
    public static final String brokerURI = "3.216.219.0";

    Activity thisActivity;
    TextView subMsgTextView;

    private ListView listViewSubMsg;
    private ArrayAdapter<String> listAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        thisActivity = this;
        listViewSubMsg = findViewById(R.id.listViewSubMsg);
        listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        listViewSubMsg.setAdapter(listAdapter);

    }

    /** Called when the user taps the Send button */
    public void publishMessage(View view) {
        Intent intent = new Intent(this, DisplayMessageActivity.class);
        EditText topicName = (EditText) findViewById(R.id.editTextTopicName);
        EditText value = (EditText) findViewById(R.id.editTextValue);

        Mqtt5BlockingClient client = Mqtt5Client.builder()
                .identifier(UUID.randomUUID().toString())
                .serverHost(brokerURI)
                .buildBlocking();

        client.connect();

        int temperatura = Integer.parseInt(value.getText().toString());
        String payloadString = Integer.toString(temperatura);
        byte[] payloadBytes = payloadString.getBytes();


        client.publishWith().topic(topicName.getText().toString()).qos(MqttQos.AT_LEAST_ONCE).payload(payloadBytes).send();
        client.disconnect();

//        String message = topicName.getText().toString() + " " + value.getText().toString();
//        intent.putExtra(EXTRA_MESSAGE, message);
//        startActivity(intent);
    }

    public void sendSubscription(View view) {
        EditText topicName = (EditText) findViewById(R.id.editTextTopicNameSub);

        Mqtt5BlockingClient client = Mqtt5Client.builder()
                .identifier(UUID.randomUUID().toString())
                .serverHost(brokerURI)
                .buildBlocking();

        client.connect();

        // Limpe a lista antes de adicionar novos elementos
        listAdapter.clear();

        // Use a callback to show the message on the screen
        client.toAsync().subscribeWith()
                .topicFilter(topicName.getText().toString())
                .qos(MqttQos.AT_LEAST_ONCE)
                .callback(msg -> {
                    thisActivity.runOnUiThread(() -> {
                        String message = new String(msg.getPayloadAsBytes(), StandardCharsets.UTF_8);
                        listAdapter.add("Temperatura: " + message);
                    });
                })
                .send();
    }



}