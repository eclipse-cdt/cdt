/*******************************************************************************
 * Copyright (c) 2012 Sage Electronic Engineering, LLC. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Jason Litton (Sage Electronic Engineering, LLC) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb;

/**
 * An abstract interface for those listening for GDB Error messages.
 * Implementing classes will be notified of GDB errors as Strings
 */
public interface IGdbErrorListener {
	public abstract void gdbErrorNotification(String message);
}
