/*******************************************************************************
 * Copyright (c) 2016, 2017 Kichwa Coders Ltd (https://kichwacoders.com/) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.linkerscript;

import org.eclipse.xtext.conversion.IValueConverter;
import org.eclipse.xtext.conversion.ValueConverterException;
import org.eclipse.xtext.nodemodel.INode;

public class IDValueConverter implements IValueConverter<String> {

	protected boolean idNeedsQuoting(String id) {
		if (id.isEmpty()) {
			return true;
		}
		char char0 = id.charAt(0);
		if (char0 != '.' && !Character.isLetter(char0)) {
			return true;
		}

		boolean allHex = true;
		for (int i = 0; i < id.length(); i++) {
			char charAt = id.charAt(i);
			if (Character.isWhitespace(charAt)) {
				return true;
			}

			// if the id looks like a number we need
			// to quote. A surprising example is "dd", see
			// details of HEX in xtext file
			if (i != id.length() - 1) {
				allHex = allHex && Character.digit(charAt, 16) != -1;;
			}
		}

		if (allHex && id.length() >= 2) {
			char suffix = id.charAt(id.length() - 1);
			switch (suffix) {
			case 'd':
			case 'D':
			case 'o':
			case 'O':
			case 'b':
			case 'B':
			case 'x':
			case 'X':
			case 'h':
			case 'H':
				return true;
			}
		}

		return false;
	}

	@Override
	public String toValue(String string, INode node) throws ValueConverterException {
		if (string == null) {
			return null;
		}
		if (string.startsWith("\"") || string.endsWith("\"")) {
			if (string.length() < 2 || !string.startsWith("\"") || !string.endsWith("\"")) {
				throw new ValueConverterException("Could not convert '" + string + "' to an ID", node, null);
			}
			return string.substring(1, string.length() - 1);
		}
		return string;
	}

	@Override
	public String toString(String value) throws ValueConverterException {
		if (value == null) {
			throw new ValueConverterException("ID may not be null.", null, null);
		} else if (value.contains("\"")) {
			throw new ValueConverterException(
					"Invalid ID, identifiers in Linker Scripts cannot contain double-quotes \"", null, null);
		}
		if (idNeedsQuoting(value)) {
			return '"' + value + '"';
		}
		return value;
	}

}
