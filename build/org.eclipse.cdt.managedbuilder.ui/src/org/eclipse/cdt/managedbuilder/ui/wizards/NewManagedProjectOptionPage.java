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

import org.eclipse.cdt.managedbuilder.internal.ui.ErrorParserBlock;
import org.eclipse.cdt.managedbuilder.internal.ui.ManagedBuilderUIPlugin;
import org.eclipse.cdt.managedbuilder.internal.ui.ManagedProjectOptionBlock;
import org.eclipse.cdt.ui.dialogs.IndexerBlock;
import org.eclipse.cdt.ui.dialogs.ReferenceBlock;
import org.eclipse.cdt.ui.dialogs.TabFolderOptionBlock;
import org.eclipse.cdt.ui.wizards.NewCProjectWizard;
import org.eclipse.cdt.ui.wizards.NewCProjectWizardOptionPage;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Preferences;

public class NewManagedProjectOptionPage extends NewCProjectWizardOptionPage {
	

	public class ManagedWizardOptionBlock extends ManagedProjectOptionBlock {
		
		NewManagedProjectOptionPage parent;
		ErrorParserBlock errorParsers;
		IndexerBlock indexBlock;
		

		public ManagedWizardOptionBlock(NewManagedProjectOptionPage parentPage) {
			super(parentPage);
			parent = parentPage;
		}
		
		public void updateTargetProperties() {
			//  Update the error parser list
			if (errorParsers != null) {
				errorParsers.updateValues();
			}
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.ui.dialogs.TabFolderOptionBlock#addTabs()
		 */
		protected void addTabs() {
			addTab(new ReferenceBlock());
			errorParsers = new ErrorParserBlock();
			addTab(errorParsers);
			addTab(indexBlock = new IndexerBlock()); 
		}
	}
	
	protected ManagedWizardOptionBlock optionBlock;
	protected NewManagedProjectWizard parentWizard;

	/**
	 * @param pageName
	 */
	public NewManagedProjectOptionPage(String pageName, NewManagedProjectWizard parentWizard) {
		super(pageName);
		this.parentWizard = parentWizard;
		optionBlock = new ManagedWizardOptionBlock(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.wizards.NewCProjectWizardOptionPage#createOptionBlock()
	 */
	protected TabFolderOptionBlock createOptionBlock() {
		return optionBlock;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.dialogs.ICOptionContainer#getProject()
	 */
	public IProject getProject() {
		return ((NewCProjectWizard)getWizard()).getNewProject();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.dialogs.ICOptionContainer#getPreferenceStore()
	 */
	public Preferences getPreferences() {
		return ManagedBuilderUIPlugin.getDefault().getPluginPreferences();
	}
	
	public void updateTargetProperties() {
		//  Update the error parser list
		optionBlock.updateTargetProperties();
	}
	
	
}
