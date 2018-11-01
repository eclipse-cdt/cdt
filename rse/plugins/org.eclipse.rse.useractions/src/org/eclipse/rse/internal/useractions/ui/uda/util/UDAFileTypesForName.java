package org.eclipse.rse.internal.useractions.ui.uda.util;

/*******************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
public class UDAFileTypesForName {
	String name;
	String types;

	public UDAFileTypesForName(String p_name, String p_types) {
		name = p_name;
		types = p_types;
	}

	public String getName() {
		return name;
	}

	public String getTypes() {
		return types;
	}
}
