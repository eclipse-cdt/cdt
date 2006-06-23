/*******************************************************************************
 * Copyright (c) 2004, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.dialogs.cpaths;

import java.util.ArrayList;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IContainerEntry;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.cdt.internal.ui.wizards.NewElementWizardPage;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.StringDialogField;
import org.eclipse.cdt.ui.wizards.IPathEntryContainerPage;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 */
public class CPathContainerDefaultPage extends NewElementWizardPage implements IPathEntryContainerPage {

	private StringDialogField fEntryField;
	private ArrayList fUsedPaths;

	/**
	 * Constructor for ClasspathContainerDefaultPage.
	 */
	public CPathContainerDefaultPage() {
		super("CPathContainerDefaultPage"); //$NON-NLS-1$
		setTitle(CPathEntryMessages.getString("CPathContainerDefaultPage.title")); //$NON-NLS-1$
		setDescription(CPathEntryMessages.getString("CPathContainerDefaultPage.description")); //$NON-NLS-1$
		setImageDescriptor(CPluginImages.DESC_WIZBAN_ADD_LIBRARY);
		
		fUsedPaths= new ArrayList();
		
		fEntryField= new StringDialogField();
		fEntryField.setLabelText(CPathEntryMessages.getString("CPathContainerDefaultPage.path.label")); //$NON-NLS-1$
		fEntryField.setDialogFieldListener(new IDialogFieldListener() {
			public void dialogFieldChanged(DialogField field) {
				validatePath();
			}
		});
		validatePath();
	}

	protected void validatePath() {
		StatusInfo status= new StatusInfo();
		String str= fEntryField.getText();
		if (str.length() == 0) {
			status.setError(CPathEntryMessages.getString("CPathContainerDefaultPage.path.error.enterpath")); //$NON-NLS-1$
		} else if (!Path.ROOT.isValidPath(str)) {
			status.setError(CPathEntryMessages.getString("CPathContainerDefaultPage.path.error.invalidpath")); //$NON-NLS-1$
		} else {
			IPath path= new Path(str);
			if (path.segmentCount() == 0) {
				status.setError(CPathEntryMessages.getString("CPathContainerDefaultPage.path.error.needssegment")); //$NON-NLS-1$
			} else if (fUsedPaths.contains(path)) {
				status.setError(CPathEntryMessages.getString("CPathContainerDefaultPage.path.error.alreadyexists")); //$NON-NLS-1$
			}
		}
		updateStatus(status);
	}

	/* (non-Javadoc)
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite composite= new Composite(parent, SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.numColumns= 1;
		composite.setLayout(layout);
		
		fEntryField.doFillIntoGrid(composite, 2);
		LayoutUtil.setHorizontalGrabbing(fEntryField.getTextControl(null));
		
		fEntryField.setFocus();
		
		setControl(composite);
		Dialog.applyDialogFont(composite);
//		WorkbenchHelp.setHelp(composite, IJavaHelpContextIds.CLASSPATH_CONTAINER_DEFAULT_PAGE);
	}

	/* (non-Javadoc)
	 * @see IClasspathContainerPage#finish()
	 */
	public boolean finish() {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see IClasspathContainerPage#getSelection()
	 */
	public IContainerEntry[] getNewContainers() {
		return new IContainerEntry[] {CoreModel.newContainerEntry(new Path(fEntryField.getText()))};
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.ui.wizards.IClasspathContainerPageExtension#initialize(org.eclipse.jdt.core.IJavaProject, org.eclipse.jdt.core.IClasspathEntry)
	 */
	public void initialize(ICProject project, IPathEntry[] currentEntries) {
		for (int i= 0; i < currentEntries.length; i++) {
			IPathEntry curr= currentEntries[i];
			if (curr.getEntryKind() == IPathEntry.CDT_CONTAINER) {
				fUsedPaths.add(curr.getPath());
			}
		}
	}		

	/* (non-Javadoc)
	 * @see IClasspathContainerPage#setSelection(IClasspathEntry)
	 */
	public void setSelection(IContainerEntry containerEntry) {
		if (containerEntry != null) {
			fUsedPaths.remove(containerEntry.getPath());
			fEntryField.setText(containerEntry.getPath().toString());
		} else {
			fEntryField.setText(""); //$NON-NLS-1$
		}
	}
}
