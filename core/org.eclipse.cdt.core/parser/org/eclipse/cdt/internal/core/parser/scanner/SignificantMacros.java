/*******************************************************************************
 * Copyright (c) 2011 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser.scanner;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.parser.IMacroDictionary;
import org.eclipse.cdt.core.parser.ISignificantMacros;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;

/**
 * A set of static methods to encode Map<String, String> as an array of characters and to decode
 * it back.
 *
 * The map is encoded as:
 * <code>&lt;number_of_entries&gt;,&lt;key1&gt;&lt;value1&gt;...&lt;keyN&gt;&lt;valueN&gt;</code>.
 * <p>
 * Each string is encoded as: <code>&lt;number_of_characters&gt;,&lt;characters&gt;</code>.
 * A <code>null</code> string is encoded as a single comma.
 */
public class SignificantMacros implements ISignificantMacros {
	private static final char[] UNDEFINED = {}; 
	private static final char[] DEFINED = {};
	private static final int ENCODED_UNDEFINED = Character.MAX_VALUE;
	private static final int ENCODED_DEFINED = ENCODED_UNDEFINED-1; 
			
	private final char[] fEncoded;
	private volatile Map<String, char[]> fMap;
	private int fHash;

	public SignificantMacros(char[] encoded) {
		fEncoded= encoded == null ? CharArrayUtils.EMPTY : encoded;
	}

	@Override
	public int hashCode() {
		int h = fHash;
		if (h == 0) {
		    char val[] = fEncoded;
		    int len = fEncoded.length;
		    for (int i = 0; i < len; i++) {
		    	h = 31*h + val[i];
		    }
		    fHash = h;
		}
		return h;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof SignificantMacros 
				&& hashCode() == obj.hashCode() 
				&& CharArrayUtils.equals(fEncoded, ((SignificantMacros) obj).fEncoded);
	}
	
	public boolean isComplient(IMacroDictionary macroDictionary) {
		Map<String, char[]> map= getMap();
		for (Map.Entry<String, char[]> entry : map.entrySet()) {
			if (entry.getValue() == UNDEFINED) {
				if (macroDictionary.isDefined(entry.getKey()))
					return false;
			} else if (entry.getValue() == DEFINED) {
				if (!macroDictionary.isDefined(entry.getKey()))
					return false;
			} else {
				if (!macroDictionary.hasValue(entry.getKey(), entry.getValue()))
					return false;
			}
		}
		return true;
	}

	private Map<String, char[]> getMap() {
		if (fMap == null) {
			fMap= rebuildMap();
		}
		return fMap;
	}

	private Map<String, char[]> rebuildMap() {
		final char[] encoded = fEncoded;
		final int len = encoded.length;
		if (len == 0)
			return Collections.emptyMap();
		
		Map<String, char[]> map= new HashMap<String, char[]>();
		int i= 0;
		while (i < len) {
			final int len1 = encoded[i];
			int end1= i+ len1;
			if (end1 >= len) 
				break;
			
			String macro= new String(encoded, i, len1);
			final int len2 = encoded[end1];
			if (len2 == ENCODED_UNDEFINED) {
				i= end1+1;
				map.put(macro, UNDEFINED);
			} else if (len2 == ENCODED_DEFINED) {
				i= end1+1;
				map.put(macro, DEFINED);
			} else {
				i= end1+len2;
				if (i > len) 
					break;
				
				char[] value= new char[len2];
				System.arraycopy(encoded, end1, value, 0, len2);
				map.put(macro, value);
			} 
		}
		return map;
	}

	public char[] encode() {
		return fEncoded;
	}
}
