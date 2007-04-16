/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.settings.model;

public interface ICSettingEntry {
	int BUILTIN = 1;
	int READONLY = 1 << 1;
	int LOCAL = 1 << 2;
	int VALUE_WORKSPACE_PATH = 1 << 3;
	int RESOLVED = 1 << 4;
	
	int INCLUDE_PATH = 1;
	int INCLUDE_FILE = 1 << 1;
	int MACRO = 1 << 2;
	int MACRO_FILE = 1 << 3;
	int LIBRARY_PATH = 1 << 4;
	int LIBRARY_FILE = 1 << 5;
	int OUTPUT_PATH = 1 << 6;
	int SOURCE_PATH = 1 << 7;
	int ALL = ~0;

	boolean isReadOnly();
	
	int getKind();
	
	String getName();

	String getValue();
	
	boolean isBuiltIn();
	
	boolean isResolved();
	
	boolean equalsByName(ICSettingEntry entry);

	boolean equalsByContents(ICSettingEntry entry);

	int getFlags();

}
