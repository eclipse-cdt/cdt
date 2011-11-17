/*******************************************************************************
 * Copyright (c) 2009, 2009 Andrew Gvozdev (Quoin Inc.) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev (Quoin Inc.) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.errorparsers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.IErrorParser;
import org.eclipse.cdt.core.IErrorParserNamed;

/**
 * {@code RegexerrorParser} is an error parser designed to use regular expressions in order
 * to parse build output to produce Errors, Warnings or Infos in Problems View.
 *
 * Clients may extend this class. As it implements {@link Cloneable} interface those clients
 * must implement {@link Object#clone} and {@link Object#equals} methods to avoid slicing.
 * Hint to implementers: if you want to extend it with customized {@link RegexErrorPattern}
 * it is possible to inject it in {@link #addPattern(RegexErrorPattern)}.
 *
 * @see IErrorParser
 * @since 5.2
 */
public class RegexErrorParser implements IErrorParserNamed {
	private String fId;
	private String fName;
	private final List<RegexErrorPattern> fPatterns= new ArrayList<RegexErrorPattern>();

	/**
	 * Default constructor will initialize the error parser with the name of the class
	 * using reflection mechanism.
	 */
	public RegexErrorParser() {
		fName = this.getClass().getSimpleName();
		fId = this.getClass().getCanonicalName();
	}

	/**
	 * Constructor to initialize ID and name of the error parser.
	 *
	 * @param id - ID of the error parser.
	 * @param name - name of the error parser.
	 */
	public RegexErrorParser(String id, String name) {
		fName = name;
		fId = id;
	}

	/**
	 * Set error parser ID.
	 *
	 * @param id of error parser
	 */
	@Override
	public void setId(String id) {
		fId = id;
	}

	/**
	 * Set error parser name.
	 *
	 * @param name of error parser
	 */
	@Override
	public void setName(String name) {
		fName = name;
	}

	/**
	 * Add new {@link RegexErrorPattern}.
	 *
	 * @param pattern - new pattern
	 */
	public void addPattern(RegexErrorPattern pattern) {
		fPatterns.add(pattern);
	}

	/**
	 * Remove error pattern from processing.
	 *
	 * @param pattern - error pattern to remove
	 */
	public void removePattern(RegexErrorPattern pattern) {
		fPatterns.remove(pattern);
	}

	/**
	 * Remove all error patterns.
	 */
	public void clearPatterns() {
		fPatterns.clear();
	}

	/**
	 * Method toString() for debugging purposes.
	 */
	@Override
	public String toString() {
		return "id="+fId+", name="+fName;  //$NON-NLS-1$//$NON-NLS-2$
	}

	/**
	 * @return id of error parser
	 */
	@Override
	public String getId() {
		return fId;
	}

	/**
	 * @return name of error parser
	 */
	@Override
	public String getName() {
		return fName;
	}

	/**
	 * @return array of error patterns of this error parser.
	 */
	public RegexErrorPattern[] getPatterns() {
		return fPatterns.toArray(new RegexErrorPattern[0]);
	}


	/**
	 * Parse a line of build output and register errors/warnings/infos for
	 * Problems view in internal list of {@link ErrorParserManager}.
	 *
	 * @param line - line of the input
	 * @param epManager - error parsers manager
	 * @return true if error parser recognized and accepted line, false otherwise
	 */
	@Override
	public boolean processLine(String line, ErrorParserManager epManager) {
		for (RegexErrorPattern pattern : fPatterns)
			try {
				if (pattern.processLine(line, epManager))
					return true;
			} catch (Exception e){
				String message = "Error parsing line [" + line + "]";  //$NON-NLS-1$//$NON-NLS-2$
				CCorePlugin.log(message, e);
			}

		return false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (o instanceof RegexErrorParser) {
			RegexErrorParser that = (RegexErrorParser)o;
			return this.fId.equals(that.fId)
				&& this.fName.equals(that.fName)
				&& this.fPatterns.equals(that.fPatterns);
		}
		return false;

	}

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		RegexErrorParser that = new RegexErrorParser(fId, fName);
		for (RegexErrorPattern pattern : fPatterns) {
			that.addPattern((RegexErrorPattern)pattern.clone());
		}
		return that;
	}
}
