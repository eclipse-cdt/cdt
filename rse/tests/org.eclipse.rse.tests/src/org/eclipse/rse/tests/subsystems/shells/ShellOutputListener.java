/********************************************************************************
 * Copyright (c) 2008 MontaVista Software, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Anna Dushistova (MontaVista) - [249102][testing] Improve ShellService Unittests
 ********************************************************************************/
package org.eclipse.rse.tests.subsystems.shells;

import java.util.ArrayList;

import org.eclipse.rse.services.shells.IHostOutput;
import org.eclipse.rse.services.shells.IHostShellChangeEvent;
import org.eclipse.rse.services.shells.IHostShellOutputListener;

public class ShellOutputListener implements IHostShellOutputListener {

	private ArrayList outputs;

	public ShellOutputListener() {
		outputs = new ArrayList();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.rse.services.shells.IHostShellOutputListener#shellOutputChanged
	 * (org.eclipse.rse.services.shells.IHostShellChangeEvent)
	 */
	public void shellOutputChanged(IHostShellChangeEvent event) {
		IHostOutput[] output = event.getLines();
		for (int i = 0; i < output.length; i++)
			outputs.add(output[i]);
	}

	/**
	 * @return
	 */
	public Object[] getAllOutput() {
		return outputs.toArray();
	}

}
