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
import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.help.WorkbenchHelp;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


public class IndexerOptionPropertyPage extends PropertyPage {
	
	private IndexerOptionDialogPage optionPage;
	private boolean oldIndexerValue;
	private int oldIndexerProblemsValue;
	private boolean requestedIndexAll;
	
	public IndexerOptionPropertyPage(){
		super();
		optionPage = new IndexerOptionDialogPage();
		requestedIndexAll = false;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new FillLayout());

		optionPage.createControl(composite);
		WorkbenchHelp.setHelp(composite, ICHelpContextIds.PROJECT_INDEXER_PROPERTIES);	
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
			oldIndexerProblemsValue = getIndexerProblemsEnabled( project );
		} catch (CoreException e) {
			e.printStackTrace();
		}
		
		optionPage.setIndexerValue(oldIndexerValue);
		optionPage.setIndexerProblemValues( oldIndexerProblemsValue );
	}
	
	/*
	 * @see IPreferencePage#performOk()
	 */
	public boolean performOk() {
	
		boolean newIndexerValue = optionPage.getIndexerValue();
		int newIndexerProblemsValue = optionPage.getIndexerProblemsValues();
		
		boolean indexChanged = (oldIndexerValue != newIndexerValue);
		boolean problemsChanged = (oldIndexerProblemsValue != newIndexerProblemsValue );
		
		if ( indexChanged || problemsChanged){
			//persist new values
			IProject tempProject = getProject();
			optionPage.persistIndexerValues(tempProject);
		
			if( problemsChanged && newIndexerProblemsValue == 0)
				CCorePlugin.getDefault().getCoreModel().getIndexManager().removeIndexerProblems(tempProject);
						
			//if indexer is now on send a index all request 
			if( indexChanged && newIndexerValue && !requestedIndexAll  ) {
				CCorePlugin.getDefault().getCoreModel().getIndexManager().indexAll(tempProject);
				requestedIndexAll = true;	
			} else if( indexChanged && !newIndexerValue ){
				CCorePlugin.getDefault().getCoreModel().getIndexManager().discardJobs( tempProject.getName() );
			}
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
		// See if there's already one associated with the resource for this
		// session
		 Boolean indexValue = (Boolean) project.getSessionProperty(IndexManager.activationKey);

		// Try to load one for the project
		if (indexValue == null) {
			indexValue = loadIndexerEnabledFromCDescriptor(project);
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
			optionPage.persistIndexerValues(project);
		}
		
		return indexValue.booleanValue();
	}
	
	public int getIndexerProblemsEnabled( IProject project ) throws CoreException 
	{		
		// See if there's already one associated with the resource for this session
		 Integer value = (Integer) project.getSessionProperty( IndexManager.problemsActivationKey );

		// Try to load one for the project
		if (value == null) {
			value = loadIndexerProblemsEnabledFromCDescriptor(project);
		}
	
		// There is nothing persisted for the session, or saved in a file so
		// create a build info object
		if (value != null) {
			project.setSessionProperty(IndexManager.problemsActivationKey, value);
		} else {
			//Hmm, no persisted indexer value. Could be an old project - set all to false and persist
			value = new Integer( 0 );
			optionPage.setIndexerProblemValues( 0 );
			optionPage.persistIndexerValues(project);
		}
		
		return value.intValue();
	}
	/**
	 * Loads dis from .cdtproject file
	 * @param project
	 * @param includes
	 * @param symbols
	 * @throws CoreException
	 */
	private Boolean loadIndexerEnabledFromCDescriptor(IProject project) throws CoreException {
		ICDescriptor descriptor = CCorePlugin.getDefault().getCProjectDescription(project, true);
		
		Node child = descriptor.getProjectData(IndexManager.CDT_INDEXER).getFirstChild();
		Boolean strBool = null;
		
		while (child != null) {
			if (child.getNodeName().equals(IndexManager.INDEXER_ENABLED)) 
				 strBool = Boolean.valueOf(((Element)child).getAttribute(IndexManager.INDEXER_VALUE));
			
			
			child = child.getNextSibling();
		}
		
		return strBool;
	}
	
	private Integer loadIndexerProblemsEnabledFromCDescriptor( IProject project ) throws CoreException
	{
		ICDescriptor descriptor = CCorePlugin.getDefault().getCProjectDescription(project, true);
		
		Node child = descriptor.getProjectData(IndexManager.CDT_INDEXER).getFirstChild();
		Integer strInt = null;
		
		while (child != null) {
			if (child.getNodeName().equals(IndexManager.INDEXER_PROBLEMS_ENABLED)) {
				String val = ((Element)child).getAttribute(IndexManager.INDEXER_PROBLEMS_VALUE);
				try{
					strInt = Integer.valueOf( val );
				} catch( NumberFormatException e ){
					//some old projects might have a boolean stored, translate that into just preprocessors
					Boolean bool = Boolean.valueOf( val );
					if( bool.booleanValue() )
						strInt = new Integer( IndexManager.PREPROCESSOR_PROBLEMS_BIT );
					else 
						strInt = new Integer( 0 );
				}
				break;
			}
			
			child = child.getNextSibling();
		}
		return strInt;
	}
	
}
