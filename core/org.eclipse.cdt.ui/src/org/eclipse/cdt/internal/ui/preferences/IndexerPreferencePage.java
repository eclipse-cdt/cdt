/*******************************************************************************
 * Copyright (c) 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.preferences;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.dialogs.ICOptionContainer;
import org.eclipse.cdt.ui.dialogs.IndexerBlock;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class IndexerPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage, ICOptionContainer {

	private IndexerBlock fOptionBlock;
	
	public IndexerPreferencePage(){
		setPreferenceStore(CUIPlugin.getDefault().getPreferenceStore());
		setDescription(PreferencesMessages.getString("IndexerPrefs.description"));  //$NON-NLS-1$
		fOptionBlock = new IndexerBlock();
	}
	
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new FillLayout());
	
		fOptionBlock.createControl(composite);
		
		return composite;
	}

	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub

	}

	public void updateContainer() {
		// TODO Auto-generated method stub

	}

	public IProject getProject() {
		// TODO Auto-generated method stub
		return null;
	}

	public Preferences getPreferences() {
		return null;
	}

	public boolean performOk() {
		try {
			fOptionBlock.performApply(null);
		} catch (CoreException e) {}
		CUIPlugin.getDefault().savePluginPreferences();
		return true;
	}
}
