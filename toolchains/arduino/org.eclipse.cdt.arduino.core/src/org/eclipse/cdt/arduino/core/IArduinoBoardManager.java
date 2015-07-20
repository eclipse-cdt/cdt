/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.arduino.core;

import java.util.Collection;

/**
 * Interface into the board package data.
 */
public interface IArduinoBoardManager {

	/**
	 * Many of the calls into the board manager require reaching out to the web.
	 * In order to not block UI, these calls are asynchronous. This handler
	 * interface is how the results of the call are returned.
	 *
	 * @param <T>
	 */
	public interface Handler<T> {
		void handle(T result);
	}

	Board getBoard(String id);

	Collection<Board> getBoards();

}
