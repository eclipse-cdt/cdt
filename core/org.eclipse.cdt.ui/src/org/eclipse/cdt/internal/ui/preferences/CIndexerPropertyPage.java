package org.eclipse.cdt.internal.ui.preferences;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.index.IndexModel;
import org.eclipse.core.resources.IProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.PropertyPage;

public class CIndexerPropertyPage extends PropertyPage  {
	private Button indexerSwitch;

	protected Control createContents(Composite parent) {
		Composite composite= new Composite(parent, SWT.NONE);
		GridLayout grid = new GridLayout();
		grid.numColumns = 1;
		composite.setLayout(grid);
		
		IProject project= getProject();
		IndexModel indexer = CCorePlugin.getDefault().getIndexModel();			

		indexerSwitch = new Button(composite, SWT.CHECK | SWT.RIGHT);
		indexerSwitch.setAlignment(SWT.LEFT);
		indexerSwitch.setText("Enable indexing service for this project");
		indexerSwitch.setSelection(indexer.isEnabled(project));
		return composite;
	}

	/**
	 * @see PreferencePage#performOk
	 */	
	public boolean performOk() {
		IProject project= getProject();
		IndexModel indexer = CCorePlugin.getDefault().getIndexModel();			
		indexer.setEnabled(project, indexerSwitch.getSelection());
		return true;
	}
		
	private IProject getProject() {
		Object element= getElement();
		if (element instanceof IProject) {
			return (IProject)element;
		}
		return null;
	} 

	protected void performDefaults() {
		IProject project= getProject();
		IndexModel indexer = CCorePlugin.getDefault().getIndexModel();			
		indexer.setEnabled(project, false);
		super.performDefaults();
	}

}
