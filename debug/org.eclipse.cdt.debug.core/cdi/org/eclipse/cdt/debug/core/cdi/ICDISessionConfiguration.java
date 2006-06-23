/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.core.cdi;

/**
 * Describes the configuration of debug session.
 * 
 * @since Aug 6, 2002
 */
public interface ICDISessionConfiguration extends ICDISessionObject {

	/**
	 * Returns whether the session should be terminated when the inferior exits.
	 *
	 * @return whether the session be terminated when the inferior exits
	 */
	boolean terminateSessionOnExit();
}
