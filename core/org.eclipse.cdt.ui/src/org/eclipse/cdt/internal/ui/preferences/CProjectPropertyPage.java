package org.eclipse.cdt.internal.ui.preferences;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.dialogs.IStatusChangeListener;
import org.eclipse.cdt.internal.ui.dialogs.StatusTool;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.wizards.IndexerBlock;
import org.eclipse.cdt.ui.wizards.SettingsBlock;
import org.eclipse.cdt.utils.ui.controls.TabFolderLayout;
import org.eclipse.cdt.utils.ui.swt.IValidation;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.help.WorkbenchHelp;

public class CProjectPropertyPage extends PropertyPage implements IStatusChangeListener, IValidation {
	
	private static final String MSG_NOCPROJECT= "CProjectPropertyPage.nocproject";
	private static final String MSG_CLOSEDPROJECT= "CProjectPropertyPage.closedproject";
	
	private TabFolder folder;
	SettingsBlock settingsBlock;
	IndexerBlock indexerBlock;

	protected Control createContents(Composite parent) {
		Composite composite= new Composite(parent, SWT.NONE);
		composite.setLayout(new FillLayout());
		
		IProject project= getProject();
		if (!project.isOpen()) {
			contentForClosedProject(composite);	
		} else {
			contentForCProject(composite);
		}
			
		return composite;
	}
	
	private void contentForCProject(Composite parent) {
		folder = new TabFolder(parent, SWT.NONE);
		folder.setLayout(new TabFolderLayout());

		settingsBlock = new SettingsBlock(this, getProject());
		TabItem item2 = new TabItem(folder, SWT.NONE);
		item2.setText(settingsBlock.getLabel());
		Image img2 = settingsBlock.getImage();
		if (img2 != null)
			item2.setImage(img2);
		item2.setData(settingsBlock);
		item2.setControl(settingsBlock.getControl(folder));

		indexerBlock = new IndexerBlock(this, getProject());
		TabItem item3 = new TabItem(folder, SWT.NONE);
		item3.setText(indexerBlock.getLabel());
		Image img3 = indexerBlock.getImage();
		if (img3 != null)
			item3.setImage(img3);
		item3.setData(indexerBlock);
		item3.setControl(indexerBlock.getControl(folder));

		WorkbenchHelp.setHelp(parent, ICHelpContextIds.PROJECT_PROPERTY_PAGE);	
	}
	
	private void contentForClosedProject(Composite parent) {
		Label label= new Label(parent, SWT.LEFT);
		label.setText(CUIPlugin.getResourceString(MSG_CLOSEDPROJECT));
		label.setFont(parent.getFont());
		
		noDefaultAndApplyButton();
	}	

	public void setComplete(boolean complete) {
		boolean ok = true;
		
		if (ok && settingsBlock != null) {
			ok = settingsBlock.isValid();
		}
		if (ok && indexerBlock != null) {
			ok = indexerBlock.isValid();
		}
		setValid(ok);
	}

	/**
	 * @see PreferencePage#performOk
	 */	
	public boolean performOk() {
		if (settingsBlock != null)
			settingsBlock.doRun(getProject(), null);
		if (indexerBlock != null) {
			indexerBlock.doRun(getProject(), null);
		}
		return true;
	}
		
	private IProject getProject() {
		Object element= getElement();
		if (element instanceof IProject) {
			return (IProject)element;
		}
		return null;
	}
	
	/**
	 * @see DialogPage#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible && folder != null) {
			settingsBlock.setVisible(visible);
			indexerBlock.setVisible(visible);
			folder.setFocus();
		}
	}	
	
	/**
	 * @see IStatusChangeListener#statusChanged(IStatus)
	 */
	public void statusChanged(IStatus status) {
		setValid(!status.matches(IStatus.ERROR));
		StatusTool.applyToStatusLine(this, status);
	}		
	
}
