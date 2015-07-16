/*******************************************************************************
 * Copyright (c) 2015 Kichwa Coders and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Jonah Graham (Kichwa Coders) - initial API and implementation to Add support for gdb's "set substitute-path" (Bug 472765)
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.sourcelookup;

import org.eclipse.cdt.debug.core.sourcelookup.MappingSourceContainer;
import org.eclipse.cdt.debug.core.sourcelookup.SourceSubstitutePathSourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.debug.ui.sourcelookup.AbstractSourceContainerBrowser;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;

// copied from NewMappingSourceContainerBrowser
public class NewSourceSubstiturePathSourceContainerBrowser extends AbstractSourceContainerBrowser {
	private static final String NEW_SOURCE_SUB_MESSAGE = "New Source Substitute Path";

	@Override
	public ISourceContainer[] addSourceContainers(Shell shell, ISourceLookupDirector director) {
		SourceSubstiturePathSourceContainerDialog dialog = new SourceSubstiturePathSourceContainerDialog(shell,
				new SourceSubstitutePathSourceContainer(NEW_SOURCE_SUB_MESSAGE));
		if (dialog.open() == Window.OK) {
			return new ISourceContainer[] { dialog.getContainer() };
		}
		return new ISourceContainer[0];
	}

	@Override
	public boolean canEditSourceContainers(ISourceLookupDirector director, ISourceContainer[] containers) {
		return (containers.length == 1 && containers[0] instanceof MappingSourceContainer);
	}

	@Override
	public ISourceContainer[] editSourceContainers(Shell shell, ISourceLookupDirector director,
			ISourceContainer[] containers) {
		if (containers.length == 1 && containers[0] instanceof SourceSubstitutePathSourceContainer) {
			SourceSubstiturePathSourceContainerDialog dialog = new SourceSubstiturePathSourceContainerDialog(shell,
					(SourceSubstitutePathSourceContainer) containers[0]);
			if (dialog.open() == Window.OK) {
				return new ISourceContainer[] { dialog.getContainer() };
			}
		}
		return new ISourceContainer[0];
	}
}
