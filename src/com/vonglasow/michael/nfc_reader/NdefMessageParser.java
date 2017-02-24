/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vonglasow.michael.nfc_reader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.vonglasow.michael.nfc_reader.record.ParsedNdefRecord;
import com.vonglasow.michael.nfc_reader.record.SmartPoster;
import com.vonglasow.michael.nfc_reader.record.TextRecord;
import com.vonglasow.michael.nfc_reader.record.UriRecord;
import com.vonglasow.michael.nfc_reader.R;

import android.app.Activity;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Utility class for creating {@link ParsedNdefMessage}s.
 */
public class NdefMessageParser {

    // Utility class
    private NdefMessageParser() {

    }

    /** Parse an NdefMessage */
    public static List<ParsedNdefRecord> parse(NdefMessage message) {
        return getRecords(message.getRecords());
    }

    public static List<ParsedNdefRecord> getRecords(NdefRecord[] records) {
        List<ParsedNdefRecord> elements = new ArrayList<ParsedNdefRecord>();
        for (final NdefRecord record : records) {
            if (UriRecord.isUri(record)) {
                elements.add(UriRecord.parse(record));
            } else if (TextRecord.isText(record)) {
                elements.add(TextRecord.parse(record));
            } else if (SmartPoster.isPoster(record)) {
                elements.add(SmartPoster.parse(record));
            } else {
            	elements.add(new ParsedNdefRecord() {
					@Override
					public View getView(Activity activity, LayoutInflater inflater, ViewGroup parent, int offset) {
				        TextView text = (TextView) inflater.inflate(R.layout.tag_text, parent, false);
				        String data = dumpHeader(record);
				        
				        data = data + "\n" + Util.dump(record.getPayload());
				        
				        text.setText(data);
				        return text;
					}
            		
            	});
            }
        }
        return elements;
    }

	/**
	 * @brief Dumps the NDEF record header to a string
	 * 
	 * @param record The record
	 * @return NDEF record header in string form
	 */
	public static String dumpHeader(NdefRecord record) {
		String data;
		
		switch(record.getTnf()) {
		case NdefRecord.TNF_ABSOLUTE_URI:
			data = "TNF_ABSOLUTE_URI\n";
			break;
		case NdefRecord.TNF_EMPTY:
			data = "TNF_EMPTY\n";
			break;
		case NdefRecord.TNF_EXTERNAL_TYPE:
			data = "TNF_EXTERNAL_TYPE\n";
			break;
		case NdefRecord.TNF_MIME_MEDIA:
			data = "TNF_MIME_MEDIA\n";
			break;
		case NdefRecord.TNF_UNCHANGED:
			data = "TNF_UNCHANGED\n";
			break;
		case NdefRecord.TNF_UNKNOWN:
			data = "TNF_UNKNOWN\n";
			break;
		case NdefRecord.TNF_WELL_KNOWN:
			data = "TNF_WELL_KNOWN\n";
			break;
			default:
				data = String.format("Unknown TNF (%d)", record.getTnf());
		}
		
		if (record.getTnf() == NdefRecord.TNF_WELL_KNOWN) {
			data = data + "Type: ";
			if (Arrays.equals(record.getType(), NdefRecord.RTD_ALTERNATIVE_CARRIER))
				data = data + "RTD_ALTERNATIVE_CARRIER";
			else if (Arrays.equals(record.getType(), NdefRecord.RTD_HANDOVER_CARRIER))
				data = data + "RTD_HANDOVER_CARRIER";
			else if (Arrays.equals(record.getType(), NdefRecord.RTD_HANDOVER_REQUEST))
				data = data + "RTD_HANDOVER_REQUEST";
			else if (Arrays.equals(record.getType(), NdefRecord.RTD_HANDOVER_SELECT))
				data = data + "RTD_HANDOVER_SELECT";
			else if (Arrays.equals(record.getType(), NdefRecord.RTD_SMART_POSTER))
				data = data + "RTD_SMART_POSTER";
			else if (Arrays.equals(record.getType(), NdefRecord.RTD_TEXT))
				data = data + "RTD_TEXT";
			else if (Arrays.equals(record.getType(), NdefRecord.RTD_URI))
				data = data + "RTD_URI";
			else
				data = data + "Unknown RTD";
			data = data + String.format(" (%s)\n", new String(record.getType()));
		} else {
			data = data + "Type: " + ((record.getType().length > 0) ? Util.dump(record.getType()) : "(empty)") + "\n";
		}
		
		data = data + "ID: " + ((record.getId().length > 0) ? Util.getHex(record.getId()) : "(empty)");
		return data;
	}
}
