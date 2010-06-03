/*******************************************************************************
 * Copyright (c) 2008, 2009 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.cdtvariables;

/**
 * Public interface to access StorableCdtVariables class methods
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IStorableCdtVariables {
	ICdtVariable getMacro(String name);
	ICdtVariable[] getMacros();
	boolean deleteAll();
	boolean contains(ICdtVariable var);
	ICdtVariable deleteMacro(String name);
	boolean isChanged();
	ICdtVariable createMacro(ICdtVariable copy);
	ICdtVariable createMacro(String name, int type, String value);
	ICdtVariable createMacro(String name, int type, String value[]);
	void createMacros(ICdtVariable macros[]);
	boolean isEmpty();
}
