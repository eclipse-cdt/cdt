/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
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

import org.eclipse.cdt.core.IMarkerGenerator;

public class GLDErrorParser extends AbstractErrorParser {

	private static final ErrorPattern[] patterns = {
		new ErrorPattern("(.*)\\(\\.text\\+.*\\): (.*)", 1, 0, 2, 0, IMarkerGenerator.SEVERITY_ERROR_RESOURCE), //$NON-NLS-1
		new ErrorPattern("ld(\\.exe)?: ([Ww]arning .*)", 2, IMarkerGenerator.SEVERITY_WARNING), //$NON-NLS-1
		new ErrorPattern("ld(\\.exe)?: (.*)", 0, IMarkerGenerator.SEVERITY_ERROR_RESOURCE) { //$NON-NLS-1
			public String getDesc(Matcher matcher) {
				// add in the name of the link command to give it some context
				StringBuffer buff = new StringBuffer();
				buff.append("ld: "); //$NON-NLS-1$
				buff.append(matcher.group(2));
				return buff.toString();
			}
		}
	};
	
	public GLDErrorParser() {
		super(patterns);
	}
	
}
