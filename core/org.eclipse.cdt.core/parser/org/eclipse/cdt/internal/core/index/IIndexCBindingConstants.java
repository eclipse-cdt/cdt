/*******************************************************************************
 * Copyright (c) 2007, 2009 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
