/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.core.model;

/**
 *
 * Enter type comment.
 * 
 * @since: Sep 30, 2002
 */
public interface ICDebugTargetType
{
	public static final int TARGET_TYPE_UNKNOWN = 0;
	public static final int TARGET_TYPE_LOCAL_RUN = 1;
	public static final int TARGET_TYPE_LOCAL_ATTACH = 2;
	public static final int TARGET_TYPE_LOCAL_CORE_DUMP = 3;
	
	/**
	 * Returns the type of this target.
	 * 
	 * @return the type of this target
	 */
	int getTargetType();
}
