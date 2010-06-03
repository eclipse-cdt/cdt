/*******************************************************************************
 * Copyright (c) 2008, 2010 Freescale and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Freescale - Initial implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.internal.core.sourcelookup;

import org.eclipse.cdt.debug.core.sourcelookup.ProgramRelativePathSourceContainer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.AbstractSourceContainerTypeDelegate;

public class ProgramRelativePathSourceContainerType extends AbstractSourceContainerTypeDelegate {

	private final static String ELEMENT_NAME = "programRelativePath"; //$NON-NLS-1$

	public ISourceContainer createSourceContainer(String memento) throws CoreException {
		if ( ELEMENT_NAME.equals( memento ) ) {
			return new ProgramRelativePathSourceContainer();
		}
		abort( InternalSourceLookupMessages.getString("ProgramRelativePathSourceContainerType.1"), null ); //$NON-NLS-1$
		return null;
	}

	public String getMemento(ISourceContainer container) throws CoreException {
		if (container instanceof ProgramRelativePathSourceContainer){		
			return ELEMENT_NAME;
		}
		else{
			return ""; //$NON-NLS-1$
		}
	}
}
