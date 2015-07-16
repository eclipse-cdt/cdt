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

import org.eclipse.cdt.debug.core.sourcelookup.MappingSourceContainer;
import org.eclipse.cdt.debug.core.sourcelookup.SourceSubstitutePathSourceContainer;

public class SourceSubstitutePathContainerType extends MappingSourceContainerType {

	@Override
	protected MappingSourceContainer newSourceContainer(String name) {
		return new SourceSubstitutePathSourceContainer(name);
	}

	@Override
	protected String getMapEntryTypeId() {
		return SourceSubstitutePathEntrySourceContainer.TYPE_ID;
	}
}
