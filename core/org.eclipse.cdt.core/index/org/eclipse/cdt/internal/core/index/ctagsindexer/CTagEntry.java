/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.internal.core.index.ctagsindexer;

import java.util.HashMap;
import java.util.StringTokenizer;


class CTagEntry{
	private final CTagsConsoleParser parser;
    String elementName;
	String fileName;
	int lineNumber;

	/* Miscellaneous extension fields */
	HashMap tagExtensionField;

	String line;

	public CTagEntry(CTagsConsoleParser parser, String line) {
		this.line = line;
        this.parser = parser; 
		elementName = ""; //$NON-NLS-1$
		fileName =""; //$NON-NLS-1$
		lineNumber = 0;
		tagExtensionField = new HashMap();
		parse();
	}
	
	void parse () {
		String delim = CTagsConsoleParser.TAB_SEPARATOR;
		StringTokenizer st = new StringTokenizer(line, delim);
		for (int state = 0; st.hasMoreTokens(); state++) {
			String token = st.nextToken();
			
			switch (state) {
				case 0: // ELEMENT_NAME:
					elementName = token;
				break;

				case 1: // FILE_NAME:
					fileName = token;
				break;

				case 2: // LINE NUMBER;
					try {
						String sub = token.trim();
						if (Character.isDigit(sub.charAt(0))) {
							lineNumber = Integer.parseInt(sub);
						}
					} catch (NumberFormatException e) {
					} catch (IndexOutOfBoundsException e) {
					}
				break;

				default: // EXTENSION_FIELDS:
					int i = token.indexOf(':');
					if (i != -1) {
						String key = token.substring(0, i);
						String value = token.substring(i + 1);
						tagExtensionField.put(key, value);
					}
				break;
			}
		}
	}
}