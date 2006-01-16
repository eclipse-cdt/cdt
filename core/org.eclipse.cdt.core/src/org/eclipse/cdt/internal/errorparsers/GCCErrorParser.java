/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.errorparsers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.IMarkerGenerator;

public class GCCErrorParser extends AbstractErrorParser {
	
	private static final Pattern[] varPatterns = {
		Pattern.compile("'(.*)' undeclared"),
		Pattern.compile("'(.*)' defined but not used"),
		Pattern.compile("conflicting types for '(.*)'"),
		Pattern.compile("parse error before '(.*)'")
	};
	
	private static final ErrorPattern[] patterns = {
		// The following are skipped
		new ErrorPattern("\\(Each undeclared identifier is reported only once"),
		new ErrorPattern("for each function it appears in.\\)"),
		new ErrorPattern(": note:"),
		// The following are not...
		new ErrorPattern("((.:)?[^:]*):([0-9]*):([0-9]*:)? ((warning: )?.*)", 1, 3, 5, 0, 0) {
			public String getVarName(Matcher matcher) {
				String desc = getDesc(matcher);
				Matcher varMatcher = null;
				for (int i = 0; i < varPatterns.length; ++i) {
					varMatcher = varPatterns[i].matcher(desc);
					if (varMatcher.find())
						break;
					else
						varMatcher = null;
				}

				return varMatcher != null ? varMatcher.group(1) : null;
			}
			public int getSeverity(Matcher matcher) {
				String warningGroup = matcher.group(6);
				if (warningGroup != null)
					return IMarkerGenerator.SEVERITY_WARNING;
				else
					return IMarkerGenerator.SEVERITY_ERROR_RESOURCE;
			}
		}
	};

	public GCCErrorParser() {
		super(patterns);
	}
	
}
