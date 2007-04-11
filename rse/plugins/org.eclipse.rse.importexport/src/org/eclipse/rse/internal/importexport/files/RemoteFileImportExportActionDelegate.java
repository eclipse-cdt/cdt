/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.rse.internal.importexport.files;

import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

/**
 * This class is the super class of file import/export action delegate.
 */
public abstract class RemoteFileImportExportActionDelegate implements IActionDelegate {
	protected IStructuredSelection fSelection;

	/**
	 * Sets the selection. The selection is only set if given a structured selection, otherwise it is set to an
	 * empty structured selection.
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			fSelection = (IStructuredSelection) selection;
		} else {
			fSelection = StructuredSelection.EMPTY;
		}
	}

	/**
	 * Returns the remote file in the selection.
	 * Use this method if this action allows only a single remote file selection.
	 * @return the single remote file.
	 */
	protected IRemoteFile getRemoteFile(IStructuredSelection selection) {
		return (IRemoteFile) selection.getFirstElement();
	}

	/**
	 * Returns the remote files in the selection.
	 * Use this method if this action allows multiple remote file selection.
	 * @return an array of remote files.
	 */
	protected IRemoteFile[] getRemoteFiles(IStructuredSelection selection) {
		IRemoteFile[] files = new IRemoteFile[selection.size()];
		Iterator iter = selection.iterator();
		int i = 0;
		while (iter.hasNext()) {
			files[i++] = (IRemoteFile) iter.next();
		}
		return files;
	}

	/**
	 * Returns the description file for the first description file in
	 * the selection. Use this method if this action allows only
	 * a single file selection.
	 * @return the single description file.
	 */
	protected IFile getDescriptionFile(IStructuredSelection selection) {
		return (IFile) selection.getFirstElement();
	}

	/**
	 * Returns a description file for each description file in
	 * the selection. Use this method if this action allows multiple
	 * selection.
	 * @return an array of description files.
	 */
	protected IFile[] getDescriptionFiles(IStructuredSelection selection) {
		IFile[] files = new IFile[selection.size()];
		Iterator iter = selection.iterator();
		int i = 0;
		while (iter.hasNext()) {
			files[i++] = (IFile) iter.next();
		}
		return files;
	}

	/**
	 * Returns the workbench.
	 * @return the workbench.
	 */
	protected IWorkbench getWorkbench() {
		return PlatformUI.getWorkbench();
	}

	/**
	 * Returns the active shell.
	 * @return the active shell.
	 */
	protected Shell getShell() {
		return Display.getDefault().getActiveShell();
	}

	/**
	 * Returns the selection.
	 * @return the selection.
	 */
	protected IStructuredSelection getSelection() {
		return fSelection;
	}
}
