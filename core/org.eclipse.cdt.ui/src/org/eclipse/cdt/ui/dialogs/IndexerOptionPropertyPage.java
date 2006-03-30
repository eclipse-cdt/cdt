/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 *******************************************************************************/

package org.eclipse.cdt.ui.dialogs;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyPage;

public class IndexerOptionPropertyPage extends PropertyPage {

	private IndexerBlock optionPage;
	private String oldIndexerID;

	public IndexerOptionPropertyPage(){
		super();
		optionPage = new IndexerBlock();
	}

	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new FillLayout());

		optionPage.createControl(composite);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, ICHelpContextIds.PROJECT_INDEXER_PROPERTIES);	
		initialize();
		
		return composite;
	}

	protected void performDefaults() {
		IProject tempProject = getProject();
		optionPage.resetIndexerPageSettings(tempProject);
	}
	
	private void initialize(){
		IProject project = getProject();
		oldIndexerID = CCorePlugin.getPDOMManager().getIndexerId(project);
		optionPage.setIndexerID(oldIndexerID, project);
	}
	
	public boolean performOk() {
		IProject tempProject = getProject();
		try {
			optionPage.persistIndexerSettings(tempProject, new NullProgressMonitor());
		} catch (CoreException e) {}
		
		return true;
	}
	
	public IProject getProject(){
		Object tempElement = getElement();
		IProject project = null;
		if (tempElement != null && tempElement instanceof IProject)
			project = (IProject) tempElement;
			
		return project;
	}

}
