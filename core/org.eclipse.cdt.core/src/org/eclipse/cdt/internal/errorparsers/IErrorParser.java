package org.eclipse.cdt.internal.errorparsers;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

public interface IErrorParser {
	/**
	 * Finds error or warnings on the given line
	 */
	boolean processLine(String line, ErrorParserManager eoParser);

}

