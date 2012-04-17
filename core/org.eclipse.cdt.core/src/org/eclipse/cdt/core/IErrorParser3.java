/*******************************************************************************
 * Copyright (c) 2012 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alex Ruiz (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core;

/**
 * @since 5.4
 */
public interface IErrorParser3 extends IErrorParser2 {
	/**
	 * Notification that the stream of data to parse has ended.
	 */
	void streamFinished();
}
