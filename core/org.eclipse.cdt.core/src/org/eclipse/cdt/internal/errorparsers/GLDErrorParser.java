package org.eclipse.cdt.internal.errorparsers;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.cdt.errorparsers.ErrorParserManager;
import org.eclipse.cdt.errorparsers.IErrorParser;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;

public class GLDErrorParser implements IErrorParser {

	public boolean processLine(String line, ErrorParserManager eoParser) {
		// binutils linker error:
		// 1- an error when trying to link
		// tempfile: In function `function':
		// tempfile(.text+0xhex): undefined reference to `symbol'
		// 2-
		// Something went wrong check if it is "ld" the linkeer bay cheching
		// the last letter for "ld"
		int firstColon= line.indexOf(':');
		if (firstColon != -1) {
			String buf= line.substring(0, firstColon);
			String desc= line.substring(firstColon + 1);
			int firstPara= buf.indexOf('(');
			int secondPara= buf.indexOf(')');
			if (firstPara >= 0 && secondPara >= 0) {
				String fileName = buf.substring(0, firstPara);
				String previous = eoParser.getPreviousLine();
				if (previous == null) 
					previous = "";
				int colon = previous.indexOf(':');
				if (colon != -1) {
					previous = previous.substring(colon + 1);
				}
				 
				desc = "*" + previous + " " + desc;
				// Since we do not have any way to know the name of the C file
				// where the undefined reference is refering we set the error
				// on the project.
				IFile file = eoParser.findFilePath(fileName);
				if (file == null) {
					desc = fileName + " " + desc;
				} 
				eoParser.generateMarker(file, 0, desc, IMarker.SEVERITY_ERROR, null);
			} else if (buf.endsWith("ld")){
				String fileName = line.substring(0, firstColon);
				IFile file = eoParser.findFilePath(fileName);
				if (file == null) {
					desc = fileName + " " + desc;
				} 
				eoParser.generateMarker(file, 0, desc, IMarker.SEVERITY_ERROR, null);
			}
		}
		return false;
	}
}
