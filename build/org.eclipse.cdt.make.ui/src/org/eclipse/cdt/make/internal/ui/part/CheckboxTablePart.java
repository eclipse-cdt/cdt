/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.cdt.make.internal.ui.part;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/**
 * @version 	1.0
 */
public class CheckboxTablePart extends StructuredViewerPart {
	public CheckboxTablePart(String[] buttonLabels) {
		super(buttonLabels);
	}

	/*
	 * @see StructuredViewerPart#createStructuredViewer(Composite, FormWidgetFactory)
	 */
	@Override
	protected StructuredViewer createStructuredViewer(Composite parent, int style) {
		style |= SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER;
		CheckboxTableViewer tableViewer = CheckboxTableViewer.newCheckList(parent, style);
		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent e) {
				CheckboxTablePart.this.selectionChanged((IStructuredSelection) e.getSelection());
			}
		});
		tableViewer.addCheckStateListener(new ICheckStateListener() {
			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				elementChecked(event.getElement(), event.getChecked());
			}
		});
		return tableViewer;
	}

	public CheckboxTableViewer getTableViewer() {
		return (CheckboxTableViewer) getViewer();
	}

	/*
	 * @see SharedPartWithButtons#buttonSelected(int)
	 */
	@Override
	protected void buttonSelected(Button button, int index) {
	}

	protected void elementChecked(Object element, boolean checked) {
	}

	protected void selectionChanged(IStructuredSelection selection) {
	}
}
