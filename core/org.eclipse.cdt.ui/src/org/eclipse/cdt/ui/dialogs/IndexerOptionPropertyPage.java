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
import org.eclipse.cdt.internal.core.search.indexing.IndexManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.PropertyPage;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


public class IndexerOptionPropertyPage extends PropertyPage {
	
	private IndexerOptionDialogPage optionPage;
	private boolean oldIndexerValue;
	
	public IndexerOptionPropertyPage(){
		super();
		optionPage = new IndexerOptionDialogPage();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new FillLayout());

		optionPage.createControl(composite);
		
		initialize();
		
		return composite;
	}
	

	protected void performDefaults() {
		initialize();
		super.performDefaults();
	}
	
	private void initialize(){
		IProject project = getProject();
		
		
		try {
			oldIndexerValue = getIndexerEnabled(project);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		optionPage.setIndexerValue(oldIndexerValue);
	}
	
	/*
	 * @see IPreferencePage#performOk()
	 */
	public boolean performOk() {
	
		boolean newIndexerValue = optionPage.getIndexerValue();	
		
		if (oldIndexerValue != newIndexerValue){
			//persist new value
			IProject tempProject = getProject();
			optionPage.persistIndexerValue(tempProject);
		
			//if indexer is now on send a index all request 
			if (newIndexerValue)
				CCorePlugin.getDefault().getCoreModel().getIndexManager().indexAll(tempProject);
			
				
		}
		
		return true;
	}
	
	public IProject getProject(){
		Object tempElement = getElement();
		IProject project = null;
		if (tempElement != null && tempElement instanceof IProject)
			project = (IProject) tempElement;
			
		return project;
	}
	
	public boolean getIndexerEnabled(IProject project) throws CoreException {
		boolean indexerEnabled = false;
		
		// See if there's already one associated with the resource for this
		// session
		 Boolean indexValue = (Boolean) project.getSessionProperty(IndexManager.activationKey);

		// Try to load one for the project
		if (indexValue == null) {
			indexValue = loadIndexerEnabledromCDescriptor(project);
		}
	
		// There is nothing persisted for the session, or saved in a file so
		// create a build info object
		if (indexValue != null) {
			project.setSessionProperty(IndexManager.activationKey, indexValue);
		}
		else{
			//Hmm, no persisted indexer value. Could be an old project - set to true and persist
			indexValue = new Boolean(true);
			optionPage.setIndexerValue(true);
			optionPage.persistIndexerValue(project);
		}
		
		return indexValue.booleanValue();
	}
	
	/**
	 * Loads dis from .cdtproject file
	 * @param project
	 * @param includes
	 * @param symbols
	 * @throws CoreException
	 */
	private Boolean loadIndexerEnabledromCDescriptor(IProject project) throws CoreException {
		ICDescriptor descriptor = CCorePlugin.getDefault().getCProjectDescription(project);
		
		Node child = descriptor.getProjectData(IndexerBlock.CDT_INDEXER).getFirstChild();
		Boolean strBool = null;
		
		while (child != null) {
			if (child.getNodeName().equals(IndexerBlock.INDEXER_ENABLED)) 
				 strBool = Boolean.valueOf(((Element)child).getAttribute(IndexerBlock.INDEXER_VALUE));
			
			
			child = child.getNextSibling();
		}
		
		return strBool;
	}
	
	
}
