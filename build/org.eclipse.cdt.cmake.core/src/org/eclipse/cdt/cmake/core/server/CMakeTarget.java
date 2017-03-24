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

import java.util.List;

public class CMakeTarget {
	public String fullName;
	public String buildDirectory;
	public String linkerLanguage;
	public String name;
	public String type;
	public List<String> artifacts;
	public List<CMakeFileGroup> fileGroups;
}
