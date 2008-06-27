/*******************************************************************************
 * Copyright (c) 2008 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.internal.ui.views.executables;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.debug.core.executables.Executable;
import org.eclipse.cdt.ui.CElementContentProvider;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;

public class SourceFilesContentProvider extends CElementContentProvider {

	public SourceFilesContentProvider(SourceFilesViewer viewer) {
		super(true, true);
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof ITranslationUnit) {
			IPath path = ((ITranslationUnit) element).getLocation();
			if (path != null && !path.toFile().exists())
				return false;
		}
		return super.hasChildren(element);
	}

	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof Executable) {
			Executable executable = (Executable) inputElement;
			ITranslationUnit[] sourceFiles = executable.getSourceFiles(new NullProgressMonitor());
			if (sourceFiles.length == 0)
				return new String[] { Messages.SourceFilesContentProvider_NoFilesFound + executable.getName() };
			else
				return sourceFiles;
		}
		return new Object[] {};
	}

}