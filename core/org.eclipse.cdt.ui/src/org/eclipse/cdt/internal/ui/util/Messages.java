/*******************************************************************************
 * Copyright (c) 2006, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *    Sergey Prigogin (Google)
 *******************************************************************************/ 
package org.eclipse.cdt.internal.ui.util;

import com.ibm.icu.text.MessageFormat;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME= Messages.class.getName();

	public static String EditorUtility_calculatingChangedRegions_message;
	public static String EditorUtility_error_calculatingChangedRegions;

	public static String OpenExternalProblemAction_CannotReadExternalLocation;

	public static String OpenExternalProblemAction_ErrorOpeningFile;

	public static String format(String pattern, Object... arguments) {
        return MessageFormat.format(pattern, arguments);
    }
    
	private Messages() {
		// Do not instantiate
	}

	static {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
}
