package com.toktumi.line2.config;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class Main extends Activity implements TextWatcher {
	public static Context MainActivity;
	
	// Internal IP for softphone configuration
	private final static String INTERNAL_CONFIG_IP_EXTRA = "com.toktumi.intent.extra.INTERNAL_CONFIG_IP";
	// IP type for softphone configuration
	private final static String INTERNAL_CONFIG_TYPE_EXTRA = "com.toktumi.intent.extra.INTERNAL_CONFIG_TYPE";
	private static final String[] mTypes = { "Prod", "Dev", "Test", "Stage", "Other" };

	private Spinner typeSpinner;
    private TextView ipAddress;
    private Button saveButton;
    
    public Main() {
    	MainActivity = this;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        typeSpinner = (Spinner)findViewById(R.id.type);
        typeSpinner.setAdapter(adapter);
        typeSpinner.setOnItemSelectedListener(onTypeSelected);
 
        ipAddress = (TextView)findViewById(R.id.ip_address);
        ipAddress.addTextChangedListener(this);
       
        saveButton = (Button)findViewById(R.id.save);
		saveButton.setOnClickListener(onSaveButtonClick);
    }

	private OnItemSelectedListener onTypeSelected = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
			if (position == 0) {
				ipAddress.setText("67.159.187.100,67.159.187.104");
			}
			ipAddress.setText(loadIP(position));
		}

		@Override public void onNothingSelected(AdapterView<?> parent) { }
	};

    private OnClickListener onSaveButtonClick = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if (validate()) {
				Intent intent = new Intent("com.toktumi.intent.action.ACTION_INTERNAL_CONFIG");
				intent.putExtra(INTERNAL_CONFIG_IP_EXTRA, ipAddress.getText().toString());
				intent.putExtra(INTERNAL_CONFIG_TYPE_EXTRA, typeSpinner.getSelectedItem().toString());
				sendBroadcast(intent);
	
				Toast.makeText(Main.this, "Setting update sent. if this worked, Line2 will restart in a few seconds", Toast.LENGTH_LONG).show();
			}
		}
	};

	@Override
	public void afterTextChanged(Editable s) {
		boolean isEmpty = TextUtils.isEmpty(s.toString());
		saveButton.setEnabled(!isEmpty);
		saveIP();
	}

	private static String IP_PATTERN = "\\b(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?).(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?).(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?).(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\b";
	protected boolean validate() {
		String [] ipAddresses = ipAddress.getText().toString().split(",");
		for (String ipAddress : ipAddresses) {
			Pattern p = Pattern.compile(IP_PATTERN);
			Matcher m = p.matcher(ipAddress);
            if (!m.matches()) {
				Toast.makeText(Main.this, "Invalid IP address", Toast.LENGTH_LONG).show();
				return false;
            }
		}
		return true;
	}

	@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
	@Override public void onTextChanged(CharSequence s, int start, int before, int count) { }

	private void saveIP() {
		AppSettings appSettings = new AppSettings();
		appSettings.setStringPref(typeSpinner.getSelectedItem().toString(), ipAddress.getText().toString());
	}

	private String loadIP(int position) {
		AppSettings appSettings = new AppSettings();
		return appSettings.getStringPref(mTypes[position], "");
	}

}