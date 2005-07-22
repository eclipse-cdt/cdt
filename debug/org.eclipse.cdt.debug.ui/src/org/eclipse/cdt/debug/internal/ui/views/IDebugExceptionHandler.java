/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.views;

import org.eclipse.debug.core.DebugException;
 
/**
 * A plugable  exception handler.
 */
public interface IDebugExceptionHandler {
	/**
	 * Handles the given debug exception.
	 * 
	 * @param e debug exception
	 */
	public abstract void handleException( DebugException e );
}
