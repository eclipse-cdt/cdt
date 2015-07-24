/*******************************************************************************
 * Copyright (c) 2015 Mentor Graphics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Mentor Graphics - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.service;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.mi.service.IMIRunControl;

/**
 * @since 4.8
 */
public interface IGDBRunControl extends IMIRunControl {

	public void canRunGDBScript(IContainerDMContext contDmc, DataRequestMonitor<Boolean> rm);

	public void runGDBScript(IContainerDMContext contDmc, String scriptFile, RequestMonitor rm);
}
