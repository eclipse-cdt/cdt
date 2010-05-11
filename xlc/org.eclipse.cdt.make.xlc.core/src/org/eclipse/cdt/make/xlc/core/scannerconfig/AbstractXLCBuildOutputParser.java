/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.xlc.core.scannerconfig;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector2;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoConsoleParser;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.TraceUtil;
import org.eclipse.cdt.make.internal.core.scannerconfig2.SCProfileInstance;
import org.eclipse.cdt.make.internal.core.scannerconfig2.ScannerConfigProfileManager;
import org.eclipse.cdt.make.internal.core.scannerconfig2.ScannerConfigProfile.BuildOutputProvider;
import org.eclipse.cdt.make.xlc.core.activator.Activator;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;

/**
 * @author crecoskie
 * 
 */
public abstract class AbstractXLCBuildOutputParser implements IScannerInfoConsoleParser {

	protected static final String[] COMPILER_INVOCATION = { "xlc", "xlC"//$NON-NLS-1$ //$NON-NLS-2$
	};
	protected static final String DASHIDASH = "-I-"; //$NON-NLS-1$
	protected static final String DASHI = "-I"; //$NON-NLS-1$
	protected static final String DASHD = "-D"; //$NON-NLS-1$

	protected IProject fProject;
	protected IScannerInfoCollector fCollector;
	protected IPath fWorkingDir;
	protected IMarkerGenerator fMarkerGenerator;
	protected XLCBuildOutputParserUtility fUtility;

	protected boolean fBMultiline = false;
	protected String fSMultiline = ""; //$NON-NLS-1$

	protected String[] fCompilerCommands = { "xlc", "xlC" }; //$NON-NLS-1$ //$NON-NLS-2$

	/**
	 * @return Returns the fProject.
	 */
	protected IProject getProject() {
		return fProject;
	}

	/**
	 * @return Returns the fCollector.
	 */
	protected IScannerInfoCollector getCollector() {
		return fCollector;
	}

	public void startup(IProject project, IScannerInfoCollector collector) {
		fProject = project;
		fCollector = collector;
		fCompilerCommands = computeCompilerCommands();
	}

	/**
	 * Returns array of additional compiler commands to look for
	 * 
	 * @return String[]
	 */
	protected String[] computeCompilerCommands() {
		if (fProject != null) {
			SCProfileInstance profileInstance = ScannerConfigProfileManager.getInstance().getSCProfileInstance(
					fProject, ScannerConfigProfileManager.NULL_PROFILE_ID);
			BuildOutputProvider boProvider = profileInstance.getProfile().getBuildOutputProviderElement();
			if (boProvider != null) {
				String compilerCommandsString = boProvider.getScannerInfoConsoleParser().getCompilerCommands();
				if (compilerCommandsString != null && compilerCommandsString.length() > 0) {
					String[] compilerCommands = compilerCommandsString.split(",\\s*"); //$NON-NLS-1$
					if (compilerCommands.length > 0) {
						String[] compilerInvocation = new String[COMPILER_INVOCATION.length + compilerCommands.length];
						System.arraycopy(COMPILER_INVOCATION, 0, compilerInvocation, 0, COMPILER_INVOCATION.length);
						System.arraycopy(compilerCommands, 0, compilerInvocation, COMPILER_INVOCATION.length,
								compilerCommands.length);
						return compilerInvocation;
					}
				}
			}
		}
		return COMPILER_INVOCATION;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.make.core.scannerconfig.IScannerInfoConsoleParser#processLine
	 * (java.lang.String)
	 */
	public boolean processLine(String line) {
		boolean rc = false;
		int lineBreakPos = line.length() - 1;
		char[] lineChars = line.toCharArray();
		while (lineBreakPos >= 0 && Character.isWhitespace(lineChars[lineBreakPos])) {
			lineBreakPos--;
		}
		if (lineBreakPos >= 0) {
			if (lineChars[lineBreakPos] != '\\' || (lineBreakPos > 0 && lineChars[lineBreakPos - 1] == '\\')) {
				lineBreakPos = -1;
			}
		}
		// check for multiline commands (ends with '\')
		if (lineBreakPos >= 0) {
			fSMultiline += line.substring(0, lineBreakPos);
			fBMultiline = true;
			return rc;
		}
		if (fBMultiline) {
			line = fSMultiline + line;
			fBMultiline = false;
			fSMultiline = ""; //$NON-NLS-1$
		}
		line = line.trim();
		TraceUtil.outputTrace("XLCBuildOutputParser parsing line: [", line, "]"); //$NON-NLS-1$ //$NON-NLS-2$
		// make\[[0-9]*\]: error_desc
		int firstColon = line.indexOf(':');
		String make = line.substring(0, firstColon + 1);
		if (firstColon != -1 && make.indexOf("make") != -1) { //$NON-NLS-1$
			boolean enter = false;
			String msg = line.substring(firstColon + 1).trim();
			if ((enter = msg.startsWith("Entering directory")) || //$NON-NLS-1$
					(msg.startsWith("Leaving directory"))) { //$NON-NLS-1$
				int s = msg.indexOf('`');
				int e = msg.indexOf('\'');
				if (s != -1 && e != -1) {
					String dir = msg.substring(s + 1, e);
					if (getUtility() != null) {
						getUtility().changeMakeDirectory(dir, getDirectoryLevel(line), enter);
					}
					return rc;
				}
			}
		}
		// call sublclass to process a single line
		return processSingleLine(line.trim());
	}

	protected synchronized XLCBuildOutputParserUtility getUtility() {
		if (fUtility == null)
			fUtility = new XLCBuildOutputParserUtility(fProject, fWorkingDir, fMarkerGenerator);

		return fUtility;
	}

	protected int getDirectoryLevel(String line) {
		int s = line.indexOf('[');
		int num = 0;
		if (s != -1) {
			int e = line.indexOf(']');
			String number = line.substring(s + 1, e).trim();
			try {
				num = Integer.parseInt(number);
			} catch (NumberFormatException exc) {
			}
		}
		return num;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.make.core.scannerconfig.IScannerInfoConsoleParser#shutdown
	 * ()
	 */
	public void shutdown() {
		if (getUtility() != null) {
			getUtility().reportProblems();
		}
		
		if(fCollector != null && fCollector instanceof IScannerInfoCollector2) {
			IScannerInfoCollector2 collector = (IScannerInfoCollector2) fCollector;
			try {
				collector.updateScannerConfiguration(null);
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				Activator.log(e);
			}
		}
	}

	/**
	 * Tokenizes a line into an array of commands. Commands are separated by
	 * ';', '&&' or '||'. Tokens are separated by whitespace unless found inside
	 * of quotes, back-quotes, or double quotes. Outside of single-, double- or
	 * back-quotes a backslash escapes white-spaces, all quotes, the backslash,
	 * '&' and '|'. A backslash used for escaping is removed. Quotes other than
	 * the back-quote plus '&&', '||', ';' are removed, also.
	 * 
	 * @param line
	 *            to tokenize
	 * @return array of commands
	 */
	protected String[][] tokenize(String line, boolean escapeInsideDoubleQuotes) {
		ArrayList<String[]> commands = new ArrayList<String[]>();
		ArrayList<String> tokens = new ArrayList<String>();
		StringBuffer token = new StringBuffer();

		final char[] input = line.toCharArray();
		boolean nextEscaped = false;
		char currentQuote = 0;
		for (int i = 0; i < input.length; i++) {
			final char c = input[i];
			final boolean escaped = nextEscaped;
			nextEscaped = false;

			if (currentQuote != 0) {
				if (c == currentQuote) {
					if (escaped) {
						token.append(c);
					} else {
						if (c == '`') {
							token.append(c); // preserve back-quotes
						}
						currentQuote = 0;
					}
				} else {
					if (escapeInsideDoubleQuotes && currentQuote == '"' && c == '\\') {
						nextEscaped = !escaped;
						if (escaped) {
							token.append(c);
						}
					} else {
						if (escaped) {
							token.append('\\');
						}
						token.append(c);
					}
				}
			} else {
				switch (c) {
				case '\\':
					if (escaped) {
						token.append(c);
					} else {
						nextEscaped = true;
					}
					break;
				case '\'':
				case '"':
				case '`':
					if (escaped) {
						token.append(c);
					} else {
						if (c == '`') {
							token.append(c);
						}
						currentQuote = c;
					}
					break;
				case ';':
					if (escaped) {
						token.append(c);
					} else {
						endCommand(token, tokens, commands);
					}
					break;
				case '&':
				case '|':
					if (escaped || i + 1 >= input.length || input[i + 1] != c) {
						token.append(c);
					} else {
						i++;
						endCommand(token, tokens, commands);
					}
					break;

				default:
					if (Character.isWhitespace(c)) {
						if (escaped) {
							token.append(c);
						} else {
							endToken(token, tokens);
						}
					} else {
						if (escaped) {
							token.append('\\'); // for windows put backslash
												// back onto the token.
						}
						token.append(c);
					}
				}
			}
		}
		endCommand(token, tokens, commands);
		return commands.toArray(new String[commands.size()][]);
	}

	protected void endCommand(StringBuffer token, ArrayList<String> tokens, ArrayList<String[]> commands) {
		endToken(token, tokens);
		if (!tokens.isEmpty()) {
			commands.add(tokens.toArray(new String[tokens.size()]));
			tokens.clear();
		}
	}

	protected void endToken(StringBuffer token, ArrayList<String> tokens) {
		if (token.length() > 0) {
			tokens.add(token.toString());
			token.setLength(0);
		}
	}

	protected boolean processSingleLine(String line) {
		boolean rc = false;
		String[][] tokens = tokenize(line, true);
		for (int i = 0; i < tokens.length; i++) {
			String[] command = tokens[i];
			if (processCommand(command)) {
				rc = true;
			} else { // go inside quotes, if the compiler is called per wrapper
						// or shell script
				for (int j = 0; j < command.length; j++) {
					String[][] subtokens = tokenize(command[j], true);
					for (int k = 0; k < subtokens.length; k++) {
						String[] subcommand = subtokens[k];
						if (subcommand.length > 1) { // only proceed if there is
														// any additional info
							if (processCommand(subcommand)) {
								rc = true;
							}
						}
					}
				}
			}
		}
		return rc;
	}

	protected int findCompilerInvocation(String[] tokens) {
		for (int i = 0; i < tokens.length; i++) {
			final String token = tokens[i].toLowerCase();
			final int searchFromOffset = Math.max(token.lastIndexOf('/'), token.lastIndexOf('\\')) + 1;
			for (int j = 0; j < fCompilerCommands.length; j++) {
				if (token.indexOf(fCompilerCommands[j], searchFromOffset) != -1) {
					return i;
				}
			}
		}
		return -1;
	}

	public void startup(IProject project, IPath workingDirectory, IScannerInfoCollector collector,
			IMarkerGenerator markerGenerator) {
		fProject = project;
		fWorkingDir = workingDirectory;
		fCollector = collector;
		fMarkerGenerator = markerGenerator;

	}

	abstract protected boolean processCommand(String[] tokens);

	protected  List<String> getFileExtensionsList() {
		IContentTypeManager manager = Platform.getContentTypeManager();
		List<String> extensions = new LinkedList<String>();
		IContentType cSource = manager.getContentType(CCorePlugin.CONTENT_TYPE_CSOURCE);
		IContentType cppSource = manager.getContentType(CCorePlugin.CONTENT_TYPE_CXXSOURCE);

		String[] cExtensions = cSource.getFileSpecs(IContentType.FILE_EXTENSION_SPEC);
		String[] cppExtensions = cppSource.getFileSpecs(IContentType.FILE_EXTENSION_SPEC);

		for (int k = 0; k < cExtensions.length; k++) {
			extensions.add("." + cExtensions[k]); //$NON-NLS-1$
		}

		for (int k = 0; k < cppExtensions.length; k++) {
			extensions.add("." + cppExtensions[k]); //$NON-NLS-1$
		}

		return extensions;
	}

	protected String[] getFileExtensions() {
		IContentTypeManager manager = Platform.getContentTypeManager();
		List<String> extensions = new LinkedList<String>();
		IContentType cSource = manager.getContentType(CCorePlugin.CONTENT_TYPE_CSOURCE);
		IContentType cppSource = manager.getContentType(CCorePlugin.CONTENT_TYPE_CXXSOURCE);

		String[] cExtensions = cSource.getFileSpecs(IContentType.FILE_EXTENSION_SPEC);
		String[] cppExtensions = cppSource.getFileSpecs(IContentType.FILE_EXTENSION_SPEC);

		for (int k = 0; k < cExtensions.length; k++) {
			extensions.add("." + cExtensions[k]); //$NON-NLS-1$
		}

		for (int k = 0; k < cppExtensions.length; k++) {
			extensions.add("." + cppExtensions[k]); //$NON-NLS-1$
		}

		return extensions.toArray(new String[0]);
	}

}
