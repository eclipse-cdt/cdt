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

import org.eclipse.core.runtime.IPath;

public class SourceSubstitutePathEntryContainerType extends MapEntrySourceContainerType {
	@Override
	protected MapEntrySourceContainer createEntrySourceContainer(IPath backend, IPath local) {
		return new SourceSubstitutePathEntrySourceContainer(backend, local);
	}
}
