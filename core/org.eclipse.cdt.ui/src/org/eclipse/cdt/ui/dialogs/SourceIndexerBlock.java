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
import org.eclipse.cdt.core.index.ICDTIndexer;
import org.eclipse.cdt.internal.core.index.sourceindexer.SourceIndexer;
import org.eclipse.cdt.internal.ui.CUIMessages;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.index.AbstractIndexerPage;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;


public class SourceIndexerBlock extends AbstractIndexerPage {
	
	public final static String PREF_INDEX_ENABLED = CUIPlugin.PLUGIN_ID + ".indexenabled"; //$NON-NLS-1$
	public final static String PREF_INDEX_MARKERS = CUIPlugin.PLUGIN_ID + ".indexmarkers"; //$NON-NLS-1$
	
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
	
	private boolean oldIndexerValue = false;
	private int oldIndexerProblemsValue = 0;
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.dialogs.ICOptionPage#performApply(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void performApply(IProgressMonitor monitor) throws CoreException {
	
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		monitor.beginTask(CUIMessages.getString("IndexerOptiosn.task.savingAttributes "), 1);  //$NON-NLS-1$
		ICOptionContainer container = getContainer();
		IProject proj = null;
		
		if (container != null){
			proj = container.getProject();
		}
		else{
			proj = currentProject;
		}
		
		if (proj != null) {
			ICDescriptor cdesc = CCorePlugin.getDefault().getCProjectDescription(proj, false);
			ICExtensionReference[] cext = cdesc.get(CCorePlugin.INDEXER_UNIQ_ID);
			if (cext.length > 0) {
				//initializeIndexerId();
				for (int i = 0; i < cext.length; i++) {
					String id = cext[i].getID();
					//if (cext[i].getID().equals(parserID)) {
						String orig = cext[i].getExtensionData("indexenabled"); //$NON-NLS-1$
						String indexEnabled = getIndexerEnabledString();
						if (orig == null || !orig.equals(indexEnabled)) {
							cext[i].setExtensionData("indexenabled", indexEnabled); //$NON-NLS-1$
						}
						orig = cext[i].getExtensionData("indexmarkers"); //$NON-NLS-1$
						String indexProblems = getIndexerProblemsValuesString();
						if (orig == null || !orig.equals(indexProblems)) {
							cext[i].setExtensionData("indexmarkers", indexProblems); //$NON-NLS-1$
						}
					//}
				}
			}
		} else {
			Preferences store = null;
			if (container != null){
				store = container.getPreferences();
			}
			
			if (store != null) {
				String indexEnabled = getIndexerEnabledString();
				String indexMarkers = getIndexerProblemsValuesString();
				store.setValue(PREF_INDEX_ENABLED, indexEnabled);
				store.setValue(PREF_INDEX_MARKERS, indexMarkers);
			}
		}
		
		boolean indexProject = getIndexerValue();
		
		if ((indexProject != oldIndexerValue)
			&& (currentProject != null)
			&& indexProject) {
			ICDTIndexer indexer = CCorePlugin.getDefault().getCoreModel().getIndexManager().getIndexerForProject(currentProject);
			if (indexer instanceof SourceIndexer)
			 ((SourceIndexer) indexer).indexAll(currentProject);
		}
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
	
	public boolean getIndexerValue(){
		return indexerEnabled.getSelection();
	}
	
	public String getIndexerProblemsValuesString(){
		int result = 0;
		result |= preprocessorProblemsEnabled.getSelection() ? SourceIndexer.PREPROCESSOR_PROBLEMS_BIT : 0;
		if( syntacticProblemsEnabled != null )
			result |= syntacticProblemsEnabled.getSelection() ? SourceIndexer.SYNTACTIC_PROBLEMS_BIT : 0;
		result |= semanticProblemsEnabled.getSelection() ? SourceIndexer.SEMANTIC_PROBLEMS_BIT : 0;
		Integer tempInt = new Integer(result);
		
		return tempInt.toString();
	}
	
	private String getIndexerEnabledString(){
		if (indexerEnabled.getSelection())
			return "true"; //$NON-NLS-1$
		
		return "false"; //$NON-NLS-1$
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.index2.AbstractIndexerPage#initialize(org.eclipse.core.resources.IProject)
	 */
	public void initialize(IProject project) {
		
		try {
			loadPersistedValues(project);
			this.currentProject = project;
		} catch (CoreException e) {
			e.printStackTrace();
		}
		
		//Set the index enabled checkbox
		setIndexerValue(oldIndexerValue);
		
		//Set the IProblem checkboxes
		setIndexerProblemValues(oldIndexerProblemsValue);
	}
	
	public void loadPersistedValues(IProject project) throws CoreException {
		
		ICDescriptor cdesc = CCorePlugin.getDefault().getCProjectDescription(project, false);
		ICExtensionReference[] cext = cdesc.get(CCorePlugin.INDEXER_UNIQ_ID);
		if (cext.length > 0) {
			//initializeIndexerId();
			for (int i = 0; i < cext.length; i++) {
				String id = cext[i].getID();
				//if (cext[i].getID().equals(parserID)) {
					String orig = cext[i].getExtensionData("indexenabled"); //$NON-NLS-1$
					if (orig != null){
						Boolean tempBool = new Boolean(orig);
						oldIndexerValue = tempBool.booleanValue();
					}
	
					orig = cext[i].getExtensionData("indexmarkers"); //$NON-NLS-1$
					if (orig != null){
						Integer tempInt = new Integer(orig);
						oldIndexerProblemsValue = tempInt.intValue();
					}
				//}
			}
		}
	
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
	
}
