package com.vonglasow.michael.nfc_reader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.TagLostException;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TagFormatter extends Activity {
	
    private static String MIME_TYPE = "application/prs.com.vonglasow.michael.nfc_reader";

	private TextView mText;

    private NfcAdapter mAdapter;
    private PendingIntent mPendingIntent;
    
    private NdefRecord mRecord;
    private NdefMessage mMessage;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tag_formatter);
		mText = (TextView) findViewById(R.id.tag_formatter_text);
		
        mAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mAdapter == null) {
        	mText.setText(R.string.no_nfc);
            return;
        }

        mPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        
        mRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA, MIME_TYPE.getBytes(), new byte[] {42}, "Written by NFC Test".getBytes());
        mMessage = new NdefMessage(new NdefRecord[] { mRecord });
	}
	
    @Override
    protected void onPause() {
        super.onPause();
        if (mAdapter != null) {
            mAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAdapter != null) {
            if (!mAdapter.isEnabled()) {
                Util.showWirelessSettingsDialog(this);
            }
            mAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        setIntent(intent);
        resolveIntent(intent);
    }

    private void resolveIntent(Intent intent) {
    	Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
    	
    	if (tag == null)
    		return;
    	
    	FormatJob job = new FormatJob(tag, mMessage);
    	
    	new FormatTask().execute(job);
    }
    
    private class FormatJob {
    	private Tag tag;
    	private NdefMessage message;
    	
    	public FormatJob(Tag tag, NdefMessage message) {
    		super();
    		this.tag = tag;
    		this.message = message;
    	}
    	
    	public Tag getTag() {
    		return tag;
    	}
    	
    	public NdefMessage getMessage() {
    		return message;
    	}
    }
    
    private class FormatTask extends AsyncTask<FormatJob, Void, Integer> {
    	/**
    	 * @return A resource ID for the string to display. There is a string for success, as well
    	 * as for almost every possible error.
    	 */
    	protected Integer doInBackground(FormatJob... jobs) {
    		// Normally there shouldn't ever be more than one format job
    		for (FormatJob job : jobs) {
    			Ndef ndef = Ndef.get(job.getTag());
    			NdefFormatable ndefFormatable = NdefFormatable.get(job.getTag());
    			NdefMessage message = job.getMessage();
    			if (ndef != null) {
    				// NDEF formatted already, overwrite it
    				try {
    					ndef.connect();
    					if (!ndef.isWritable()) {
        					try { ndef.close(); } catch (IOException e) {}
    						return R.string.tag_not_writable;
    					}
    					if (ndef.getMaxSize() < message.toByteArray().length) {
        					try { ndef.close(); } catch (IOException e) {}
    						return R.string.tag_size_exceeded;
    					}
    					ndef.writeNdefMessage(message);
    				} catch (TagLostException e) {
    					return R.string.tag_lost;
    				} catch (IOException e) {
    					return R.string.write_error;
    				} catch (FormatException e) {
    					return R.string.write_error;
    				} finally {
    					try {
    						ndef.close();
    					} catch (IOException e) {
    						// NOP
    					}
    				}
    			} else if (ndefFormatable != null) {
    				// not NDEF formatted but formatable, format it
    				try {
    					ndefFormatable.connect();
    					ndefFormatable.format(message);
    				} catch (TagLostException e) {
    					return R.string.tag_lost;
    				} catch (IOException e) {
    					return R.string.write_error;
    				} catch (FormatException e) {
    					return R.string.write_error;
    				} finally {
    					try {
							ndefFormatable.close();
						} catch (IOException e) {
							// NOP
						}
    				}
            	} else
            		return R.string.invalid_tag;
            }
            return R.string.format_successful;
        }

        protected void onPostExecute(Integer result) {
            // show success/failure message
        	mText.setText(result);
        }
    }
}
