/********************************************************************************
 * Copyright (c) 2008 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * David Dykstal (IBM) - [197167] adding notification and waiting for RSE model
 ********************************************************************************/
package org.eclipse.rse.core;

/**
 * An IRSEInitListener will be invoked when the initialization of RSE reaches
 * the completion of each phase.
 *
 * @since org.eclipse.rse.core 3.0
 */
public interface IRSEInitListener {

	/**
	 * @param phase The phase of initialization that has completed.
	 * @see RSECorePlugin#INIT_MODEL
	 * @see RSECorePlugin#INIT_ALL
	 */
	public void phaseComplete(int phase);

}
