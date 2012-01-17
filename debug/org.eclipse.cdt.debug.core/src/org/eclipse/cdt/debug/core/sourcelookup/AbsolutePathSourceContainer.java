/*******************************************************************************
 * Copyright (c) 2006, 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nokia - Initial implementation (159833)
 *     Broadcom - http://bugs.eclipse.org/247853
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.debug.core.sourcelookup;

import java.io.File;

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.internal.core.sourcelookup.SourceUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.sourcelookup.ISourceContainerType;
import org.eclipse.debug.core.sourcelookup.containers.AbstractSourceContainer;

public class AbsolutePathSourceContainer extends AbstractSourceContainer {
	/**
	 * Unique identifier for the absolute source container type
	 * (value <code>org.eclipse.cdt.debug.core.containerType.absolutePath</code>).
	 */
	public static final String TYPE_ID = CDebugCorePlugin.getUniqueIdentifier() + ".containerType.absolutePath";	 //$NON-NLS-1$

	public boolean isValidAbsoluteFilePath(String name) {
		return isValidAbsoluteFilePath(new File(name));	
	}

	public boolean isValidAbsoluteFilePath(File file) {
		return file.isAbsolute() && file.exists() && file.isFile();	
	}
	
	@Override
	public Object[] findSourceElements(String name) throws CoreException {
		if (name != null) {
			File file = new File(name);
			if (isValidAbsoluteFilePath(file)) {
				return SourceUtils.findSourceElements(file, getDirector());
			}
		}
		return new Object[0];
	}

	@Override
	public String getName() {
		return SourceLookupMessages.AbsolutePathSourceContainer_0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.sourcelookup.ISourceContainer#getType()
	 */
	@Override
	public ISourceContainerType getType() {
		return getSourceContainerType(TYPE_ID);
	}

	@Override
	public int hashCode() {
		return TYPE_ID.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
	    if (!(obj instanceof AbsolutePathSourceContainer))
		    return false;
	    return true;
    }
}
