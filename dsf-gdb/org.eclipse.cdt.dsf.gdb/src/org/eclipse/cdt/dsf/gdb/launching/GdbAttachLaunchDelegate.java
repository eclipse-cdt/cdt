/*******************************************************************************
 * Copyright (c) 2010 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Ericsson   - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.launching; 

import org.eclipse.cdt.dsf.concurrent.ThreadSafe;
 
@ThreadSafe
public class GdbAttachLaunchDelegate extends GdbLaunchDelegate
{
	public GdbAttachLaunchDelegate() {
		// For an attach session, we don't require a project
		// to be specified
		super(false);
	}
}
