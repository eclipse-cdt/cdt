/*******************************************************************************
 * Copyright (c) 2004, 2012 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.dialogs.cpaths;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.cdt.internal.ui.util.SWTUtil;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.dialogs.NewFolderDialog;
import org.eclipse.ui.dialogs.SelectionStatusDialog;
import org.eclipse.ui.views.navigator.ResourceComparator;

/**
 * @deprecated as of CDT 4.0. This class was used for property pages
 * for 3.X style projects.
 */
@Deprecated
public class MultipleFolderSelectionDialog extends SelectionStatusDialog implements ISelectionChangedListener {

	CheckboxTreeViewer fViewer;

	private ILabelProvider fLabelProvider;
	private ITreeContentProvider fContentProvider;
	private List<ViewerFilter> fFilters;

	private Object fInput;
	private Button fNewFolderButton;
	private IContainer fSelectedContainer;
	Set<Object> fExisting;
	private Object fFocusElement;

	public MultipleFolderSelectionDialog(Shell parent, ILabelProvider labelProvider,
			ITreeContentProvider contentProvider) {
		super(parent);
		fLabelProvider = labelProvider;
		fContentProvider = contentProvider;

		setSelectionResult(null);
		setStatusLineAboveButtons(true);

		int shellStyle = getShellStyle();
		setShellStyle(shellStyle | SWT.MAX | SWT.RESIZE);

		fExisting = null;
		fFocusElement = null;
		fFilters = null;
	}

	public void setExisting(Object[] existing) {
		fExisting = new HashSet<>();
		for (int i = 0; i < existing.length; i++) {
			fExisting.add(existing[i]);
		}
	}

	/**
	 * Sets the tree input.
	 *
	 * @param input
	 *        the tree input.
	 */
	public void setInput(Object input) {
		fInput = input;
	}

	/**
	 * Adds a filter to the tree viewer.
	 *
	 * @param filter
	 *        a filter.
	 */
	public void addFilter(ViewerFilter filter) {
		if (fFilters == null)
			fFilters = new ArrayList<>(4);

		fFilters.add(filter);
	}

	/**
	 * Handles cancel button pressed event.
	 */
	@Override
	protected void cancelPressed() {
		setSelectionResult(null);
		super.cancelPressed();
	}

	/*
	 * @see SelectionStatusDialog#computeResult()
	 */
	@Override
	protected void computeResult() {
		Object[] checked = fViewer.getCheckedElements();
		if (fExisting == null) {
			if (checked.length == 0) {
				checked = null;
			}
		} else {
			ArrayList<Object> res = new ArrayList<>();
			for (int i = 0; i < checked.length; i++) {
				Object elem = checked[i];
				if (!fExisting.contains(elem)) {
					res.add(elem);
				}
			}
			if (!res.isEmpty()) {
				checked = res.toArray();
			} else {
				checked = null;
			}
		}
		setSelectionResult(checked);
	}

	void access$superCreate() {
		super.create();
	}

	/*
	 * @see Window#create()
	 */
	@Override
	public void create() {

		BusyIndicator.showWhile(null, () -> {
			access$superCreate();

			fViewer.setCheckedElements(getInitialElementSelections().toArray());

			fViewer.expandToLevel(2);
			if (fExisting != null) {
				for (Iterator<Object> iter = fExisting.iterator(); iter.hasNext();) {
					fViewer.reveal(iter.next());
				}
			}

			updateOKStatus();
		});

	}

	/**
	 * Creates the tree viewer.
	 *
	 * @param parent
	 *        the parent composite
	 * @return the tree viewer
	 */
	protected CheckboxTreeViewer createTreeViewer(Composite parent) {
		fViewer = new CheckboxTreeViewer(parent, SWT.BORDER);

		fViewer.setContentProvider(fContentProvider);
		fViewer.setLabelProvider(fLabelProvider);
		fViewer.addCheckStateListener(event -> updateOKStatus());

		fViewer.setComparator(new ResourceComparator(ResourceComparator.NAME));
		if (fFilters != null) {
			for (int i = 0; i != fFilters.size(); i++)
				fViewer.addFilter(fFilters.get(i));
		}

		fViewer.setInput(fInput);

		return fViewer;
	}

	/**
	 *
	 */
	protected void updateOKStatus() {
		computeResult();
		if (getResult() != null) {
			updateStatus(new StatusInfo());
		} else {
			updateStatus(new StatusInfo(IStatus.ERROR, "")); //$NON-NLS-1$
		}
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);

		createMessageArea(composite);
		CheckboxTreeViewer treeViewer = createTreeViewer(composite);

		GridData data = new GridData(GridData.FILL_BOTH);
		data.widthHint = convertWidthInCharsToPixels(60);
		data.heightHint = convertHeightInCharsToPixels(18);

		Tree treeWidget = treeViewer.getTree();
		treeWidget.setLayoutData(data);
		treeWidget.setFont(composite.getFont());

		Button button = new Button(composite, SWT.PUSH);
		button.setText(CPathEntryMessages.MultipleFolderSelectionDialog_button);
		button.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				newFolderButtonPressed();
			}
		});
		button.setFont(composite.getFont());
		button.setLayoutData(new GridData());
		SWTUtil.setButtonDimensionHint(button);
		fNewFolderButton = button;

		treeViewer.addSelectionChangedListener(this);
		if (fExisting != null) {
			Object[] existing = fExisting.toArray();
			treeViewer.setGrayedElements(existing);
			setInitialSelections(existing);
		}
		if (fFocusElement != null) {
			treeViewer.setSelection(new StructuredSelection(fFocusElement), true);
		}
		treeViewer.addCheckStateListener(event -> forceExistingChecked(event));

		applyDialogFont(composite);
		return composite;
	}

	protected void forceExistingChecked(CheckStateChangedEvent event) {
		if (fExisting != null) {
			Object elem = event.getElement();
			if (fExisting.contains(elem)) {
				fViewer.setChecked(elem, true);
			}
		}
	}

	private void updateNewFolderButtonState() {
		IStructuredSelection selection = (IStructuredSelection) fViewer.getSelection();
		fSelectedContainer = null;
		if (selection.size() == 1) {
			Object first = selection.getFirstElement();
			if (first instanceof IContainer) {
				fSelectedContainer = (IContainer) first;
			}
		}
		fNewFolderButton.setEnabled(fSelectedContainer != null);
	}

	protected void newFolderButtonPressed() {
		NewFolderDialog dialog = new NewFolderDialog(getShell(), fSelectedContainer);
		if (dialog.open() == Window.OK) {
			CheckboxTreeViewer treeViewer = fViewer;
			treeViewer.refresh(fSelectedContainer);
			Object createdFolder = dialog.getResult()[0];
			treeViewer.reveal(createdFolder);
			treeViewer.setChecked(createdFolder, true);
			treeViewer.setSelection(new StructuredSelection(createdFolder));
			updateOKStatus();
		}
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		updateNewFolderButtonState();
	}

	public void setInitialFocus(Object focusElement) {
		fFocusElement = focusElement;
	}

}
