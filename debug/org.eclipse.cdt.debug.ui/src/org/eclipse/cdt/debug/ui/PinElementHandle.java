/*****************************************************************
 * Copyright (c) 2010 Texas Instruments and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Patrick Chuong (Texas Instruments) - Pin and Clone Supports (331781)
 *     Patrick Chuong (Texas Instruments) - Add support for icon overlay in the debug view (Bug 334566)
 *****************************************************************/
package org.eclipse.cdt.debug.ui;

import org.eclipse.cdt.debug.ui.IPinProvider.IPinElementColorDescriptor;
import org.eclipse.cdt.debug.ui.IPinProvider.IPinElementHandle;
import org.eclipse.core.runtime.PlatformObject;

/**
 * A class that encapsulates the pin element handle and implements <code>IPinHandleLableProvider</code>.
 * 
 * @since 7.1
 */
public class PinElementHandle extends PlatformObject implements IPinElementHandle {
	private Object fDebugContext;
	private String fLabel;
	private IPinElementColorDescriptor fColorDescriptor;
	
	public PinElementHandle(Object debugContext, String label, IPinElementColorDescriptor colorDescriptor) {
		fDebugContext = debugContext;
		fLabel = label;
		fColorDescriptor = colorDescriptor;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.debug.ui.IPinProvider.IPinElementHandle#getDebugContext()
	 */
	@Override
	public synchronized Object getDebugContext() {
		return fDebugContext;
	}	
	
	/**
	 * Sets the debug context.
	 * 
	 * @param debugContext the new debug context
	 */
	public synchronized void setDebugContext(Object debugContext) {
		fDebugContext = debugContext;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.IPinProvider.IHandleLabelProvider#getLabel()
	 */
	@Override
	public String getLabel() {
		return fLabel;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.debug.ui.IPinProvider.IPinElementHandle#getPinElementColorDescriptor()
	 */
	@Override
	public IPinElementColorDescriptor getPinElementColorDescriptor() {
		return fColorDescriptor;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PinElementHandle) {
			if (fDebugContext == null)
				return ((PinElementHandle) obj).getDebugContext() == null;
			else
				return fDebugContext.equals(((PinElementHandle) obj).getDebugContext());
		}
		return false;
	}
}
