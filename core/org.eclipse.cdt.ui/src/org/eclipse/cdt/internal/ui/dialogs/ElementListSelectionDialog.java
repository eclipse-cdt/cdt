/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.dialogs;

import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ILabelProvider;

/**
 * A class to select one or more elements out of an indexed property
 */
public class ElementListSelectionDialog extends AbstractElementListSelectionDialog {
	
	private List fElements;
	
	/*
	 * @private
	 */
	protected void computeResult() {
		setResult(getWidgetSelection());
	}
	/*
	 * @private
	 */	
	protected Control createDialogArea(Composite parent) {
		Control result= super.createDialogArea(parent);
		
		setSelectionListElements(fElements, false);
	  	//a little trick to make the window come up faster
	  	String initialFilter= null;
	  	if (getPrimaryInitialSelection() instanceof String)
			initialFilter= (String)getPrimaryInitialSelection();
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
	/*
	 * @private
	 */	
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
	public ElementListSelectionDialog(Shell parent, String title, Image image, ILabelProvider renderer, boolean ignoreCase, boolean multipleSelection) {
		super (parent, title, image, renderer, ignoreCase, multipleSelection);
	}
	/**
	 * Constructs a list selection dialog.
	 * @param renderer The label renderer used
	 * @param ignoreCase Decides if the match string ignores lower/upppr case
	 * @param multipleSelection Allow multiple selection	 
	 */
	public ElementListSelectionDialog(Shell parent, ILabelProvider renderer, boolean ignoreCase, boolean multipleSelection) {
		this(parent, "", null, renderer, ignoreCase, multipleSelection); //$NON-NLS-1$
	}
	public int open(Object[] elements) {
		return open(Arrays.asList(elements));
	}
	public int open(Object[] elements, String initialSelection) {
		return open(Arrays.asList(elements), initialSelection);
	}
	/**
	 * Open the dialog.
	 * @param elements The elements to show in the list
	 * @return Returns OK or CANCEL
	 */	
	public int open(List elements) {
		setElements(elements);
		return open();
	}
	/**
	 * Open the dialog.
	 * @param elements The elements to show in the list
	 * @param initialSelection The initial content of the match text box.
	 * @return Returns OK or CANCEL
	 */
	public int open(List elements, String initialSelection) {
		setElements(elements);
		setInitialSelection(initialSelection);
		return open();
	}
	/**
	 * Sets the elements presented by this dialog.
	 */
	public void setElements(List elements) {
		fElements= elements;	
	}
}
