/*******************************************************************************
 * Copyright (c) 2000 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui;

import java.io.IOException;
import java.io.Reader;


public abstract class SingleCharReader extends Reader {
	
	/**
	 * @see Reader#read(char)
	 */
	@Override
	public abstract int read() throws IOException;


	/**
	 * @see Reader#read(char[],int,int)
	 */
	@Override
	public int read(char cbuf[], int off, int len) throws IOException {
		int end= off + len;
		for (int i= off; i < end; i++) {
			int ch= read();
			if (ch == -1) {
				if (i == off) {
					return -1;
				}
				return i - off;
			}
			cbuf[i]= (char)ch;
		}
		return len;
	}		
	
	/**
	 * @see Reader#ready()
	 */		
    @Override
	public boolean ready() throws IOException {
		return true;
	}
	
	/**
	 * Gets the content as a String
	 */
	public String getString() throws IOException {
		StringBuffer buf= new StringBuffer();
		int ch;
		while ((ch= read()) != -1) {
			buf.append((char)ch);
		}
		return buf.toString();
	}
}
