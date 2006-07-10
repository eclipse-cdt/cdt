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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.ICExtensionReference;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.index.AbstractIndexerPage;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;

import org.eclipse.cdt.internal.ui.CUIMessages;


public class DOMSourceIndexerBlock extends AbstractIndexerPage {
	
	public final static String PREF_INDEX_MARKERS = CUIPlugin.PLUGIN_ID + ".indexmarkers"; //$NON-NLS-1$
	
	private static final String ENABLE_PREPROCESSOR_PROBLEMS = CUIMessages.getString( "IndexerOptions.enablePreprocessor" ); //$NON-NLS-1$
	private static final String ENABLE_SEMANTIC_PROBLEMS = CUIMessages.getString( "IndexerOptions.enableSemantic" ); //$NON-NLS-1$
	private static final String ENABLE_SYNTACTIC_PROBLEMS = CUIMessages.getString( "IndexerOptions.enableSyntactic" ); //$NON-NLS-1$

	private static final String INDEXER_PROBLEMS = CUIMessages.getString("IndexerOptions.problemReporting" ); //$NON-NLS-1$
		
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
//        String indexMarkers = getIndexerProblemsValuesString();
		
		if (container != null){
			proj = container.getProject();
		}
		else{
			proj = currentProject.getProject();
		}
		
		if (proj != null) {
			ICDescriptor cdesc = CCorePlugin.getDefault().getCProjectDescription(proj, false);
			ICExtensionReference[] cext = cdesc.get(CCorePlugin.INDEXER_UNIQ_ID);
			if (cext.length > 0) {
				for (int i = 0; i < cext.length; i++) {
//					String id = cext[i].getID();
//					String orig = cext[i].getExtensionData("indexmarkers"); //$NON-NLS-1$
//					String indexProblems = getIndexerProblemsValuesString();
//					if (orig == null || !orig.equals(indexProblems)) {
//						cext[i].setExtensionData("indexmarkers", indexProblems); //$NON-NLS-1$
//					}
				
				}
			}
		} else {
			if (prefStore != null) {
//				prefStore.setValue(PREF_INDEX_MARKERS, indexMarkers);
			}
		}

		//Project has just been created and its values have been store - don't need to request 
		//an indexAll as one will come through the DeltaProcessor
		if (currentProject == null)
			return;
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
		        
		Group problemsGroup = ControlFactory.createGroup(page,INDEXER_PROBLEMS,1);
        
        GridData gd2 = (GridData) problemsGroup.getLayoutData();
        gd2.grabExcessHorizontalSpace = true;
        gd2.horizontalAlignment = GridData.FILL;
        
		
		ControlFactory.createCheckBox( problemsGroup, ENABLE_PREPROCESSOR_PROBLEMS );
		ControlFactory.createCheckBox( problemsGroup, ENABLE_SEMANTIC_PROBLEMS );
		ControlFactory.createCheckBox( problemsGroup, ENABLE_SYNTACTIC_PROBLEMS );
		
		setControl(page);
		
	}
	
//	public String getIndexerProblemsValuesString(){
//		int result = 0;
//		result |= preprocessorProblemsEnabled.getSelection() ? DOMSourceIndexer.PREPROCESSOR_PROBLEMS_BIT : 0;
//		if( syntacticProblemsEnabled != null )
//			result |= syntacticProblemsEnabled.getSelection() ? DOMSourceIndexer.SYNTACTIC_PROBLEMS_BIT : 0;
//		result |= semanticProblemsEnabled.getSelection() ? DOMSourceIndexer.SEMANTIC_PROBLEMS_BIT : 0;
//		Integer tempInt = new Integer(result);
//		
//		return tempInt.toString();
//	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.index2.AbstractIndexerPage#initialize(org.eclipse.core.resources.IProject)
	 */
	public void initialize(ICProject project) {
		
		try {
			loadPersistedValues(project.getProject());
			this.currentProject = project;
		} catch (CoreException e) {}
	
		//Set the IProblem checkboxes
//		setIndexerProblemValues(oldIndexerProblemsValue);
	}

	public void loadPersistedValues(IProject project) throws CoreException {
		
		ICDescriptor cdesc = CCorePlugin.getDefault().getCProjectDescription(project, false);
		ICExtensionReference[] cext = cdesc.get(CCorePlugin.INDEXER_UNIQ_ID);
		if (cext.length > 0) {
			for (int i = 0; i < cext.length; i++) {
//				String id = cext[i].getID();
				
				String orig = cext[i].getExtensionData("indexmarkers"); //$NON-NLS-1$
				if (orig != null){
//					Integer tempInt = new Integer(orig);
//					oldIndexerProblemsValue = tempInt.intValue();
				}
			}
		}
	
	}
	
//	public void setIndexerProblemValues( int value ){
//		preprocessorProblemsEnabled.setSelection( (value & DOMSourceIndexer.PREPROCESSOR_PROBLEMS_BIT) != 0 );
//		if( syntacticProblemsEnabled != null ) 
//			syntacticProblemsEnabled.setSelection( (value & DOMSourceIndexer.SYNTACTIC_PROBLEMS_BIT) != 0 );
//		semanticProblemsEnabled.setSelection( (value & DOMSourceIndexer.SEMANTIC_PROBLEMS_BIT) != 0 );
//	}
	
	public void loadPreferences() {
		String indexerId=prefStore.getString(PREF_INDEX_MARKERS);
		if (!indexerId.equals("")) { //$NON-NLS-1$
//		   oldIndexerProblemsValue = (new Integer(indexerId)).intValue();
//		   setIndexerProblemValues(oldIndexerProblemsValue);
		}
	}
	
	public void removePreferences() {
		prefStore.setToDefault(PREF_INDEX_MARKERS);
	}
	
}
