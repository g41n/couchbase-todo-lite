package com.couchbase.todo;

import android.content.Context;
import android.database.DataSetObserver;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.couchbase.lite.Document;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    private Application app;
    private ArrayAdapter servicesListAdapter;
    private List<NsdServiceInfo> services = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        setTitle("Settings");

        app = (Application) getApplication();
        updateListenerStatus();

        Button startListenerButton = (Button) findViewById(R.id.startListenerButton);
        startListenerButton.setOnClickListener(new AdapterView.OnClickListener() {
            @Override
            public void onClick(View view) {
                app.startLiteListener();
                updateListenerStatus();
            }
        });

        Button stopListenerButton = (Button) findViewById(R.id.stopListenerButton);
        stopListenerButton .setOnClickListener(new AdapterView.OnClickListener() {
            @Override
            public void onClick(View view) {
                app.stopLiteListener();
                updateListenerStatus();
            }
        });

        Button startServiceRegistration = (Button) findViewById(R.id.startServiceRegistration);
        startServiceRegistration.setOnClickListener(new AdapterView.OnClickListener() {
            @Override
            public void onClick(View view) {
                app.startServiceRegistration();
            }
        });

        Button showServiceRegistration = (Button) findViewById(R.id.stopServiceRegistration);
        showServiceRegistration.setOnClickListener(new AdapterView.OnClickListener() {
            @Override
            public void onClick(View view) {
                app.stopServiceRegistration();
            }
        });

        Button startServiceDiscovery = (Button) findViewById(R.id.startServiceDiscovery);
        startServiceDiscovery.setOnClickListener(new AdapterView.OnClickListener() {
            @Override
            public void onClick(View view) {
                app.startServiceDiscovery();
            }
        });

        Button stopServiceDiscovery = (Button) findViewById(R.id.stopServiceDiscovery);
        stopServiceDiscovery.setOnClickListener(new AdapterView.OnClickListener() {
            @Override
            public void onClick(View view) {
                app.stopServiceDiscovery();
                servicesListAdapter.notifyDataSetChanged();
            }
        });

        ListView listView = (ListView) findViewById(R.id.servicesList);
        //ServicesListAdapter adapter = new ServicesListAdapter(this, app.services);
        //listView.setAdapter(adapter);
        servicesListAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, app.services);
        listView.setAdapter(servicesListAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                NsdServiceInfo serviceInfo = (NsdServiceInfo) servicesListAdapter.getItem(i);
                app.mSyncGatewayUrl = "http:/" + serviceInfo.getHost() + ":" + serviceInfo.getPort() + "/db/";
                Log.d("aaa", "activating sync on " + app.mSyncGatewayUrl);
                app.mSyncEnabled = true;
                app.startReplication("db", null, false);
            }
        });

        Button startSync= (Button) findViewById(R.id.startSync);
        startSync.setOnClickListener(new AdapterView.OnClickListener() {
            @Override
            public void onClick(View view) {
                app.mSyncGatewayUrl = "http://192.168.1.150:4984/db/";
                Log.d("aaa", "activating sync on " + app.mSyncGatewayUrl);
                app.mSyncEnabled = true;
                app.startReplication("db", null, true);
            }
        });

        Button stopSync = (Button) findViewById(R.id.stopSync);
        stopSync.setOnClickListener(new AdapterView.OnClickListener() {
            @Override
            public void onClick(View view) {
                app.stopReplication();
                servicesListAdapter.notifyDataSetChanged();
            }
        });

    }

    private void updateListenerStatus() {
        String msg = "listener active: " + (app.listener != null);
        TextView listenerStatus = (TextView) findViewById(R.id.listenerStatusTextView);
        Log.d("LiteL", msg);
        listenerStatus.setText(msg);
    }


    private void updateServiceList() {
        Log.d("LiteL", "services: " + app.services.toString());
    }
}

class ServicesListAdapter extends ArrayAdapter<NsdServiceInfo> {

    public ServicesListAdapter(Context context, List<NsdServiceInfo> accesses) {
        super(context, 0, accesses);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        NsdServiceInfo serviceInfo = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.view_services_list, parent, false);
        }
        // Lookup view for data population
        TextView name = (TextView) convertView.findViewById(R.id.name);
        TextView host = (TextView) convertView.findViewById(R.id.host);
        TextView port = (TextView) convertView.findViewById(R.id.port);
        // Populate the data into the template view using the data object
        name.setText(serviceInfo.getServiceName());
        host.setText(serviceInfo.getHost().toString());
        port.setText(serviceInfo.getPort());

        // Return the completed view to render on screen
        return convertView;
    }
}