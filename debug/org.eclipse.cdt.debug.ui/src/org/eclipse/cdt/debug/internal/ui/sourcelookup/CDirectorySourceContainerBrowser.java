/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/ 
package org.eclipse.cdt.debug.internal.ui.sourcelookup; 

import org.eclipse.cdt.debug.core.sourcelookup.CDirectorySourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.debug.ui.sourcelookup.AbstractSourceContainerBrowser;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
 
/**
 * The browser for adding and editing a directory source container.
 */
public class CDirectorySourceContainerBrowser extends AbstractSourceContainerBrowser {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.sourcelookup.AbstractSourceContainerBrowser#addSourceContainers(org.eclipse.swt.widgets.Shell, org.eclipse.debug.core.sourcelookup.ISourceLookupDirector)
	 */
	public ISourceContainer[] addSourceContainers( Shell shell, ISourceLookupDirector director ) {
		CDirectorySourceContainerDialog dialog = new CDirectorySourceContainerDialog( shell );
		if ( dialog.open() == Window.OK ) {
			return new ISourceContainer[] { new CDirectorySourceContainer( dialog.getDirectory(), dialog.isSearchSubfolders() ) };
		}
		return new ISourceContainer[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.sourcelookup.AbstractSourceContainerBrowser#canAddSourceContainers(org.eclipse.debug.core.sourcelookup.ISourceLookupDirector)
	 */
	public boolean canAddSourceContainers( ISourceLookupDirector director ) {
		return true;
	}
}
