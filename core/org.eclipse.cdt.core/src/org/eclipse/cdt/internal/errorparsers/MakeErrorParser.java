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

import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.core.runtime.Path;

public class MakeErrorParser extends AbstractErrorParser {

	private static final ErrorPattern[] patterns = {
		new ErrorPattern("make\\[(.*)\\]: Entering directory `(.*)'", 0, 0) { //$NON-NLS-1
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
		new ErrorPattern("make\\[.*\\]: Leaving directory", 0, 0) { //$NON-NLS-1
			protected boolean recordError(Matcher matcher, ErrorParserManager eoParser) {
				eoParser.popDirectory();
				return true;
			}
		},
		new ErrorPattern("(make: \\*\\*\\* \\[.*\\] Error .*)", 1, IMarkerGenerator.SEVERITY_ERROR_RESOURCE) { //$NON-NLS-1
			protected boolean recordError(Matcher matcher, ErrorParserManager eoParser) {
				if (!eoParser.hasErrors())
					super.recordError(matcher, eoParser);
				return true;
			}
		}
	};
	
	public MakeErrorParser() {
		super(patterns);
	}
	
}
