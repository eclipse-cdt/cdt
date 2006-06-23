/*******************************************************************************
 * Copyright (c) 2002, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.wizards;

import org.eclipse.cdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.cdt.internal.ui.dialogs.StatusUtil;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.wizard.WizardPage;

/**
 * Base class for wizard page responsible to create C elements. The class
 * provides API to update the wizard's status line and OK button according to
 * the value of a <code>IStatus</code> object.
 */
public abstract class NewElementWizardPage extends WizardPage {

	private IStatus fCurrStatus;
	
	private boolean fPageVisible;

	/**
	 * Creates a <code>NewElementWizardPage</code>.
	 * 
	 * @param name the wizard page's name
	 */	
	public NewElementWizardPage(String name) {
		super(name);
		fPageVisible = false;
		fCurrStatus =  new StatusInfo();
	}
		
	// ---- WizardPage ----------------
	
	/*
	 * @see WizardPage#becomesVisible
	 */
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		fPageVisible = visible;
		// policy: wizards are not allowed to come up with an error message
		if (visible && fCurrStatus.matches(IStatus.ERROR)) {
			StatusInfo status = new StatusInfo();
			status.setError("");  //$NON-NLS-1$
			fCurrStatus = status;
		} 
		updateStatus(fCurrStatus);
	}	

	/**
	 * Updates the status line and the ok button according to the given status
	 * 
	 * @param status status to apply
	 */
	protected void updateStatus(IStatus status) {
		fCurrStatus = status;
		setPageComplete(!status.matches(IStatus.ERROR));
		if (fPageVisible) {
			StatusUtil.applyToStatusLine(this, status);
		}
	}
	
	/**
	 * Updates the status line and the ok button according to the status evaluate from
	 * an array of status. The most severe error is taken.  In case that two status with 
	 * the same severity exists, the status with lower index is taken.
	 * 
	 * @param status the array of status
	 */
	protected void updateStatus(IStatus[] status) {
		updateStatus(StatusUtil.getMostSevere(status));
	}	
			
}
