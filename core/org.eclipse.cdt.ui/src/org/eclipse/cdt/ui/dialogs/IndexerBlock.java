package org.eclipse.cdt.ui.dialogs;
/***********************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * QNX Software Systems - Initial API and implementation
***********************************************************************/

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.index.IndexModel;
import org.eclipse.cdt.internal.core.search.indexing.IndexManager;
import org.eclipse.cdt.internal.core.sourcedependency.DependencyManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class IndexerBlock extends AbstractCOptionPage {
	private Button indexerSwitch;
	private Button indexerSwitch2;
	private Button dTreeSwitch;

	public IndexerBlock() {
		super("Indexer");
		setDescription("Project Indexer option");
	}

	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout grid = new GridLayout();
		grid.numColumns = 1;
		composite.setLayout(grid);

		indexerSwitch = new Button(composite, SWT.CHECK | SWT.RIGHT);
		indexerSwitch.setAlignment(SWT.LEFT);
		indexerSwitch.setText("Enable CTAGS indexing service for this project");
	
		indexerSwitch2 = new Button(composite, SWT.CHECK | SWT.RIGHT);
		indexerSwitch2.setAlignment(SWT.LEFT);
		indexerSwitch2.setText("Enable NEW indexing service for this project");
	
		dTreeSwitch = new Button(composite, SWT.CHECK | SWT.RIGHT);
		dTreeSwitch.setAlignment(SWT.LEFT);
		dTreeSwitch.setText("Enable dependency tree service for this project");

		IProject project = getContainer().getProject();
		if (project != null) {
			IndexModel indexer = CCorePlugin.getDefault().getIndexModel();
			IndexManager newIndexer = CCorePlugin.getDefault().getCoreModel().getIndexManager();
			if (indexerSwitch != null) {
				//indexerSwitch.setAlignment(SWT.LEFT);
				//indexerSwitch.setText("Enable indexing service for this project");
				indexerSwitch.setSelection(indexer.isEnabled(project));
			}

			if (indexerSwitch2 != null) {
				indexerSwitch2.setSelection(newIndexer.isEnabled(project));
			}

			DependencyManager depManager = CCorePlugin.getDefault().getCoreModel().getDependencyManager();

			if (dTreeSwitch != null) {
				dTreeSwitch.setSelection(depManager.isEnabled(project));
			}
		}
		setControl(composite);
	}

	public void performApply(IProgressMonitor monitor) throws CoreException {
		IProject project = getContainer().getProject();
		if (project != null) {
			IndexModel indexer = CCorePlugin.getDefault().getIndexModel();
			indexer.setEnabled(project, indexerSwitch.getSelection());

			IndexManager newIndexer = CCorePlugin.getDefault().getCoreModel().getIndexManager();
			newIndexer.setEnabled(project, indexerSwitch2.getSelection());

			DependencyManager depManager = CCorePlugin.getDefault().getCoreModel().getDependencyManager();
			depManager.setEnabled(project, dTreeSwitch.getSelection());
		}
	}

	public void performDefaults() {
		if (getContainer().getProject() != null) {
			indexerSwitch.setSelection(false);
			indexerSwitch2.setSelection(false);
			dTreeSwitch.setSelection(false);
		}
	}
}
