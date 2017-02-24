package com.vonglasow.michael.nfc_reader;

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
		
		return "Payload: " + (isText ? new String(array) : getHex(array));
	}

}
