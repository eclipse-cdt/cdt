/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser.scanner;

import org.eclipse.cdt.core.parser.IToken;

/**
 * @author ddaoust
 *
 */
public class ScannerStringBuffer {
	private int current_size;
	private char [] s_buff;
	private int s_pos;

	public ScannerStringBuffer(int initialSize) {
		current_size = initialSize;
		s_buff = new char[current_size];
		s_pos = 0;
	}
	public final void startString(){ 
		s_pos = 0; 
	}
	public final void append(int c) {
		try {
			s_buff[s_pos++]= (char)c;
		}
		catch (ArrayIndexOutOfBoundsException a)
		{
			int new_size = current_size*2;
			char [] new_sbuf = new char[new_size];
			for (int i = 0; i < current_size; i++)
				new_sbuf[i] = s_buff[i];
			new_sbuf[current_size] = (char)c;
			current_size = new_size;
			s_buff = new_sbuf;
		}
	}
	public final void append(String s) {
		int len = s.length();
		for(int i=0; i < len; i++)
			append(s.charAt(i));
	}
	public final void append(IToken t) {
		append(t.getImage());
	}
	public final int length() {
		return s_pos;
	}
	public final String toString() {
		return String.valueOf(s_buff, 0, s_pos);
	}
}
