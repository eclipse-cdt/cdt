/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.utils.debug.stabs;

import java.io.IOException;
import java.io.Reader;


public class TypeNumber {

	int typeno;
	int fileno;

	public TypeNumber(int f, int t) {
		fileno = f;
		typeno = t;
	}

	public TypeNumber(Reader reader) {
		parseTypeNumber(reader);
	}

	public int getTypeNo() {
		return typeno;
	}

	public int getFileNo() {
		return fileno;
	} 

	public boolean equals(Object obj) {
		if (obj instanceof TypeNumber) {
			TypeNumber tn = (TypeNumber)obj;
			return tn.typeno == typeno && tn.fileno == fileno;
		}
		return super.equals(obj);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return fileno*10 + typeno;
	}

	void parseTypeNumber(Reader reader) {
		try {
			int c = reader.read();
			char ch = (char)c;
			if (c == -1) {
				return;
			} else if (ch == '(') {
				StringBuffer sb = new StringBuffer();
				while ((c = reader.read()) != -1) {
					ch = (char)c;
					if (ch == ')') {
						try {
							typeno = Integer.parseInt(sb.toString());
						} catch (NumberFormatException e) {
						}
						break;
					} else if (ch == ',') {
						try {
							fileno = Integer.parseInt(sb.toString());
						} catch (NumberFormatException e) {
						}
						sb.setLength(0);
					} else if (Character.isDigit(ch)) {
						sb.append(ch);
					} else {
						break;
					}
				}
			} else if (Character.isDigit(ch)) {
				StringBuffer sb = new StringBuffer();
				sb.append(ch);
				reader.mark(1);
				while ((c = reader.read()) != -1) {
					ch = (char)c;
					if (Character.isDigit(ch)) {
						sb.append(ch);
					} else {
						reader.reset();
						break;
					}
				}
				try {
					typeno = Integer.parseInt(sb.toString());
				} catch (NumberFormatException e) {
				}
			}
		} catch (IOException e) {
		}
	}

}

