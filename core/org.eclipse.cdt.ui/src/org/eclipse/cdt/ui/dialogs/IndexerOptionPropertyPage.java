/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 ******************************************************************************/

package org.eclipse.cdt.ui.dialogs;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.ICExtensionReference;
import org.eclipse.cdt.internal.core.search.indexing.IndexManager;
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
import org.w3c.dom.Element;
import org.w3c.dom.Node;


public class IndexerOptionPropertyPage extends PropertyPage {
	


	private IndexerBlock optionPage;
	private String oldIndexerID;

	private boolean requestedIndexAll;
	 
	public IndexerOptionPropertyPage(){
		super();
		optionPage = new IndexerBlock();
		requestedIndexAll = false;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
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
		
		try {
			oldIndexerID = getIndexerID(project);
			
		} catch (CoreException e) {
			e.printStackTrace();
		}
		
		optionPage.setIndexerID(oldIndexerID, project);
	}
	
	/*
	 * @see IPreferencePage#performOk()
	 */
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
	
	public String getIndexerID(IProject project) throws CoreException {
		
	
		ICDescriptor desc = CCorePlugin.getDefault().getCProjectDescription(project, true);
		ICExtensionReference[] ref = desc.get(CCorePlugin.INDEXER_UNIQ_ID);
		String indexerID = null;
		for (int i = 0; i < ref.length; i++) {
			indexerID = ref[i].getID();
		}
		
		return indexerID;
	}

	/**
	 * Loads indexerID from .cdtproject file
	 * @param project
	 * @param includes
	 * @param symbols
	 * @throws CoreException
	 */
	private String loadIndexerIDFromCDescriptor(IProject project) throws CoreException {
		ICDescriptor descriptor = CCorePlugin.getDefault().getCProjectDescription(project, true);
		
		Node child = descriptor.getProjectData(IndexManager.CDT_INDEXER).getFirstChild();
		
		String indexerID = ""; //$NON-NLS-1$
		
		while (child != null) {
			if (child.getNodeName().equals(IndexerBlock.INDEXER_UI)) 
				  indexerID = ((Element)child).getAttribute(IndexerBlock.INDEXER_UI_VALUE);
			
			child = child.getNextSibling();
		}
		
		return indexerID;
	}
	
}
