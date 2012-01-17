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

import java.util.Date;
import java.util.List;

import org.eclipse.cdt.debug.core.executables.Executable;
import org.eclipse.cdt.debug.core.executables.ExecutablesManager;
import org.eclipse.cdt.debug.core.executables.IExecutablesChangeListener;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.ui.progress.WorkbenchJob;

import com.ibm.icu.text.DateFormat;

class ExecutablesContentProvider extends ColumnLabelProvider implements IStructuredContentProvider, ITreeContentProvider, IExecutablesChangeListener {

	final private TreeViewer viewer;

	public ExecutablesContentProvider(final TreeViewer viewer) {
		this.viewer = viewer;
		ExecutablesManager.getExecutablesManager().addExecutablesChangeListener(this);
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.BaseLabelProvider#dispose()
	 */
	@Override
	public void dispose() {
		ExecutablesManager.getExecutablesManager().removeExecutablesChangeListener(this);
	}

	@Override
	public Object[] getElements(final Object inputElement) {
		if (inputElement instanceof ExecutablesManager) {
			ExecutablesManager em = (ExecutablesManager) inputElement;
			return em.getExecutables().toArray();
		}
		return new Object[] {};
	}

	@Override
	public Object getParent(Object element) {
		return null;
	}

	@Override
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

	@Override
	public Object[] getChildren(Object parentElement) {
		return new Object[] {};
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.executables.IExecutablesChangeListener#executablesListChanged()
	 */
	@Override
	public void executablesListChanged() {
		new WorkbenchJob("execs list changed") { //$NON-NLS-1$
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				viewer.refresh(null);
				if (viewer instanceof BaseViewer) {
					((BaseViewer)viewer).packColumns();						
				}
				return Status.OK_STATUS;
			}
		}.schedule();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.executables.IExecutablesChangeListener#executablesChanged(java.util.List)
	 */
	@Override
	public void executablesChanged(List<Executable> executables) {
		// Our concern is only if the list of executables changed. The 
		// content provider for the source files viewer will care about 
		// whether the Executables themselves change
	}
}