/*******************************************************************************
 * Copyright (c) 2007, 2013 Symbian Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index;

/**
 * Constants used by IIndexFragment implementations for identifying persisted binding types
 */
public interface IIndexBindingConstants {
	int LINKAGE = 0;
	int ENUMERATOR = 3;
	int MACRO_DEFINITION = 4;
	int MACRO_CONTAINER = 5;
	int LAST_CONSTANT = MACRO_CONTAINER;
}
