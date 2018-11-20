/*******************************************************************************
 * Copyright (c) 2007, 2011 Symbian Software Systems and others.
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
public interface IIndexCBindingConstants {
	int CENUMERATOR = IIndexBindingConstants.ENUMERATOR;

	int CVARIABLE = IIndexBindingConstants.LAST_CONSTANT + 1;
	int CFUNCTION = IIndexBindingConstants.LAST_CONSTANT + 2;
	int CSTRUCTURE = IIndexBindingConstants.LAST_CONSTANT + 3;
	int CFIELD = IIndexBindingConstants.LAST_CONSTANT + 4;
	int CENUMERATION = IIndexBindingConstants.LAST_CONSTANT + 5;
	int CTYPEDEF = IIndexBindingConstants.LAST_CONSTANT + 7;
	int CPARAMETER = IIndexBindingConstants.LAST_CONSTANT + 8;
}
