/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.core.cdi;

import org.eclipse.cdt.debug.core.cdi.model.ICDIWatchpoint;

/**
 * 
 * Represents an information provided by the session when a watchpoint 
 * is triggered.
 * 
 * @since Aug 27, 2002
 */
public interface ICDIWatchpointTrigger extends ICDISessionObject {
	/**
	 * Returns the triggered watchpoint.
	 * 
	 * @return the triggered watchpoint
	 */
	ICDIWatchpoint getWatchpoint();
	
	/**
	 * Returns the old value of the watching expression.
	 * 
	 * @return the old value of the watching expression
	 */
	String getOldValue();
	
	/**
	 * Returns the new value of the watching expression.
	 * 
	 * @return the new value of the watching expression
	 */
	String getNewValue();
}
