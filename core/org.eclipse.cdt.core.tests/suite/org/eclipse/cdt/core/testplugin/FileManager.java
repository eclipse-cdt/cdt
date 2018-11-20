/*******************************************************************************
 *  Copyright (c) 2005, 2008 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 * Created on Sep 8, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.eclipse.cdt.core.testplugin;

import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/**
 * @author bgheorgh
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class FileManager {
	HashSet<IFile> fileHandles;

	public FileManager() {
		fileHandles = new HashSet<>();
	}

	public void addFile(IFile file) {
		fileHandles.add(file);
	}

	public void closeAllFiles() throws CoreException, InterruptedException {
		int wait = 1;
		for (int i = 0; i < 11; i++) {
			for (Iterator iter = fileHandles.iterator(); iter.hasNext();) {
				IFile tempFile = (IFile) iter.next();
				try {
					if (i == 1) {
						tempFile.refreshLocal(IResource.DEPTH_INFINITE, null);
					}
					tempFile.delete(true, null);
					iter.remove();
				} catch (CoreException e) {
					if (wait > 2000)
						throw e;
				}
			}

			if (fileHandles.isEmpty())
				return;
			Thread.sleep(wait);
			wait *= 2;
		}
	}
}