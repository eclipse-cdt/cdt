package org.eclipse.cdt.internal.errorparsers;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.IErrorParser;
import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.core.runtime.Path;

public class MakeErrorParser implements IErrorParser {
			
	public MakeErrorParser() {
	}
	
	static int getDirectoryLevel(String line) {
		int s = line.indexOf('[');
		if (s != -1) {
			int e = line.indexOf(']');
			String number = line.substring(s + 1, e);		
			int num= Integer.parseInt(number);
			return num;
		}
		return 0;
	}
	
	public boolean processLine(String line, ErrorParserManager eoParser) {
		// make\[[0-9]*\]:  error_desc
		int firstColon= line.indexOf(':');
		if (firstColon != -1 && line.startsWith("make")) {
			boolean enter = false;
			String msg= line.substring(firstColon + 1);		
			if ((enter = msg.startsWith(" Entering directory")) ||
			    (msg.startsWith(" Leaving directory"))) {
			    int s = msg.indexOf('`');
			    int e = msg.indexOf('\'');
			    if (s != -1 && e != -1) {
			    	String dir = msg.substring(s+1, e);
			    	if (enter) {
			    		/* Sometimes make screws up the output, so
			    		 * "leave" events can't be seen.  Double-check level
			    		 * here.
			    		 */
			    		int level = getDirectoryLevel(line);
			    		int parseLevel = eoParser.getDirectoryLevel();
		    			for (; level < parseLevel; level++) {
		    				eoParser.popDirectory();
		    			}
			    		eoParser.pushDirectory(new Path(dir));
			    	} else {
			    		eoParser.popDirectory();
			    		/* Could check to see if they match */
			    	}
				}
			} else if (msg.startsWith(" ***")) {
				eoParser.generateMarker(null, -1, msg, IMarkerGenerator.SEVERITY_ERROR_BUILD, null);
			}
		}
		return false;
	}
}
