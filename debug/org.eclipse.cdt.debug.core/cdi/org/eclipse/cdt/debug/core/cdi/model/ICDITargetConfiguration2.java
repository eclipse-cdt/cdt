/*******************************************************************************
 * Copyright (c) 2006 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nokia - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.core.cdi.model;

public interface ICDITargetConfiguration2 extends ICDITargetConfiguration {

	/**
	 * Returns whether this target supports thread control, namely whether it
	 * supports suspending/resuming threads individually.
	 * 
	 * @return  whether this target supports thread control, namely whether it
	 * supports suspending/resuming threads individually.
	 */
	boolean supportsThreadControl();

	/**
	 * Returns whether this target supports passive variable updating. If so
	 * targets will not be actively sending variable value change notification
	 * when a thread is suspended but will wait until they are asked to
	 * redisplay the value. Passive variable updating lets a CDI plugin avoid
	 * maintaining its own variable cache and having to keep it in sync with
	 * CDT's. Targets that support this feature will need to be able to detect
	 * when a variable value has changed and fire a changedEvent in its
	 * implementation of ICDIValue.getValueString().
	 * 
	 * @return whether this target supports passive variable updating.
	 */
	boolean supportsPassiveVariableUpdate();
	
	/**
	 * Returns whether this target supports runtime type indentification.
	 * If so this means the type of a variable may change when its value changes.
	 * 
	 * @return  whether this target supports runtime type indentification.
	 * If so this means the type of a variable may change when its value changes.
	 */
	boolean supportsRuntimeTypeIdentification();

}
