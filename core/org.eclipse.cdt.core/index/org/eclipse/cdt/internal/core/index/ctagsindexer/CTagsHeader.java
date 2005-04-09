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

import java.io.IOException;
import java.io.BufferedReader;


public class CTagsHeader {

	final static String TAG_FILE_FORMAT =
		"!_TAG_FILE_FORMAT\t2\t/extended format; --format=1 will not append ;\" to lines/"; //$NON-NLS-1$
	final static String TAG_FILE_SORTED = 
		"!_TAG_FILE_SORTED\t0\t/0=unsorted, 1=sorted, 2=foldcase/"; //$NON-NLS-1$
	final static String TAG_PROGRAM_AUTHOR =
		"!_TAG_PROGRAM_AUTHOR\tDarren Hiebert\t/dhiebert@users.sourceforge.net/"; //$NON-NLS-1$
	final static String TAG_PROGRAM_NAME =
		"!_TAG_PROGRAM_NAME\tExuberant Ctags\t//"; //$NON-NLS-1$
	final static String TAG_PROGRAM_URL =
		"!_TAG_PROGRAM_URL\thttp://ctags.sourceforge.net\t/official site/"; //$NON-NLS-1$
	final static String TAG_PROGRAM_VERSION =
		"!_TAG_PROGRAM_VERSION\t5.5.4\t//"; //$NON-NLS-1$

	/* information about the structure of the tag file */

	final String TAGS_PREFIX = "!_"; //$NON-NLS-1$

	/* Format of tag file (1 = original, 2 = extended) */
	String format;

	/* Is the tag file sorted? (0 = unsorted, 1 = sorted) */
	String sorted;

	/* Information about the program which created this tag file */

	/* Name of author of generating program (may be null) */
	String author;

	/* Name of program (may be null) */
	String name;

	/* URL of distribution (may be null) */
	String url;

	/* program version (may be null) */
	String version;

	void parse (BufferedReader in) throws IOException {
		// !_TAG_FILE_FORMAT   2      /extended format; --format=1 will not append ;" to lines/
		format = in.readLine();
		if (format == null || !format.equals(TAG_FILE_FORMAT)) {
			throw new IOException("Wrong Tag Format Header: Needs to be --format=2"); //$NON-NLS-1$
		}
		
		// !_TAG_FILE_SORTED       0       /0=unsorted, 1=sorted/
		sorted = in.readLine();
		if (sorted == null || !sorted.equals(TAG_FILE_SORTED)) {
			throw new IOException("Wrong Tag Format Header: Sort needs to be --sort=no"); //$NON-NLS-1$
		}

		// !_TAG_PROGRAM_AUTHOR    Darren Hiebert  /dhiebert@users.sourceforge.net/
		author = in.readLine();
		/*if (author == null || !author.equals(TAG_PROGRAM_AUTHOR)) {
			throw new IOException("Wrong Tag Format Header");
		}*/

		// !_TAG_PROGRAM_NAME      Exuberant Ctags //
		name = in.readLine();
		if (name == null || !name.equals(TAG_PROGRAM_NAME)) {
			throw new IOException("Wrong Tag Format Header: Must use Exuberant Ctags"); //$NON-NLS-1$
		}

		// !_TAG_PROGRAM_URL       http://ctags.sourceforge.net    /official site/
		url = in.readLine();
		/*if (url == null || !url.equals(TAG_PROGRAM_URL)) {
			throw new IOException("Wrong Tag Format Header"); //$NON-NLS-1$
		}*/

		// !_TAG_PROGRAM_VERSION   5.5.4   //
		version = in.readLine();
		/*if (version == null || !version.equals(TAG_PROGRAM_VERSION)) {
			throw new IOException("Wrong Tag Format Header: Need Exuberant CTags 5.5.4"); //$NON-NLS-1$
		}*/
	} 

	
	public String getHeader() {
	    return format + "\n" + sorted + "\n" +   //$NON-NLS-1$//$NON-NLS-2$
	    author + "\n" + name + "\n" +  //$NON-NLS-1$//$NON-NLS-2$
	    url + "\n" + version + "\n"; //$NON-NLS-1$//$NON-NLS-2$
	}
}
