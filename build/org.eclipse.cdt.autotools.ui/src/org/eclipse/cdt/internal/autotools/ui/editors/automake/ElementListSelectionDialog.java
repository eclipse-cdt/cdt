/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
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
 *     QNX Software System
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui.editors.automake;

import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * A class to select one or more elements out of an indexed property
 */
public class ElementListSelectionDialog extends AbstractElementListSelectionDialog {

	private List<Object> fElements;

	@Override
	protected void computeResult() {
		setResult(getWidgetSelection());
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Control result = super.createDialogArea(parent);

		setSelectionListElements(fElements, false);
		//a little trick to make the window come up faster
		String initialFilter = null;
		if (getPrimaryInitialSelection() instanceof String)
			initialFilter = (String) getPrimaryInitialSelection();
		if (initialFilter != null)
			setFilter(initialFilter, true);
		else
			refilter();

		return result;
	}

	public Object getSelectedElement() {
		return getPrimaryResult();
	}

	public Object[] getSelectedElements() {
		return getResult();
	}

	@Override
	protected void handleDoubleClick() {
		if (verifyCurrentSelection()) {
			buttonPressed(IDialogConstants.OK_ID);
		}
	}

	/**
	 * Constructs a list selection dialog.
	 * @param renderer The label renderer used
	 * @param ignoreCase Decides if the match string ignores lower/upper case
	 * @param multipleSelection Allow multiple selection
	 */
	public ElementListSelectionDialog(Shell parent, String title, Image image, ILabelProvider renderer,
			boolean ignoreCase, boolean multipleSelection) {
		super(parent, title, image, renderer, ignoreCase, multipleSelection);
	}

	/**
	 * Constructs a list selection dialog.
	 * @param renderer The label renderer used
	 * @param ignoreCase Decides if the match string ignores lower/upppr case
	 * @param multipleSelection Allow multiple selection
	 */
	public ElementListSelectionDialog(Shell parent, ILabelProvider renderer, boolean ignoreCase,
			boolean multipleSelection) {
		this(parent, "", null, renderer, ignoreCase, multipleSelection); //$NON-NLS-1$
	}

	/**
	 * Open the dialog.
	 * @param elements The elements to show in the list
	 * @return Returns OK or CANCEL
	 */
	public int open(List<Object> elements) {
		setElements(elements);
		return open();
	}

	/**
	 * Open the dialog.
	 * @param elements The elements to show in the list
	 * @param initialSelection The initial content of the match text box.
	 * @return Returns OK or CANCEL
	 */
	public int open(List<Object> elements, String initialSelection) {
		setElements(elements);
		setInitialSelection(initialSelection);
		return open();
	}

	/**
	 * Sets the elements presented by this dialog.
	 */
	public void setElements(List<Object> elements) {
		fElements = elements;
	}
}
