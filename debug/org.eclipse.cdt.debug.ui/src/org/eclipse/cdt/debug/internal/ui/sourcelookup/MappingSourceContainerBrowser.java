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

import org.eclipse.cdt.debug.core.sourcelookup.MappingSourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.debug.ui.sourcelookup.AbstractSourceContainerBrowser;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
 
/**
 * Adds a path mapping to the source lookup path.
 */
public class MappingSourceContainerBrowser extends AbstractSourceContainerBrowser {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.sourcelookup.AbstractSourceContainerBrowser#addSourceContainers(org.eclipse.swt.widgets.Shell, org.eclipse.debug.core.sourcelookup.ISourceLookupDirector)
	 */
	public ISourceContainer[] addSourceContainers( Shell shell, ISourceLookupDirector director ) {
		return new ISourceContainer[] { new MappingSourceContainer() };
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.sourcelookup.AbstractSourceContainerBrowser#canAddSourceContainers(org.eclipse.debug.core.sourcelookup.ISourceLookupDirector)
	 */
	public boolean canAddSourceContainers( ISourceLookupDirector director ) {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.sourcelookup.AbstractSourceContainerBrowser#canEditSourceContainers(org.eclipse.debug.core.sourcelookup.ISourceLookupDirector, org.eclipse.debug.core.sourcelookup.ISourceContainer[])
	 */
	public boolean canEditSourceContainers( ISourceLookupDirector director, ISourceContainer[] containers ) {
		return ( containers.length == 1 && containers[0] instanceof MappingSourceContainer );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.sourcelookup.AbstractSourceContainerBrowser#editSourceContainers(org.eclipse.swt.widgets.Shell, org.eclipse.debug.core.sourcelookup.ISourceLookupDirector, org.eclipse.debug.core.sourcelookup.ISourceContainer[])
	 */
	public ISourceContainer[] editSourceContainers( Shell shell, ISourceLookupDirector director, ISourceContainer[] containers ) {
		if ( containers.length == 1 && containers[0] instanceof MappingSourceContainer ) {
			PathMappingDialog dialog = new PathMappingDialog( shell, (MappingSourceContainer)containers[0] );
			if ( dialog.open() == Window.OK ) {
				return new ISourceContainer[] { dialog.getMapping() };
			}
		}
		return new ISourceContainer[0];
	}
}
