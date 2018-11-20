/*******************************************************************************
 * Copyright (c) 2011, 2012 Anton Gorenkov
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Anton Gorenkov - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.testsrunner.model;

/**
 * Represents a failure in the Tests Runner operations.
 */
public class TestingException extends Exception {

	/**
	 * Constructs an exception with the given descriptive message.
	 *
	 * @param msg Description of the occurred exception.
	 */
	public TestingException(String msg) {
		super(msg);
	}

}
