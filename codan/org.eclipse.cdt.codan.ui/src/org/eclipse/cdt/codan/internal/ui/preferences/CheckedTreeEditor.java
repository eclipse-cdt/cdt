/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.ui.preferences;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
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
public class CheckedTreeEditor extends FieldEditor implements
		ICheckStateListener {
	/**
	 * The list widget; <code>null</code> if none (before creation or after
	 * disposal).
	 */
	private CheckboxTreeViewer treeViewer;
	private Composite listParent;
	private boolean isValid;
	private static String LIST_SEP = ",";
	private boolean emptySelectionAllowed = false;

	/**
	 * Creates a new list field editor
	 */
	protected CheckedTreeEditor() {
	}

	/**
	 * Creates a list field editor.
	 * 
	 * @param name
	 *            the name of the preference this field editor works on
	 * @param labelText
	 *            the label text of the field editor
	 * @param parent
	 *            the parent of the field editor's control
	 */
	public CheckedTreeEditor(String name, String labelText, Composite parent) {
		init(name, labelText);
		createControl(parent);
	}

	/*
	 * (non-Javadoc) Method declared on FieldEditor.
	 */
	protected void adjustForNumColumns(int numColumns) {
		Control control = getLabelControl();
		if (control != null) {
			((GridData) control.getLayoutData()).horizontalSpan = numColumns;
			((GridData) getTreeControl().getLayoutData()).horizontalSpan = numColumns;
		} else {
			((GridData) getTreeControl().getLayoutData()).horizontalSpan = numColumns;
		}
	}

	/*
	 * (non-Javadoc) Method declared on FieldEditor.
	 */
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

	/*
	 * (non-Javadoc) Method declared on FieldEditor.
	 */
	protected void doLoad() {
		if (getTreeControl() != null) {
			String s = getPreferenceStore().getString(getPreferenceName());
			setTreeData(parseString(s));
		}
	}

	Control getTreeControl() {
		if (treeViewer == null)
			return null;
		return treeViewer.getControl();
	}

	public CheckboxTreeViewer getTreeViewer() {
		return treeViewer;
	}

	/**
	 * @param map
	 */
	private void setTreeData(Map<Object, Boolean> checked) {
		for (Iterator iter = checked.keySet().iterator(); iter.hasNext();) {
			Object element = iter.next();
			if (element != null) {
				Boolean state = checked.get(element);
				treeViewer.setChecked(element, state);
				checkStateChanged(new CheckStateChangedEvent(treeViewer,
						element, state));
			}
		}
	}



	/**
	 * @Override
	 * @see org.eclipse.jface.viewers.ICheckStateListener#checkStateChanged(org.eclipse.jface.viewers.CheckStateChangedEvent)
	 */
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

	/**
	 * @param parent
	 * @param event
	 */
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

	/*
	 * (non-Javadoc) Method declared on FieldEditor.
	 */
	protected void doLoadDefault() {
		if (getTreeControl() != null) {
			treeViewer.refresh();
			String s = getPreferenceStore().getDefaultString(
					getPreferenceName());
			setTreeData(parseString(s));
		}
	}

	/*
	 * (non-Javadoc) Method declared on FieldEditor.
	 */
	protected void doStore() {
		String s = unparseTree();
		if (s != null) {
			getPreferenceStore().setValue(getPreferenceName(), s);
		}
	}

	/**
	 * Returns this field editor's list control.
	 * 
	 * @param parent
	 *            the parent control
	 * @return the list control
	 */
	public Tree createListControl(Composite parent) {
		Tree table = (Tree) getTreeControl();
		if (table == null) {
			listParent = parent;
			treeViewer = new CheckboxTreeViewer(parent, SWT.BORDER | SWT.MULTI
					| SWT.V_SCROLL | SWT.H_SCROLL);
			table = treeViewer.getTree();
			table.setFont(parent.getFont());
			treeViewer.addCheckStateListener(this);
		} else {
			checkParent(table, parent);
		}
		return table;
	}

	public StructuredViewer getViewer() {
		return treeViewer;
	}

	/*
	 * (non-Javadoc) Method declared on FieldEditor.
	 */
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

	/**
	 * Stored as element=true|false,...
	 * 
	 * @param stringList
	 * @return
	 */
	public Map<Object, Boolean> parseString(String stringList) {
		Map<Object, Boolean> data = new HashMap<Object, Boolean>();
		String[] arr = stringList.split(LIST_SEP);
		for (int i = 0; i < arr.length; i++) {
			String elem = arr[i];
			String[] pair = elem.split("=", 2);
			if (pair.length == 0)
				continue;
			String id = pair[0];
			if (pair.length == 1) {
				data.put(parseObject(id), true);
				continue;
			}
			String check = pair[1];
			data.put(parseObject(id), Boolean.valueOf(check));
		}
		return data;
	}

	protected Object parseObject(String string) {
		return string;
	}

	/*
	 * (non-Javadoc) Method declared on FieldEditor.
	 */
	public void setFocus() {
		if (getTreeControl() != null) {
			getTreeControl().setFocus();
		}
	}

	/*
	 * @see FieldEditor.setEnabled(boolean,Composite).
	 */
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
	 * @see #parseString
	 */
	protected String unparseTree() {
		StringBuffer buf = new StringBuffer();
		Map<Object, Boolean> map = fillElementsFromUi(treeViewer.getInput(),
				new HashMap<Object, Boolean>());
		for (Iterator iterator = map.keySet().iterator(); iterator.hasNext();) {
			Object element = iterator.next();
			buf.append(unparseElement(element));
			buf.append('=');
			buf.append(map.get(element));
			if (iterator.hasNext())
				buf.append(LIST_SEP);
		}
		return buf.toString();
	}

	/**
	 * @param element
	 * @return
	 */
	protected String unparseElement(Object element) {
		return element.toString();
	}

	/**
	 * @param root
	 */
	public Map<Object, Boolean> fillElementsFromUi(Object root,
			Map<Object, Boolean> checked) {

			Object[] children = getContentProvider().getChildren(root);
		if (children.length == 0) {
			checked.put(root, treeViewer.getChecked(root));
			} else {
				for (int i = 0; i < children.length; i++) {
					Object object = children[i];
				fillElementsFromUi(object, checked);
				}
			}

		return checked;
	}

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

	/*
	 * (non-Javadoc) Method declared on FieldEditor.
	 */
	public boolean isValid() {
		return isValid;
	}

	/*
	 * (non-Javadoc) Method declared on FieldEditor.
	 */
	protected void refreshValidState() {
		isValid = checkState();
	}

	protected boolean checkState() {
		if (!emptySelectionAllowed) {
			Object[] checkedElements = getTreeViewer().getCheckedElements();
			if (checkedElements.length == 0) {
				showErrorMessage("Selection cannot be empty");
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
