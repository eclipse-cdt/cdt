/*******************************************************************************
 * Copyright (c) 2009,2010 Alena Laskavaia
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.model.cfg;

/**
 * Interface to access data object that control flow graph block carries,
 * usually it is an ast node.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICfgData {
	/**
	 * @return data object
	 */
	public abstract Object getData();

	/**
	 * Sets data object for the node
	 *
	 * @param data
	 */
	public abstract void setData(Object data);
}