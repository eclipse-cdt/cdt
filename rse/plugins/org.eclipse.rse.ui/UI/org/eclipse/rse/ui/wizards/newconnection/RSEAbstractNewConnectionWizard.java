/********************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * Uwe Stieber (Wind River) - Reworked new connection wizard extension point.
 ********************************************************************************/

package org.eclipse.rse.ui.wizards.newconnection;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Abstract base implementation of the RSE new connection wizard.
 */
public abstract class RSEAbstractNewConnectionWizard extends Wizard implements ISelectionChangedListener {
	private IRSESystemType systemType;
	private boolean isBusy;
	
	/**
	 * Constructor.
	 */
	public RSEAbstractNewConnectionWizard() {
		systemType = null;
		isBusy = false;
		
		setNeedsProgressMonitor(true);
		setForcePreviousAndNextButtons(true);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#dispose()
	 */
	public void dispose() {
		super.dispose();
		systemType = null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.wizards.IRSENewConnectionWizardDelegate#getSystemType()
	 */
	public IRSESystemType getSystemType() {
		return systemType;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	public void selectionChanged(SelectionChangedEvent event) {
		if (event != null && event.getSelection() instanceof IStructuredSelection) {
			IStructuredSelection selection = (IStructuredSelection)event.getSelection();
			Object candidate = selection.getFirstElement();
			if (candidate instanceof IRSESystemType) systemType = (IRSESystemType)candidate;
		}
	}

	/**
	 * Sets the system cursor to a wait cursor. This method can be called in
	 * a display thread only!
	 * 
	 * @param busy <code>True</code> if to show the wait cursor, <code>false</code> otherwise.
	 */
	protected void setBusyCursor(boolean busy) {
		assert Display.findDisplay(Thread.currentThread()) != null;
		
		Shell shell = getShell();
		if (isBusy != busy) {
			if (shell != null) {
				shell.setCursor(busy ? shell.getDisplay().getSystemCursor(SWT.CURSOR_WAIT) : null);
			}
			isBusy = busy;
		}
	}
}