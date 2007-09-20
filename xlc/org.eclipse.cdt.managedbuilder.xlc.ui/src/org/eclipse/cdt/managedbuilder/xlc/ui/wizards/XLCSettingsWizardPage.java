/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
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
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

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

		public void widgetDefaultSelected(SelectionEvent e) {
			// meaningless for a button... do nothing
			
		}

		public void widgetSelected(SelectionEvent e) {
			// open a browse dialog
			DirectoryDialog dirDialog = new DirectoryDialog(composite.getShell(), SWT.APPLICATION_MODAL);
			String browsedDirectory = dirDialog.open();
			fDirTextBox.setText(browsedDirectory);
			
		}
	}
	
	private boolean fVisited = false;
	
	private Composite fComposite = null;

	private Text fDirTextBox;

	private Combo fVersionCombo;
	
	/**
	 * @param pageID
	 */
	public XLCSettingsWizardPage(String pageID) {
		super(pageID);
	}

	/**
	 * 
	 */
	public XLCSettingsWizardPage() {
		super(PAGE_ID);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPage#isCustomPageComplete()
	 */
	protected boolean isCustomPageComplete() {
		// Don't allow the user to finish without visiting the page.
		return fVisited;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizardPage#getName()
	 */
	public String getName() {
		String name = Messages.XLCSettingsWizardPage_0;
		return name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		// create a new composite
		fComposite = new Composite(parent, SWT.NONE);
		fComposite.setBounds(parent.getBounds());
		GridLayout layout = new GridLayout(3, false);
		fComposite.setLayout(layout);
		fComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		
		
		// set the layout data for the first column, which contains labels
		GridData labelGridData = new GridData();
		
		
		// create the first label
		Label label1 = new Label(fComposite, SWT.NONE);
		label1.setText(Messages.XLCSettingsWizardPage_1);
		labelGridData.widthHint = 120;
		label1.setLayoutData(labelGridData);
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
		MBSCustomPageManager.addPageProperty(PAGE_ID, PreferenceConstants.P_XL_COMPILER_ROOT, fDirTextBox.getText());
		
		fDirTextBox.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				// update the page manager with the setting
				MBSCustomPageManager.addPageProperty(PAGE_ID, PreferenceConstants.P_XL_COMPILER_ROOT, fDirTextBox.getText());
				
			}
			
		});
		
		// create the browse button
		String selectedPath = null;
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
		label2.setLayoutData(labelGridData);
		
		label2.setVisible(true);
		
		// create the version dropdown
		GridData comboData = new GridData();
		comboData.horizontalSpan = 2;
		comboData.grabExcessHorizontalSpace = true;
		comboData.horizontalAlignment = SWT.FILL;
		
		fVersionCombo = new Combo(fComposite, SWT.READ_ONLY);
		fVersionCombo.setLayoutData(comboData);
		
		// populate the combo
		fVersionCombo.add(PreferenceConstants.P_XL_COMPILER_VERSION_8_NAME);
		fVersionCombo.add(PreferenceConstants.P_XL_COMPILER_VERSION_9_NAME);
		
		// set the default based on the workbench preference
		String compilerVersion = prefStore.getString(PreferenceConstants.P_XLC_COMPILER_VERSION);
		fVersionCombo.setText(compilerVersion);
		
		// update the page manager with the setting
		MBSCustomPageManager.addPageProperty(PAGE_ID, PreferenceConstants.P_XLC_COMPILER_VERSION, fVersionCombo.getText());
		
		fVersionCombo.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				// update the page manager with the setting
				MBSCustomPageManager.addPageProperty(PAGE_ID, PreferenceConstants.P_XLC_COMPILER_VERSION, fVersionCombo.getText());
				
			}
			
		});
		
		fVersionCombo.setVisible(true);
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#dispose()
	 */
	public void dispose() {
		fComposite.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#getControl()
	 */
	public Control getControl() {
		return fComposite;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#getDescription()
	 */
	public String getDescription() {
		return Messages.XLCSettingsWizardPage_4;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#getErrorMessage()
	 */
	public String getErrorMessage() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#getImage()
	 */
	public Image getImage() {
		return wizard.getDefaultPageImage();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#getMessage()
	 */
	public String getMessage() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#getTitle()
	 */
	public String getTitle() {
		return Messages.XLCSettingsWizardPage_5;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#performHelp()
	 */
	public void performHelp() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#setDescription(java.lang.String)
	 */
	public void setDescription(String description) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#setImageDescriptor(org.eclipse.jface.resource.ImageDescriptor)
	 */
	public void setImageDescriptor(ImageDescriptor image) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#setTitle(java.lang.String)
	 */
	public void setTitle(String title) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		fComposite.setVisible(visible);
		if (visible) {
			fVisited = true;
		}

	}

}
