package org.eclipse.cdt.make.ui.wizards;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.cdt.make.internal.ui.MakeProjectOptionBlock;
import org.eclipse.cdt.ui.dialogs.ICOptionContainer;
import org.eclipse.cdt.ui.dialogs.IndexerBlock;
import org.eclipse.cdt.ui.dialogs.ReferenceBlock;
import org.eclipse.cdt.ui.dialogs.TabFolderOptionBlock;
import org.eclipse.cdt.ui.wizards.NewCProjectWizard;
import org.eclipse.cdt.ui.wizards.NewCProjectWizardOptionPage;
import org.eclipse.core.resources.IProject;

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
		return new MakeWizardOptionBlock(this);
	}

	public IProject getProject() {
		return ((NewCProjectWizard)getWizard()).getNewProject();
	}
}
