/*******************************************************************************
 * Copyright (c) 2017 IAR Systems AB and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jesper Eskilson (IAR Systems AB) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.cmake.core.server;

public class CMakeProgress {
	public String progressMessage;
	public int progressMinimum;
	public int progressMaximum;
	public int progressCurrent;

	public CMakeProgress(String progressMessage, int progressMinimum, int progressMaximum, int progressCurrent) {
		super();
		this.progressMessage = progressMessage;
		this.progressMinimum = progressMinimum;
		this.progressMaximum = progressMaximum;
		this.progressCurrent = progressCurrent;
	}
}
