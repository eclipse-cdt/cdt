/*******************************************************************************
 * Copyright (c) 2002, 2003, 2004 QNX Software Systems Ltd. and others. All
 * rights reserved. This program and the accompanying materials are made
 * available under the terms of the Common Public License v1.0 which
 * accompanies this distribution, and is available at
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

public class CPathSymbolEntryPage extends CPathBasePage {

	private ListDialogField fSymbolsList;
	private TreeListDialogField fSrcList;

	private class SymbolsListAdapter implements IListAdapter, IDialogFieldListener {

		public void dialogFieldChanged(DialogField field) {
		}

		public void customButtonPressed(ListDialogField field, int index) {
		}

		public void selectionChanged(ListDialogField field) {
		}

		public void doubleClicked(ListDialogField field) {
		}
	}

	public CPathSymbolEntryPage(ITreeListAdapter adapter) {
		super(CPathEntryMessages.getString("SymbolEntryPage.title")); //$NON-NLS-1$
		SymbolsListAdapter includeListAdaper = new SymbolsListAdapter();

		String[] buttonLabel = new String[] { CPathEntryMessages.getString("SymbolEntryPage.add"), CPathEntryMessages.getString("SymbolEntryPage.addFromProject"), CPathEntryMessages.getString("SymbolEntryPage.addContributed"), null, CPathEntryMessages.getString("SymbolEntryPage.remove")}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		fSymbolsList = new ListDialogField(includeListAdaper, buttonLabel, new CPListLabelProvider());
		fSymbolsList.setDialogFieldListener(includeListAdaper);
		fSymbolsList.setLabelText(CPathEntryMessages.getString("SymbolEntryPage.defines")); //$NON-NLS-1$
		fSrcList = new TreeListDialogField(adapter, new String[] { CPathEntryMessages.getString("SymbolEntryPage.editSourcePaths")}, new CElementLabelProvider()); //$NON-NLS-1$
		fSrcList.setLabelText(CPathEntryMessages.getString("SymbolEntryPage.sourcePath")); //$NON-NLS-1$
	}

	public void createControl(Composite parent) {
		PixelConverter converter = new PixelConverter(parent);

		Composite composite = new Composite(parent, SWT.NONE);

		setControl(composite);
		LayoutUtil.doDefaultLayout(composite, new DialogField[] { fSrcList, fSymbolsList}, true);
		LayoutUtil.setHorizontalGrabbing(fSymbolsList.getListControl(null));

		int buttonBarWidth = converter.convertWidthInCharsToPixels(30);
		fSymbolsList.setButtonsMinWidth(buttonBarWidth);
	}

	public List getSelection() {
		return fSrcList.getSelectedElements();
	}

	public void setSelection(List selection) {
		fSrcList.selectElements(new StructuredSelection(selection));
	}

	public boolean isEntryKind(int kind) {
		return kind == IPathEntry.CDT_MACRO;
	}

	public void performApply(IProgressMonitor monitor) throws CoreException {
	}

	public void performDefaults() {
	}

	public void init(ICProject project, List list) {
		fSrcList.setElements(project.getChildrenOfType(ICElement.C_CCONTAINER));
		fSymbolsList.setElements(filterList(list));
	}
}
