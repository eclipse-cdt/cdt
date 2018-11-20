/*******************************************************************************
 * Copyright (c) 2008, 2009 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
