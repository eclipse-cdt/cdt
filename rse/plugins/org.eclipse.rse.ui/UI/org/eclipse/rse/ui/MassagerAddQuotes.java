/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.ui;

/**
 * This massager will take a string an add quotes to it by
 * wrapping the string in the quote character and doubling
 * any interior instances of the quote character.
 */
public class MassagerAddQuotes implements ISystemMassager {


	
	private char quote = '\'';

	/**
	 * Construct a new instance of the massager.  This instance
	 * assumes the quote character is the apostrophe '\''.
	 */
	public MassagerAddQuotes() {
		super();
	}

	/**
	 * Construct a new instance of the massager.  This instance
	 * uses the supplied character as the quoting character.
	 * 
	 * @param quote the quote character to use in quoting strings
	 */
	public MassagerAddQuotes(char quote) {
		this.quote = quote;
	}
	
	/**
	 * Quotes the string by surround the original string with 
	 * the quote character and doubling any internal occurences of 
	 * the character.
	 * 
	 * @param text the string to be quoted
	 * @return the quoted string
	 * @see org.eclipse.rse.ui.ISystemMassager#massage(String)
	 */
	public String massage(String text) {

		char[] chars = text.toCharArray();

		/* determine the number of extra quotes needed */
		int n = 0;
		for (int i = 0; i < chars.length; i++) {
			if (chars[i] == quote) {
				n++;
			}
		}
		n += 2;

		/* Allocate and move the characters into the buffer */
		StringBuffer buf = new StringBuffer(chars.length + n);
		buf.append(quote);
		for (int i = 0; i < chars.length; i++) {
			if (chars[i] == quote) {
				buf.append(quote);
			}
			buf.append(chars[i]);
		}
		buf.append(quote);

		return buf.toString();
	}
	
	

}