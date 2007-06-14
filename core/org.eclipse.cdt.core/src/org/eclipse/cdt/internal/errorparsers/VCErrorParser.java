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

public class VCErrorParser extends AbstractErrorParser {
	
	private static final ErrorPattern[] patterns = {
		new ErrorPattern("(.+?)(\\(([0-9]+)\\))? : (fatal error|error|warning) (.*)", 1, 3, 5, 0, 0) {
			public int getSeverity(Matcher matcher) {
				return "warning".equals(matcher.group(4))
					? IMarkerGenerator.SEVERITY_WARNING
					: IMarkerGenerator.SEVERITY_ERROR_RESOURCE;
			}
		}
	};
	
	public VCErrorParser() {
		super(patterns);
	}
	
}
