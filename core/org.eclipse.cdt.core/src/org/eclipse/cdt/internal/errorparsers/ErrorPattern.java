/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.errorparsers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.core.resources.IFile;

/**
 * @author Doug Schaefer
 *
 */
public class ErrorPattern {
	private final Pattern pattern;
	private final int groupFileName;
	private final int groupLineNum;
	private final int groupDesc;
	private final int groupVarName;
	private final int severity;
	
	/**
	 * Full Pattern Constructor.
	 * 
	 * @param pattern
	 * @param groupFileName
	 * @param groupLineNum
	 * @param groupDesc
	 * @param groupVarName
	 * @param severity
	 */
	public ErrorPattern(String pattern,
						int groupFileName,
						int groupLineNum,
						int groupDesc,
						int groupVarName,
						int severity) {
		this.pattern = Pattern.compile(pattern);
		this.groupFileName = groupFileName;
		this.groupLineNum = groupLineNum;
		this.groupDesc = groupDesc;
		this.groupVarName = groupVarName;
		this.severity = severity;
	}
	
	/**
	 * Pattern for errors not associated file a file
	 * (e.g. make and linker errors).
	 * 
	 * @param pattern
	 * @param groupDesc
	 * @param severity
	 */
	public ErrorPattern(String pattern, int groupDesc, int severity) {
		this(pattern, 0, 0, groupDesc, 0, severity);
	}

	/**
	 * Pattern for errors that should be skipped.
	 * 
	 * @param pattern
	 */
	public ErrorPattern(String pattern) {
		this(pattern, 0, 0, 0, 0, -1);
	}
	public Matcher getMatcher(CharSequence input) {
		return pattern.matcher(input);
	}
	
	public String getFileName(Matcher matcher) {
		return groupFileName != 0 ? matcher.group(groupFileName) : null;
	}
	
	public int getLineNum(Matcher matcher) {
		try {
			return groupLineNum != 0
				? Integer.valueOf(matcher.group(groupLineNum))
				: 0;
		} catch (NumberFormatException e) {
			return 0;
		}
	}
	
	public String getDesc(Matcher matcher) {
		return groupDesc != 0 ? matcher.group(groupDesc) : null;
	}
	
	public String getVarName(Matcher matcher) {
		return groupVarName != 0 ? matcher.group(groupVarName) : null;
	}
	
	public int getSeverity(Matcher matcher) {
		return severity;
	}
	
	public boolean processLine(String line, ErrorParserManager eoParser) {
		Matcher matcher = getMatcher(line);
		if (!matcher.find())
			return false;

		return recordError(matcher, eoParser);
	}
	
	protected boolean recordError(Matcher matcher, ErrorParserManager eoParser) {
		int severity = getSeverity(matcher);
		if (severity == -1)
			// Skip
			return true;

		String fileName = getFileName(matcher);
		int lineNum = getLineNum(matcher);
		String desc = getDesc(matcher);
		String varName = getVarName(matcher);
		
		IFile file = null;
		if (fileName != null) {
			file = eoParser.findFileName(fileName);
			if (file != null) {
				if (eoParser.isConflictingName(fileName)) {
					file = null;
				}
			} else {
				file = eoParser.findFilePath(fileName);
			}
			if (file == null) {
				desc = fileName + " " + desc; //$NON-NLS-1$
			}
		}
		
		eoParser.generateMarker(file, lineNum, desc, severity, varName);
		return true;
	}
}
