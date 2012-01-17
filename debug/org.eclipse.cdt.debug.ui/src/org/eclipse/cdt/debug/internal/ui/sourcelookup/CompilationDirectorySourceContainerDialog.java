/*******************************************************************************
 * Copyright (c) 2010 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	  Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.sourcelookup;

import java.io.File;

import org.eclipse.cdt.debug.internal.ui.CDebugImages;
import org.eclipse.cdt.debug.internal.ui.ICDebugHelpContextIds;
import org.eclipse.cdt.debug.internal.ui.IInternalCDebugUIConstants;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

/**
 * The dialog for selecting the external folder for which a compilation directory source container
 * will be created.
 */
public class CompilationDirectorySourceContainerDialog extends TitleAreaDialog {
	private static final String LAST_PATH_SETTING = "EXT_FOLDER_LAST_PATH_SETTING"; //$NON-NLS-1$
	private static final String LAST_SUBDIR_SETTING = "EXT_FOLDER_LAST_SUBDIR_SETTING"; //$NON-NLS-1$

	private String fDirectory;
	private boolean fCompilationSubfolders;
	
	private Text fDirText;
	private Button fSubfoldersButton;
	private boolean fNewContainer;

	/**
	 * Creates a dialog to select a new file system folder.
	 * 
	 * @param shell shell
	 */
	public CompilationDirectorySourceContainerDialog(Shell shell) {
		this(shell, "", //$NON-NLS-1$
				CDebugUIPlugin.getDefault().getDialogSettings().getBoolean(LAST_SUBDIR_SETTING));
		fNewContainer = true;
	}

	/**
	 * Creates a dialog to edit file system folder.
	 *  
	 * @param shell shell
	 * @param directory directory to edit or empty string
	 * @param compilationSubfolders whether the 'Subdirectories are also used for compilation'
	 * 		checkbox should be checked
	 * @param newContainer 
	 */
	public CompilationDirectorySourceContainerDialog(Shell shell, String directory, boolean compilationSubfolders) {
		super(shell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		fDirectory = directory;
		fCompilationSubfolders = compilationSubfolders;
		fNewContainer = false;
	}
	
	/**
	 * Returns the result of the dialog.open() operation
	 * @return the dialog.open() result
	 */
	public String getDirectory() {
		return fDirectory;
	}

	/**
	 * Returns whether the 'Subdirectories are also used for compilation' option is selected.
	 * 
	 * @return whether the 'Subdirectories are also used for compilation' option is selected
	 */
	public boolean isCompilationSubfolders() {
		return fCompilationSubfolders;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.TitleAreaDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Image image = fNewContainer ?
				CDebugImages.get(IInternalCDebugUIConstants.IMG_ADD_COMP_DIR_WIZ) : 
				CDebugImages.get(IInternalCDebugUIConstants.IMG_EDIT_COMP_DIR_WIZ);
		setTitle(SourceLookupUIMessages.CompilationDirectorySourceContainerDialog_2);
		setMessage(SourceLookupUIMessages.CompilationDirectorySourceContainerDialog_3);
		setTitleImage(image);
		Composite parentComposite = (Composite) super.createDialogArea(parent);
		Font font = parentComposite.getFont();
		Composite composite = new Composite(parentComposite, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setFont(font);

        Composite dirComposite = new Composite(composite, SWT.NONE);
        layout = new GridLayout(2, false);
		dirComposite.setLayout(layout);
		dirComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		dirComposite.setFont(font);

        Label label = new Label(dirComposite, SWT.NONE);
        label.setText(SourceLookupUIMessages.CompilationDirectorySourceContainerDialog_4);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 2;
        label.setLayoutData(data);
        label.setFont(font);
        
        fDirText = new Text(dirComposite, SWT.BORDER);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 1;
        fDirText.setLayoutData(data);
        fDirText.setFont(font);
        fDirText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				validate();
			}        	
        });

        Button button = new Button(dirComposite, SWT.PUSH);
        button.setText(SourceLookupUIMessages.CompilationDirectorySourceContainerDialog_5);
        data = new GridData();
        int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
        Point minSize = button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
        data.widthHint = Math.max(widthHint, minSize.x);
        button.setLayoutData(data);
        button.setFont(JFaceResources.getDialogFont());
        button.addSelectionListener(new SelectionAdapter() {
            @Override
			public void widgetSelected(SelectionEvent event) {
            	browse();
            }
        });

        fSubfoldersButton = new Button(dirComposite, SWT.CHECK);
        fSubfoldersButton.setText(SourceLookupUIMessages.CompilationDirectorySourceContainerDialog_6);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.verticalIndent = layout.verticalSpacing;  // Extra vertical spacing
        fSubfoldersButton.setLayoutData(data);
        fSubfoldersButton.setFont(font);

        return parentComposite;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	@Override
	protected void configureShell(Shell newShell) {
		String title = null;
		if (fNewContainer) {
			title = SourceLookupUIMessages.CompilationDirectorySourceContainerDialog_7;
		} else {
			title = SourceLookupUIMessages.CompilationDirectorySourceContainerDialog_8;
		}
		newShell.setText(title);
		super.configureShell(newShell);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.TitleAreaDialog#createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
		Control c = super.createContents(parent);
		fDirText.setText(fDirectory);
		fSubfoldersButton.setSelection(fCompilationSubfolders);
		validate();
		PlatformUI.getWorkbench().getHelpSystem().setHelp(c, ICDebugHelpContextIds.COMPILATION_DIRECTORY_SOURCE_CONTAINER_DIALOG);
		return c;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	@Override
	protected void okPressed() {
		fDirectory = fDirText.getText().trim();
		fCompilationSubfolders = fSubfoldersButton.getSelection();
		CDebugUIPlugin.getDefault().getDialogSettings().put(LAST_PATH_SETTING, fDirectory);
		CDebugUIPlugin.getDefault().getDialogSettings().put(LAST_SUBDIR_SETTING, fCompilationSubfolders);	
		super.okPressed();
	}

	private void browse() {
		String last = fDirText.getText().trim();
		if (last.length() == 0) {
			last = CDebugUIPlugin.getDefault().getDialogSettings().get(LAST_PATH_SETTING);
		}
		if (last == null) {
			last = "";  //$NON-NLS-1$
		}
		DirectoryDialog dialog = new DirectoryDialog(getShell(), SWT.SINGLE);
		dialog.setText(SourceLookupUIMessages.CompilationDirectorySourceContainerDialog_0); 
		dialog.setMessage(SourceLookupUIMessages.CompilationDirectorySourceContainerDialog_1); 
		dialog.setFilterPath(last);
		String result = dialog.open();
		if (result == null) {
			return;
		}
		fDirText.setText(result);
	}

	private void validate() {
		File file = new File(fDirText.getText().trim());
		getButton(IDialogConstants.OK_ID).setEnabled(file.isDirectory() && file.exists());
	}
}
