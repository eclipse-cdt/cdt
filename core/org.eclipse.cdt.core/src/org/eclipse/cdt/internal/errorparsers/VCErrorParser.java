/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.errorparsers;

import java.util.regex.Matcher;

import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.core.errorparsers.AbstractErrorParser;
import org.eclipse.cdt.core.errorparsers.ErrorPattern;

public class VCErrorParser extends AbstractErrorParser {

	private static final ErrorPattern[] patterns = {
			new ErrorPattern("(.+?)(\\(([0-9]+)\\))? : (fatal error|error|warning) (.*)", 1, 3, 5, 0, 0) { //$NON-NLS-1$
				@Override
				public int getSeverity(Matcher matcher) {
					return "warning".equals(matcher.group(4)) //$NON-NLS-1$
							? IMarkerGenerator.SEVERITY_WARNING
							: IMarkerGenerator.SEVERITY_ERROR_RESOURCE;
				}
			} };

	public VCErrorParser() {
		super(patterns);
	}

}
