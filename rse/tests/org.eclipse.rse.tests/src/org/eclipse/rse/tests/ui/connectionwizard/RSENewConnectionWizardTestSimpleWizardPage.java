/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Uwe Stieber (Wind River) - initial API and implementation.
 *******************************************************************************/
package org.eclipse.rse.tests.ui.connectionwizard;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.rse.core.IRSECoreRegistry;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.ui.wizards.newconnection.RSEMainNewConnectionWizard;
import org.eclipse.rse.ui.wizards.newconnection.RSENewConnectionWizardRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;

/**
 * Simple wizard page listing the available system types in a simple text list.
 * 
 * @author uwe.stieber@windriver.com
 */
public class RSENewConnectionWizardTestSimpleWizardPage extends WizardPage {
	private List fList;
	private IRSESystemType[] fSystemTypes;
	RSEMainNewConnectionWizard fMainWizard;
	/**
	 * Constructor.
	 * 
     * @param wizardRegistry The wizard registry to use. Must not be <code>null</code>
     * @param pageName the name of the page
	 */
	public RSENewConnectionWizardTestSimpleWizardPage(RSENewConnectionWizardRegistry wizardRegistry, String pageName) {
		super(pageName);
		assert wizardRegistry != null;
	}

	/**
	 * Constructor.
	 * 
     * @param wizardRegistry The wizard registry to use. Must not be <code>null</code>
     * @param pageName the name of the page
     * @param title the title for this wizard page,
     *   or <code>null</code> if none
     * @param titleImage the image descriptor for the title of this wizard page,
     *   or <code>null</code> if none
	 */
	public RSENewConnectionWizardTestSimpleWizardPage(RSENewConnectionWizardRegistry wizardRegistry, String pageName, String title, ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
		assert wizardRegistry != null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.DialogPage#dispose()
	 */
	public void dispose() {
		fSystemTypes = null;
		
		fList = null;
		
		if (fMainWizard != null) { fMainWizard.dispose(); fMainWizard = null; }
		
		super.dispose();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		setControl(composite);
		
		fList = new List(composite, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL);
		fList.setLayoutData(new GridData(GridData.FILL_BOTH));

		IRSECoreRegistry coreRegistry = RSECorePlugin.getTheCoreRegistry();
		fSystemTypes = coreRegistry.getSystemTypes();
		for (int i = 0; i < fSystemTypes.length; i++) {
			fList.add(fSystemTypes[i].getLabel());
		}
		
		fList.addSelectionListener(new SelectionAdapter() {
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			public void widgetSelected(SelectionEvent e) {
				// Update the main wizard if already created
				if (fMainWizard != null && getSelectedSystemType() != null) fMainWizard.restrictToSystemType(getSelectedSystemType());
				setPageComplete(getSelectedSystemType() != null);
			}
		});
		
		setPageComplete(false);
	}

	/**
	 * Returns the selected system type instance. This method will
	 * return <code>null</code> if the page hasn't been visible yet or
	 * got already disposed.
	 *  
	 * @return The selected RSE system type instance or <code>null</code>.
	 */
	public IRSESystemType getSelectedSystemType() {
		if (fList != null && !fList.isDisposed() && fList.getSelectionIndex() != -1) {
			return fSystemTypes[fList.getSelectionIndex()];
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.WizardPage#canFlipToNextPage()
	 */
	public boolean canFlipToNextPage() {
		return super.canFlipToNextPage() && (fList != null && !fList.isDisposed() && fList.getSelectionIndex() != -1);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.WizardPage#getNextPage()
	 */
	public IWizardPage getNextPage() {
		if (fMainWizard == null) {
			// Create a new instance
			fMainWizard = new RSEMainNewConnectionWizard();
			// Create the pages
			fMainWizard.addPages();
			// Restrict the wizard to the selected system type
			if (getSelectedSystemType() != null) fMainWizard.restrictToSystemType(getSelectedSystemType());
		}
		return fMainWizard.getStartingPage();
	}
}
