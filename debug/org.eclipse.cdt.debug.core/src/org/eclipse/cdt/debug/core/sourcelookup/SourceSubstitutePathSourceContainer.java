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

package org.eclipse.cdt.debug.core.sourcelookup;

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.sourcelookup.ISourceContainerType;

/**
 * A mapping source container that translates to GDB's set substitute-path.
 * 
 * By using this type of container, it causes GDB resolve paths and as a result
 * no further source lookup needs to be done within CDT.
 * 
 * @since 7.8
 */
public class SourceSubstitutePathSourceContainer extends MappingSourceContainer {

	public static final String TYPE_ID = CDebugCorePlugin.getUniqueIdentifier() + ".containerType.sourceSubstitutePath"; //$NON-NLS-1$

	public SourceSubstitutePathSourceContainer(String name) {
		super(name);
	}

	@Override
	public ISourceContainerType getType() {
		return getSourceContainerType(TYPE_ID);
	}

	@Override
	protected MappingSourceContainer newMappingSourceContainer(String name) {
		return new SourceSubstitutePathSourceContainer(name);
	}

	@Override
	public Object[] findSourceElements(String name) throws CoreException {
		return new Object[0];
	}

	/**
	 * As the whole point of using SourceSubstitutePath is that the real
	 * compilation path is the same as the source name as far as CDT is
	 * concerned, we always return null, which forces using the input
	 * sourceName.
	 */
	@Override
	public IPath getCompilationPath(String sourceName) {
		return null;
	}
}
