/*******************************************************************************
 * Copyright (c) 2005, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.preferences;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.dialogs.ICOptionContainer;
import org.eclipse.cdt.ui.dialogs.IndexerBlock;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class IndexerPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage, ICOptionContainer {

	private IndexerBlock fOptionBlock;
	private Button applyIndexerToAllButton;
	
	public IndexerPreferencePage(){
		setPreferenceStore(CUIPlugin.getDefault().getPreferenceStore());
		setDescription(PreferencesMessages.getString("IndexerPrefs.description"));  //$NON-NLS-1$
		fOptionBlock = new IndexerBlock();
	}
	
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, true);
		composite.setLayout(layout);
	
		fOptionBlock.createControl(composite);
		
		applyIndexerToAllButton = new Button(composite, SWT.CHECK);
		applyIndexerToAllButton.setText(CUIPlugin.getResourceString("IndexerPreferencePage.applyToAllProjects")); //$NON-NLS-1$
		
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

	private static class ApplyIndexer extends Job {
		private final String indexerId;
		public ApplyIndexer(String indexerId) {
			super("ApplyIndexer"); //$NON-NLS-1$
			setSystem(true);
			this.indexerId = indexerId;
		}
		protected IStatus run(IProgressMonitor monitor) {
			try {
				IPDOMManager manager = CCorePlugin.getPDOMManager();
				ICProject[] projects = CoreModel.getDefault().getCModel().getCProjects();
				for (int i = 0; i < projects.length; ++i) {
					manager.setIndexerId(projects[i], indexerId);
				}
				return Status.OK_STATUS;
			} catch (CoreException e) {
				return e.getStatus();
			}
		}
	}
	public boolean performOk() {
		try {
			fOptionBlock.performApply(null);
			if (applyIndexerToAllButton.getSelection()) {
				String indexerName = fOptionBlock.getSelectedIndexerID();
				String indexerId = fOptionBlock.getIndexerPageId(indexerName);
				new ApplyIndexer(indexerId).schedule();
			}
		} catch (CoreException e) {}
		CUIPlugin.getDefault().savePluginPreferences();
		return true;
	}
}
