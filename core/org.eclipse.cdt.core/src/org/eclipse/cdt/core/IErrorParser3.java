/*******************************************************************************
 * Copyright (c) 2012 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alex Ruiz  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core;

/**
 * @author alruiz@google.com (Alex Ruiz)
 */
public interface IErrorParser3 extends IErrorParser {
	/**
	 * Notification that the stream of data to parse has ended.
	 */
	void streamFinished();
}
