/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.contentassist;

/**
 * @author hamer
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public interface ITimeoutThreadOwner {
	/**
	 * Sets the timeout limit for the timer
	 * @param timeout
	 */
	public void setTimeout(int timeout);
	/**
	 * Starts the timer
	 */
	public void startTimer();
	/**
	 * Stops the timer
	 */
	public void stopTimer();
}
