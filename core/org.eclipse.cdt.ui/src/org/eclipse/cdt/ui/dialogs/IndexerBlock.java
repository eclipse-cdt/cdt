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
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;


public class IndexerBlock extends AbstractCOptionPage {
	
	private IndexerOptionDialogPage optionPage;


	private static final String INDEXER_LABEL = "IndexerBlock.label"; //$NON-NLS-1$
	private static final String INDEXER_DESC = "IndexerBlock.desc"; //$NON-NLS-1$
	
	public IndexerBlock(){
		super(CUIPlugin.getResourceString(INDEXER_LABEL));
		setDescription(CUIPlugin.getResourceString(INDEXER_DESC));
		optionPage = new IndexerOptionDialogPage();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.dialogs.ICOptionPage#performApply(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void performApply(IProgressMonitor monitor) throws CoreException {
		IProject newProject = null;
		newProject = getContainer().getProject();
		
		optionPage.persistIndexerValues(newProject);
		
		boolean indexProject = optionPage.getIndexerValue();
		
		if (indexProject && newProject != null)
			CCorePlugin.getDefault().getCoreModel().getIndexManager().indexAll(newProject);
	    
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
		Composite result = new Composite(parent, SWT.NONE);
		result.setLayout(new GridLayout());
		result.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		optionPage.createControl(result);
		optionPage.setIndexerValue(true);
		
		setControl(result);
		

	}
	
	public boolean isIndexEnabled(){
		return optionPage.getIndexerValue();
	}

}
