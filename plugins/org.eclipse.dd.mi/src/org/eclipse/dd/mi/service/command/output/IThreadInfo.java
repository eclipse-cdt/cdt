/*******************************************************************************
 * Copyright (c) 2008 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - Initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.mi.service.command.output;


/**
 * @since 1.1
 */
public interface IThreadInfo {
	String getThreadId();
	String getTargetId();
	String getOsId();
	IThreadFrame getTopFrame();
	String getDetails();
	String getState();
}
