/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
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

import org.eclipse.cdt.core.IMarkerGenerator;

public class GLDErrorParser extends AbstractErrorParser {

	private static final ErrorPattern[] patterns = {
		new ErrorPattern(Messages.GLDErrorParser_error_text, 1, 0, 2, 0, IMarkerGenerator.SEVERITY_ERROR_RESOURCE), //$NON-NLS-1
		new ErrorPattern(Messages.GLDErrorParser_warning_general, 2, IMarkerGenerator.SEVERITY_WARNING), //$NON-NLS-1
		new ErrorPattern(Messages.GLDErrorParser_error_general, 2, IMarkerGenerator.SEVERITY_ERROR_RESOURCE) //$NON-NLS-1
	};
	
	public GLDErrorParser() {
		super(patterns);
	}
	
}
