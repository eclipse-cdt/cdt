package org.eclipse.cdt.ui.wizards;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.internal.ui.CPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;


/**
 */
public abstract class StdMakeProjectWizard extends CProjectWizard {
	
	private static final String OP_ERROR= "StdMakeProjectWizard.op_error";
	private static final String WZ_TITLE= "StdMakeProjectWizard.title";
	private static final String WZ_DESC= "StdMakeProjectWizard.description";
	private static final String SETTINGS_TITLE= "StdMakeWizardSettings.title"; //$NON-NLS-1$
	private static final String SETTINGS_DESC= "StdMakeWizardSettings.description"; //$NON-NLS-1$

	private ReferenceBlock referenceBlock;
	private SettingsBlock settingsBlock;

	public StdMakeProjectWizard() {
		this(CPlugin.getResourceString(WZ_TITLE), CPlugin.getResourceString(WZ_DESC));
	}
	
	public StdMakeProjectWizard(String title, String desc) {
		super(title, desc);
	}

	public void addTabItems(TabFolder folder) {
		fTabFolderPage.setTitle(CPlugin.getResourceString(SETTINGS_TITLE));
		fTabFolderPage.setDescription(CPlugin.getResourceString(SETTINGS_DESC));

		referenceBlock = new ReferenceBlock(getValidation());
		TabItem item = new TabItem(folder, SWT.NONE);
		item.setText(referenceBlock.getLabel());
		Image img = referenceBlock.getImage();
		if (img != null)
			item.setImage(img);
		item.setData(referenceBlock);
		item.setControl(referenceBlock.getControl(folder));
		addTabItem(referenceBlock);

		settingsBlock = new SettingsBlock(getValidation());
		TabItem item2 = new TabItem(folder, SWT.NONE);
		item2.setText(settingsBlock.getLabel());
		Image img2 = settingsBlock.getImage();
		if (img2 != null)
			item2.setImage(img2);
		item2.setData(settingsBlock);
		item2.setControl(settingsBlock.getControl(folder));
		addTabItem(settingsBlock);
	}

	protected void doRunPrologue(IProgressMonitor monitor) {
	}

	protected void doRunEpilogue(IProgressMonitor monitor) {
	}

	protected void doRun(IProgressMonitor monitor) throws CoreException {
        // super.doRun() just creates the project and does not assign a builder to it.
		super.doRun(monitor);
        
        // Modify the project based on what the user has selected
		if (newProject != null) {
			if (monitor == null) {
				monitor = new NullProgressMonitor();
			}
			monitor.beginTask("Standard Make", 3);
			// Update the referenced project if provided.
			if (referenceBlock != null) {
				referenceBlock.doRun(newProject, new SubProgressMonitor(monitor, 1));
			}
			// Update the settings.
			if (settingsBlock != null) {
				settingsBlock.doRun(newProject, new SubProgressMonitor(monitor, 1));
			}
            
            // Set the Default C Builder to make this a StdMakeProject.
            CCorePlugin.getDefault().addDefaultCBuilder(newProject, new SubProgressMonitor(monitor, 1));
		}
	}
	
	public String getProjectID() {
		return CCorePlugin.getDefault().PLUGIN_ID + ".make";
	}

}
