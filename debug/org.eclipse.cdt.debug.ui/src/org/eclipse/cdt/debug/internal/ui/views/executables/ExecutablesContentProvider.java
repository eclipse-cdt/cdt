/*******************************************************************************
 * Copyright (c) 2008, 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.internal.ui.views.executables;

import com.ibm.icu.text.DateFormat;
import java.util.Date;

import org.eclipse.cdt.debug.core.executables.Executable;
import org.eclipse.cdt.debug.core.executables.ExecutablesManager;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;

class ExecutablesContentProvider extends ColumnLabelProvider implements IStructuredContentProvider, ITreeContentProvider {

	public ExecutablesContentProvider(TreeViewer viewer) {
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	public void dispose() {
	}

	public Object[] getElements(final Object inputElement) {
		if (inputElement instanceof ExecutablesManager) {
			ExecutablesManager em = (ExecutablesManager) inputElement;
			return em.getExecutables().toArray();
		}
		return new Object[] {};
	}

	public Object getParent(Object element) {
		return null;
	}

	public boolean hasChildren(Object element) {
		return false;
	}

	@Override
	public void update(ViewerCell cell) {
		super.update(cell);
		Object element = cell.getElement();
		if (element instanceof Executable) {
			Executable exe = (Executable) element;
			String cellText = exe.getName();
			if (cell.getColumnIndex() == 1)
				cellText = exe.getProject().getName();
			else if (cell.getColumnIndex() == 2)
				cellText = exe.getPath().toOSString();
			else if (cell.getColumnIndex() == 3) {
				cellText = ""; //$NON-NLS-1$
				IPath path = exe.getPath();
				if (path != null && path.toFile().exists()) {
					long fileLength = path.toFile().length();
					cellText = Long.toString(fileLength);
				}
				cell.setImage(null);
			} else if (cell.getColumnIndex() == 4) {
				cellText = ""; //$NON-NLS-1$
				IPath path = exe.getPath();
				if (path != null && path.toFile().exists()) {
					long modified = path.toFile().lastModified();
					cellText = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date(modified));
				}
				cell.setImage(null);
			} else if (cell.getColumnIndex() == 5) {
				cellText = ""; //$NON-NLS-1$
				String fileExtension = exe.getPath().getFileExtension();
				if (fileExtension != null)
					cellText = fileExtension;
			}
			cell.setText(cellText);
		}
	}

	@Override
	public String getText(Object element) {
		if (element instanceof Executable) {
			return ((Executable) element).getName();
		} else
			return super.getText(element);
	}

	public Object[] getChildren(Object parentElement) {
		return new Object[] {};
	}

}