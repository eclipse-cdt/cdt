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
import org.eclipse.cdt.internal.ui.CUIMessages;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


public class IndexerOptionDialogPage extends DialogPage {
	
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
	
	public IndexerOptionDialogPage(){
		super();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Composite result= new Composite(parent, SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.marginHeight= convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.marginWidth= 0;
		layout.verticalSpacing= convertVerticalDLUsToPixels(10);
		layout.horizontalSpacing= convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		result.setLayout(layout);
		
		Group group= new Group(result, SWT.NONE);
		group.setLayout(new GridLayout());
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		group.setText( INDEXER );

		indexerEnabled = createCheckButton(group, ENABLE_INDEXING );
		
		setControl(result);
	}
	
	/**
	 * Creates a button with the given label and sets the default 
	 * configuration data.
	 */
	private Button createCheckButton( Composite parent, String label )
	{
		Button button = new Button( parent, SWT.CHECK | SWT.LEFT );
		button.setText( label );
		// FieldEditor GridData
		GridData data = new GridData();
		button.setLayoutData( data );
		return button;
	}
	
	public void setIndexerValue(boolean value){
		indexerEnabled.setSelection(value);
	}
		
	public boolean getIndexerValue(){
		return indexerEnabled.getSelection();
	}
	
	
	public void persistIndexerValues(IProject project){
		ICDescriptor descriptor = null;
		Element rootElement = null;
		IProject newProject = null;
		
		try {
			newProject = project;
			descriptor = CCorePlugin.getDefault().getCProjectDescription(newProject);
			rootElement = descriptor.getProjectData(IndexManager.CDT_INDEXER);
		
			// Clear out all current children
			Node child = rootElement.getFirstChild();
			while (child != null) {
				rootElement.removeChild(child);
				child = rootElement.getFirstChild();
			}
			Document doc = rootElement.getOwnerDocument();
	
			boolean indexProject = getIndexerValue();
					
			saveIndexerEnabled(indexProject, rootElement, doc);
			
			descriptor.saveProjectData();
			
			//Update project session property
			
			project.setSessionProperty(IndexManager.activationKey,new Boolean(indexProject));
	
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	 
	private static void saveIndexerEnabled (boolean indexerEnabled, Element rootElement, Document doc ) {
		
		Element indexEnabled = doc.createElement(IndexManager.INDEXER_ENABLED);
		Boolean tempValue= new Boolean(indexerEnabled);
		
		indexEnabled.setAttribute(IndexManager.INDEXER_VALUE,tempValue.toString());
		rootElement.appendChild(indexEnabled);

	}

}
