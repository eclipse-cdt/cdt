/*******************************************************************************
 * Copyright (c) 2014 - 2018 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.terminal.view.core.interfaces;

/**
 * An interface to be implemented by listeners who want to listen
 * to the streams data without interfering with the original data receiver.
 * <p>
 * Listeners are invoked within the monitor processing thread.
 */
public interface ITerminalServiceOutputStreamMonitorListener {

	/**
	 * Signals that some content has been read from the monitored stream.
	 *
	 * @param byteBuffer The byte stream. Must not be <code>null</code>.
	 * @param bytesRead The number of bytes that were read into the read buffer.
	 */
	public void onContentReadFromStream(byte[] byteBuffer, int bytesRead);
}
