/*
 * Created on May 28, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.cdt.internal.core.parser.scanner2;

/**
 * @author dschaefe
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class CharArrayUtils {

	public static final int hash(char[] str, int start, int length) {
		int h = 0;
		int end = start + length;
		
		for (int curr = start; curr < end; ++curr)
			h += (h << 3) + str[curr];

		return h;
	}

	public static final int hash(char[] str) {
		return hash(str, 0, str.length);
	}
	
	public static final boolean equals(char[] str1, char[] str2) {
		if (str1 == str2)
			return true;
		
		if (str1.length != str2.length)
			return false;
		
		for (int i = 0; i < str1.length; ++i)
			if (str1[i] != str2[i])
				return false;
		
		return true;
	}
	
	public static final boolean equals(char[] str1, int start1, int length1, char[] str2) {
		if (length1 != str2.length)
			return false;
		
		for (int i = 0; i < length1; ++i)
			if (str1[start1++] != str2[i])
				return false;
		
		return true;
	}
	
	public static final char[] extract(char[] str, int start, int length) {
		if (start == 0 && length == str.length)
			return str;
		
		char[] copy = new char[length];
		System.arraycopy(str, start, copy, 0, length);
		return copy;
	}
}
