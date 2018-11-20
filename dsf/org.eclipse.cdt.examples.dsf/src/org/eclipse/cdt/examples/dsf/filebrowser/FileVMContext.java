/*******************************************************************************
 * Copyright (c) 2006, 2009 Wind River Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.examples.dsf.filebrowser;

import java.io.File;

import org.eclipse.cdt.dsf.ui.viewmodel.AbstractVMContext;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMNode;

class FileVMContext extends AbstractVMContext {
	private File fFile;

	FileVMContext(IVMNode layoutNode, File file) {
		super(layoutNode);
		fFile = file;
	}

	File getFile() {
		return fFile;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof FileVMContext && ((FileVMContext) obj).getFile().equals(fFile);
	}

	@Override
	public int hashCode() {
		return fFile.hashCode();
	}

	@Override
	public String toString() {
		return fFile.toString();
	}
}