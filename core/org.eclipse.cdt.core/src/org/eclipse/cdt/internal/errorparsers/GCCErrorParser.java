/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     James Blackburn - Patch for PR 85264
 *     Norbert Ploett (Siemens AG) - externalized strings
 *******************************************************************************/

package org.eclipse.cdt.internal.errorparsers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.core.errorparsers.AbstractErrorParser;
import org.eclipse.cdt.core.errorparsers.ErrorPattern;

public class GCCErrorParser extends AbstractErrorParser {
	
	private static final Pattern[] varPatterns = {
		Pattern.compile(Messages.GCCErrorParser_varPattern_undeclared),
		Pattern.compile(Messages.GCCErrorParser_varPattern_defdNotUsed),
		Pattern.compile(Messages.GCCErrorParser_varPattern_conflictTypes),
		Pattern.compile(Messages.GCCErrorParser_varPattern_parseError)
	};
	
	private static final ErrorPattern[] patterns = {
		// The following are skipped
		new ErrorPattern(Messages.GCCErrorParser_skip_UndeclaredOnlyOnce),
		new ErrorPattern(Messages.GCCErrorParser_skip_forEachFunction),
		new ErrorPattern(Messages.GCCErrorParser_skip_note),
		new ErrorPattern(Messages.GCCErrorParser_sikp_instantiatedFromHere),
		// The following are not...
		new ErrorPattern(Messages.GCCErrorParser_Warnings, 1, 2, 5, 0, 0) {
			@Override
			public String getVarName(Matcher matcher) {
				String desc = getDesc(matcher);
				if (desc == null)
					return null;
				
				for (int i = 0; i < varPatterns.length; ++i) {
					Matcher varMatcher = varPatterns[i].matcher(desc);
					if (varMatcher.find())
						return varMatcher.group(1);
				}
				return null;
			}
			@Override
			public int getSeverity(Matcher matcher) {
				String warningGroup = matcher.group(4);
				if (warningGroup != null && warningGroup.toLowerCase().endsWith("warning:")) //$NON-NLS-1$
					return IMarkerGenerator.SEVERITY_WARNING;
				
				return IMarkerGenerator.SEVERITY_ERROR_RESOURCE;
			}
		}
	};

	public GCCErrorParser() {
		super(patterns);
	}
	
}
