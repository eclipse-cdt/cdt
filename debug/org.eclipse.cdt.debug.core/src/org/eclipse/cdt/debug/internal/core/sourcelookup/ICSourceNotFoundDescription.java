/*******************************************************************************
 * Copyright (c) 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nokia - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.internal.core.sourcelookup;


/**
 * This interface is used to provide a description of a debug element,
 * usually a stack frame, when no source can be located for it.
 * An instance is usually provided by an adapter.
 */
public interface ICSourceNotFoundDescription {

	/**
	 * Returns a description of the debug element suitable for use by the
	 * CSourceNotFoundEditor. This description is then used by the editor to
	 * inform the user when describing what it can't locate source for.
	 * 
	 * @return the description of the debug element, or null if not available
	 */
	String getDescription();

}
