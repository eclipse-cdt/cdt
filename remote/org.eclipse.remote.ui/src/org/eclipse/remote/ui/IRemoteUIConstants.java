/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.remote.ui;

/**
 * Constants use by UI operations.
 */
public interface IRemoteUIConstants {
	/**
	 * A constant indicating that no bits are set.
	 */
	public static int NONE = 0;

	/**
	 * A constant used to indicate a dialog used for opening files.
	 */
	public static int OPEN = 1 << 1;

	/**
	 * A constant used to indicate a dialog should be used for saving files.
	 */
	public static int SAVE = 1 << 2;
}
