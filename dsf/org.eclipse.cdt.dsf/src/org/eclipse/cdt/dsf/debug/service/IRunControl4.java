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

package org.eclipse.cdt.dsf.debug.service;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;

/**
 * @since 2.5
 */
public interface IRunControl4 extends IRunControl3 {

	public interface IContainerDMData {

		public String getExecutable();
	}

	public void getContainerData(IContainerDMContext dmc, DataRequestMonitor<IContainerDMData> rm);
}
