package org.eclipse.cdt.make.ui.wizards;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.internal.ui.MakeProjectOptionBlock;
import org.eclipse.cdt.ui.dialogs.ICOptionContainer;
import org.eclipse.cdt.ui.dialogs.IndexerBlock;
import org.eclipse.cdt.ui.dialogs.ReferenceBlock;
import org.eclipse.cdt.ui.dialogs.TabFolderOptionBlock;
import org.eclipse.cdt.ui.wizards.NewCProjectWizard;
import org.eclipse.cdt.ui.wizards.NewCProjectWizardOptionPage;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Preferences;

/**
 * Standard main page for a wizard that is creates a project resource.
 * <p>
 * This page may be used by clients as-is; it may be also be subclassed to suit.
 * </p>
 * <p>
 * Example useage:
 * <pre>
 * mainPage = new CProjectWizardPage("basicCProjectPage");
 * mainPage.setTitle("Project");
 * mainPage.setDescription("Create a new project resource.");
 * </pre>
 * </p>
 */
public class MakeProjectWizardOptionPage extends NewCProjectWizardOptionPage {
	MakeWizardOptionBlock makeWizardBlock; 
	
	public class MakeWizardOptionBlock extends MakeProjectOptionBlock {
		IndexerBlock indexBlock;
		
		public MakeWizardOptionBlock(ICOptionContainer parent) {
			super(parent);
		}

		protected void addTabs() {
			addTab(new ReferenceBlock());
			super.addTabs();
			addTab(indexBlock = new IndexerBlock()); 
		}
	}

	public MakeProjectWizardOptionPage(String title, String description) {
		super("MakeProjectSettingsPage"); //$NON-NLS-1$
		setTitle(title);
		setDescription(description);
	}

	protected TabFolderOptionBlock createOptionBlock() {
		return (makeWizardBlock  = new MakeWizardOptionBlock(this));
	}

	public IProject getProject() {
		return ((NewCProjectWizard)getWizard()).getNewProject();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.dialogs.ICOptionContainer#getPreference()
	 */
	public Preferences getPreferences() {
		return MakeCorePlugin.getDefault().getPluginPreferences();
	}
	
	public boolean isIndexerEnabled(){
	  return	makeWizardBlock.indexBlock.isIndexEnabled();
	}

}
