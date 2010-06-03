/*******************************************************************************
 * Copyright (c) 2006, 2008 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial implementation (159833)
 *******************************************************************************/

package org.eclipse.cdt.debug.internal.core.sourcelookup;

import org.eclipse.cdt.debug.core.sourcelookup.AbsolutePathSourceContainer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.AbstractSourceContainerTypeDelegate;

public class AbsolutePathSourceContainerType extends AbstractSourceContainerTypeDelegate {

	public ISourceContainer createSourceContainer(String memento) throws CoreException {
		return new AbsolutePathSourceContainer();
	}

	public String getMemento(ISourceContainer container) throws CoreException {
		return "AbsolutePath"; //$NON-NLS-1$
	}

}
