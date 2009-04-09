/*******************************************************************************
 * Copyright (c) 2009 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.ui.preferences;

import org.eclipse.cdt.codan.core.model.CheckersRegisry;
import org.eclipse.cdt.codan.core.model.CodanProblem;
import org.eclipse.cdt.codan.core.model.CodanSeverity;
import org.eclipse.cdt.codan.core.model.IProblem;
import org.eclipse.cdt.codan.core.model.IProblemCategory;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeColumn;

public class ProblemsTreeEditor extends CheckedTreeEditor {
	public ProblemsTreeEditor() {
		super();
		// TODO Auto-generated constructor stub
	}

	class ProblemsContentProvider implements IContentProvider,
			ITreeContentProvider {
		public void dispose() {
			// TODO Auto-generated method stub
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// TODO Auto-generated method stub
		}

		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof Object[])
				return (Object[]) parentElement;
			if (parentElement instanceof IProblemCategory) {
				return ((IProblemCategory) parentElement).getChildren();
			}
			return new Object[0];
		}

		public Object getParent(Object element) {
			return null;
		}

		public boolean hasChildren(Object element) {
			return getChildren(element).length > 0;
		}

		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}
	}

	class ProblemsLabelProvider extends BaseLabelProvider implements
			IBaseLabelProvider, ITableLabelProvider {
		public Image getColumnImage(Object element, int columnIndex) {
			// TODO Auto-generated method stub
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			if (element instanceof IProblem) {
				IProblem p = (IProblem) element;
				if (columnIndex == 0)
					return p.getName();
				if (columnIndex == 1)
					return p.getSeverity().toString();
			}
			if (element instanceof IProblemCategory) {
				IProblemCategory p = (IProblemCategory) element;
				if (columnIndex == 0)
					return p.getName();
			}
			return null;
		}
	}

	@Override
	protected String unparseElement(Object element) {
		IProblem p = ((IProblem) element);
		return p.getId() + ":" + p.getSeverity();
	}

	@Override
	protected Object parseObject(String string) {
		String[] pair = string.split(":");
		if (pair.length == 0)
			return null;
		String id = pair[0];
		String arg = "";
		if (pair.length > 1) {
			arg = pair[1];
		}
		CodanSeverity sev;
		try {
			sev = CodanSeverity.valueOf(arg);
		} catch (RuntimeException e) {
			sev = CodanSeverity.Warning;
		}
		IProblem prob = CheckersRegisry.getInstance().findProblem(id);
		if (prob instanceof CodanProblem) {
			((CodanProblem) prob).setSeverity(sev);
		}
		return prob;
	}

	public void checkStateChanged(CheckStateChangedEvent event) {
		Object element = event.getElement();
		if (element instanceof CodanProblem) {
			((CodanProblem) element).setEnabled(event.getChecked());
		}
	}

	public ProblemsTreeEditor(Composite parent) {
		super("problems", "Problems", parent);
		setEmptySelectionAllowed(true);
		getTreeViewer().getTree().setHeaderVisible(true);
		// getTreeViewer().getTree().
		getTreeViewer().setContentProvider(new ProblemsContentProvider());
		getTreeViewer().setLabelProvider(new ProblemsLabelProvider());
		// column Name
		TreeColumn column = new TreeColumn(getTreeViewer().getTree(), SWT.NONE);
		column.setWidth(300);
		column.setText("Name");
		// column Severity
		TreeColumn column2 = new TreeColumn(getTreeViewer().getTree(), SWT.NONE);
		column2.setWidth(100);
		column2.setText("Severity");
		getTreeViewer().setInput(
				CheckersRegisry.getInstance().getProblemsTree());
	}
}
