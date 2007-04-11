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
package org.eclipse.rse.internal.importexport;

import org.eclipse.core.resources.IFile;

/**
 * A runnable class that exports from an export description file. Use this class
 * to export in a non-UI thread, by using Display.syncExec() or Display.asyncExec().
 */
public class RemoteImportExportRunnable implements Runnable {
	// description file
	private IFile file;
	private boolean export;

	/**
	 * Constrcutor.
	 * @param descriptionFile the description file.
	 * @param export <code>true</code> to export, otherwise <code>false</code>.
	 */
	public RemoteImportExportRunnable(IFile descriptionFile, boolean export) {
		this.file = descriptionFile;
		this.export = export;
	}

	/**
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		if (export) {
			RemoteImportExportUtil.getInstance().exportFromDescriptionFile(file);
		}
	}
}
