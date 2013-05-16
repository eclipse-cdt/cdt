/*******************************************************************************
 * Copyright (c) 2013 Mentor Graphics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Mentor Graphics - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service;

/**
 * Adjustment of the debugger path is required for earlier versions of GDB to 
 * provide a workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=232415.
 * 
 * @since 4.2
 */
public interface IMIBreakpointPathAdjuster {

	public String adjustDebuggerPath(String originalPath);
}
