/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.internal.ui.wizards;

import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.wizard.WizardPage;

public abstract class StatusWizardPage extends WizardPage {

	private IStatus fCurrStatus;
	
	private boolean fPageVisible;
	private boolean fNoErrorOnStartup;
	
	public StatusWizardPage(String name, boolean noErrorOnStartup) {
		super(name);
		fPageVisible= false;
		fCurrStatus= createStatus(IStatus.OK, ""); //$NON-NLS-1$
		fNoErrorOnStartup= noErrorOnStartup;
	}
		
	// ---- WizardPage ----------------
	
	/*
	 * @see WizardPage#becomesVisible
	 */
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		fPageVisible= visible;
		// policy: wizards are not allowed to come up with an error message
		if (visible && fNoErrorOnStartup && fCurrStatus.matches(IStatus.ERROR)) {
			// keep the error state, but remove the message
			fCurrStatus= createStatus(IStatus.ERROR, ""); //$NON-NLS-1$
		} 
		updateStatus(fCurrStatus);
	}	

	/**
	 * Updates the status line and the ok button depending on the status
	 */
	protected void updateStatus(IStatus status) {
		fCurrStatus= status;
		setPageComplete(!status.matches(IStatus.ERROR));
		if (fPageVisible) {
			applyToStatusLine(this, status);
		}
	}

	/**
	 * Applies the status to a dialog page
	 */
	public static void applyToStatusLine(DialogPage page, IStatus status) {
		String errorMessage= null;
		String warningMessage= null;
		String statusMessage= status.getMessage();
		if (statusMessage.length() > 0) {
			if (status.matches(IStatus.ERROR)) {
				errorMessage= statusMessage;
			} else if (!status.isOK()) {
				warningMessage= statusMessage;
			}
		}
		page.setErrorMessage(errorMessage);
		page.setMessage(warningMessage,status.getSeverity());
	}
	
	public static IStatus getMoreSevere(IStatus s1, IStatus s2) {
		if (s1.getSeverity() >= s2.getSeverity()) {
			return s1;
		} 
		return s2;
	}	
	
	
	public static IStatus createStatus(int severity, String message) {
		return new Status(severity, MakeUIPlugin.getUniqueIdentifier(), severity, message, null);
	}	
			
}
