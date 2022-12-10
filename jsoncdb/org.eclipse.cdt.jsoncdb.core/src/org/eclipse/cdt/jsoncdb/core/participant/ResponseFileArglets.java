/*******************************************************************************
 * Copyright (c) 2017-2019 Martin Weber.
 *
 * Content is provided to you under the terms and conditions of the Eclipse Public License Version 2.0 "EPL".
 * A copy of the EPL is available at http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.jsoncdb.core.participant;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;

import org.eclipse.cdt.jsoncdb.core.participant.Arglets.NameOptionMatcher;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * Various response-file argument parser implementations.
 *
 * @author Martin Weber
 */
public class ResponseFileArglets {
	/**
	 * matches a response file name with quoted file name. Name in matcher group 2
	 */
	@SuppressWarnings("nls")
	private static final String REGEX_QUOTED_FILE = "\\s*([\"'])(.+?)\\1";
	/**
	 * matches a response file name with unquoted file name. Name in matcher group 1
	 */
	@SuppressWarnings("nls")
	private static final String REGEX_UNQUOTED_FILE = "\\s*([^\\s]+)";

	/**
	 * Handles a response file argument that starts with the '@' character.
	 */
	public static class At implements IResponseFileArglet {
		@SuppressWarnings("nls")
		private static final NameOptionMatcher[] optionMatchers = {
				/* unquoted directory */
				new NameOptionMatcher("@" + REGEX_UNQUOTED_FILE, 1),
				/* quoted directory */
				new NameOptionMatcher("@" + REGEX_QUOTED_FILE, 2) };

		/*
		 * (non-Javadoc)
		 *
		 * @see org.eclipse.cdt.jsoncdb.IResponseFileArglet
		 * #process(org.eclipse.cdt.jsoncdb.IParserHandler, java.lang.String)
		 */
		@Override
		public int process(IParserHandler parserHandler, String argsLine) {
			for (NameOptionMatcher oMatcher : optionMatchers) {
				final Matcher matcher = oMatcher.pattern.matcher(argsLine);

				if (matcher.lookingAt()) {
					String fname = matcher.group(oMatcher.nameGroup);
					final int consumed = matcher.end();
					if ("<<".equals(fname)) { //$NON-NLS-1$
						// see https://github.com/15knots/cmake4eclipse/issues/94
						// Handle '@<< compiler-args <<' syntax: The file '<<' does not exist, arguments
						// come from argsline.
						// so we just do not open the non-existing file
						return consumed;
					}

					IPath path = Path.fromOSString(fname);
					if (!path.isAbsolute()) {
						// relative path, prepend CWD
						fname = parserHandler.getCompilerWorkingDirectory().append(path).toString();
					}

					// parse file
					java.nio.file.Path fpath = Paths.get(fname);
					try {
						String args2 = new String(Files.readAllBytes(fpath));
						parserHandler.parseArguments(args2);
					} catch (IOException e) {
						// swallow exception for now
						e.printStackTrace();
					}
					return consumed;
				}
			}
			return 0;// no input consumed
		}

	}

}
