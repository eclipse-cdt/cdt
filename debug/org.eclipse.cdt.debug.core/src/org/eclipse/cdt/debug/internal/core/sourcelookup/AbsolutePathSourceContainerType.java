/*******************************************************************************
 * Copyright (c) 2006, 2012 Nokia and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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

	@Override
	public ISourceContainer createSourceContainer(String memento) throws CoreException {
		return new AbsolutePathSourceContainer();
	}

	@Override
	public String getMemento(ISourceContainer container) throws CoreException {
		return "AbsolutePath"; //$NON-NLS-1$
	}

}
