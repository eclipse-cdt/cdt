/*******************************************************************************
 * Copyright (c) 2014 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Marc Khouzam (Ericsson) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.core.breakpoints;

import java.util.Map;

import org.eclipse.cdt.debug.core.model.ICDynamicPrintf;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/**
 * Base class for different types of location DynamicPrintf.
 *
 * @since 7.5
 */
public abstract class AbstractDynamicPrintf extends AbstractLineBreakpoint implements ICDynamicPrintf {

	public AbstractDynamicPrintf() {
		super();
	}

	public AbstractDynamicPrintf(IResource resource, Map<String, Object> attributes, boolean add) throws CoreException {
		super(resource, attributes, add);
	}

	@Override
	public String getPrintfString() throws CoreException {
		return ensureMarker().getAttribute(PRINTF_STRING, ""); //$NON-NLS-1$
	}

	@Override
	public void setPrintfString(String str) throws CoreException {
		setAttribute(PRINTF_STRING, str);
		setAttribute(IMarker.MESSAGE, getMarkerMessage());
	}
}
