package org.eclipse.cdt.internal.ui.preferences;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;

import org.eclipse.jface.dialogs.ErrorDialog;

import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.help.DialogPageContextComputer;
import org.eclipse.ui.help.WorkbenchHelp;

import org.eclipse.cdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.cdt.internal.ui.dialogs.StatusTool;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IStringButtonAdapter;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.StringButtonDialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.StringDialogField;
import org.eclipse.cdt.internal.ui.wizards.swt.MGridLayout;

import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.internal.ui.CPlugin;
import org.eclipse.cdt.internal.ui.ICHelpContextIds;
public class CLaunchingPropertyPage extends PropertyPage {
	
	private static final String PAGE_NAME= "CLaunchingPropertyPage";
	private static final String ARGUMENTS= PAGE_NAME + ".arguments";
	private static final String WORKINGDIR= PAGE_NAME + ".workingdir";
	
	private static final String NO_CPROJECT= PAGE_NAME + ".nocproject.label";
	
	private static final String ERROR_WORKINGDIR_NOTEXISTS= PAGE_NAME + ".error.WorkingDirNotExists";
	
	private StringDialogField fArgumentField;
	private StringButtonDialogField fWorkingDirField;
	
	private StatusInfo fWorkingDirStatus;
	
	private QualifiedName fArgumentsPropertyName;
	private QualifiedName fWorkingDirPropertyName;
	
	private Shell fShell;
	
	public CLaunchingPropertyPage() {
		LaunchingDialogFieldsAdapter adapter= new LaunchingDialogFieldsAdapter();
				
		fArgumentField= new StringDialogField();
		fArgumentField.setLabelText(CPlugin.getResourceString(ARGUMENTS + ".label"));
		fArgumentField.setDialogFieldListener(adapter);
		
		fWorkingDirField= new StringButtonDialogField(adapter);		
		fWorkingDirField.setLabelText(CPlugin.getResourceString(WORKINGDIR + ".label"));
		fWorkingDirField.setButtonLabel(CPlugin.getResourceString(WORKINGDIR + ".browse"));
		fWorkingDirField.setDialogFieldListener(adapter);
		
		fWorkingDirStatus= new StatusInfo();
		
		fArgumentsPropertyName= new QualifiedName(CPlugin.PLUGIN_ID, "arguments");
		fWorkingDirPropertyName= new QualifiedName(CPlugin.PLUGIN_ID, "workingdir");
	}

	/**
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {
		Composite composite= new Composite(parent, SWT.NONE);
		fShell= parent.getShell();
			
		MGridLayout layout= new MGridLayout();
		layout.marginWidth= 0;
		layout.marginHeight= 0;	
		layout.minimumWidth= 400;
		layout.minimumHeight= 350;
		layout.numColumns= 3;		
		composite.setLayout(layout);
		
		boolean isCProject= false;
		try {
			IFile file= getInputFile();
			isCProject= (file.getProject().hasNature(CProjectNature.C_NATURE_ID));
		} catch (CoreException e) {
			CPlugin.log(e);
		}
		
		if (isCProject) {
			fArgumentField.doFillIntoGrid(composite, 3);
			fWorkingDirField.doFillIntoGrid(composite, 3);
			initialize();
		} else {
			DialogField labelField= new DialogField();
			labelField.setLabelText(CPlugin.getResourceString(NO_CPROJECT));
			labelField.doFillIntoGrid(composite, 3);
		}
		WorkbenchHelp.setHelp(parent, new DialogPageContextComputer(this, ICHelpContextIds.LAUNCH_PROPERTY_PAGE));

		return composite;
	}
		
	
	private void initialize() {
		IFile file= getInputFile();
		if (file != null) {
			try {
				String arguments= file.getPersistentProperty(fArgumentsPropertyName);
				if (arguments != null) {
					fArgumentField.setText(arguments);
				}
				
				String workingdir= file.getPersistentProperty(fWorkingDirPropertyName);
				if (workingdir != null) {
					fWorkingDirField.setText(workingdir);
				} else {
					fWorkingDirField.setText(file.getParent().getLocation().toOSString());
				}
			} catch (CoreException e) {
				CPlugin.log(e.getStatus());
			}
		}	
	}
	
	/**
	 * @see PreferencePage#performOk
	 */	
	public boolean performOk() {
		IFile file= getInputFile();
		if (file != null) {
			try {
				file.setPersistentProperty(fArgumentsPropertyName, fArgumentField.getText());
				file.setPersistentProperty(fWorkingDirPropertyName, fWorkingDirField.getText());
			} catch (CoreException e) {
				ErrorDialog.openError(fShell, "Error", null, e.getStatus());
				CPlugin.log(e.getStatus());
				return false;
			}
		}
		return true;
	}
				
	/**
	 * @see PreferencePage#doDefaults
	 */
	protected void performDefaults() {
		initialize();
		super.performDefaults();
	}
	
	private class LaunchingDialogFieldsAdapter implements IDialogFieldListener, IStringButtonAdapter {
		
		public void changeControlPressed(DialogField field) {
			String oldValue= fWorkingDirField.getText();
			String newValue= chooseFolder(oldValue);
			if (newValue != null) {
				fWorkingDirField.setText(newValue);
			}
		}
		
		public void dialogFieldChanged(DialogField field) {
			doFieldChanged(field);
		}
	}

	private void doFieldChanged(DialogField field) {
		if (field == fWorkingDirField) {
			updateWorkingDirStatus();
		}
		projectStatusChanged(fWorkingDirStatus);
	}
		
		
	private void updateWorkingDirStatus() {
		String str= fWorkingDirField.getText();
		if (!"".equals(str)) {
			IPath path= new Path(str);
			if (!path.toFile().isDirectory()) {
				fWorkingDirStatus.setError(CPlugin.getResourceString(ERROR_WORKINGDIR_NOTEXISTS));
				return;
			}
		}
		fWorkingDirStatus.setOK();
	}	
	
	private IFile getInputFile() {
		return (IFile)getElement();
	}
			
	private String chooseFolder(String initPath) {
		DirectoryDialog dialog= new DirectoryDialog(fShell, 0);
		dialog.setFilterPath(initPath);
		String res= dialog.open();
		return res;
	}
	
	public void projectStatusChanged(IStatus status) {
		setValid(!status.matches(IStatus.ERROR));
		StatusTool.applyToStatusLine(this, status);		
	}		
	
	/**
	 * @see DialogPage#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible && fShell != null) {
			fArgumentField.postSetFocusOnDialogField(fShell.getDisplay());
		}
	}
}
