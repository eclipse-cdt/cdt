/*******************************************************************************
 * Copyright (c) 2001 Rational Software Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     Rational Software - initial implementation
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.wizards.dialogfields;

import java.io.File;

import org.eclipse.cdt.internal.ui.wizards.NewWizardMessages;
import org.eclipse.core.resources.IPathVariableManager;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.ide.dialogs.PathVariableSelectionDialog;

/**
 */
public class LinkToFileGroup extends StringButtonDialogField {
	private String fText;
	protected Listener listener;
	private String initialLinkTarget;
	private int type;
	protected boolean createLink = false;

	// used to compute layout sizes
	//private FontMetrics fontMetrics;

	// widgets
	//private Composite groupComposite;
	protected Text linkTargetField;
	protected Button linkButton;
	protected Button browseButton;
	protected Button variablesButton;
	private Label resolvedPathLabelText;
	private Label resolvedPathLabelData;
	
	public LinkToFileGroup(IStringButtonAdapter adapter, Listener listener) {
		super(adapter);
		this.listener = listener;
		this.type = IResource.FILE;
	}
	
	public Control[] doFillIntoGrid(Composite parent, int nColumns) {
		assertEnoughColumns(nColumns);
		//initializeDialogUnits(parent);
		
		Label label= getLabelControl(parent);
		label.setLayoutData(gridDataForLabel(1));
			
		getLinkCheckButtonControl(parent);		

		Text text= getTextControl(parent);
		text.setLayoutData(gridDataForText(1));

		Button browseButton = getBrowseButtonControl(parent);		
		browseButton.setLayoutData(gridDataForButton(browseButton, 1));

		Button variablesButton = getVariablesButtonControl(parent);		
		variablesButton.setLayoutData(gridDataForButton(variablesButton, 1));
		
		DialogField.createEmptySpace(parent);

		resolvedPathLabelText = new Label(parent, SWT.SINGLE);
		resolvedPathLabelText.setText(NewWizardMessages.getString("CreateLinkedResourceGroup.resolvedPathLabel")); //$NON-NLS-1$
		resolvedPathLabelText.setVisible(true);		
		
		resolvedPathLabelData = new Label(parent, SWT.SINGLE);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 3;
		resolvedPathLabelData.setLayoutData(data);
		resolvedPathLabelData.setVisible(true);
		
		return null;
	}
	
	/*
	 * @see DialogField#getNumberOfControls
	 */		
	public int getNumberOfControls() {
		return 5;	
	}
	
	public Button getLinkCheckButtonControl(Composite parent){
		if(linkButton == null){
			linkButton = new Button(parent, SWT.CHECK);
			linkButton.setText(NewWizardMessages.getString("NewClassWizardPage.files.linkFileButton")); //$NON-NLS-1$
			linkButton.setSelection(createLink);
			linkButton.setFont(parent.getFont());
			SelectionListener selectionListener = new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					createLink = linkButton.getSelection();
					browseButton.setEnabled(createLink);
					variablesButton.setEnabled(createLink);
					linkTargetField.setEnabled(createLink);
					if (listener != null)
						listener.handleEvent(new Event());
				}
			};
			linkButton.addSelectionListener(selectionListener);
		}
		return linkButton;
	}
	
	public String getText() {
		return linkTargetField.getText();
	}
	
	public void setText(String text) {
		fText= text;
		if (isOkToUse(linkTargetField)) {
			linkTargetField.setText(text);
		} else {
			dialogFieldChanged();
		}	
	}
	
	public Text getTextControl(Composite parent){
		if(linkTargetField == null){
			assertCompositeNotNull(parent);
			linkTargetField = new Text(parent, SWT.BORDER);
			linkTargetField.setFont(parent.getFont());
			linkTargetField.setEnabled(createLink);
			linkTargetField.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					resolveVariable();
					if (listener != null)
						listener.handleEvent(new Event());
				}
			});
			if (initialLinkTarget != null)
				linkTargetField.setText(initialLinkTarget);
		}
		return 	linkTargetField;
	}
	
	public static GridData gridDataForText(int span){
		GridData data = new GridData();
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		data.horizontalAlignment= GridData.FILL;
		data.grabExcessHorizontalSpace= false;
		data.horizontalSpan= span;		
		return data;
	}
	
	public Button getBrowseButtonControl(Composite parent){
		// browse button
		if(browseButton == null){
			assertCompositeNotNull(parent);
			browseButton = new Button(parent, SWT.PUSH);
			//setButtonLayoutData(browseButton);
			browseButton.setFont(parent.getFont());
			browseButton.setText(NewWizardMessages.getString("CreateLinkedResourceGroup.browseButton")); //$NON-NLS-1$
			browseButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					handleLinkTargetBrowseButtonPressed();
				}
			});
			browseButton.setEnabled(createLink);
		}
		return browseButton;
	}

	public Button getVariablesButtonControl(Composite parent){
		// variables button
		if(variablesButton == null){
			assertCompositeNotNull(parent);
			variablesButton = new Button(parent, SWT.PUSH);
			//setButtonLayoutData(variablesButton);
			variablesButton.setFont(parent.getFont());
			variablesButton.setText(NewWizardMessages.getString("CreateLinkedResourceGroup.variablesButton")); //$NON-NLS-1$
			variablesButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					handleVariablesButtonPressed();
				}
			});
			variablesButton.setEnabled(createLink);
		}
		return variablesButton;
	}
	/**
	 * Returns a new status object with the given severity and message.
	 * 
	 * @return a new status object with the given severity and message.
	 */
	private IStatus createStatus(int severity, String message) {
		return new Status(
			severity,
			WorkbenchPlugin.getDefault().getBundle().getSymbolicName(),
			severity,
			message,	
			null);
	}	
	/**
	 * Returns the link target location entered by the user. 
	 *
	 * @return the link target location entered by the user. null if the user
	 * 	chose not to create a link.
	 */
	public String getLinkTarget() {
		if (createLink && linkTargetField != null && linkTargetField.isDisposed() == false)
			return linkTargetField.getText();

		return null;
	}

	public String getResolvedPath() {
		if (createLink && resolvedPathLabelData != null && resolvedPathLabelData.isDisposed() == false)
			return resolvedPathLabelData.getText();

		return null;
	}

	/**
	 * Opens a file or directory browser depending on the link type.
	 */
	protected void handleLinkTargetBrowseButtonPressed() {
		String linkTargetName = linkTargetField.getText();
		File file = null;
		String selection = null;
	
		if ("".equals(linkTargetName) == false) {	//$NON-NLS-1$
			file = new File(linkTargetName);
			if (file.exists() == false)
				file = null;
		}
		if (type == IResource.FILE) {
			FileDialog dialog = new FileDialog(linkTargetField.getShell());
			dialog.setText(NewWizardMessages.getString("CreateLinkedResourceGroup.open")); //$NON-NLS-1$
			if (file != null) {
				if (file.isFile())
					dialog.setFileName(linkTargetName);
				else
					dialog.setFilterPath(linkTargetName);
			}
			selection = dialog.open();		
		}
		else {
			DirectoryDialog dialog = new DirectoryDialog(linkTargetField.getShell());
			dialog.setText(NewWizardMessages.getString("CreateLinkedResourceGroup.open")); //$NON-NLS-1$
			if (file != null) {
				if (file.isFile())
					linkTargetName = file.getParent();
				if (linkTargetName != null)
					dialog.setFilterPath(linkTargetName);
			}
			dialog.setMessage(WorkbenchMessages.getString("CreateLinkedResourceGroup.targetSelectionLabel")); //$NON-NLS-1$
			selection = dialog.open();
		}					
		if (selection != null)
			linkTargetField.setText(selection);
	}
	/**
	 * Opens a path variable selection dialog
	 */
	protected void handleVariablesButtonPressed() {
		int variableTypes = IResource.FOLDER;
	
		// allow selecting file and folder variables when creating a 
		// linked file
		if (type == IResource.FILE)
			variableTypes |= IResource.FILE;

		PathVariableSelectionDialog dialog = 
			new PathVariableSelectionDialog(linkTargetField.getShell(), variableTypes);
		if (dialog.open() == IDialogConstants.OK_ID) {
			String[] variableNames = (String[]) dialog.getResult();			
			if (variableNames != null && variableNames.length == 1)
				linkTargetField.setText(variableNames[0]);
		}
	}
	/**
	 * Tries to resolve the value entered in the link target field as 
	 * a variable, if the value is a relative path.
	 * Displays the resolved value if the entered value is a variable.
	 */
	protected void resolveVariable() {
		if(!linkTargetField.isEnabled())
			return;
			
		IPathVariableManager pathVariableManager = ResourcesPlugin.getWorkspace().getPathVariableManager();
		IPath path = new Path(linkTargetField.getText());
		IPath resolvedPath = pathVariableManager.resolvePath(path);
	
		/* (path.equals(resolvedPath)) {
			resolvedPathLabelText.setVisible(false);
			resolvedPathLabelData.setVisible(false);
		} else {
			resolvedPathLabelText.setVisible(true);
			resolvedPathLabelData.setVisible(true);
		}
		*/
		resolvedPathLabelData.setText(resolvedPath.toOSString());
	}
	/**
	 * Sets the value of the link target field
	 * 
	 * @param target the value of the link target field
	 */
	public void setLinkTarget(String target) {
		initialLinkTarget = target;
		if (linkTargetField != null && linkTargetField.isDisposed() == false)
			linkTargetField.setText(target);
	}
	/**
	 * Validates the type of the given file against the link type specified
	 * in the constructor.
	 * 
	 * @param linkTargetFile file to validate
	 * @return IStatus indicating the validation result. IStatus.OK if the 
	 * 	given file is valid.
	 */
	private IStatus validateFileType(File linkTargetFile) {
		if (type == IResource.FILE && linkTargetFile.isFile() == false) {
			return createStatus(
				IStatus.ERROR,
				WorkbenchMessages.getString("CreateLinkedResourceGroup.linkTargetNotFile"));	//$NON-NLS-1$
		} else if (type == IResource.FOLDER && linkTargetFile.isDirectory() == false) {
			return createStatus(
				IStatus.ERROR,
				WorkbenchMessages.getString("CreateLinkedResourceGroup.linkTargetNotFolder"));	//$NON-NLS-1$
		}
		return createStatus(IStatus.OK, ""); //$NON-NLS-1$
	}
	/**
	 * Validates this page's controls.
	 *
	 * @return IStatus indicating the validation result. IStatus.OK if the 
	 * 	specified link target is valid given the linkHandle.
	 */
	public IStatus validateLinkLocation(IResource linkHandle) {
		if (linkTargetField == null || linkTargetField.isDisposed())
			return createStatus(IStatus.OK, "");	//$NON-NLS-1$
	
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		String linkTargetName = linkTargetField.getText();
		IPath path = new Path(linkTargetName);
	
		if (createLink == false)
			return createStatus(IStatus.OK, ""); //$NON-NLS-1$

		IStatus locationStatus = workspace.validateLinkLocation(linkHandle,	path);
		if (locationStatus.getSeverity() == IStatus.ERROR)
			return locationStatus;

		// use the resolved link target name
		linkTargetName = resolvedPathLabelData.getText();
		path = new Path(linkTargetName);
		File linkTargetFile = new Path(linkTargetName).toFile();
		if (linkTargetFile.exists()) {
			IStatus fileTypeStatus = validateFileType(linkTargetFile);
			if (fileTypeStatus.isOK() == false)
				return fileTypeStatus;
		} else if (locationStatus.getSeverity() == IStatus.OK) {
			// locationStatus takes precedence over missing location warning.
			return createStatus(
				IStatus.WARNING,
				WorkbenchMessages.getString("CreateLinkedResourceGroup.linkTargetNonExistent"));	//$NON-NLS-1$	
		}
		return locationStatus;
	}
	
	public boolean linkCreated (){
		return createLink;
	}
}
