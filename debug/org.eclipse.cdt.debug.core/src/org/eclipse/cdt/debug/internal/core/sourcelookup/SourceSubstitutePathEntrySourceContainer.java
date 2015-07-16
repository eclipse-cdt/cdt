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
package org.eclipse.cdt.debug.internal.core.sourcelookup;

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.sourcelookup.ISourceSubstitutePathContainer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.sourcelookup.ISourceContainerType;

public class SourceSubstitutePathEntrySourceContainer extends MapEntrySourceContainer
		implements ISourceSubstitutePathContainer {

	public static final String TYPE_ID = CDebugCorePlugin.getUniqueIdentifier() + ".containerType.sourceSubstitutePathEntry"; //$NON-NLS-1$

	public SourceSubstitutePathEntrySourceContainer() {
		super();
	}

	public SourceSubstitutePathEntrySourceContainer(IPath backend, IPath local) {
		super(backend, local);
	}
	
	@Override
	public Object[] findSourceElements(String name) throws CoreException {
		return new Object[0];
	}
	
	@Override
	public ISourceContainerType getType() {
		return getSourceContainerType(TYPE_ID);
	}
	
	@Override
	public MapEntrySourceContainer copy() {
		return new SourceSubstitutePathEntrySourceContainer(getBackendPath(), getLocalPath());
	}
}
