/*******************************************************************************
 * Copyright (c) 2008, 2009 Takuya Miyamoto and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Takuya Miyamoto - initial API and implementation
 *******************************************************************************/
package org.eclipse.rse.internal.synchronize.provisional;

import org.eclipse.core.resources.IResource;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;

/**
 * Helper class to calculate byte array for synchronization in some algorithm.
 * This interface is used in comparison of resource and resourceVariant. In the
 * comparison, it's necessary to calculate byte array associated with them.
 * 
 */
public interface ISynchronizeByteCalculator {
	/**
	 * Timestamp: The last modification time is used for comparison
	 */
	public static final int DIFF_TYPE_TIMESTAMP = 0;
	/**
	 * MD5: The message digest 5 is used for comparison
	 */
	public static final int DIFF_TYPE_MD5 = 1;

	/**
	 * Return byte array for comparison used in synchronization. The criterion
	 * is specified by option.
	 * 
	 * @param local
	 * @param option
	 * @return
	 */
	public byte[] cacByte(IResource local, int option);

	/**
	 * Return byte array for comparison used in synchronization. The criterion
	 * is specified by option.
	 * 
	 * @param remote
	 * @param option
	 * @return
	 */
	public byte[] calcByte(IRemoteFile remote, int option);

}
