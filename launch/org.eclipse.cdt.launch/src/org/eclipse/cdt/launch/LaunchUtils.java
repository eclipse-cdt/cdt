/*******************************************************************************
 * Copyright (c) 2004, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.launch; 

import java.util.ArrayList;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.osgi.service.environment.Constants;

/**
 * Utility methods.
 */
public class LaunchUtils {
	/**
	 * For given launch configuration returns the program arguments as 
	 * an array of individual arguments.
	 */
	public static String[] getProgramArgumentsArray( ILaunchConfiguration config ) throws CoreException {
		return parseArguments( getProgramArguments( config ) );
	}

	/**
	 * Returns the program arguments as a String.
	 */
	public static String getProgramArguments(ILaunchConfiguration config) throws CoreException {
		String args = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, (String)null);
		if (args != null) {
			args = getStringVariableManager().performStringSubstitution(args);
		}
		return args;
	}

	/**
	 * Convenience method.
	 */
	public static IStringVariableManager getStringVariableManager() {
		return VariablesPlugin.getDefault().getStringVariableManager();
	}

	private static String[] parseArguments( String args ) {
		return argumentsToArray(args);
	}
	
	// this method is extracted as CommandLineUtil.argumentsToArray on HEAD
	private static String[] argumentsToArray(String line) {
		boolean osWin;
		try {
			osWin = Platform.getOS().equals(Constants.OS_WIN32);
		} catch (Exception e) {
			osWin = false;
		}
		if (osWin) {
			return argumentsToArrayWindowsStyle(line);
		} else {
			return argumentsToArrayUnixStyle(line);
		}
	}
	/**
	 * Parsing arguments in a shell style.
	 * i.e.
	 * <code>
	 * ["a b c" d] -> [[a b c],[d]]
	 * [a   d] -> [[a],[d]]
	 * ['"quoted"'] -> [["quoted"]]
	 * [\\ \" \a] -> [[\],["],[a]]
	 * ["str\\str\a"] -> [[str\str\a]]
	 * </code>
	 * @param line
	 * @return array of arguments, or empty array if line is null or empty
	 */
	private static String[] argumentsToArrayUnixStyle(String line) {
		final int INITIAL = 0;
		final int IN_DOUBLE_QUOTES = 1;
		final int IN_DOUBLE_QUOTES_ESCAPED = 2;
		final int ESCAPED = 3;
		final int IN_SINGLE_QUOTES = 4;
		final int IN_ARG = 5;

		if (line == null) {
			line = ""; //$NON-NLS-1$
		}

		char[] array = line.trim().toCharArray();
		ArrayList<String> aList = new ArrayList<String>();
		StringBuilder buffer = new StringBuilder();
		int state = INITIAL;
		for (int i = 0; i < array.length; i++) {
			char c = array[i];

			switch (state) {
				case IN_ARG:
					// fall through
				case INITIAL:
					if (Character.isWhitespace(c)) {
						if (state == INITIAL) break; // ignore extra spaces
						// add argument
						state = INITIAL;
						String arg = buffer.toString();
						buffer = new StringBuilder();
						aList.add(arg);
					} else {
						switch (c) {
							case '\\':
								state = ESCAPED;
								break;
							case '\'':
								state = IN_SINGLE_QUOTES;
								break;
							case '\"':
								state = IN_DOUBLE_QUOTES;
								break;
							default:
								state = IN_ARG;
								buffer.append(c);
								break;
						}
					}
					break;
				case IN_DOUBLE_QUOTES:
					switch (c) {
						case '\\':
							state = IN_DOUBLE_QUOTES_ESCAPED;
							break;
						case '\"':
							state = IN_ARG;
							break;
						default:
							buffer.append(c);
							break;
					}
					break;
				case IN_SINGLE_QUOTES:
					switch (c) {
						case '\'':
							state = IN_ARG;
							break;
						default:
							buffer.append(c);
							break;
					}
					break;
				case IN_DOUBLE_QUOTES_ESCAPED:
					switch (c) {
						case '\"':
						case '\\':
							buffer.append(c);
							break;
						case 'n':
							buffer.append("\n"); //$NON-NLS-1$
							break;
						default:
							buffer.append('\\');
							buffer.append(c);
							break;
					}
					state = IN_DOUBLE_QUOTES;
					break;
				case ESCAPED:
					buffer.append(c);
					state = IN_ARG;
					break;
			}
		}

		if (state != INITIAL) { // this allow to process empty string as an argument
			aList.add(buffer.toString());
		}
		return aList.toArray(new String[aList.size()]);
	}
	
	
	/**
	 * Parsing arguments in a cmd style.
	 * i.e.
	 * <code>
	 * ["a b c" d] -> [[a b c],[d]]
	 * [a   d] -> [[a],[d]]
	 * ['"quoted"'] -> [['quoted']]
	 * [\\ \" \a] -> [[\\],["],[\a]]
	 * ["str\\str\a"] -> [[str\\str\a]]
	 * </code>
	 * @param line
	 * @return array of arguments, or empty array if line is null or empty
	 */
	private static String[] argumentsToArrayWindowsStyle(String line) {
		final int INITIAL = 0;
		final int IN_DOUBLE_QUOTES = 1;
		final int IN_DOUBLE_QUOTES_ESCAPED = 2;
		final int ESCAPED = 3;
		final int IN_ARG = 5;

		if (line == null) {
			line = ""; //$NON-NLS-1$
		}

		char[] array = line.trim().toCharArray();
		ArrayList<String> aList = new ArrayList<String>();
		StringBuilder buffer = new StringBuilder();
		int state = INITIAL;
		for (int i = 0; i < array.length; i++) {
			char c = array[i];

			switch (state) {
				case IN_ARG:
					// fall through
				case INITIAL:
					if (Character.isWhitespace(c)) {
						if (state == INITIAL) break; // ignore extra spaces
						// add argument
						state = INITIAL;
						String arg = buffer.toString();
						buffer = new StringBuilder();
						aList.add(arg);
					} else {
						switch (c) {
							case '\\':
								state = ESCAPED;
								break;
							case '\"':
								state = IN_DOUBLE_QUOTES;
								break;
							default:
								state = IN_ARG;
								buffer.append(c);
								break;
						}
					}
					break;
				case IN_DOUBLE_QUOTES:
					switch (c) {
						case '\\':
							state = IN_DOUBLE_QUOTES_ESCAPED;
							break;
						case '\"':
							state = IN_ARG;
							break;
						default:
							buffer.append(c);
							break;
					}
					break;
				case IN_DOUBLE_QUOTES_ESCAPED:
					switch (c) {
						case '\"':
							buffer.append(c);
							break;
						default:
							buffer.append('\\');
							buffer.append(c);
							break;
					}
					state = IN_DOUBLE_QUOTES;
					break;
				case ESCAPED:
					state = IN_ARG;
					switch (c) {
					case ' ':
					case '\"':
						buffer.append(c);
						break;
					default:
						buffer.append('\\');
						buffer.append(c);
						break;
					}
					break;
			}
		}

		if (state != INITIAL) { // this allow to process empty string as an argument
			aList.add(buffer.toString());
		}
		return aList.toArray(new String[aList.size()]);
	}
}
