/*******************************************************************************
 * Copyright (c) 2009 ARM Limited and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     ARM Limited - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.sourcelookup;

import org.eclipse.cdt.debug.core.sourcelookup.MappingSourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.debug.ui.sourcelookup.AbstractSourceContainerBrowser;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;

public class NewMappingSourceContainerBrowser extends AbstractSourceContainerBrowser {
    private static final String MAPPING = SourceLookupUIMessages.MappingSourceContainerBrowser_0;

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.sourcelookup.AbstractSourceContainerBrowser#addSourceContainers(org.eclipse.swt.widgets.Shell, org.eclipse.debug.core.sourcelookup.ISourceLookupDirector)
     */
    @Override
    public ISourceContainer[] addSourceContainers(Shell shell, ISourceLookupDirector director) {
        MappingSourceContainerDialog dialog = 
            	new MappingSourceContainerDialog(shell, new MappingSourceContainer(MAPPING));
        if (dialog.open() == Window.OK) {
            return new ISourceContainer[] { dialog.getContainer() };
        }
        return new ISourceContainer[0];
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.sourcelookup.AbstractSourceContainerBrowser#canEditSourceContainers(org.eclipse.debug.core.sourcelookup.ISourceLookupDirector, org.eclipse.debug.core.sourcelookup.ISourceContainer[])
     */
    @Override
	public boolean canEditSourceContainers(ISourceLookupDirector director, ISourceContainer[] containers) {
        return (containers.length == 1 && containers[0] instanceof MappingSourceContainer);
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.sourcelookup.AbstractSourceContainerBrowser#editSourceContainers(org.eclipse.swt.widgets.Shell, org.eclipse.debug.core.sourcelookup.ISourceLookupDirector, org.eclipse.debug.core.sourcelookup.ISourceContainer[])
     */
    @Override
	public ISourceContainer[] editSourceContainers(Shell shell, ISourceLookupDirector director, ISourceContainer[] containers) {
        if (containers.length == 1 && containers[0] instanceof MappingSourceContainer) {
            MappingSourceContainerDialog dialog = 
                	new MappingSourceContainerDialog(shell, (MappingSourceContainer)containers[0]);
            if (dialog.open() == Window.OK) {
                return new ISourceContainer[] { dialog.getContainer() };
            }
        }
        return new ISourceContainer[0];
    }
}
