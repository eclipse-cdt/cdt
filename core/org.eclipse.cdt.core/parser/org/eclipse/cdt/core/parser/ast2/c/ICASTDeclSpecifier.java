/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.core.parser.ast2.c;

import java.util.List;

/**
 * @author Doug Schaefer
 */
public interface ICASTDeclSpecifier extends ICASTNode {

	/**
	 * This is the storage class for the declaration. At most one storage
	 * class specifier can be use so this is an enum. C specifies typedef
	 * as a storage class but that is silly and is fixed in C++. C++ also
	 * adds in mutable.
	 * 
	 * @return the storage class for this declaration.
	 */
	public int getStorageClass();
	public static final int SC_EXTERN = 1;
	public static final int SC_STATIC = 2;
	public static final int SC_AUTO = 3;
	public static final int SC_REGISTER = 4;
	public static final int SC_MUTABLE = 5;

	public boolean isInline();
	public boolean isVirtual();
	public boolean isExplicit();
	
	public boolean isFriend();
	public boolean isTypedef();
	
	/**
	 * @return List of ICASTTypeSpecifier
	 */
	public List getTypeSpecifiers();

}
