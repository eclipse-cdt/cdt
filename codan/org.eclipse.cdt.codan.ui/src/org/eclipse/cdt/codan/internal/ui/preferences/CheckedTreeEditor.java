/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.ui.preferences;

import org.eclipse.cdt.codan.internal.ui.CodanUIMessages;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;

/**
 * An abstract field editor that manages a checked tree. Values in the tree
 * should be set outside by setting getTreeViewer().setContentProvider() and
 * getTreeViewer().setInput() Control stores checked elements only in preference
 * storage.
 *
 * <p>
 * Subclasses may implement the <code>parseString</code>,
 * <code>createList</code>, <code>storeValue</code> and
 * <code>getListSeparator</code> framework methods.
 * </p>
 */
public abstract class CheckedTreeEditor extends FieldEditor implements ICheckStateListener {
	/**
	 * The list widget; <code>null</code> if none (before creation or after disposal).
	 */
	private CheckboxTreeViewer treeViewer;
	private Composite listParent;
	private boolean isValid;
	private boolean emptySelectionAllowed;

	/**
	 * Creates a new list field editor
	 */
	protected CheckedTreeEditor() {
	}

	/**
	 * Creates a list field editor.
	 *
	 * @param name
	 *        the name of the preference this field editor works on
	 * @param labelText
	 *        the label text of the field editor
	 * @param parent
	 *        the parent of the field editor's control
	 */
	public CheckedTreeEditor(String name, String labelText, Composite parent) {
		init(name, labelText);
		createControl(parent);
	}

	@Override
	protected void adjustForNumColumns(int numColumns) {
		Control control = getLabelControl();
		if (control != null) {
			((GridData) control.getLayoutData()).horizontalSpan = numColumns;
			((GridData) getTreeControl().getLayoutData()).horizontalSpan = numColumns;
		} else {
			((GridData) getTreeControl().getLayoutData()).horizontalSpan = numColumns;
		}
	}

	@Override
	protected void doFillIntoGrid(Composite parent, int numColumns) {
		doFillLabelIntoGrid(parent, numColumns);
		doFillBoxIntoGrid(parent, numColumns);
	}

	protected void doFillLabelIntoGrid(Composite parent, int numColumns) {
		String text = getLabelText();
		if (text != null && text.length() > 0) {
			Control control = getLabelControl(parent);
			GridData gd = new GridData();
			gd.horizontalSpan = numColumns;
			control.setLayoutData(gd);
		}
	}

	protected void doFillBoxIntoGrid(Composite parent, int numColumns) {
		GridData gd;
		Control list = createListControl(parent);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.verticalAlignment = GridData.FILL;
		gd.horizontalSpan = numColumns;
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		list.setLayoutData(gd);
	}

	@Override
	protected void doLoad() {
		if (getTreeControl() != null) {
			String s = getPreferenceStore().getString(getPreferenceName());
			getViewer().setInput(modelFromString(s));
		}
	}

	protected abstract Object modelFromString(String s);

	Control getTreeControl() {
		if (treeViewer == null)
			return null;
		return treeViewer.getControl();
	}

	public CheckboxTreeViewer getTreeViewer() {
		return treeViewer;
	}

	/**
	 * @Override
	 * @see org.eclipse.jface.viewers.ICheckStateListener#checkStateChanged(org.eclipse.jface.viewers.CheckStateChangedEvent)
	 */
	@Override
	public void checkStateChanged(CheckStateChangedEvent event) {
		Object element = event.getElement();
		treeViewer.setGrayed(element, false);
		treeViewer.setSubtreeChecked(element, event.getChecked());
		Object parent = getContentProvider().getParent(element);
		if (parent != null) {
			updateCheckedState(parent);
			treeViewer.setParentsGrayed(parent, true);
		}
		refreshValidState();
	}

	private ITreeContentProvider getContentProvider() {
		return ((ITreeContentProvider) treeViewer.getContentProvider());
	}

	private void updateCheckedState(Object parent) {
		Object[] children = getContentProvider().getChildren(parent);
		int i, count = 0;
		for (i = 0; i < children.length; i++) {
			Object object = children[i];
			if (treeViewer.getChecked(object)) {
				count++;
			}
		}
		if (count > 0) {
			treeViewer.setChecked(parent, true);
		}
		if (count == 0) {
			treeViewer.setGrayed(parent, false);
			treeViewer.setChecked(parent, false);
			return;
		}
		if (count < i) {
			treeViewer.setGrayed(parent, true);
		}
	}

	@Override
	protected void doLoadDefault() {
		if (getTreeControl() != null) {
			String s = getPreferenceStore().getDefaultString(getPreferenceName());
			getViewer().setInput(modelFromString(s));
		}
	}

	@Override
	protected void doStore() {
		String s = modelToString(getViewer().getInput());
		if (s != null) {
			getPreferenceStore().setValue(getPreferenceName(), s);
		}
	}

	/**
	 * Returns this field editor's list control.
	 *
	 * @param parent
	 *        the parent control
	 * @return the list control
	 */
	public Tree createListControl(Composite parent) {
		Tree table = (Tree) getTreeControl();
		if (table == null) {
			listParent = parent;
			treeViewer = doCreateTreeViewer(parent,
					SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION);
			table = treeViewer.getTree();
			table.setFont(parent.getFont());
			treeViewer.setComparator(new ViewerComparator());
			treeViewer.addCheckStateListener(this);
		}
		return table;
	}

	protected CheckboxTreeViewer doCreateTreeViewer(Composite parent, int style) {
		return new CheckboxTreeViewer(parent, style);
	}

	public StructuredViewer getViewer() {
		return treeViewer;
	}

	@Override
	public int getNumberOfControls() {
		return 2;
	}

	/**
	 * Returns this field editor's shell.
	 * <p>
	 * This method is internal to the framework; subclassers should not call
	 * this method.
	 * </p>
	 *
	 * @return the shell
	 */
	protected Shell getShell() {
		return treeViewer.getControl().getShell();
	}

	@Override
	public void setFocus() {
		if (getTreeControl() != null) {
			getTreeControl().setFocus();
		}
	}

	/*
	 * @see FieldEditor.setEnabled(boolean,Composite).
	 */
	@Override
	public void setEnabled(boolean enabled, Composite parent) {
		super.setEnabled(enabled, parent);
		createListControl(parent).setEnabled(enabled);
	}

	/**
	 * Combines the given list of items into a single string. This method is the
	 * converse of <code>parseString</code>.
	 * <p>
	 * Subclasses may implement this method.
	 * </p>
	 *
	 * @return the combined string
	 * @see #modelFromString(String)
	 */
	protected abstract String modelToString(Object model);

	@Override
	protected void createControl(Composite parent) {
		GridLayout ly = (GridLayout) parent.getLayout();
		doFillIntoGrid(parent, ly.numColumns);
	}

	/**
	 * @param b
	 */
	public void setEnabled(boolean b) {
		setEnabled(b, listParent);
	}

	public Composite getTreeParent() {
		return listParent;
	}

	@Override
	public boolean isValid() {
		return isValid;
	}

	@Override
	protected void refreshValidState() {
		isValid = checkState();
	}

	protected boolean checkState() {
		if (!emptySelectionAllowed) {
			Object[] checkedElements = getTreeViewer().getCheckedElements();
			if (checkedElements.length == 0) {
				showErrorMessage(CodanUIMessages.CheckedTreeEditor_SelectionCannotBeEmpty);
				return false;
			}
		}
		clearErrorMessage();
		return true;
	}

	public final boolean isEmptySelectionAllowed() {
		return emptySelectionAllowed;
	}

	public final void setEmptySelectionAllowed(boolean emptySelectionAllowed) {
		this.emptySelectionAllowed = emptySelectionAllowed;
	}
}
