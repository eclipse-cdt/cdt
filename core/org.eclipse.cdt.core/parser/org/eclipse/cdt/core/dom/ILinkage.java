/*******************************************************************************
 * Copyright (c) 2006, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.core.dom;

/**
 * Represents a linkage under which bindings are stored in the index.
 * @since 4.0
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ILinkage {
	final static String NO_LINKAGE_NAME= "none"; //$NON-NLS-1$
	final static String C_LINKAGE_NAME= "C"; //$NON-NLS-1$
	final static String CPP_LINKAGE_NAME= "C++"; //$NON-NLS-1$
	final static String FORTRAN_LINKAGE_NAME= "Fortran"; //$NON-NLS-1$
	/**
	 * @since 5.1
	 */
	final static String OBJC_LINKAGE_NAME= "Objective-C"; //$NON-NLS-1$

	final static int NO_LINKAGE_ID= 0;
	final static int CPP_LINKAGE_ID= 1;
	final static int C_LINKAGE_ID= 2;
	final static int FORTRAN_LINKAGE_ID= 3;
	/**
	 * @since 5.1
	 */
	final static int OBJC_LINKAGE_ID= 4;

	
	/**
	 * Additional linkage ids may be added in future.
	 */
	@Deprecated
	final static int MAX_LINKAGE_ID= FORTRAN_LINKAGE_ID;

	/**
	 * Returns the name of the linkage.
	 */
	String getLinkageName();
	
	/**
	 * Returns a unique id for the linkage.
	 */
	int getLinkageID();
}
