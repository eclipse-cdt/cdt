/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.make.internal.ui.properties;

import org.eclipse.cdt.ui.dialogs.ICOptionContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.ui.dialogs.ScannerConfigPage;

/**
 * Scanner config property page
 * @author vhirsl
 */
public class ScannerConfigPropertyPage extends PropertyPage implements ICOptionContainer {

	private ScannerConfigPage fScannerConfigPage;

	public ScannerConfigPropertyPage() {
		super();
	}

	/**
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {
		fScannerConfigPage = new ScannerConfigPage(true);	// add title
		// must set container before call to createControl
		fScannerConfigPage.setContainer(this);
		
		fScannerConfigPage.createControl(parent);
		return fScannerConfigPage.getControl();
	}

	protected void performDefaults() {
		fScannerConfigPage.performDefaults();
		super.performDefaults();
	}
	
	public boolean performOk() {
		try {
			fScannerConfigPage.performApply(null);
			return true;
		}
		catch (CoreException e) {
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.dialogs.ICOptionContainer#updateContainer()
	 */
	public void updateContainer() {
		setValid(fScannerConfigPage.isValid());
		setErrorMessage(fScannerConfigPage.getErrorMessage());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.dialogs.ICOptionContainer#getProject()
	 */
	public IProject getProject() {
		return (IProject) getElement();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.dialogs.ICOptionContainer#getPreferences()
	 */
	public Preferences getPreferences() {
		return MakeCorePlugin.getDefault().getPluginPreferences();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferencePage#isValid()
	 */
	public boolean isValid() {
		updateContainer();
		return super.isValid();
	}
}
