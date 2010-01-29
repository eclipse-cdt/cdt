/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev (Quoin Inc.) - initial API and implementation adapted from MakeErrorParser
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
	private static final ErrorPattern[] patterns = {
			new ErrorPattern("make\\[(.*)\\]: Entering directory `(.*)'", 0, 0) {
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
			}, new ErrorPattern("make\\[.*\\]: Leaving directory", 0, 0) {
				@Override
				protected boolean recordError(Matcher matcher, ErrorParserManager eoParser) {
					eoParser.popDirectoryURI();
					return true;
				}
			},

	};

	public CWDLocator() {
		super(patterns);
	}
}
