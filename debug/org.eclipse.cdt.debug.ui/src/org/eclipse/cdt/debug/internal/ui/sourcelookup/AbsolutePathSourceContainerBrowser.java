/*******************************************************************************
 * Copyright (c) 2004, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.sourcelookup;

import org.eclipse.cdt.debug.core.sourcelookup.AbsolutePathSourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.debug.ui.sourcelookup.AbstractSourceContainerBrowser;
import org.eclipse.swt.widgets.Shell;

/**
 * Adds a absolute path to the source lookup path.
 */
public class AbsolutePathSourceContainerBrowser extends AbstractSourceContainerBrowser {

	@Override
	public ISourceContainer[] addSourceContainers(Shell shell, ISourceLookupDirector director) {
		return new ISourceContainer[] { new AbsolutePathSourceContainer() };
	}
}
