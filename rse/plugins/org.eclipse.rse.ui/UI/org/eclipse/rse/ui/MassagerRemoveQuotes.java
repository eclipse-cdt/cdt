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
 * Utility class used by UI widgets to format a srcpf or member Description string.<br>
 * It checks to see if the string is legally quoted, and if not, it returns it as is. A legally quoted
 * string is one which begins and ends with single quotes, and where all single quotes inside the 
 * string are escaped with another single quote. If the string is legally quoted, it de-quotes it 
 * (hence the name). Dequoting means remove the single quotes, and remove any escape quotes 
 * from the inside of the string.
 * There is also a general constructor that takes in the quoting character, the character to escape, 
 * and the character to use as an escape charcter. It also takes n a boolean flag that decides wether 
 * or not the string has to be quoted before this massager actually does the job. 
 */
public class MassagerRemoveQuotes implements ISystemMassager {



	private String quoteChar;
	private String charToEscape;
	private String escapeChar;
	private boolean mustBeQuoted;

	/**
	 * Default constructor for MassagerRemoveQuotes.<br>
	 * Assumes that a legal string is one where the string is quoted with single quotes,
	 * and all inner quotes are escaped with a single quote.
	 */
	public MassagerRemoveQuotes() {
		this('\'', '\'', '\'', true);
	}

	/**
	 * Generic constructor. A valid string is one where every charToEscape is actually 
	 * escaped with an escapeChar before it. If mustBeQuoted is true, then the string
	 * is only valid if it is quoted with the quoteChar, and the characters inside the 
	 * string itself are properly escaped. If the string is determined to be a valid string, 
	 * this massager returns the string with the quotes and escape characters stripped out.
	 * if not, the string is returned as is.  
	*/
	public MassagerRemoveQuotes(
		char quoteChar,
		char charToEscape,
		char escapeChar,
		boolean mustBeQuoted) {

		this.quoteChar = String.valueOf(quoteChar);
		this.charToEscape = String.valueOf(charToEscape);
		this.escapeChar = String.valueOf(escapeChar);
		this.mustBeQuoted = mustBeQuoted;
	}

	public String massage(String text) {
		String strippedText = text;

		if (mustBeQuoted) {
			if (!isQuoted(text))
				// String is not quoted, when it should be, return it as is. 
				// No need to de-quote since it is not a legal string.
				return text;
			else
				strippedText = stripQuotes(text);
		}

		// check to see if string is a legal string, and if it is, de-quote it. 
		boolean islegal = isLegalString(strippedText);
		if (islegal)
			return deQuote(strippedText);
		else
			return text;
	}

	/**
	 * Returns true if string is single quoted.
	 */
	protected boolean isQuoted(String text) {
		if (text.startsWith(quoteChar) && text.endsWith(quoteChar))
			return true;
		else
			return false;
	}

	/**
	 * Checks to see if we have a valid string. A valid string is one where all 
	 * quotes are escaped with another quote.
	 */
	protected boolean isLegalString(String text) {
		if (charToEscape.equals(escapeChar))
			return doForwardChecking(text);
		else
			return doBackwardChecking(text);
	}

	private boolean doForwardChecking(String text) {
		int index = text.indexOf(charToEscape);
		while (index != -1) {
			// check the char AFTER the escape char since they are both the
			// same. . Be careful if it is the last char.
			if ((index == text.length() - 1)
				|| (text.charAt(index + 1) != escapeChar.charAt(0)))
				// we have a quote that is not escaped => not a legal string. 
				return false;

			// search for another quote *after* the escaped one.	
			index = text.indexOf(charToEscape, index + 2);
		}

		// all quotes are escaped, legal string.
		return true;

	}

	private boolean doBackwardChecking(String text) {
		int index = text.indexOf(charToEscape);
		while (index != -1) {
			// check the char before the character to escape. Be careful if it is the first char.
			if ((index == 0)
				|| (text.charAt(index - 1) != escapeChar.charAt(0)))
				// we have a quote that is not escaped => not a legal string. 
				return false;

			// search for another quote *after* the escaped one.	
			index = text.indexOf(charToEscape, index + 1);
		}

		// all quotes are escaped, legal string.
		return true;

	}

	/**
	 * Removes first and last chars if they are single quotes, otherwise 
	 * returns the string as is.
	 */
	private String stripQuotes(String text) {
		if (isQuoted(text)) {
			text = text.substring(1, text.length() - 1);
		}
		return text;
	}

	/**
	 * This method assumes that the passed string is a legal string, and it does
	 * the qe-quoting.
	 */
	private String deQuote(String text) {
		if (charToEscape.equals(escapeChar))
			return doForwardDeQuote(text);
		else
			return doBackwardDeQuote(text);
	}

	private String doForwardDeQuote(String text) {
		int index = text.indexOf(charToEscape);
		while (index != -1) {
			// strip the escape char.
			text = text.substring(0, index) + text.substring(index + 1);

			// search for another quote *after* the escaped one.	
			index = text.indexOf(charToEscape, index + 2);
		}
		return text;
	}

	private String doBackwardDeQuote(String text) {
		int index = text.indexOf(charToEscape);
		while (index != -1) {
			// strip the escape char.
			text = text.substring(0, index - 1) + text.substring(index);

			// search for another quote *after* the escaped one.	
			index = text.indexOf(charToEscape, index + 1);
		}
		return text;
	}

}