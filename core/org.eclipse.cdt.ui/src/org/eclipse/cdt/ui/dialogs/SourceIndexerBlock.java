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
import org.eclipse.cdt.internal.core.index.sourceindexer.SourceIndexer;
import org.eclipse.cdt.internal.core.search.indexing.IndexManager;
import org.eclipse.cdt.internal.ui.CUIMessages;
import org.eclipse.cdt.ui.index.AbstractIndexerPage;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


public class SourceIndexerBlock extends AbstractIndexerPage {
	
	private static final String ENABLE_PREPROCESSOR_PROBLEMS = CUIMessages.getString( "IndexerOptions.enablePreprocessor" ); //$NON-NLS-1$
	private static final String ENABLE_SEMANTIC_PROBLEMS = CUIMessages.getString( "IndexerOptions.enableSemantic" ); //$NON-NLS-1$
	private static final String ENABLE_SYNTACTIC_PROBLEMS = CUIMessages.getString( "IndexerOptions.enableSyntactic" ); //$NON-NLS-1$
	private static final String ENABLE_INDEXING = CUIMessages.getString( "IndexerOptions.enableIndexing" ); //$NON-NLS-1$
	private static final String INDEXER = CUIMessages.getString("IndexerOptions.indexer" ); //$NON-NLS-1$ 
	private static final String INDEXER_PROBLEMS = CUIMessages.getString("IndexerOptions.problemReporting" ); //$NON-NLS-1$
	
	private Button indexerEnabled;
	private Button preprocessorProblemsEnabled;
	private Button syntacticProblemsEnabled;
	private Button semanticProblemsEnabled;
	
	private boolean oldIndexerValue;
	private int oldIndexerProblemsValue;
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.dialogs.ICOptionPage#performApply(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void performApply(IProgressMonitor monitor) throws CoreException {
		
		IProject newProject = null;
		newProject = getContainer().getProject();
		
		this.persistIndexerValues(newProject);
		
		boolean indexProject = getIndexerValue();
		
		//if (indexProject && newProject != null)
			//SourceIndexer.indexAll()
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.dialogs.ICOptionPage#performDefaults()
	 */
	public void performDefaults() {
		// TODO Auto-generated method stub
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		
		Composite page = ControlFactory.createComposite(parent, 1);
		
		Group group = ControlFactory.createGroup(page,INDEXER,1);
        
        GridData gd = (GridData) group.getLayoutData();
        gd.grabExcessHorizontalSpace = true;
        gd.horizontalAlignment = GridData.FILL;


		indexerEnabled = ControlFactory.createCheckBox(group, ENABLE_INDEXING );
		
		
		Group problemsGroup = ControlFactory.createGroup(page,INDEXER_PROBLEMS,1);
        
        GridData gd2 = (GridData) problemsGroup.getLayoutData();
        gd2.grabExcessHorizontalSpace = true;
        gd2.horizontalAlignment = GridData.FILL;
        
		
		preprocessorProblemsEnabled = ControlFactory.createCheckBox( problemsGroup, ENABLE_PREPROCESSOR_PROBLEMS );
		semanticProblemsEnabled = ControlFactory.createCheckBox( problemsGroup, ENABLE_SEMANTIC_PROBLEMS );
		//uncomment when we want to report syntax problems
		syntacticProblemsEnabled = ControlFactory.createCheckBox( problemsGroup, ENABLE_SYNTACTIC_PROBLEMS );
		setControl(page);
		
	}
	
	public void persistIndexerValues(IProject project){
		ICDescriptor descriptor = null;
		Element rootElement = null;
		IProject newProject = null;
		
		try {
			newProject = project;
			descriptor = CCorePlugin.getDefault().getCProjectDescription(newProject, true);
			rootElement = descriptor.getProjectData(SourceIndexer.SOURCE_INDEXER);
		
			// Clear out all current children
			Node child = rootElement.getFirstChild();
			while (child != null) {
				rootElement.removeChild(child);
				child = rootElement.getFirstChild();
			}
			Document doc = rootElement.getOwnerDocument();
	
			boolean indexProject = getIndexerValue();
			int problemValues = getIndexerProblemsValues();
					
			saveIndexerEnabled(indexProject, rootElement, doc);
			saveIndexerProblemsEnabled( problemValues, rootElement, doc );
			
			descriptor.saveProjectData();
			
			//Update project session property
			
			project.setSessionProperty(SourceIndexer.activationKey,new Boolean(indexProject));
			project.setSessionProperty(SourceIndexer.problemsActivationKey, new Integer( problemValues ));	
	
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	
	public boolean getIndexerValue(){
		return indexerEnabled.getSelection();
	}
	
	public int getIndexerProblemsValues(){
		int result = 0;
		result |= preprocessorProblemsEnabled.getSelection() ? SourceIndexer.PREPROCESSOR_PROBLEMS_BIT : 0;
		if( syntacticProblemsEnabled != null )
			result |= syntacticProblemsEnabled.getSelection() ? SourceIndexer.SYNTACTIC_PROBLEMS_BIT : 0;
		result |= semanticProblemsEnabled.getSelection() ? SourceIndexer.SEMANTIC_PROBLEMS_BIT : 0;
		return result;
	}
	
	private static void saveIndexerEnabled (boolean indexerEnabled, Element rootElement, Document doc ) {
		
		Element indexEnabled = doc.createElement(SourceIndexer.INDEXER_ENABLED);
		Boolean tempValue= new Boolean(indexerEnabled);
		
		indexEnabled.setAttribute(SourceIndexer.INDEXER_VALUE,tempValue.toString());
		rootElement.appendChild(indexEnabled);

	}
	private static void saveIndexerProblemsEnabled ( int problemValues, Element rootElement, Document doc ) {
		
		Element enabled = doc.createElement(SourceIndexer.INDEXER_PROBLEMS_ENABLED);
		Integer tempValue= new Integer( problemValues );
		
		enabled.setAttribute(SourceIndexer.INDEXER_PROBLEMS_VALUE, tempValue.toString());
		rootElement.appendChild(enabled);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.index2.AbstractIndexerPage#initialize(org.eclipse.core.resources.IProject)
	 */
	public void initialize(IProject project) {
		
		try {
			oldIndexerValue = getIndexerEnabled(project);
			oldIndexerProblemsValue = getIndexerProblemsEnabled( project );
		} catch (CoreException e) {
			e.printStackTrace();
		}
		
		//Set the index enabled checkbox
		setIndexerValue(oldIndexerValue);
		
		//Set the IProblem checkboxes
		setIndexerProblemValues(oldIndexerProblemsValue);
	}
	
	public boolean getIndexerEnabled(IProject project) throws CoreException {
		// See if there's already one associated with the resource for this
		// session
		 Boolean indexValue = (Boolean) project.getSessionProperty(SourceIndexer.activationKey);

		// Try to load one for the project
		if (indexValue == null) {
			indexValue = loadIndexerEnabledFromCDescriptor(project);
		}
	
		// There is nothing persisted for the session, or saved in a file so
		// create a build info object
		if (indexValue != null) {
			project.setSessionProperty(SourceIndexer.activationKey, indexValue);
		}
		else{
			//Hmm, no persisted indexer value. Could be an old project - set to true and persist
			indexValue = new Boolean(true);
			setIndexerValue(true);
			persistIndexerValues(project);
		}
		
		return indexValue.booleanValue();
	}
	
	public int getIndexerProblemsEnabled( IProject project ) throws CoreException 
	{		
		// See if there's already one associated with the resource for this session
		 Integer value = (Integer) project.getSessionProperty( SourceIndexer.problemsActivationKey );

		// Try to load one for the project
		if (value == null) {
			value = loadIndexerProblemsEnabledFromCDescriptor(project);
		}
	
		// There is nothing persisted for the session, or saved in a file so
		// create a build info object
		if (value != null) {
			project.setSessionProperty(SourceIndexer.problemsActivationKey, value);
		} else {
			//Hmm, no persisted indexer value. Could be an old project - set all to false and persist
			value = new Integer( 0 );
			setIndexerProblemValues( 0 );
			persistIndexerValues(project);
		}
		
		return value.intValue();
	}
	
	public void setIndexerValue(boolean value){
		indexerEnabled.setSelection(value);
	}
	
	public void setIndexerProblemValues( int value ){
		preprocessorProblemsEnabled.setSelection( (value & SourceIndexer.PREPROCESSOR_PROBLEMS_BIT) != 0 );
		if( syntacticProblemsEnabled != null ) 
			syntacticProblemsEnabled.setSelection( (value & SourceIndexer.SYNTACTIC_PROBLEMS_BIT) != 0 );
		semanticProblemsEnabled.setSelection( (value & SourceIndexer.SEMANTIC_PROBLEMS_BIT) != 0 );
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
		
		Node child = descriptor.getProjectData(SourceIndexer.SOURCE_INDEXER).getFirstChild();
		Boolean strBool = null;
		
		while (child != null) {
			if (child.getNodeName().equals(SourceIndexer.INDEXER_ENABLED)) 
				 strBool = Boolean.valueOf(((Element)child).getAttribute(SourceIndexer.INDEXER_VALUE));
			
			
			child = child.getNextSibling();
		}
		
		return strBool;
	}
	
	private Integer loadIndexerProblemsEnabledFromCDescriptor( IProject project ) throws CoreException
	{
		ICDescriptor descriptor = CCorePlugin.getDefault().getCProjectDescription(project, true);
		
		Node child = descriptor.getProjectData(SourceIndexer.SOURCE_INDEXER).getFirstChild();
		Integer strInt = null;
		
		while (child != null) {
			if (child.getNodeName().equals(SourceIndexer.INDEXER_PROBLEMS_ENABLED)) {
				String val = ((Element)child).getAttribute(SourceIndexer.INDEXER_PROBLEMS_VALUE);
				try{
					strInt = Integer.valueOf( val );
				} catch( NumberFormatException e ){
					//some old projects might have a boolean stored, translate that into just preprocessors
					Boolean bool = Boolean.valueOf( val );
					if( bool.booleanValue() )
						strInt = new Integer( SourceIndexer.PREPROCESSOR_PROBLEMS_BIT );
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
