package org.eclipse.cdt.internal.core.index;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
 
import java.io.BufferedReader;
import java.io.IOException;


/* This structure contains information about the tag file. */
public class CTagsHeader {

	final static String TAG_FILE_FORMAT =
		"!_TAG_FILE_FORMAT\t2\t/extended format; --format=1 will not append ;\" to lines/";
	final static String TAG_FILE_SORTED = 
		"!_TAG_FILE_SORTED\t0\t/0=unsorted, 1=sorted/";
	final static String TAG_PROGRAM_AUTHOR =
		"!_TAG_PROGRAM_AUTHOR\tDarren Hiebert\t/dhiebert@users.sourceforge.net/";
	final static String TAG_PROGRAM_NAME =
		"!_TAG_PROGRAM_NAME\tExuberant Ctags\t//";
	final static String TAG_PROGRAM_URL =
		"!_TAG_PROGRAM_URL\thttp://ctags.sourceforge.net\t/official site/";
	final static String TAG_PROGRAM_VERSION =
		"!_TAG_PROGRAM_VERSION\t5.2.3\t//";

	/* information about the structure of the tag file */

	final String TAGS_PREFIX = "!_";

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
			throw new IOException("Wrong Tag Format Header");
		}
		
		// !_TAG_FILE_SORTED       0       /0=unsorted, 1=sorted/
		sorted = in.readLine();
		if (sorted == null || !sorted.equals(TAG_FILE_SORTED)) {
			throw new IOException("Wrong Tag Format Header");
		}

		// !_TAG_PROGRAM_AUTHOR    Darren Hiebert  /dhiebert@users.sourceforge.net/
		author = in.readLine();
		if (author == null || !author.equals(TAG_PROGRAM_AUTHOR)) {
			throw new IOException("Wrong Tag Format Header");
		}

		// !_TAG_PROGRAM_NAME      Exuberant Ctags //
		name = in.readLine();
		if (name == null || !name.equals(TAG_PROGRAM_NAME)) {
			throw new IOException("Wrong Tag Format Header");
		}

		// !_TAG_PROGRAM_URL       http://ctags.sourceforge.net    /official site/
		url = in.readLine();
		if (url == null || !url.equals(TAG_PROGRAM_URL)) {
			throw new IOException("Wrong Tag Format Header");
		}

		// !_TAG_PROGRAM_VERSION   5.2.3   //
		version = in.readLine();
		if (version == null || !version.equals(TAG_PROGRAM_VERSION)) {
			throw new IOException("Wrong Tag Format Header");
		}
	} 

	public static String header() {
		return
		TAG_FILE_FORMAT + "\n" + TAG_FILE_SORTED + "\n" +
		TAG_PROGRAM_AUTHOR + "\n" + TAG_PROGRAM_NAME + "\n" +
		TAG_PROGRAM_URL + "\n" + TAG_PROGRAM_VERSION + "\n";
	}
}
