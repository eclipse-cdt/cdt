package org.eclipse.cdt.managedbuilder.ui.wizards;

/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
 * **********************************************************************/

import org.eclipse.cdt.managedbuilder.internal.ui.ManagedProjectOptionBlock;
import org.eclipse.cdt.ui.dialogs.ICOptionContainer;
import org.eclipse.cdt.ui.dialogs.IndexerBlock;
import org.eclipse.cdt.ui.dialogs.ReferenceBlock;
import org.eclipse.cdt.ui.dialogs.TabFolderOptionBlock;
import org.eclipse.cdt.ui.wizards.NewCProjectWizard;
import org.eclipse.cdt.ui.wizards.NewCProjectWizardOptionPage;
import org.eclipse.core.resources.IProject;

public class NewManagedProjectOptionPage extends NewCProjectWizardOptionPage {

	public class ManagedWizardOptionBlock extends ManagedProjectOptionBlock {
		IndexerBlock indexBlock;
		
		public ManagedWizardOptionBlock(ICOptionContainer parent) {
			super(parent);
		}

		protected void addTabs() {
			addTab(new ReferenceBlock());
			addTab(indexBlock = new IndexerBlock());
		}
	}

	/**
	 * @param pageName
	 */
	public NewManagedProjectOptionPage(String pageName) {
		super(pageName);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.wizards.NewCProjectWizardOptionPage#createOptionBlock()
	 */
	protected TabFolderOptionBlock createOptionBlock() {
		return new ManagedWizardOptionBlock(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.dialogs.ICOptionContainer#getProject()
	 */
	public IProject getProject() {
		return ((NewCProjectWizard)getWizard()).getNewProject();
	}

}
