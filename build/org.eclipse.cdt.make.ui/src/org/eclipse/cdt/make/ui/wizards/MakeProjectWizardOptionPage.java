package org.eclipse.cdt.make.ui.wizards;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.cdt.make.internal.ui.MakeProjectOptionBlock;
import org.eclipse.cdt.ui.TabFolderOptionBlock;
import org.eclipse.cdt.ui.ICOptionContainer;
import org.eclipse.cdt.ui.ReferenceBlock;
import org.eclipse.cdt.ui.wizards.CProjectWizard;
import org.eclipse.cdt.ui.wizards.CProjectWizardOptionPage;
import org.eclipse.core.resources.IProject;
import org.eclipse.swt.widgets.TabItem;

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
public class MakeProjectWizardOptionPage extends CProjectWizardOptionPage {

	public class MakeWizardOptionBlock extends MakeProjectOptionBlock {

		public MakeWizardOptionBlock(ICOptionContainer parent) {
			super(parent);
		}

		protected TabItem addTabs() {
			TabItem item = addTab(new ReferenceBlock());
			super.addTabs();
			return item;
		}
	}

	public MakeProjectWizardOptionPage(String title, String description) {
		super("MakeProjectSettingsPage");
		setTitle(title);
		setDescription(description);
	}

	protected TabFolderOptionBlock createOptionBlock() {
		return new MakeWizardOptionBlock(this);
	}

	public IProject getProject() {
		return ((CProjectWizard)getWizard()).getNewProject();
	}
}
