package org.eclipse.cdt.internal.errorparsers;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.IErrorParser;
import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.core.resources.IFile;

public class GLDErrorParser implements IErrorParser {

	public boolean processLine(String line, ErrorParserManager eoParser) {
		// binutils linker error:
		// 1- an error when trying to link
		// tempfile: In function `function':
		// tempfile(.text+0xhex): undefined reference to `symbol'
		// 2-
		// Something went wrong check if it is "ld" the linkeer bay cheching
		// the last letter for "ld"
		// An example might be (not all are warnings):
		// ntox86-ld: warning: libcpp.so.2, needed by C:/temp//libdisplay.so, may conflict with libcpp.so.3
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
				eoParser.generateMarker(file, 0, desc, IMarkerGenerator.SEVERITY_ERROR_RESOURCE, null);
			} else if (buf.endsWith("ld")){
				// By default treat the condition as fatal/error, unless marked as a warning
				int errorType = IMarkerGenerator.SEVERITY_ERROR_RESOURCE;
				desc = desc.trim();
				if(desc.startsWith("warning") || desc.startsWith("Warning")) {
					errorType = IMarkerGenerator.SEVERITY_WARNING;
				}

				String fileName = line.substring(0, firstColon);
				IFile file = eoParser.findFilePath(fileName);
				if (file == null) {
					desc = fileName + " " + desc;
				} 
				
				eoParser.generateMarker(file, 0, desc, errorType, null);
			}
		}
		return false;
	}
}
