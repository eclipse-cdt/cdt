/*******************************************************************************
 *  Copyright (c) 2000, 2008 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 * 	   Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.correction;

import org.eclipse.osgi.util.NLS;

/**
 * Helper class to get NLSed messages.
 */
public final class CorrectionMessages extends NLS {
	static {
		NLS.initializeMessages(CorrectionMessages.class.getName(), CorrectionMessages.class);
	}

	private CorrectionMessages() {
		// Do not instantiate
	}

	public static String CCorrectionProcessor_error_quickassist_message;
	public static String CCorrectionProcessor_error_quickfix_message;
	public static String CCorrectionProcessor_error_status;
	public static String MarkerResolutionProposal_additionaldesc;
	public static String NoCorrectionProposal_description;
	
	public static String ChangeCorrectionProposal_error_title;
	public static String ChangeCorrectionProposal_error_message;
	public static String ChangeCorrectionProposal_name_with_shortcut;
	public static String TUCorrectionProposal_error_title;
	public static String TUCorrectionProposal_error_message;
	public static String LinkedNamesAssistProposal_proposalinfo;
	public static String LinkedNamesAssistProposal_description;
	public static String RenameRefactoringProposal_additionalInfo;
	public static String RenameRefactoringProposal_name;
}
