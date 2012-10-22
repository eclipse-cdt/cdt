/*******************************************************************************
 * Copyright (c) 2012 Mentor Graphics and others.
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
 * Extension to the Breakpoints service that allows breakpoint tracking 
 * notifications from clients.
 * @since 4.2
 */
public interface IMIBreakpointsExtension {
	
	public void breakpointTrackingStarted();
	
	public void breakpointTrackingStopped();
}
