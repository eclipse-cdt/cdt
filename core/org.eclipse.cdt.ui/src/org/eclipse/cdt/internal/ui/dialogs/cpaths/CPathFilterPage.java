/*
 * Created on Apr 26, 2004
 *
 * Copyright (c) 2002,2003 QNX Software Systems Ltd.
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.ui.dialogs.cpaths;

import java.util.List;

import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.internal.ui.viewsupport.ListContentProvider;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;


public class CPathFilterPage extends WizardPage {
	private final int fFilterType;

	private CheckboxTableViewer viewer;
	private IPathEntry fContainerPath;
	private List fPaths;
			
	protected CPathFilterPage(int filterType) {
		super("CPathFilterPage"); //$NON-NLS-1$
		fFilterType = filterType;
	}
	

	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		container.setLayout(layout);
		Label label = new Label(container, SWT.NULL);
		label.setText(CPathEntryMessages.getString("CPathFilterPage.label")); //$NON-NLS-1$
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		label.setLayoutData(gd);
		viewer =
			CheckboxTableViewer.newCheckList(
				container,
				SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		viewer.setContentProvider(new ListContentProvider());
		viewer.setLabelProvider(new CPElementLabelProvider());
		viewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				// Prevent user to change checkbox states
				viewer.setChecked(event.getElement(), !event.getChecked());
			}
		});
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent e) {
				handleSelectionChanged((IStructuredSelection) e.getSelection());
			}
		});
		viewer.addFilter(new CPElementFilter(fFilterType, true));
		gd = new GridData(GridData.FILL_BOTH);
		gd.widthHint = 400;
		gd.heightHint = 300;
		viewer.getTable().setLayoutData(gd);
		setControl(container);
		Dialog.applyDialogFont(container);
	}
	
	
	public void setVisible(boolean visible) {
		if (fPaths != null) {
			viewer.setInput(fPaths);
		}
	}
	protected void handleSelectionChanged(IStructuredSelection selection) {
	}

	public void setEntries(IPathEntry entry) {
		fContainerPath = entry;
	}
	
	public IPathEntry[] getSelectedEntries() {
		return (IPathEntry[]) viewer.getCheckedElements();
	}

}
