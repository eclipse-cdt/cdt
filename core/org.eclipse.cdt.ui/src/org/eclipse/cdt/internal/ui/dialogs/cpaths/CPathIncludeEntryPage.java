/*******************************************************************************
 * Copyright (c) 2002, 2003, 2004 QNX Software Systems Ltd. and others. All
 * rights reserved. This program and the accompanying materials are made
 * available under the terms of the Common Public License v1.0 which accompanies
 * this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: QNX Software Systems - Initial API and implementation
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.dialogs.cpaths;

import java.util.List;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.internal.ui.util.PixelConverter;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IListAdapter;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.ITreeListAdapter;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.ListDialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.TreeListDialogField;
import org.eclipse.cdt.ui.CElementLabelProvider;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

public class CPathIncludeEntryPage extends CPathBasePage {

	private ListDialogField fIncludeList;
	private TreeListDialogField fSrcList;
	private List fCPathList;

	private class IncludeListAdapter implements IListAdapter, IDialogFieldListener {

		public void dialogFieldChanged(DialogField field) {
		}

		public void customButtonPressed(ListDialogField field, int index) {
		}

		public void selectionChanged(ListDialogField field) {
		}

		public void doubleClicked(ListDialogField field) {
		}
	}

	public CPathIncludeEntryPage(ITreeListAdapter adapter) {
		super(CPathEntryMessages.getString("IncludeEntryPage.title")); //$NON-NLS-1$
		IncludeListAdapter includeListAdaper = new IncludeListAdapter();

		String[] buttonLabel = new String[] { CPathEntryMessages.getString("IncludeEntryPage.addFromWorksapce"), CPathEntryMessages.getString("IncludeEntryPage.addExternal"), CPathEntryMessages.getString("IncludeEntryPage.addContributed"), null, CPathEntryMessages.getString("IncludeEntryPage.remove")}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		fIncludeList = new ListDialogField(includeListAdaper, buttonLabel, new CPListLabelProvider());
		fIncludeList.setDialogFieldListener(includeListAdaper);
		fIncludeList.setLabelText(CPathEntryMessages.getString("IncludeEntryPage.includePath")); //$NON-NLS-1$
		fSrcList = new TreeListDialogField(adapter, new String[] { CPathEntryMessages.getString("IncludeEntryPage.editSourcePaths")}, new CElementLabelProvider()); //$NON-NLS-1$
		fSrcList.setLabelText(CPathEntryMessages.getString("IncludeEntryPage.sourcePaths")); //$NON-NLS-1$
	}

	public void createControl(Composite parent) {
		PixelConverter converter = new PixelConverter(parent);

		Composite composite = new Composite(parent, SWT.NONE);

		setControl(composite);

		LayoutUtil.doDefaultLayout(composite, new DialogField[] { fSrcList, fIncludeList}, true);
		LayoutUtil.setHorizontalGrabbing(fIncludeList.getListControl(null));

		int buttonBarWidth = converter.convertWidthInCharsToPixels(30);
		fIncludeList.setButtonsMinWidth(buttonBarWidth);
	}

	public void init(ICProject project, List cPaths) {
		fSrcList.setElements(project.getChildrenOfType(ICElement.C_CCONTAINER));
		fIncludeList.setElements(filterList(cPaths));
	}
	
	public List getSelection() {
		return fSrcList.getSelectedElements();
	}

	public void setSelection(List selection) {
		fSrcList.selectElements(new StructuredSelection(selection));
	}

	public boolean isEntryKind(int kind) {
		return kind == IPathEntry.CDT_INCLUDE;
	}

	public void performApply(IProgressMonitor monitor) throws CoreException {
	}

	public void performDefaults() {
	}
}
