/*********************************************************************************
 * Copyright (c) 2008 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License 2.0 which accompanies this distribution, and is 
 * available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 * David Dykstal (IBM) - [197167] initial contribution.
 *********************************************************************************/

package org.eclipse.rse.tests.initialization;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.rse.core.IRSEInitListener;

/**
 * A listener for initialization
 */
public class InitListener implements IRSEInitListener {
	
	Set phases = new HashSet();

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.IRSEInitListener#phaseComplete(int)
	 */
	public void phaseComplete(int phase) {
		phases.add(new Integer(phase));
	}
	
	public boolean sawPhase(int phase) {
		return phases.contains(new Integer(phase));
	}

}
