/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.utils.debug.stabs;

import java.io.IOException;
import java.io.Reader;



public class TypeInformation {
	TypeNumber typeNumber;
	char typeDescriptor;
	boolean isTypeDefinition;

	public TypeInformation(Reader reader) {
		parseTypeInformation(reader);
	}

	public TypeNumber getTypeNumber() {
	    return typeNumber;
	}

	public char getTypeDescriptor() {
	    return typeDescriptor;
	}

	public boolean isTypeDefinition() {
	    return isTypeDefinition;
	}

	/**
	 * int:t(0,1)=r(0,1);-2147483648;2147483647;
	 * We receieve as input:
	 * (0,1)=r(0,1);-2147483648;2147483647;
	 * typeNumber: (0,1)
	 * typeDescriptor: r
	 * attributes: (0,1);-2147483648;2147483647;
	 * isTypeDefinition = true;
	 */
	void parseTypeInformation(Reader reader) {
		try {
			typeNumber = new TypeNumber(reader);
			reader.mark(1);
			int c = reader.read();
			if (c == '=') {
				isTypeDefinition = true;
				reader.mark(1);
				c = reader.read();
				if (isTypeDescriptor((char)c)) {
					typeDescriptor = (char)c;
				} else {
					reader.reset();
				}
			} else {
				reader.reset();
			}
		} catch (IOException e) {
		}
	}

	boolean isTypeDescriptor(char c) {
		return Character.isLetter(c) || c == '=' || c == '#' || c =='*'
			|| c == '&' || c == '@';
	}
}

