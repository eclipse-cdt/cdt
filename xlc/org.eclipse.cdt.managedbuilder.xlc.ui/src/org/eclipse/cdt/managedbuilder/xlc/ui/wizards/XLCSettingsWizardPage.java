/*******************************************************************************
 *  Copyright (c) 2007, 2011 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.xlc.ui.wizards;

import org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPage;
import org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPageManager;
import org.eclipse.cdt.managedbuilder.xlc.ui.Messages;
import org.eclipse.cdt.managedbuilder.xlc.ui.XLCUIPlugin;
import org.eclipse.cdt.managedbuilder.xlc.ui.preferences.PreferenceConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * @author crecoskie
 *
 */
public class XLCSettingsWizardPage extends MBSCustomPage {

	public static final String PAGE_ID = "org.eclipse.cdt.managedbuilder.xlc.ui.XlcSettingsWizardPage"; //$NON-NLS-1$

	private final class BrowseButtonSelectionListener implements
			SelectionListener {
		private final Composite composite;

		private BrowseButtonSelectionListener(Composite composite) {
			this.composite = composite;
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			// meaningless for a button... do nothing

		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			// open a browse dialog
			DirectoryDialog dirDialog = new DirectoryDialog(composite.getShell(), SWT.APPLICATION_MODAL);
			String browsedDirectory = dirDialog.open();
			if (browsedDirectory != null) {
				fDirTextBox.setText(browsedDirectory);
			}
		}
	}

	private Composite fComposite = null;

	private Text fDirTextBox;

	private Combo fVersionCombo;

	/**
	 * @param pageID
	 */
	public XLCSettingsWizardPage(String pageID) {
		super(pageID);
		setDefaultPreferences(pageID);
	}

	/**
	 *
	 */
	public XLCSettingsWizardPage() {
		super(PAGE_ID);
		setDefaultPreferences(PAGE_ID);
	}

	private void setDefaultPreferences(String pageID) {
		String compilerPath = XLCUIPlugin.getDefault().getPreferenceStore().getString(PreferenceConstants.P_XL_COMPILER_ROOT);
		MBSCustomPageManager.addPageProperty(pageID, PreferenceConstants.P_XL_COMPILER_ROOT, compilerPath);
		MBSCustomPageManager.addPageProperty(pageID, PreferenceConstants.P_XLC_COMPILER_VERSION, PreferenceConstants.P_XL_COMPILER_VERSION_8);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPage#isCustomPageComplete()
	 */
	@Override
	protected boolean isCustomPageComplete() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizardPage#getName()
	 */
	@Override
	public String getName() {
		String name = Messages.XLCSettingsWizardPage_0;
		return name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		// create a new composite
		fComposite = new Composite(parent, SWT.NONE);
		fComposite.setBounds(parent.getBounds());
		GridLayout layout = new GridLayout(3, false);
		fComposite.setLayout(layout);
		fComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		// create the first label
		Label label1 = new Label(fComposite, SWT.NONE);
		label1.setText(Messages.XLCSettingsWizardPage_1);
		label1.setVisible(true);

		// create the text box for the path
		GridData dirBoxGridData = new GridData();
		dirBoxGridData.grabExcessHorizontalSpace = true;
		dirBoxGridData.horizontalAlignment = SWT.FILL;
		fDirTextBox = new Text(fComposite, SWT.SINGLE | SWT.BORDER);
		fDirTextBox.setLayoutData(dirBoxGridData);
		fDirTextBox.setVisible(true);

		// set the default compiler location based on preferences
		IPreferenceStore prefStore = XLCUIPlugin.getDefault().getPreferenceStore();
		String compilerPath = prefStore.getString(PreferenceConstants.P_XL_COMPILER_ROOT);
		fDirTextBox.setText(compilerPath);

		// update the page manager with the setting
		MBSCustomPageManager.addPageProperty(pageID, PreferenceConstants.P_XL_COMPILER_ROOT, fDirTextBox.getText());

		fDirTextBox.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				// update the page manager with the setting
				MBSCustomPageManager.addPageProperty(pageID, PreferenceConstants.P_XL_COMPILER_ROOT, fDirTextBox.getText());

			}

		});

		// create the browse button
		//String selectedPath = null;
		GridData buttonData = new GridData();
		buttonData.horizontalAlignment = SWT.RIGHT;
		Button browseButton = new Button(fComposite, SWT.PUSH);
		browseButton.setAlignment(SWT.CENTER);
		browseButton.setText(Messages.XLCSettingsWizardPage_2);
		browseButton.addSelectionListener(new BrowseButtonSelectionListener(fComposite)
		);

		browseButton.setVisible(true);

		// create the second label
		Label label2 = new Label(fComposite, SWT.NONE);
		label2.setText(Messages.XLCSettingsWizardPage_3);

		label2.setVisible(true);

		// create the version dropdown
		GridData comboData = new GridData();
		comboData.grabExcessHorizontalSpace = true;
		comboData.horizontalAlignment = SWT.FILL;

		fVersionCombo = new Combo(fComposite, SWT.READ_ONLY);
		fVersionCombo.setLayoutData(comboData);

		// populate the combo
		fVersionCombo.add(PreferenceConstants.P_XL_COMPILER_VERSION_8_NAME);
		fVersionCombo.add(PreferenceConstants.P_XL_COMPILER_VERSION_9_NAME);
		fVersionCombo.add(PreferenceConstants.P_XL_COMPILER_VERSION_10_NAME);
		fVersionCombo.add(PreferenceConstants.P_XL_COMPILER_VERSION_11_NAME);

		// set the default based on the workbench preference
		String compilerVersion = prefStore.getString(PreferenceConstants.P_XLC_COMPILER_VERSION);
		fVersionCombo.setText(PreferenceConstants.getVersionLabel(compilerVersion));

		// update the page manager with the setting
		MBSCustomPageManager.addPageProperty(pageID, PreferenceConstants.P_XLC_COMPILER_VERSION, PreferenceConstants.getVersion(fVersionCombo.getText()));

		fVersionCombo.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				// update the page manager with the setting
				MBSCustomPageManager.addPageProperty(pageID, PreferenceConstants.P_XLC_COMPILER_VERSION, PreferenceConstants.getVersion(fVersionCombo.getText()));

			}

		});

		fVersionCombo.setVisible(true);

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#dispose()
	 */
	@Override
	public void dispose() {
		fComposite.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#getControl()
	 */
	@Override
	public Control getControl() {
		return fComposite;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#getDescription()
	 */
	@Override
	public String getDescription() {
		return Messages.XLCSettingsWizardPage_4;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#getErrorMessage()
	 */
	@Override
	public String getErrorMessage() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#getImage()
	 */
	@Override
	public Image getImage() {
		return wizard.getDefaultPageImage();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#getMessage()
	 */
	@Override
	public String getMessage() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#getTitle()
	 */
	@Override
	public String getTitle() {
		return Messages.XLCSettingsWizardPage_5;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#performHelp()
	 */
	@Override
	public void performHelp() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#setDescription(java.lang.String)
	 */
	@Override
	public void setDescription(String description) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#setImageDescriptor(org.eclipse.jface.resource.ImageDescriptor)
	 */
	@Override
	public void setImageDescriptor(ImageDescriptor image) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#setTitle(java.lang.String)
	 */
	@Override
	public void setTitle(String title) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#setVisible(boolean)
	 */
	@Override
	public void setVisible(boolean visible) {
		fComposite.setVisible(visible);
	}

}
