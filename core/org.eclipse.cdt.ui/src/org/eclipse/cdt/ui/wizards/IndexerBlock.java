package org.eclipse.cdt.ui.wizards;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.index.IndexModel;
import org.eclipse.cdt.utils.ui.swt.IValidation;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class IndexerBlock implements IWizardTab {
	private Button indexerSwitch;
	IProject project;
	IValidation page;

	public IndexerBlock(IValidation valid, IProject p) {
		page = valid;
		project = p;
	}
	
	/**
	 * @see org.eclipse.cdt.ui.wizards.IWizardTab#getControl(Composite)
	 */
	public Composite getControl(Composite parent) {
		Composite composite= new Composite(parent, SWT.NONE);
		GridLayout grid = new GridLayout();
		grid.numColumns = 1;
		composite.setLayout(grid);

		IndexModel indexer = CCorePlugin.getDefault().getIndexModel();			

		indexerSwitch = new Button(composite, SWT.CHECK | SWT.RIGHT);
		indexerSwitch.setAlignment(SWT.LEFT);
		indexerSwitch.setText("Enable indexing service for this project");
		indexerSwitch.setSelection(indexer.isEnabled(project));
		return composite;
	}

	/**
	 * @see org.eclipse.cdt.ui.wizards.IWizardTab#doRun(IProject, IProgressMonitor)
	 */
	public void doRun(IProject project, IProgressMonitor monitor) {
		IndexModel indexer = CCorePlugin.getDefault().getIndexModel();			
		indexer.setEnabled(project, indexerSwitch.getSelection());
	}


	/**
	 * @see org.eclipse.cdt.ui.wizards.IWizardTab#getImage()
	 */
	public Image getImage() {
		return null;
	}

	/**
	 * @see org.eclipse.cdt.ui.wizards.IWizardTab#getLabel()
	 */
	public String getLabel() {
		return "Indexer";
	}

	/**
	 * @see org.eclipse.cdt.ui.wizards.IWizardTab#isValid()
	 */
	public boolean isValid() {
		return true;
	}

	/**
	 * @see org.eclipse.cdt.ui.wizards.IWizardTab#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		IndexModel indexer = CCorePlugin.getDefault().getIndexModel();			

		if (indexerSwitch != null) {
			//indexerSwitch.setAlignment(SWT.LEFT);
			//indexerSwitch.setText("Enable indexing service for this project");
			indexerSwitch.setSelection(indexer.isEnabled(project));
		}
	}

}
