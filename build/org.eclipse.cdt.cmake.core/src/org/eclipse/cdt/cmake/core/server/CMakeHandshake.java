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

public class CMakeHandshake {
	String sourceDirectory;
	String buildDirectory;
	String generator;
	String extraGenerator;
	String platform;
	String toolset;
	CMakeProtocol protocolVersion;

	public CMakeHandshake(String sourceDirectory, String buildDirectory, String generator, String extraGenerator,
			String platform, String toolset, CMakeProtocol protocolVersion) {
		super();
		this.sourceDirectory = sourceDirectory;
		this.buildDirectory = buildDirectory;
		this.generator = generator;
		this.extraGenerator = extraGenerator;
		this.platform = platform;
		this.toolset = toolset;
		this.protocolVersion = protocolVersion;
	}

}
