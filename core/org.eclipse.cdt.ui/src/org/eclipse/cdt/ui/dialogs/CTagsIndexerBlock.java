/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.ui.dialogs;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.ICExtensionReference;
import org.eclipse.cdt.internal.ui.CUIMessages;
import org.eclipse.cdt.ui.index.AbstractIndexerPage;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

/**
 * @author Bogdan Gheorghe
 */
public class CTagsIndexerBlock extends AbstractIndexerPage {

	private Button indexerEnabled;
    private boolean oldIndexerValue;
	
    /* (non-Javadoc)
     * @see org.eclipse.cdt.ui.index.AbstractIndexerPage#initialize(org.eclipse.core.resources.IProject)
     */
    public void initialize(IProject project) {
        
		try {
			loadPersistedValues(project);
			this.currentProject = project;
		} catch (CoreException e) {
			e.printStackTrace();
		}

    }

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
					//}
				}
			}
		} /*else {
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
		}*/
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
		
		Group group = ControlFactory.createGroup(page,"CTags Indexer",1); //$NON-NLS-1$
        
        GridData gd = (GridData) group.getLayoutData();
        gd.grabExcessHorizontalSpace = true;
        gd.horizontalAlignment = GridData.FILL;

		indexerEnabled = ControlFactory.createCheckBox(group, "Enable Indexing" );  //$NON-NLS-1$
		
		setControl(page);
    }
    
    public void loadPersistedValues(IProject project) throws CoreException {
		
		ICDescriptor cdesc = CCorePlugin.getDefault().getCProjectDescription(project, false);
		ICExtensionReference[] cext = cdesc.get(CCorePlugin.INDEXER_UNIQ_ID);
		if (cext.length > 0) {
			for (int i = 0; i < cext.length; i++) {
				String id = cext[i].getID();
					String orig = cext[i].getExtensionData("indexenabled"); //$NON-NLS-1$
					if (orig != null){
						Boolean tempBool = new Boolean(orig);
						oldIndexerValue = tempBool.booleanValue();
					}
			}
		}
	}
    
	private String getIndexerEnabledString(){
		if (indexerEnabled.getSelection())
			return "true"; //$NON-NLS-1$
		
		return "false"; //$NON-NLS-1$
	}


}
