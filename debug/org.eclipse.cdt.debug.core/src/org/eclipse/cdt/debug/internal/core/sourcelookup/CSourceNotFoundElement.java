/*******************************************************************************
 * Copyright (c) 2006 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nokia - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.internal.core.sourcelookup;

import org.eclipse.cdt.debug.core.model.ICStackFrame;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;

/**
 * Wrapper for debug elements that have missing source, for example a stack frame
 * whose source file can not be located. Used to enable the CSourceNotFoundEditor
 * that will let you find the missing file.
 *
 */
public class CSourceNotFoundElement implements IDebugElement{

	private IDebugElement element;

	public IDebugElement getElement() {
		return element;
	}

	public CSourceNotFoundElement(IDebugElement element)
	{
		this.element = element;
	}

	public IDebugTarget getDebugTarget() {
		return element.getDebugTarget();
	}

	public ILaunch getLaunch() {
		return element.getLaunch();
	}

	public String getModelIdentifier() {
		return element.getModelIdentifier();
	}

	public Object getAdapter(Class adapter) {
		return element.getAdapter(adapter);
	}

	public String getFile() {
		ICStackFrame frame = (ICStackFrame)((IAdaptable)element).getAdapter( ICStackFrame.class );
		if ( frame != null ) {
			return frame.getFile().trim();
		}
		return "";
	}
	
	
}
