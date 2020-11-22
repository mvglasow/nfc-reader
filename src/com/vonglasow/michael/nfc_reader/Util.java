package com.vonglasow.michael.nfc_reader;

import com.vonglasow.michael.nfc_reader.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.provider.Settings;

public class Util {

	public static String getHex(byte[] bytes) {
	    StringBuilder sb = new StringBuilder();
	    for (int i = bytes.length - 1; i >= 0; --i) {
	        int b = bytes[i] & 0xff;
	        if (b < 0x10)
	            sb.append('0');
	        sb.append(Integer.toHexString(b));
	        if (i > 0) {
	            sb.append(" ");
	        }
	    }
	    return sb.toString();
	}

	public static long getDec(byte[] bytes) {
	    long result = 0;
	    long factor = 1;
	    for (int i = 0; i < bytes.length; ++i) {
	        long value = bytes[i] & 0xffl;
	        result += value * factor;
	        factor *= 256l;
	    }
	    return result;
	}

	public static long getReversed(byte[] bytes) {
	    long result = 0;
	    long factor = 1;
	    for (int i = bytes.length - 1; i >= 0; --i) {
	        long value = bytes[i] & 0xffl;
	        result += value * factor;
	        factor *= 256l;
	    }
	    return result;
	}

	/**
	 * @brief Dumps a byte array.
	 * 
	 * If the byte array contains nonprintable characters, it is dumped in hex format. Otherwise it
	 * is dumped as a regular string.
	 * 
	 * @param array The byte array
	 */
	public static String dump(byte[] array) {
		boolean isText = true;
		for (int i = 0; (i < array.length) && isText; i++)
			if ((array[i] < 32) && (array[i] != 10))
				isText = false;
		
		return (isText ? new String(array) : getHex(array));
	}

	static Tag cleanupTag(Tag oTag) {
		if (oTag == null)
			return null;

		String[] sTechList = oTag.getTechList();

		Parcel oParcel = Parcel.obtain();
		oTag.writeToParcel(oParcel, 0);
		oParcel.setDataPosition(0);

		int len = oParcel.readInt();
		byte[] id = null;
		if (len >= 0) {
			id = new byte[len];
			oParcel.readByteArray(id);
		}
		int[] oTechList = new int[oParcel.readInt()];
		oParcel.readIntArray(oTechList);
		Bundle[] oTechExtras = oParcel.createTypedArray(Bundle.CREATOR);
		int serviceHandle = oParcel.readInt();
		int isMock = oParcel.readInt();
		IBinder tagService;
		if (isMock == 0) {
			tagService = oParcel.readStrongBinder();
		} else {
			tagService = null;
		}
		oParcel.recycle();

		int nfca_idx = -1;
		int mc_idx = -1;
		short oSak = 0;
		short nSak = 0;

		for (int idx = 0; idx < sTechList.length; idx++) {
			if (sTechList[idx].equals(NfcA.class.getName())) {
				if (nfca_idx == -1) {
					nfca_idx = idx;
					if (oTechExtras[idx] != null && oTechExtras[idx].containsKey("sak")) {
						oSak = oTechExtras[idx].getShort("sak");
						nSak = oSak;
					}
				} else {
					if (oTechExtras[idx] != null && oTechExtras[idx].containsKey("sak")) {
						nSak = (short) (nSak | oTechExtras[idx].getShort("sak"));
					}
				}
			} else if (sTechList[idx].equals(MifareClassic.class.getName())) {
				mc_idx = idx;
			}
		}

		boolean modified = false;

		if (oSak != nSak) {
			oTechExtras[nfca_idx].putShort("sak", nSak);
			modified = true;
		}

		if (nfca_idx != -1 && mc_idx != -1 && oTechExtras[mc_idx] == null) {
			oTechExtras[mc_idx] = oTechExtras[nfca_idx];
			modified = true;
		}

		if (!modified) {
			return oTag;
		}

		Parcel nParcel = Parcel.obtain();
		nParcel.writeInt(id.length);
		nParcel.writeByteArray(id);
		nParcel.writeInt(oTechList.length);
		nParcel.writeIntArray(oTechList);
		nParcel.writeTypedArray(oTechExtras, 0);
		nParcel.writeInt(serviceHandle);
		nParcel.writeInt(isMock);
		if (isMock == 0) {
			nParcel.writeStrongBinder(tagService);
		}
		nParcel.setDataPosition(0);

		Tag nTag = Tag.CREATOR.createFromParcel(nParcel);

		nParcel.recycle();

		return nTag;
	}

	static void showWirelessSettingsDialog(final Activity activity) {
	    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
	    builder.setMessage(R.string.nfc_disabled);
	    builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialogInterface, int i) {
	            Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
	            activity.startActivity(intent);
	        }
	    });
	    builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialogInterface, int i) {
	            activity.finish();
	        }
	    });
	    builder.create().show();
	    return;
	}
}
