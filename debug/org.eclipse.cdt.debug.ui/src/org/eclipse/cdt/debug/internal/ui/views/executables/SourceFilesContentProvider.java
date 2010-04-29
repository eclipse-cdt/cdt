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
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.executables.Executable;
import org.eclipse.cdt.ui.CElementContentProvider;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

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
			final Executable executable = (Executable) inputElement;
			final ITranslationUnit[][] resultHolder = new ITranslationUnit[1][];
			Job quickParseJob = new Job("Reading Debug Symbol Information: " + executable.getName()) {

				@Override
				protected IStatus run(IProgressMonitor monitor) {
					ITranslationUnit[] sourceFiles = executable.getSourceFiles(monitor);
					resultHolder[0] = sourceFiles;
					return Status.OK_STATUS;
				}
			};
			
			try {
				quickParseJob.schedule();
				quickParseJob.join();
			} catch (InterruptedException e) {
				CDebugCorePlugin.log(e);
			}
			
			ITranslationUnit[] sourceFiles = resultHolder[0];
			if (sourceFiles.length == 0)
				return new String[] { Messages.SourceFilesContentProvider_NoFilesFound + executable.getName() };
			else
				return sourceFiles;
		}
		return new Object[] {};
	}

}