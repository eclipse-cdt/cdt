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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.viewers.ILabelProvider;

/**
 * A class to select one or more elements out of an indexed property
 */
public abstract class AbstractElementListSelectionDialog extends SelectionStatusDialog {
	
	private ILabelProvider fRenderer;
	private boolean fIgnoreCase;
	private boolean fIsMultipleSelection;
	
	private SelectionList fSelectionList;
	private Label fMessage;
	private ISelectionValidator fValidator;	
	
	private String fMessageText;
	private String fEmptyListMessage;
	private String fNothingSelectedMessage;
	
	private StatusInfo fCurrStatus;
	
	/*
	 * @private
	 */
	protected void access$superOpen() {
		super.open();
	}
	/*
	 * @private
	 * @see Dialog#cancelPressed
	 */
	protected void cancelPressed() {
		setResult(null);
		super.cancelPressed();
	}
	protected Point computeInitialSize() {
		return new Point(convertWidthInCharsToPixels(60), convertHeightInCharsToPixels(18));
	}
	/*
	 * @private
	 * @see Window#createDialogArea(Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		Composite contents= (Composite)super.createDialogArea(parent);
		
		fMessage= createMessage(contents);
		
		int flags= fIsMultipleSelection ? SWT.MULTI : SWT.SINGLE;			
		fSelectionList= new SelectionList(contents, flags | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL,
			fRenderer, fIgnoreCase);
		
		fSelectionList.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				handleDoubleClick();
			}
			public void widgetSelected(SelectionEvent e) {
				verifyCurrentSelection();
			}
		});

		GridData spec= new GridData();
		Point initialSize= computeInitialSize();
		spec.widthHint= initialSize.x;
		spec.heightHint= initialSize.y;
		spec.grabExcessVerticalSpace= true;
		spec.grabExcessHorizontalSpace= true;
		spec.horizontalAlignment= GridData.FILL;
		spec.verticalAlignment= GridData.FILL;
		fSelectionList.setLayoutData(spec);
				
		return contents;
	}
	/**
	 * Creates the message text widget and sets layout data.
	 */
	protected Label createMessage(Composite parent) {
		Label text= new Label(parent, SWT.NULL);
		text.setText(fMessageText);
		GridData spec= new GridData();
		spec.grabExcessVerticalSpace= false;
		spec.grabExcessHorizontalSpace= true;
		spec.horizontalAlignment= GridData.FILL;
		spec.verticalAlignment= GridData.BEGINNING;
		text.setLayoutData(spec);
		return text;
	}
	/*
	 * @private
	 * @see Window#create(Shell)
	 */
	public void create() {
		super.create();
	     	if (isEmptyList()) {
	     		fMessage.setEnabled(false);
	     		fSelectionList.setEnabled(false);
	     	} else {
		     	verifyCurrentSelection();		
			fSelectionList.selectFilterText();
			fSelectionList.setFocus();
	     	}	
	}
	/**
	 * Returns the currently used filter text.
	 */
	protected String getFilter() {
		return fSelectionList.getFilter();
	}
	/**
	 * Returns the selection indices.
	 */
	protected int[] getSelectionIndices() {
		return fSelectionList.getSelectionIndices();
	}
	/**
	 * Returns the widget selection. Returns empty list when the widget is not
	 * usable.
	 */
	protected List getWidgetSelection() {
		if (fSelectionList == null || fSelectionList.isDisposed())
			return new ArrayList(0);
		return fSelectionList.getSelection();	
	}
	/**
	 * An element as been selected in the list by double clicking on it.
	 * Emulate a OK button pressed to close the dialog.
	 */	
	protected abstract void handleDoubleClick();
	/**
	 * Checks whether the list of elements is empty or not.
	 */
	protected boolean isEmptyList() {
		if (fSelectionList == null)
			return true;
		return fSelectionList.isEmptyList();
	}
	/**
	 * Constructs a list selection dialog.
	 * @param renderer The label renderer used
	 * @param ignoreCase Decides if the match string ignores lower/upppr case
	 * @param multipleSelection Allow multiple selection	 
	 */
	protected AbstractElementListSelectionDialog(Shell parent, String title, Image image, ILabelProvider renderer, boolean ignoreCase, boolean multipleSelection) {
		super(parent);
		setTitle(title);
		setImage(image);
		fRenderer= renderer;
		fIgnoreCase= ignoreCase;
		fIsMultipleSelection= multipleSelection;
		
		fMessageText= ""; //$NON-NLS-1$
		
		fCurrStatus= new StatusInfo();
		
		fValidator= null;
		fEmptyListMessage= ""; //$NON-NLS-1$
		fNothingSelectedMessage= ""; //$NON-NLS-1$
	}
	/**
	 * Constructs a list selection dialog.
	 * @param renderer The label renderer used
	 * @param ignoreCase Decides if the match string ignores lower/upppr case
	 * @param multipleSelection Allow multiple selection	 
	 */
	protected AbstractElementListSelectionDialog(Shell parent, ILabelProvider renderer, boolean ignoreCase, boolean multipleSelection) {
		this(parent, "", null, renderer, ignoreCase, multipleSelection); //$NON-NLS-1$
	}
	/*
	 * @private
	 */
	public int open() {
		BusyIndicator.showWhile(null, new Runnable() {
			public void run() {
				access$superOpen();
			}
		});
		return getReturnCode() ;
	}
	/**
	 * Refilters the current list according to the filter entered into the
	 * text edit field.
	 */
	protected void refilter() {
		fSelectionList.filter(true);
	}
	/**
	 * If a empty-list message is set, a error message is shown
	 * Must be set before widget creation
	 */
	public void setEmptyListMessage(String message) {
		fEmptyListMessage= message;
	}
	/**
	 * Sets the filter text to the given value.
	 */
	protected void setFilter(String text, boolean refilter) {
		fSelectionList.setFilter(text, refilter);
	}
	/**
	 * Sets the message to be shown above the match text field.
	 * Must be set before widget creation
	 */
	public void setMessage(String message) {
		fMessageText= message;
	}
	/**
	 * If the selection is empty, this message is shown
	 */
	public void setNothingSelectedMessage(String message) {
		fNothingSelectedMessage= message;
	}
	/**
	 * Selects the elements in the list determined by the given
	 * selection indices.
	 */
	protected void setSelection(int[] selection) {
		fSelectionList.setSelection(selection);
	}
	/**
	 * Initializes the selection list widget with the given list of
	 * elements.
	 */
	protected void setSelectionListElements(List elements, boolean refilter) {
		fSelectionList.setElements(elements, refilter);
	}
	/**
	 * A validator can be set to check if the current selection
	 * is valid
	 */
	public void setValidator(ISelectionValidator validator) {
		fValidator= validator;
	}
	/**
	 * Verifies the current selection and updates the status line
	 * accordingly.
	 */
	protected boolean verifyCurrentSelection() {
		List sel= getWidgetSelection();
		int length= sel.size();
		if (length > 0) {
			if (fValidator != null) {
				fValidator.isValid(sel.toArray(), fCurrStatus);
			} else {
				fCurrStatus.setOK();
			}
		} else {
			if (isEmptyList()) {
				fCurrStatus.setError(fEmptyListMessage);
			} else {
				fCurrStatus.setError(fNothingSelectedMessage);
			}
		}
		updateStatus(fCurrStatus);
		return fCurrStatus.isOK();
	}
}
