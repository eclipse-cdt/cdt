/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Norbert Ploett (Siemens AG) - externalized strings
 *******************************************************************************/

package org.eclipse.cdt.internal.errorparsers;

import java.util.regex.Matcher;

import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.core.errorparsers.AbstractErrorParser;
import org.eclipse.cdt.core.errorparsers.ErrorPattern;
import org.eclipse.core.runtime.Path;

/**
 * @deprecated replaced with {@link CWDLocator} and {@code GmakeErrorParser}
 *
 */
@Deprecated
public class MakeErrorParser extends AbstractErrorParser {
	private static final ErrorPattern[] patterns = {
		new ErrorPattern("make\\[(.*)\\]: Entering directory `(.*)'", 0, 0) {  //$NON-NLS-1$
			@Override
			protected boolean recordError(Matcher matcher, ErrorParserManager eoParser) {
				int level;
				try {
					level = Integer.valueOf(matcher.group(1)).intValue();
				} catch (NumberFormatException e) {
					level = 0;
				}
				String dir = matcher.group(2);
	    		/* Sometimes make screws up the output, so
	    		 * "leave" events can't be seen.  Double-check level
	    		 * here.
	    		 */
	    		int parseLevel = eoParser.getDirectoryLevel();
    			for (; level < parseLevel; level++) {
    				eoParser.popDirectory();
    			}
	    		eoParser.pushDirectory(new Path(dir));
	    		return true;
			}
		},
		new ErrorPattern("make\\[.*\\]: Leaving directory", 0, 0) { //$NON-NLS-1$
			@Override
			protected boolean recordError(Matcher matcher, ErrorParserManager eoParser) {
				eoParser.popDirectory();
				return true;
			}
		},
		new ErrorPattern("(make: \\*\\*\\* \\[.*\\] Error .*)", 1, IMarkerGenerator.SEVERITY_ERROR_RESOURCE) { //$NON-NLS-1$
			@Override
			protected boolean recordError(Matcher matcher, ErrorParserManager eoParser) {				
				super.recordError(matcher, eoParser);
				return true;
			}
		},
		//make [foo] Error NN
		new ErrorPattern("(make.*\\[.*\\] Error [-]{0,1}\\d*.*)", 1, IMarkerGenerator.SEVERITY_ERROR_RESOURCE) { //$NON-NLS-1$
			@Override
			protected boolean recordError(Matcher matcher, ErrorParserManager eoParser) {				
				super.recordError(matcher, eoParser);
				return true;
			}
		},
		
		//[foo]  signal description
		// Turning off for now, bug 203269
		// This is reporting an error on the line 'make -j8 ...'
//		new ErrorPattern("(make.*\\d+\\s+\\w+.*)", 1, IMarkerGenerator.SEVERITY_ERROR_RESOURCE) { //$NON-NLS-1$
//			protected boolean recordError(Matcher matcher, ErrorParserManager eoParser) {				
//				super.recordError(matcher, eoParser);
//				return true;
//			}
//		},
		//missing separator. Stop.
		new ErrorPattern("(make.*missing separator.\\s*Stop.)", 1, IMarkerGenerator.SEVERITY_ERROR_RESOURCE) { //$NON-NLS-1$
			@Override
			protected boolean recordError(Matcher matcher, ErrorParserManager eoParser) {				
				super.recordError(matcher, eoParser);
				return true;
			}
		},
		//missing separator (did you mean TAB instead of 8 spaces?\\). Stop.
		new ErrorPattern("(make.*missing separator \\(did you mean TAB instead of 8 spaces?\\).\\s*Stop.)", 1, IMarkerGenerator.SEVERITY_ERROR_RESOURCE) { //$NON-NLS-1$
			@Override
			protected boolean recordError(Matcher matcher, ErrorParserManager eoParser) {				
				super.recordError(matcher, eoParser);
				return true;
			}
		},
		//commands commence before first target. Stop.
		new ErrorPattern("(make.*commands commence before first target.\\s*Stop.)", 1, IMarkerGenerator.SEVERITY_ERROR_RESOURCE) { //$NON-NLS-1$
			@Override
			protected boolean recordError(Matcher matcher, ErrorParserManager eoParser) {				
				super.recordError(matcher, eoParser);
				return true;
			}
		},
		//commands commence before first target. Stop.
		new ErrorPattern("(make.*commands commence before first target.\\s*Stop.)", 1, IMarkerGenerator.SEVERITY_ERROR_RESOURCE) { //$NON-NLS-1$
			@Override
			protected boolean recordError(Matcher matcher, ErrorParserManager eoParser) {
				super.recordError(matcher, eoParser);
				return true;
			}
		},
		//missing rule before commands. Stop.
		new ErrorPattern("(make.*missing rule before commands.\\s*Stop.)", 1, IMarkerGenerator.SEVERITY_ERROR_RESOURCE) { //$NON-NLS-1$
			@Override
			protected boolean recordError(Matcher matcher, ErrorParserManager eoParser) {
				super.recordError(matcher, eoParser);
				return true;
			}
		},
		//missing rule before commands. Stop.
		new ErrorPattern("(make.*missing rule before commands.\\s*Stop.)", 1, IMarkerGenerator.SEVERITY_ERROR_RESOURCE) { //$NON-NLS-1$
			@Override
			protected boolean recordError(Matcher matcher, ErrorParserManager eoParser) {
				super.recordError(matcher, eoParser);
				return true;
			}
		},
		//No rule to make target `xxx'.
		new ErrorPattern("(make.*No rule to make target `.*'.)", 1, IMarkerGenerator.SEVERITY_ERROR_RESOURCE) { //$NON-NLS-1$
			@Override
			protected boolean recordError(Matcher matcher, ErrorParserManager eoParser) {
				super.recordError(matcher, eoParser);
				return true;
			}
		},
		//No rule to make target `xxx', needed by `yyy'.
		new ErrorPattern("(make.*No rule to make target `.*', needed by `.*'.)", 1, IMarkerGenerator.SEVERITY_ERROR_RESOURCE) { //$NON-NLS-1$
			@Override
			protected boolean recordError(Matcher matcher, ErrorParserManager eoParser) {
				super.recordError(matcher, eoParser);
				return true;
			}
		},
		//No targets specified and no makefile found. Stop.
		new ErrorPattern("(make.*No targets specified and no makefile found.\\s*Stop.)", 1, IMarkerGenerator.SEVERITY_ERROR_RESOURCE) { //$NON-NLS-1$
			@Override
			protected boolean recordError(Matcher matcher, ErrorParserManager eoParser) {
				super.recordError(matcher, eoParser);
				return true;
			}
		},
		//No targets. Stop.
		new ErrorPattern("(make.*No targets.\\s*Stop.)", 1, IMarkerGenerator.SEVERITY_ERROR_RESOURCE) { //$NON-NLS-1$
			@Override
			protected boolean recordError(Matcher matcher, ErrorParserManager eoParser) {
				super.recordError(matcher, eoParser);
				return true;
			}
		},
		//Makefile `xxx' was not found.
		new ErrorPattern("(make.*Makefile `.*' was not found.)", 1, IMarkerGenerator.SEVERITY_ERROR_RESOURCE) { //$NON-NLS-1$
			@Override
			protected boolean recordError(Matcher matcher, ErrorParserManager eoParser) {
				super.recordError(matcher, eoParser);
				return true;
			}
		},
		//Included makefile `xxx' was not found.
		new ErrorPattern("(make.*Included makefile `.*' was not found.)", 1, IMarkerGenerator.SEVERITY_ERROR_RESOURCE) { //$NON-NLS-1$
			@Override
			protected boolean recordError(Matcher matcher, ErrorParserManager eoParser) {
				super.recordError(matcher, eoParser);
				return true;
			}
		},
		//warning: overriding commands for target `xxx'
		new ErrorPattern("(make.*warning: overriding commands for target `.*')", 1, IMarkerGenerator.SEVERITY_WARNING) { //$NON-NLS-1$
			@Override
			protected boolean recordError(Matcher matcher, ErrorParserManager eoParser) {
				super.recordError(matcher, eoParser);
				return false;
			}
		},
		//warning: ignoring old commands for target `xxx'
		new ErrorPattern("(make.*warning: ignoring old commands for target `.*')", 1, IMarkerGenerator.SEVERITY_WARNING) { //$NON-NLS-1$
			@Override
			protected boolean recordError(Matcher matcher, ErrorParserManager eoParser) {
				super.recordError(matcher, eoParser);
				return false;
			}
		},
		//Circular .+ <- .+ dependency dropped.
		new ErrorPattern("(make.*Circular .+ <- .+ dependency dropped.)", 1, IMarkerGenerator.SEVERITY_ERROR_RESOURCE) { //$NON-NLS-1$
			@Override
			protected boolean recordError(Matcher matcher, ErrorParserManager eoParser) {
				super.recordError(matcher, eoParser);
				return true;
			}
		},
		//Recursive variable `xxx' references itself (eventually). Stop.		
		new ErrorPattern("(make.*Recursive variable `.*' references itself \\(eventually\\).\\s*Stop.)", 1, IMarkerGenerator.SEVERITY_ERROR_RESOURCE) { //$NON-NLS-1$
			@Override
			protected boolean recordError(Matcher matcher, ErrorParserManager eoParser) {
				super.recordError(matcher, eoParser);
				return true;
			}
		},
		//Unterminated variable reference. Stop.		
		new ErrorPattern("(make.*[uU]nterminated variable reference.\\s*Stop.)", 1, IMarkerGenerator.SEVERITY_ERROR_RESOURCE) { //$NON-NLS-1$
			@Override
			protected boolean recordError(Matcher matcher, ErrorParserManager eoParser) {
				super.recordError(matcher, eoParser);
				return true;
			}
		},
		//insufficient arguments to function `.*'. Stop.
		new ErrorPattern("(make.*insufficient arguments to function `.*'.\\s*Stop.)", 1, IMarkerGenerator.SEVERITY_ERROR_RESOURCE) { //$NON-NLS-1$
			@Override
			protected boolean recordError(Matcher matcher, ErrorParserManager eoParser) {
				super.recordError(matcher, eoParser);
				return true;
			}
		},
		//missing target pattern. Stop.
		new ErrorPattern("(make.*missing target pattern.\\s*Stop.)", 1, IMarkerGenerator.SEVERITY_ERROR_RESOURCE) { //$NON-NLS-1$
			@Override
			protected boolean recordError(Matcher matcher, ErrorParserManager eoParser) {
				super.recordError(matcher, eoParser);
				return true;
			}
		},
		//multiple target patterns. Stop.
		new ErrorPattern("(make.*multiple target patterns.\\s*Stop.)", 1, IMarkerGenerator.SEVERITY_ERROR_RESOURCE) { //$NON-NLS-1$
			@Override
			protected boolean recordError(Matcher matcher, ErrorParserManager eoParser) {
				super.recordError(matcher, eoParser);
				return true;
			}
		},
		//target pattern contains no `%'. Stop.
		new ErrorPattern("(make.*target pattern contains no `%'.\\s*Stop.)", 1, IMarkerGenerator.SEVERITY_ERROR_RESOURCE) { //$NON-NLS-1$
			@Override
			protected boolean recordError(Matcher matcher, ErrorParserManager eoParser) {
				super.recordError(matcher, eoParser);
				return true;
			}
		},
		//mixed implicit and static pattern rules. Stop.
		new ErrorPattern("(make.*mixed implicit and static pattern rules.\\s*Stop.)", 1, IMarkerGenerator.SEVERITY_ERROR_RESOURCE) { //$NON-NLS-1$
			@Override
			protected boolean recordError(Matcher matcher, ErrorParserManager eoParser) {
				super.recordError(matcher, eoParser);
				return true;
			}
		},
		//mixed implicit and static pattern rules. Stop.
		new ErrorPattern("(make.*mixed implicit and static pattern rules.\\s*Stop.)", 1, IMarkerGenerator.SEVERITY_ERROR_RESOURCE) { //$NON-NLS-1$
			@Override
			protected boolean recordError(Matcher matcher, ErrorParserManager eoParser) {
				super.recordError(matcher, eoParser);
				return true;
			}
		},
		//warning: -jN forced in submake: disabling jobserver mode.
		new ErrorPattern("(make.*warning: -jN forced in submake: disabling jobserver mode.)", 1, IMarkerGenerator.SEVERITY_WARNING) { //$NON-NLS-1$
			@Override
			protected boolean recordError(Matcher matcher, ErrorParserManager eoParser) {
				super.recordError(matcher, eoParser);
				return false;
			}
		},
		//warning: jobserver unavailable: using -j1. Add `+' to parent make rule.
		new ErrorPattern("(make.*warning: jobserver unavailable: using -j1. Add `+' to parent make rule.)", 1, IMarkerGenerator.SEVERITY_WARNING) { //$NON-NLS-1$
			@Override
			protected boolean recordError(Matcher matcher, ErrorParserManager eoParser) {
				super.recordError(matcher, eoParser);
				return false;
			}
		},
		//target `abc' doesn't match the target pattern
		new ErrorPattern("(make.*target `.*' doesn't match the target pattern)", 1, IMarkerGenerator.SEVERITY_ERROR_RESOURCE) { //$NON-NLS-1$
			@Override
			protected boolean recordError(Matcher matcher, ErrorParserManager eoParser) {
				super.recordError(matcher, eoParser);
				return true;
			}
		}
		
	};	
	public MakeErrorParser() {
		super(patterns);
	}	
}
