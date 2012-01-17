/*******************************************************************************
 * Copyright (c) 2010 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	  Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.sourcelookup;

import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.debug.ui.sourcelookup.AbstractSourceContainerBrowser;
import org.eclipse.cdt.debug.internal.core.sourcelookup.CompilationDirectorySourceContainer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;

/**
 * The browser for adding a compilation directory source container.
 */
public class CompilationDirectorySourceContainerBrowser extends AbstractSourceContainerBrowser {
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.sourcelookup.ISourceContainerBrowser#createSourceContainers(org.eclipse.swt.widgets.Shell, org.eclipse.debug.core.ILaunchConfiguration)
	 */
	@Override
	public ISourceContainer[] addSourceContainers(Shell shell, ISourceLookupDirector director) {
		ISourceContainer[] containers = new ISourceContainer[1];
		CompilationDirectorySourceContainerDialog dialog = new CompilationDirectorySourceContainerDialog(shell);
		if (dialog.open() == Window.OK) {
			String directory = dialog.getDirectory();
			if (directory != null) {
				containers[0] = new CompilationDirectorySourceContainer(new Path(directory), dialog.isCompilationSubfolders());			
				return containers;			
			}
		}		
		return new ISourceContainer[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.sourcelookup.AbstractSourceContainerBrowser#canEditSourceContainers(org.eclipse.debug.core.sourcelookup.ISourceLookupDirector, org.eclipse.debug.core.sourcelookup.ISourceContainer[])
	 */
	@Override
	public boolean canEditSourceContainers(ISourceLookupDirector director, ISourceContainer[] containers) {
		return containers.length == 1 && CompilationDirectorySourceContainer.TYPE_ID.equals(containers[0].getType().getId());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.sourcelookup.AbstractSourceContainerBrowser#editSourceContainers(org.eclipse.swt.widgets.Shell, org.eclipse.debug.core.sourcelookup.ISourceLookupDirector, org.eclipse.debug.core.sourcelookup.ISourceContainer[])
	 */
	@Override
	public ISourceContainer[] editSourceContainers(Shell shell, ISourceLookupDirector director, ISourceContainer[] containers) {
		if (containers.length == 1 && CompilationDirectorySourceContainer.TYPE_ID.equals(containers[0].getType().getId()) ) {
			CompilationDirectorySourceContainer c = (CompilationDirectorySourceContainer) containers[0];
			CompilationDirectorySourceContainerDialog dialog =
					new CompilationDirectorySourceContainerDialog(shell, c.getDirectory().getPath(), c.isComposite());
			if (dialog.open() == Window.OK) {
				String directory = dialog.getDirectory();
				if (directory != null) {
					containers[0].dispose();
					return new ISourceContainer[] { new CompilationDirectorySourceContainer(new Path(directory), dialog.isCompilationSubfolders()) };			
				}
			}
		}
		return containers;
	}
}
