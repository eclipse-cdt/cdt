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

import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.cdt.core.parser.ISignificantMacros;
import org.eclipse.cdt.core.parser.util.CharArrayObjectMap;
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
	public static final char[] DEFINED = {0};
	public static final char[] UNDEFINED = {1}; 
	private static final Comparator<Object> SORTER = new Comparator<Object>() {
		@Override
		public int compare(Object o1, Object o2) {
			return CharArrayUtils.compare((char[])o1, (char[])o2);
		}
	}; 
			
	private final char[] fEncoded;
	private int fHash;

	public SignificantMacros(char[] encoded) {
		assert encoded != null;
		fEncoded= encoded;
	}

	public SignificantMacros(CharArrayObjectMap<char[]> sigMacros) {
		fEncoded= encode(sigMacros);
	}

	private char[] encode(CharArrayObjectMap<char[]> sigMacros) {
		StringBuilder buffer= new StringBuilder();
		Object[] keys= sigMacros.keyArray();
		Arrays.sort(keys, SORTER);
		for (Object key : keys) {
			char[] name= (char[]) key;
			char[] value= sigMacros.get(name);
			buffer.append((char) name.length).append(name);
			buffer.append((char) value.length).append(value);
		}
		int len= buffer.length();
		char[] result= new char[len];
		buffer.getChars(0, len, result, 0);
		return result;
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

	@Override
	public boolean accept(IVisitor visitor) {
		final char[] encoded = fEncoded;
		final int len = encoded.length;
		int i= 0;
		while (i < len) {
			final int len1 = encoded[i++];
			int v= i + len1;
			if (v >= len) 
				break;
			final int len2 = encoded[v++];
			if (v+len2 > len) 
				break;
			
			char[] macro= extract(encoded, i, len1);
			i= v+len2;
			if (len2 == 1) {
				if (encoded[v] == UNDEFINED[0]) {
					if (!visitor.visitUndefined(macro))
						return false;
					continue;
				} 
				if (encoded[v] == DEFINED[0]) {
					if (!visitor.visitDefined(macro))
						return false;
					continue;
				} 
			}				
			final char[] value = extract(encoded, v, len2);
			if (!visitor.visitValue(macro, value))
				return false;
		}
		return true;
	}

	public char[] extract(final char[] source, int from, final int length) {
		char[] value= new char[length];
		System.arraycopy(source, from, value, 0, length);
		return value;
	}

	@Override
	public char[] encode() {
		return fEncoded;
	}
	
	/**
	 * For debugging purposes.
	 */
	@SuppressWarnings("nls")
	@Override
	public String toString() {
		final StringBuilder buf= new StringBuilder();
		buf.append('{');
		accept(new IVisitor() {
			@Override
			public boolean visitValue(char[] macro, char[] value) {
				buf.append(macro).append('=').append(value).append(',');
				return true;
			}
			@Override
			public boolean visitUndefined(char[] macro) {
				buf.append(macro).append('=').append("null,");
				return true;
			}
			@Override
			public boolean visitDefined(char[] macro) {
				buf.append(macro).append('=').append("*,");
				return true;
			}
		});
		int buflen = buf.length();
		if (buflen > 1)
			buf.setLength(buflen-1);
		buf.append('}');
		return buf.toString();
	}

	public static char[] shortenValue(char[] expansion) {
		if (expansion.length <= 16)
			return expansion;
		char[] result= new char[16];
		System.arraycopy(expansion, 0, result, 0, 8);
		StreamHasher hasher= new StreamHasher();
		hasher.addChunk(expansion);
		long hash= hasher.computeHash();
		for(int i= 0; i<8; i++) {
			result[8+i]= (char) (hash & 0xff);
			hash= hash >> 1;
		}
		return result;
	}
}
