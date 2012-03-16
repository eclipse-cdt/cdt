/*******************************************************************************
 * Copyright (c) 2006, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX - initial API and implementation,  @author Doug Schaefer
 *******************************************************************************/

package org.eclipse.cdt.core.errorparsers;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.internal.core.Cygwin;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * Error Pattern - used by Error Parser to convert build output to problem markers
 * @since 5.1
 *
 * Clients may extend this class.
 */
public class ErrorPattern {
	private final Pattern pattern;
	private final int groupFileName;
	private final int groupLineNum;
	private final int groupDesc;
	private final int groupVarName;
	private final int severity;

	private static boolean isCygwin = true;

	/**
	 * Full Pattern Constructor. Note that a group equal -1 means that
	 * the parameter is missing in the error message.
	 *
	 * @param pattern - regular expression describing the message
	 * @param groupFileName - matcher group of file name
	 * @param groupLineNum - matcher group of line number
	 * @param groupDesc - matcher group of description
	 * @param groupVarName - matcher group of variable name
	 * @param severity - severity, one of
	 *        <br>{@link IMarkerGenerator#SEVERITY_INFO},
	 *        <br>{@link IMarkerGenerator#SEVERITY_WARNING},
	 *        <br>{@link IMarkerGenerator#SEVERITY_ERROR_RESOURCE},
	 *        <br>{@link IMarkerGenerator#SEVERITY_ERROR_BUILD}
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
	 * @param pattern - regular expression describing the message
	 * @param groupDesc - matcher group of description
	 * @param severity - severity, one of
	 *        <br>{@link IMarkerGenerator#SEVERITY_INFO},
	 *        <br>{@link IMarkerGenerator#SEVERITY_WARNING},
	 *        <br>{@link IMarkerGenerator#SEVERITY_ERROR_RESOURCE},
	 *        <br>{@link IMarkerGenerator#SEVERITY_ERROR_BUILD}
	 */
	public ErrorPattern(String pattern, int groupDesc, int severity) {
		this(pattern, 0, 0, groupDesc, 0, severity);
	}

	/**
	 * Pattern for errors that should be skipped.
	 *
	 * @param pattern - error pattern.
	 */
	public ErrorPattern(String pattern) {
		this(pattern, 0, 0, 0, 0, -1);
	}

	/**
	 * @param input - input line.
	 * @return matcher to interpret the input line.
	 */
	public Matcher getMatcher(CharSequence input) {
		return pattern.matcher(input);
	}

	/**
	 * @param matcher - matcher to parse the input line.
	 * @return parsed file name or {@code null}.
	 */
	public String getFileName(Matcher matcher) {
		return groupFileName != 0 ? matcher.group(groupFileName) : null;
	}

	/**
	 * @param matcher - matcher to parse the input line.
	 * @return parsed line number or {@code 0}.
	 */
	public int getLineNum(Matcher matcher) {
		try {
			return groupLineNum != 0
				? Integer.valueOf(matcher.group(groupLineNum)).intValue()
				: 0;
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	/**
	 * @param matcher - matcher to parse the input line.
	 * @return parsed description or {@code null}.
	 */
	public String getDesc(Matcher matcher) {
		return groupDesc != 0 ? matcher.group(groupDesc) : null;
	}

	/**
	 * @param matcher - matcher to parse the input line.
	 * @return parsed variable name or {@code null}.
	 */
	public String getVarName(Matcher matcher) {
		return groupVarName != 0 ? matcher.group(groupVarName) : null;
	}

	/**
	 * @param matcher - matcher to parse the input line.
	 * @return severity of the problem.
	 */
	public int getSeverity(Matcher matcher) {
		return severity;
	}

	/**
	 * Parse a line of build output and register error/warning for
	 * Problems view.
	 *
	 * @param line - one line of output.
	 * @param eoParser - {@link ErrorParserManager}.
	 * @return {@code true} if error/warning/info problem was found.
	 */
	public boolean processLine(String line, ErrorParserManager eoParser) {
		Matcher matcher = getMatcher(line);
		if (!matcher.find())
			return false;

		return recordError(matcher, eoParser);
	}

	/**
	 * Register the error in {@link ErrorParserManager}.
	 *
	 * @param matcher - matcher to parse the input line.
	 * @param eoParser - {@link ErrorParserManager}.
	 * @return {@code true} indicating that error was found.
	 */
	protected boolean recordError(Matcher matcher, ErrorParserManager eoParser) {
		int severity = getSeverity(matcher);
		if (severity == -1)
			// Skip
			return true;

		String fileName = getFileName(matcher);
		int lineNum = getLineNum(matcher);
		String desc = getDesc(matcher);
		String varName = getVarName(matcher);
		IPath externalPath = null ;

		IResource file = null;
		if (fileName != null) {
			file = eoParser.findFileName(fileName);

			if (file == null) {
				// If the file is not found in the workspace we attach the problem to the project
				// and add the external path to the file.
				file = eoParser.getProject();
				externalPath = getLocation(fileName);
			}
		}

		eoParser.generateExternalMarker(file, lineNum, desc, severity, varName, externalPath);
		return true;
	}

	/**
	 * If the file designated by filename exists, return the IPath representation of the filename
	 * If it does not exist, try cygpath translation
	 *
	 * @param filename - file name
	 * @return location (outside of the workspace).
	 */
	protected IPath getLocation(String filename)  {
		IPath path = new Path(filename);
		File file = path.toFile() ;
		if (!file.exists() && isCygwin && path.isAbsolute())  {
			try {
				String cygfilename = Cygwin.cygwinToWindowsPath(filename);
				IPath convertedPath = new Path(cygfilename);
				file = convertedPath.toFile() ;
				if (file.exists()) {
					path = convertedPath;
				}
			} catch (UnsupportedOperationException e) {
				isCygwin = false;
			} catch (IOException e) {
			}
		}
		return path ;
	}

}
