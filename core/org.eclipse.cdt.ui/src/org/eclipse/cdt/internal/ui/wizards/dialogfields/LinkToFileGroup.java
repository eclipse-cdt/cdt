/*******************************************************************************
 * Copyright (c) 2001, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Rational Software - initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.wizards.dialogfields;

import java.io.File;

import org.eclipse.cdt.internal.ui.wizards.NewWizardMessages;
import org.eclipse.cdt.ui.CUIPlugin;
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

/**
 * This class is part of the NewClassWizard.
 * It handles the Link to file part.
 */
public class LinkToFileGroup extends StringButtonDialogField {
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
	private Label resolvedPathLabelText;
	private Label resolvedPathLabelData;
	boolean preventDialogFieldChanged = false;

	public LinkToFileGroup(IStringButtonAdapter adapter, Listener listener) {
		super(adapter);
		this.listener = listener;
		this.type = IResource.FILE;
	}

	@Override
	public Control[] doFillIntoGrid(Composite parent, int nColumns) {
		assertEnoughColumns(nColumns);
		//initializeDialogUnits(parent);

		Label label = getLabelControl(parent);
		label.setLayoutData(gridDataForLabel(1));

		getLinkCheckButtonControl(parent);

		Text text = getTextControl(parent);
		text.setLayoutData(gridDataForText(2));

		Button browseButton = getBrowseButtonControl(parent);
		browseButton.setLayoutData(gridDataForButton(browseButton, 1));

		DialogField.createEmptySpace(parent);

		resolvedPathLabelText = new Label(parent, SWT.SINGLE);
		resolvedPathLabelText.setText(NewWizardMessages.CreateLinkedResourceGroup_resolvedPathLabel);
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
	@Override
	public int getNumberOfControls() {
		return 4;
	}

	public Button getLinkCheckButtonControl(Composite parent) {
		if (linkButton == null) {
			linkButton = new Button(parent, SWT.CHECK);
			linkButton.setText(NewWizardMessages.NewClassWizardPage_files_linkFileButton);
			linkButton.setSelection(createLink);
			linkButton.setFont(parent.getFont());
			SelectionListener selectionListener = new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					createLink = linkButton.getSelection();
					browseButton.setEnabled(createLink);
					linkTargetField.setEnabled(createLink);
					resolveVariable();
					if (listener != null)
						listener.handleEvent(new Event());
					if (!preventDialogFieldChanged)
						dialogFieldChanged();
				}
			};
			linkButton.addSelectionListener(selectionListener);
		}
		return linkButton;
	}

	@Override
	public String getText() {
		return linkTargetField.getText();
	}

	@Override
	public void setText(String text) {
		if (isOkToUse(linkTargetField)) {
			preventDialogFieldChanged = true;
			linkTargetField.setText(text);
			preventDialogFieldChanged = false;
		}
		//		dialogFieldChanged();
	}

	@Override
	public Text getTextControl(Composite parent) {
		if (linkTargetField == null) {
			assertCompositeNotNull(parent);
			linkTargetField = new Text(parent, SWT.BORDER);
			linkTargetField.setFont(parent.getFont());
			linkTargetField.setEnabled(createLink);
			linkTargetField.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					resolveVariable();
					if (listener != null)
						listener.handleEvent(new Event());
					if (!preventDialogFieldChanged)
						dialogFieldChanged();
				}
			});
			if (initialLinkTarget != null)
				linkTargetField.setText(initialLinkTarget);
		}
		return linkTargetField;
	}

	public static GridData gridDataForText(int span) {
		GridData data = new GridData();
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = false;
		data.horizontalSpan = span;
		return data;
	}

	public Button getBrowseButtonControl(Composite parent) {
		// browse button
		if (browseButton == null) {
			assertCompositeNotNull(parent);
			browseButton = new Button(parent, SWT.PUSH);
			//setButtonLayoutData(browseButton);
			browseButton.setFont(parent.getFont());
			browseButton.setText(NewWizardMessages.CreateLinkedResourceGroup_browseButton);
			browseButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent event) {
					handleLinkTargetBrowseButtonPressed();
				}
			});
			browseButton.setEnabled(createLink);
		}
		return browseButton;
	}

	/**
	 * Returns a new status object with the given severity and message.
	 *
	 * @return a new status object with the given severity and message.
	 */
	private IStatus createStatus(int severity, String message) {
		return new Status(severity, CUIPlugin.getPluginId(), severity, message, null);
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

		if ("".equals(linkTargetName) == false) { //$NON-NLS-1$
			file = new File(linkTargetName);
			if (file.exists() == false)
				file = null;
		}
		if (type == IResource.FILE) {
			FileDialog dialog = new FileDialog(linkTargetField.getShell());
			dialog.setText(NewWizardMessages.CreateLinkedResourceGroup_open);
			if (file != null) {
				if (file.isFile())
					dialog.setFileName(linkTargetName);
				else
					dialog.setFilterPath(linkTargetName);
			}
			selection = dialog.open();
		} else {
			DirectoryDialog dialog = new DirectoryDialog(linkTargetField.getShell());
			dialog.setText(NewWizardMessages.CreateLinkedResourceGroup_open);
			if (file != null) {
				if (file.isFile())
					linkTargetName = file.getParent();
				if (linkTargetName != null)
					dialog.setFilterPath(linkTargetName);
			}
			dialog.setMessage(NewWizardMessages.CreateLinkedResourceGroup_targetSelectionLabel);
			selection = dialog.open();
		}
		if (selection != null) {
			linkTargetField.setText(selection);
			if (!preventDialogFieldChanged)
				dialogFieldChanged();
		}
	}

	/**
	 * Tries to resolve the value entered in the link target field as
	 * a variable, if the value is a relative path.
	 * Displays the resolved value if the entered value is a variable.
	 */
	protected void resolveVariable() {
		if (!linkTargetField.isEnabled()) {
			resolvedPathLabelData.setText(""); //$NON-NLS-1$
			return;
		}

		IPathVariableManager pathVariableManager = ResourcesPlugin.getWorkspace().getPathVariableManager();
		IPath path = new Path(linkTargetField.getText());
		IPath resolvedPath = pathVariableManager.resolvePath(path);

		resolvedPathLabelData.setText(resolvedPath.toOSString());
	}

	/**
	 * Sets the value of the link target field
	 *
	 * @param target the value of the link target field
	 */
	public void setLinkTarget(String target) {
		initialLinkTarget = target;
		if (linkTargetField != null && linkTargetField.isDisposed() == false) {
			linkTargetField.setText(target);
			if (!preventDialogFieldChanged)
				dialogFieldChanged();
		}
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
			return createStatus(IStatus.ERROR, NewWizardMessages.CreateLinkedResourceGroup_linkTargetNotFile);
		} else if (type == IResource.FOLDER && linkTargetFile.isDirectory() == false) {
			return createStatus(IStatus.ERROR, NewWizardMessages.CreateLinkedResourceGroup_linkTargetNotFolder);
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
			return createStatus(IStatus.OK, ""); //$NON-NLS-1$

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		String linkTargetName = linkTargetField.getText();
		IPath path = new Path(linkTargetName);

		if (createLink == false)
			return createStatus(IStatus.OK, ""); //$NON-NLS-1$

		IStatus locationStatus = workspace.validateLinkLocation(linkHandle, path);
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
			return createStatus(IStatus.WARNING, NewWizardMessages.CreateLinkedResourceGroup_linkTargetNonExistent);
		}
		return locationStatus;
	}

	public boolean linkCreated() {
		return createLink;
	}
}
