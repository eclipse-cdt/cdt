/*******************************************************************************
 * Copyright (c) 2006, 2015 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Marc Khouzam (Ericsson) - Fix NPE for partial launches (Bug 368597)
 *     Marc Khouzam (Ericsson) - Create the gdb process through the process factory (Bug 210366)
 *     Alvaro Sanchez-Leon (Ericsson AB) - Each memory context needs a different MemoryRetrieval (Bug 250323)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.launching;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ISourceLocator;

/**
 * A class to indicate a launch that should trigger GdbAdapterFactory.
 * Extenders should extend {@link GdbLaunch} directly if they want to
 * provide their own version of GdbAdapterFactory.
 * 
 * @since 5.0
 */
public class GdbLaunchWithAdapterFactory extends GdbLaunch {

	public GdbLaunchWithAdapterFactory(ILaunchConfiguration launchConfiguration, String mode, ISourceLocator locator) {
		super(launchConfiguration, mode, locator);
	}
}