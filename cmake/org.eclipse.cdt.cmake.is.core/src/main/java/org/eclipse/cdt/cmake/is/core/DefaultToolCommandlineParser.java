/*******************************************************************************
 * Copyright (c) 2016-2020 Martin Weber.
 *
 * Content is provided to you under the terms and conditions of the Eclipse Public License Version 2.0 "EPL".
 * A copy of the EPL is available at http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.cmake.is.core;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.cdt.cmake.is.core.builtins.IBuiltinsDetectionBehavior;
import org.eclipse.cdt.cmake.is.core.internal.ParseContext;
import org.eclipse.cdt.cmake.is.core.internal.Plugin;
import org.eclipse.cdt.cmake.is.core.internal.StringUtil;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;

/**
 * Parses the build output produced by a specific tool invocation and detects
 * LanguageSettings.
 *
 * @author Martin Weber
 */
public class DefaultToolCommandlineParser implements IToolCommandlineParser {
	@SuppressWarnings("nls")
	private static final boolean DEBUG = Boolean.parseBoolean(Platform.getDebugOption(Plugin.PLUGIN_ID + "/CECC/args"));

	private final IArglet[] argumentParsers;
	private final String languageID;
	private final IResponseFileArglet responseFileArglet;
	private final IBuiltinsDetectionBehavior builtinsDetection;

	/** gathers the result */
	private ParseContext result;

	private IPath cwd;

	/**
	 * Constructs a new object with the given values.
	 * <p>
	 * NOTE: Concerning the {@code languageID} argument, please note that CDT
	 * expects "org.eclipse.cdt.core.gcc" for the C language and
	 * "org.eclipse.cdt.core.g++" for the C++ language. Some extension to CDT may
	 * recognize different language IDs, such as
	 * "com.nvidia.cuda.toolchain.language.cuda.cu"
	 * </p>
	 *
	 * @param languageID                the language ID of the language that the
	 *                                  tool compiles or {@code null} if the
	 *                                  language ID should be derived from the
	 *                                  source file-name extension
	 * @param responseFileArglet        the parsers for the response-file
	 *                                  command-line argument for the tool or
	 *                                  {@code null} if the tool does not recognize
	 *                                  a response-file argument
	 * @param builtinsDetectionBehavior the {@code IBuiltinsDetectionBehavior} which
	 *                                  specifies how built-in compiler macros and
	 *                                  include path detection is handled for a
	 *                                  specific compiler or {@code null} if the
	 *                                  compiler does not support built-in
	 *                                  detection.
	 * @param argumentParsers           the parsers for the command line arguments
	 *                                  of of interest for the tool
	 * @throws NullPointerException if any of the {@code builtinsDetection} or
	 *                              {@code argumentParsers} arguments is
	 *                              {@code null}
	 * @see Arglets various IArglet implementations you may want to use
	 * @see ResponseFileArglets IArglet implementations for response file arguments
	 *      you may want to use
	 */
	public DefaultToolCommandlineParser(String languageID, IResponseFileArglet responseFileArglet,
			IBuiltinsDetectionBehavior builtinsDetectionBehavior, IArglet... argumentParsers) {
		this.languageID = languageID;
		this.builtinsDetection = builtinsDetectionBehavior;
		this.argumentParsers = Objects.requireNonNull(argumentParsers, "argumentParsers"); //$NON-NLS-1$
		this.responseFileArglet = responseFileArglet;
	}

	@Override
	public IResult processArgs(IPath cwd, String args) {
		this.result = new ParseContext();
		this.cwd = Objects.requireNonNull(cwd, "cwd"); //$NON-NLS-1$

		ParserHandler ph = new ParserHandler();
		ph.parseArguments(responseFileArglet, args);
		return result;
	}

	/**
	 * Implemented to determine the language ID from the source file name extension,
	 * if the language ID of this object is {@code null}.
	 */
	@Override
	public String getLanguageId(String sourceFileExtension) {
		if (languageID != null) {
			return languageID;
		}
		return determineLanguageId(sourceFileExtension).orElse(null);
	}

	/**
	 * Default implementation return {@code null}.
	 *
	 * @return always {@code null}
	 */
	@Override
	public List<String> getCustomLanguageIds() {
		return null;
	}

	/**
	 * Gets the languageID of the specified file name extension. This is a
	 * convenience method for subclasses.
	 *
	 * @param sourceFileExtension The file name extension to examine
	 * @return an {@code Optional<String>} holding the language ID or {@code Optional.empty()} if the file name extension is
	 *         unknown.
	 */
	@SuppressWarnings("nls")
	protected Optional<String> determineLanguageId(String sourceFileExtension) {
		switch (sourceFileExtension) {
		case "c":
			return Optional.of("org.eclipse.cdt.core.gcc");
		case "C":
		case "cc":
		case "cpp":
		case "CPP":
		case "cp":
		case "cxx":
		case "c++":
			return Optional.of("org.eclipse.cdt.core.g++");
		default:
			return Optional.empty();
		}
	}

	@Override
	public IBuiltinsDetectionBehavior getIBuiltinsDetectionBehavior() {
		return builtinsDetection;
	}

	@SuppressWarnings("nls")
	@Override
	public String toString() {
		return "[languageID=" + this.languageID + ", argumentParsers=" + Arrays.toString(this.argumentParsers) + "]";
	}

	/**
	 * @param buildOutput the command line arguments to process
	 * @return the number of characters consumed
	 */
	private static int skipArgument(String buildOutput) {
		int consumed;

		// (blindly) advance to next whitespace
		if ((consumed = buildOutput.indexOf(' ')) != -1) {
			return consumed;
		} else {
			// non-option arg, may be a file name
			// for now, we just clear/skip the output
			return buildOutput.length();
		}
	}

	/**
	 * Handles parsing of command-line arguments.
	 *
	 * @author Martin Weber
	 */
	private class ParserHandler implements IParserHandler {

		/**
		 * @param responseFileArglet
		 * @param args               the command line arguments to process
		 */
		@SuppressWarnings("nls")
		private void parseArguments(IResponseFileArglet responseFileArglet, String args) {
			// eat buildOutput string argument by argument..
			while (!(args = StringUtil.trimLeadingWS(args)).isEmpty()) {
				boolean argParsed = false;
				int consumed;
				// parse with first parser that can handle the first argument on the
				// command-line
				if (DEBUG)
					System.out.printf(">> PARSING next argument in '%s ...'%n",
							args.substring(0, Math.min(50, args.length())));
				for (IArglet tap : argumentParsers) {
					if (DEBUG)
						System.out.printf("   Trying parser %s%n", tap.getClass().getSimpleName());
					consumed = tap.processArgument(result, cwd, args);
					if (consumed > 0) {
						if (DEBUG)
							System.out.printf("<< PARSED argument '%s'%n", args.substring(0, consumed));
						args = args.substring(consumed);
						argParsed = true;
						break;
					}
				}

				// try response file
				if (!argParsed && responseFileArglet != null) {
					if (DEBUG)
						System.out.printf("   Trying parser %s%n", responseFileArglet.getClass().getSimpleName());
					consumed = responseFileArglet.process(this, args);
					if (consumed > 0) {
						if (DEBUG)
							System.out.printf("<< PARSED ARGUMENT '%s'%n", args.substring(0, consumed));
						args = args.substring(consumed);
						argParsed = true;
					}
				}
				if (!argParsed && !args.isEmpty()) {
					// tried all parsers, argument is still not parsed,
					// skip argument
					if (DEBUG)
						System.out.printf("<< IGNORING next argument in '%s ...' (no matching parser found)%n",
								args.substring(0, Math.min(50, args.length())));
					consumed = skipArgument(args);
					if (consumed > 0) {
						args = args.substring(consumed);
					}
				}
			}
		}

		/**
		 * Parses the given String with the first parser that can handle the first
		 * argument on the command-line.
		 *
		 * @param args the command line arguments to process
		 */
		@Override
		public void parseArguments(String args) {
			parseArguments(null, args);
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.eclipse.cdt.cmake.is.IParserHandler# getCompilerWorkingDirectory()
		 */
		@Override
		public IPath getCompilerWorkingDirectory() {
			return cwd;
		}

	} // ParserHandler
} // ToolOutputParser