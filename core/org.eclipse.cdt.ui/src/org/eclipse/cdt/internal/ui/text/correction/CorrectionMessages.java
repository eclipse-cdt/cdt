/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * 	   Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.correction;

import org.eclipse.osgi.util.NLS;

/**
 * Helper class to get NLSed messages.
 */
public final class CorrectionMessages extends NLS {
	private static final String BUNDLE_NAME= CorrectionMessages.class.getName();

	static {
		NLS.initializeMessages(BUNDLE_NAME, CorrectionMessages.class);
	}

	private CorrectionMessages() {
		// Do not instantiate
	}

	public static String CCorrectionProcessor_error_quickassist_message;
	public static String CCorrectionProcessor_error_quickfix_message;
	public static String CCorrectionProcessor_error_status;
	public static String MarkerResolutionProposal_additionaldesc;
	public static String NoCorrectionProposal_description;
}
