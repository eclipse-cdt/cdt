/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Gvozdev (Quoin Inc.) - initial API and implementation adapted from MakeErrorParser
 *     Marc-Andre Laperle (Ericsson) - Bug 462036
 *******************************************************************************/

package org.eclipse.cdt.internal.errorparsers;

import java.util.regex.Matcher;

import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.errorparsers.AbstractErrorParser;
import org.eclipse.cdt.core.errorparsers.ErrorPattern;
import org.eclipse.core.runtime.Path;

/**
 * Class {@code CWDLocator} is used to change working directory from where file name is searched by
 * {@link ErrorParserManager}. The intention is to handle make output of commands "pushd" and "popd".
 */
public class CWDLocator extends AbstractErrorParser {
	private static boolean enabled = true;

	@Override
	public boolean processLine(String line, ErrorParserManager manager) {
		int lineNumber = manager.getLineCounter();
		// enable on first line (can be previously disabled if processed parallel build)
		if (lineNumber == 1)
			enabled = true;

		if (enabled)
			return super.processLine(line, manager);
		return false;
	}

	private static final ErrorPattern[] patterns = {
			// parallel build makes interleaved output and so this parser useless
			// turn it off in that case
			new ErrorPattern("^\\w*make.*\\s((-j)|(--jobs=))(\\s*\\d*)", 0, 0) { //$NON-NLS-1$
				@Override
				protected boolean recordError(Matcher matcher, ErrorParserManager eoParser) {
					String jobs = matcher.group(4).trim();
					if (!jobs.equals("1")) { //$NON-NLS-1$
						enabled = false;
						int parseLevel = eoParser.getDirectoryLevel();
						for (int level = 0; level < parseLevel; level++) {
							eoParser.popDirectoryURI();
						}
					}
					return false;
				}
			}, new ErrorPattern("make\\[(.*)\\]: Entering directory [`'](.*)'", 0, 0) { //$NON-NLS-1$
				@Override
				protected boolean recordError(Matcher matcher, ErrorParserManager eoParser) {
					int level;
					try {
						level = Integer.valueOf(matcher.group(1)).intValue();
					} catch (NumberFormatException e) {
						level = 0;
					}
					String dir = matcher.group(2);
					/*
					 * Sometimes make screws up the output, so "leave" events can't be seen. Double-check
					 * level here.
					 */
					int parseLevel = eoParser.getDirectoryLevel();
					for (; level < parseLevel; level++) {
						eoParser.popDirectoryURI();
					}
					eoParser.pushDirectory(new Path(dir));
					return true;
				}
			},
			// This is emitted by GNU make using options -w or --print-directory.
			new ErrorPattern("make: Entering directory [`'](.*)'", 0, 0) { //$NON-NLS-1$
				@Override
				protected boolean recordError(Matcher matcher, ErrorParserManager eoParser) {
					String dir = matcher.group(1);
					eoParser.pushDirectory(new Path(dir));
					return true;
				}
			}, new ErrorPattern("make(\\[.*\\])?: Leaving directory", 0, 0) { //$NON-NLS-1$
				@Override
				protected boolean recordError(Matcher matcher, ErrorParserManager eoParser) {
					eoParser.popDirectoryURI();
					return true;
				}
			},

	};

	public CWDLocator() {
		super(patterns);
		enabled = true;
	}
}
