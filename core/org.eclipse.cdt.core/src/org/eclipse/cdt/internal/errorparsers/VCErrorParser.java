package org.eclipse.cdt.internal.errorparsers;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.File;
import java.util.StringTokenizer;

import org.eclipse.cdt.errorparsers.ErrorParserManager;
import org.eclipse.cdt.errorparsers.IErrorParser;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;

public class VCErrorParser implements IErrorParser {

    public boolean processLine(String line, ErrorParserManager eoParser) {
	// msdev: filname(linenumber) : error/warning error_desc
	int firstColon= line.indexOf(':');
	if (firstColon != -1) {
	    String firstPart= line.substring(0, firstColon);
	    StringTokenizer tok= new StringTokenizer(firstPart, "()");
	    if (tok.hasMoreTokens()) {
		String fileName= tok.nextToken();
		if (tok.hasMoreTokens()) {
		    String lineNumber= tok.nextToken();
		    try {
			int num= Integer.parseInt(lineNumber);
			int i= fileName.lastIndexOf(File.separatorChar);
			if (i != -1) {
			    fileName= fileName.substring(i + 1);
			}
			IFile file= eoParser.findFileName(fileName);
			if (file != null || eoParser.isConflictingName(fileName)) {
			    String desc= line.substring(firstColon + 1).trim();
			    if (file == null) {
				desc= "*" + desc;
			    }
			    int severity= IMarker.SEVERITY_ERROR;
			    if (desc.startsWith("warning")) {
				severity= IMarker.SEVERITY_WARNING;
			    }
			    eoParser.generateMarker(file, num, desc, severity, null);
			    return true;
			}
		    } catch (NumberFormatException e) {
		    }
		}
	    }
	}
	return false;
    }
}
