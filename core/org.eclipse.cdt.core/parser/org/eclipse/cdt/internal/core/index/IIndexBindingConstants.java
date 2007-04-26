/*******************************************************************************
 * Copyright (c) 2007 Symbian Software Systems and others.
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
public interface IIndexBindingConstants {
	 int POINTER_TYPE= 1;
	 int ARRAY_TYPE= 2;
	 int QUALIFIER_TYPE= 3;
	 int FILE_LOCAL_SCOPE_TYPE= 4;
	 int LAST_CONSTANT= FILE_LOCAL_SCOPE_TYPE;
}
