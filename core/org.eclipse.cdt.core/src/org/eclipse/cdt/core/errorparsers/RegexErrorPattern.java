/*******************************************************************************
 * Copyright (c) 2009, 2010 Andrew Gvozdev (Quoin Inc.) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev (Quoin Inc.) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.errorparsers;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.utils.CygPath;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * <p>RegexErrorPattern specifies a regular expression and rules how to create markers for
 * Problems View. It is used by {@link RegexErrorParser} to process build output.
 *
 * <p>Regex pattern used by this class is Java regular expression and defines capturing groups.
 * Those capturing groups are used in file, line, description expressions to get the values.
 * <p>For example: pattern <b>"(../../..) (.*):(\d*): (Error:.*)"</b> could go along with
 * file-expression <b>"$2"</b>, line-expression <b>"$3"</b> and description-expression <b>"$1 $4"</b>.
 *
 * <p>Note: variable name is being stored in marker tag. However currently it is not being used.
 *
 * <p>Severity could be one of:
 *        <br> - {@link IMarkerGenerator#SEVERITY_INFO},
 *        <br> - {@link IMarkerGenerator#SEVERITY_WARNING},
 *        <br> - {@link IMarkerGenerator#SEVERITY_ERROR_RESOURCE},
 *        <br> - {@link IMarkerGenerator#SEVERITY_ERROR_BUILD}
 *        <br> - {@link RegexErrorPattern#SEVERITY_SKIP}
 * <br/>{@code SEVERITY_SKIP} means that output line is checked to match the pattern
 * but won't be parsed to create a marker. It is useful with conjunction with
 * {@code eatProcessedLine=true} to filter out certain lines.
 *
 * <p>{@code eatProcessedLine} specifies if the current output line is being passed
 * to the rest of patterns for further processing or consumed by the pattern.
 *
 * <p>Clients may extend this class. As it implements {@link Cloneable} interface those clients
 * must implement {@link Object#clone} and {@link Object#equals} methods to avoid slicing.
 * @since 5.2
 */
public class RegexErrorPattern implements Cloneable {
	/**
	 * Additional "severity" flag which tells if qualified output line should be ignored.
	 */
	public static final int SEVERITY_SKIP = -1;
	private static final String EMPTY_STR=""; //$NON-NLS-1$

	private Pattern pattern;
	private String fileExpression;
	private String lineExpression;
	private String descriptionExpression;
	private String varNameExpression;
	private int severity;
	private boolean eatProcessedLine;

	private static boolean isCygwin = true;

	/**
	 * Constructor.
	 *
	 * @param pattern - regular expression describing the capturing groups
	 * @param fileExpression - capturing group expression defining file name
	 * @param lineExpression - capturing group expression defining line number
	 * @param descriptionExpression - capturing group expression defining description
	 * @param varNameExpression -capturing group expression defining variable name
	 * @param severity - severity, one of
	 *        <br>{@link IMarkerGenerator#SEVERITY_INFO},
	 *        <br>{@link IMarkerGenerator#SEVERITY_WARNING},
	 *        <br>{@link IMarkerGenerator#SEVERITY_ERROR_RESOURCE},
	 *        <br>{@link IMarkerGenerator#SEVERITY_ERROR_BUILD}
	 *        <br>{@link RegexErrorPattern#SEVERITY_SKIP}
	 * @param eat - defines whether to consume output line avoiding further processing by other patterns
	 *
	 * <p>See general description for this class {@link RegexErrorPattern} for more details.
	 */
	public RegexErrorPattern(String pattern,
				String fileExpression,
				String lineExpression,
				String descriptionExpression,
				String varNameExpression,
				int severity,
				boolean eat) {
		this.pattern = Pattern.compile(pattern!=null ? pattern : EMPTY_STR);
		this.fileExpression = fileExpression!=null ? fileExpression : EMPTY_STR;
		this.lineExpression = lineExpression!=null ? lineExpression : EMPTY_STR;
		this.descriptionExpression = descriptionExpression!=null ? descriptionExpression : EMPTY_STR;
		this.varNameExpression = varNameExpression!=null ? varNameExpression : EMPTY_STR;
		this.severity = severity;
		this.eatProcessedLine = eat;
	}

	/**
	 * @return regular expression pattern
	 */
	public String getPattern() {
		return pattern.toString();
	}

	/**
	 * @return expression defining file name
	 */
	public String getFileExpression() {
		return fileExpression;
	}

	/**
	 * @return expression defining line number
	 */
	public String getLineExpression() {
		return lineExpression;
	}

	/**
	 * @return expression defining description
	 */
	public String getDescriptionExpression() {
		return descriptionExpression;
	}

	/**
	 * @return expression defining variable name
	 */
	public String getVarNameExpression() {
		return varNameExpression;
	}

	/**
	 * @return severity of the marker, one of:
	 *        <br>{@link IMarkerGenerator#SEVERITY_INFO},
	 *        <br>{@link IMarkerGenerator#SEVERITY_WARNING},
	 *        <br>{@link IMarkerGenerator#SEVERITY_ERROR_RESOURCE},
	 *        <br>{@link IMarkerGenerator#SEVERITY_ERROR_BUILD}
	 */
	public int getSeverity() {
		return severity;
	}

	/**
	 * @return whether output line is consumed and not processed further by other patterns
	 */
	public boolean isEatProcessedLine() {
		return eatProcessedLine;
	}

	/**
	 * @param pattern - regular expression pattern describing the capturing groups
	 */
	public void setPattern(String pattern) {
		this.pattern = Pattern.compile(pattern);
	}

	/**
	 * @param fileExpression - capturing group expression defining file name
	 */
	public void setFileExpression(String fileExpression) {
		this.fileExpression = fileExpression;
	}

	/**
	 * @param lineExpression - capturing group expression defining line number
	 */
	public void setLineExpression(String lineExpression) {
		this.lineExpression = lineExpression;
	}

	/**
	 * @param descriptionExpression - capturing group expression defining description
	 */
	public void setDescriptionExpression(String descriptionExpression) {
		this.descriptionExpression = descriptionExpression;
	}

	/**
	 * @param varNameExpression -capturing group expression defining variable name
	 */
	public void setVarNameExpression(String varNameExpression) {
		this.varNameExpression = varNameExpression;
	}

	/**
	 * @param severity - severity, one of
	 *        <br>{@link IMarkerGenerator#SEVERITY_INFO},
	 *        <br>{@link IMarkerGenerator#SEVERITY_WARNING},
	 *        <br>{@link IMarkerGenerator#SEVERITY_ERROR_RESOURCE},
	 *        <br>{@link IMarkerGenerator#SEVERITY_ERROR_BUILD}
	 *        <br>{@link RegexErrorPattern#SEVERITY_SKIP}
	 */
	public void setSeverity(int severity) {
		this.severity = severity;
	}

	/**
	 * @param eatProcessedLine - whether to consume output line avoiding further processing by other patterns
	 */
	public void setEatProcessedLine(boolean eatProcessedLine) {
		this.eatProcessedLine = eatProcessedLine;
	}

	/**
	 * @param input - input line.
	 * @return matcher to interpret the input line.
	 */
	private Matcher getMatcher(CharSequence input) {
		return pattern.matcher(input);
	}

	private String parseStr(Matcher matcher, String str) {
		if (str!=null)
			return matcher.replaceAll(str);
		return null;
	}
	/**
	 * @param matcher - matcher to parse the input line.
	 * @return parsed file name or {@code null}.
	 */
	protected String getFileName(Matcher matcher) {
		return parseStr(matcher, fileExpression);
	}

	/**
	 * @param matcher - matcher to parse the input line.
	 * @return parsed line number or {@code 0}.
	 */
	protected int getLineNum(Matcher matcher) {
		if (lineExpression != null)
			try {
				return Integer.valueOf(matcher.replaceAll(lineExpression)).intValue();
			} catch (NumberFormatException e) {
			}
		return 0;
	}

	/**
	 * @param matcher - matcher to parse the input line.
	 * @return parsed description or {@code null}.
	 */
	protected String getDesc(Matcher matcher) {
		return parseStr(matcher, descriptionExpression);
	}

	/**
	 * @param matcher - matcher to parse the input line.
	 * @return parsed variable name or {@code null}.
	 */
	protected String getVarName(Matcher matcher) {
		return parseStr(matcher, varNameExpression);
	}

	/**
	 * @param matcher - matcher to parse the input line.
	 * @return severity of the problem.
	 */
	protected int getSeverity(Matcher matcher) {
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
		// pattern should cover the whole line
		if (!(matcher.find() && matcher.group(0).length()==line.length()))
			return false;

		recordError(matcher, eoParser);
		return eatProcessedLine;
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
		if (severity == SEVERITY_SKIP)
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
	private IPath getLocation(String filename)  {
		IPath path = new Path(filename);
		File file = path.toFile() ;
		if (!file.exists() && isCygwin && path.isAbsolute())  {
			CygPath cygpath = null ;
			try {
				cygpath = new CygPath("cygpath"); //$NON-NLS-1$
				String cygfilename = cygpath.getFileName(filename);
				IPath convertedPath = new Path(cygfilename);
				file = convertedPath.toFile() ;
				if (file.exists()) {
					path = convertedPath;
				}
			} catch (UnsupportedOperationException e) {
				isCygwin = false;
			} catch (IOException e) {
			} finally {
				if (null!=cygpath)  {
					cygpath.dispose();
				}
			}
		}
		return path ;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (o instanceof RegexErrorPattern) {
			RegexErrorPattern that = (RegexErrorPattern)o;
			return this.pattern.toString().equals(that.pattern.toString())
				&& this.fileExpression.equals(that.fileExpression)
				&& this.lineExpression.equals(that.lineExpression)
				&& this.descriptionExpression.equals(that.descriptionExpression)
				&& this.varNameExpression.equals(that.varNameExpression)
				&& this.severity==that.severity
				&& this.eatProcessedLine==that.eatProcessedLine;
		}
		return false;

	}

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		return new RegexErrorPattern(pattern.toString(),
				fileExpression,
				lineExpression,
				descriptionExpression,
				varNameExpression,
				severity,
				eatProcessedLine);
	}
}
