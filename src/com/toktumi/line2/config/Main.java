package com.toktumi.line2.config;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class Main extends Activity {
	public static Context MainActivity;
	
	// Internal IP for softphone configuration
	private final static String INTERNAL_CONFIG_IP_EXTRA = "com.toktumi.intent.extra.INTERNAL_CONFIG_IP";
	// Internal Remote API URL for softphone configuration
	public final static String INTERNAL_CONFIG_REMOTE_API_URL_EXTRA = "com.toktumi.intent.extra.INTERNAL_CONFIG_REMOTE_API_URL";
	// IP type for softphone configuration
	private final static String INTERNAL_CONFIG_TYPE_EXTRA = "com.toktumi.intent.extra.INTERNAL_CONFIG_TYPE";
	// Show debug settings (i.e. VQE recording) boolean for softphone configuration
	public final static String INTERNAL_CONFIG_SHOW_DEBUG_EXTRA = "com.toktumi.intent.extra.INTERNAL_CONFIG_SHOW_DEBUG";

	private static final String[] mTypes = { "Prod", "Dev", "Test", "Stage", "Other" };

	private Spinner typeSpinner;
    private TextView ipAddress;
    private TextView remoteApiUrl;
    private Button saveButton;
    private CheckBox showSecretDebugSettingsCheck;

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
        ipAddress.addTextChangedListener(ipAddressTextWatcher);
       
        remoteApiUrl = (TextView)findViewById(R.id.remote_api_url);
        remoteApiUrl.addTextChangedListener(remoteApiUrlTextWatcher);

        saveButton = (Button)findViewById(R.id.save);
		saveButton.setOnClickListener(onSaveButtonClick);
		
		showSecretDebugSettingsCheck = (CheckBox)findViewById(R.id.show_debug_settings);
		showSecretDebugSettingsCheck.setOnCheckedChangeListener(onShowSecretDebugSettingsCheckChanged);
		
		Intent intent = new Intent("com.toktumi.intent.action.ACTION_INTERNAL_GET_CONFIG");
		sendOrderedBroadcast(intent, null, getInfoResultReceiver, null, 0, null, null);
    }

	private BroadcastReceiver getInfoResultReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle bundle = getResultExtras(false);
			if (bundle != null) {
				String softphoneServerIP = bundle.getString(INTERNAL_CONFIG_IP_EXTRA);
				String softphoneServerType = bundle.getString(INTERNAL_CONFIG_TYPE_EXTRA);
				String remoteApiUrlSetting = bundle.getString(INTERNAL_CONFIG_REMOTE_API_URL_EXTRA);
				boolean showSecretDebugSettings = bundle.getBoolean(INTERNAL_CONFIG_SHOW_DEBUG_EXTRA, false);
				
				Log.d("Line2-Config", "Get config returns: server IP address " + softphoneServerIP +
						" for " + softphoneServerType +
						" and " + remoteApiUrl +
						" (showSecretDebugSettings=" + showSecretDebugSettings + ")");
				
				typeSpinner.setSelection(typeToPosition(softphoneServerType));
				ipAddress.setText(softphoneServerIP);
				remoteApiUrl.setText(remoteApiUrlSetting);
				showSecretDebugSettingsCheck.setChecked(showSecretDebugSettings);
			}
		}
	};
    
	private OnCheckedChangeListener onShowSecretDebugSettingsCheckChanged = new OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			Intent intent = new Intent("com.toktumi.intent.action.ACTION_INTERNAL_CONFIG_VQE");
			intent.putExtra(INTERNAL_CONFIG_SHOW_DEBUG_EXTRA, isChecked);
			sendBroadcast(intent);
		}
	};

	private OnItemSelectedListener onTypeSelected = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
			if (position == 0) {
				ipAddress.setText("67.159.187.100,67.159.187.104");
				remoteApiUrl.setText("https://www.toktumi.com");
			}
			ipAddress.setText(loadIP(position));
			remoteApiUrl.setText(loadUrl(position));
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
				intent.putExtra(INTERNAL_CONFIG_REMOTE_API_URL_EXTRA, remoteApiUrl.getText().toString());
				sendBroadcast(intent);
	
				Toast.makeText(Main.this, "Setting update sent. if this worked, Line2 will restart in a few seconds", Toast.LENGTH_LONG).show();
			}
		}
	};

	private TextWatcher ipAddressTextWatcher = new TextWatcher() {
		
		@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
		@Override public void onTextChanged(CharSequence s, int start, int before, int count) { }

		@Override
		public void afterTextChanged(Editable s) {
			updateButtonEnabling();
			saveIP(typeSpinner.getSelectedItem().toString());
		}
	};

	private TextWatcher remoteApiUrlTextWatcher = new TextWatcher() {
		
		@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
		@Override public void onTextChanged(CharSequence s, int start, int before, int count) { }

		@Override
		public void afterTextChanged(Editable s) {
			updateButtonEnabling();
			saveUrl(typeSpinner.getSelectedItem().toString());
		}
	};
	
	private void updateButtonEnabling() {
		boolean isIPAddressEmpty = TextUtils.isEmpty(ipAddress.toString());
		boolean isRemoteApiUrlEmpty = TextUtils.isEmpty(remoteApiUrl.toString());

		saveButton.setEnabled(!isIPAddressEmpty && !isRemoteApiUrlEmpty);
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

		try {
			new URI(remoteApiUrl.getText().toString());
		} catch (URISyntaxException e) {
			Toast.makeText(Main.this, "Invalid Remote API Url", Toast.LENGTH_LONG).show();
			return false;
		}
		
		return true;
	}

	private void saveIP(String type) {
		AppSettings appSettings = new AppSettings();
		appSettings.setStringPref(type, ipAddress.getText().toString());
	}

	private void saveUrl(String type) {
		AppSettings appSettings = new AppSettings();
		appSettings.setStringPref(getRemoteApiPref(type), remoteApiUrl.getText().toString());
	}

	private String loadIP(int position) {
		AppSettings appSettings = new AppSettings();
		return appSettings.getStringPref(mTypes[position], "");
	}

	private String loadUrl(int position) {
		AppSettings appSettings = new AppSettings();
		return appSettings.getStringPref(getRemoteApiPref(mTypes[position]), "");
	}

	private int typeToPosition(String softphoneServerType) {
		for (int i = 0; i < mTypes.length; i++) {
			if (mTypes[i].equalsIgnoreCase(softphoneServerType))
				return i;
		}
		return 0;
	}
	
	String getRemoteApiPref(String type) {
		return type + "Url";
	}
}