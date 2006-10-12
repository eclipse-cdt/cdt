/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
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
 * Represents a linkage in the AST or the index.
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will
 * work or that it will remain the same. Please do not use this API without
 * consulting with the CDT team.
 * </p>
 * @since 4.0
 */
public interface ILinkage {
	final static String NO_LINKAGE_ID= "none"; //$NON-NLS-1$
	final static String C_LINKAGE_ID= "C"; //$NON-NLS-1$
	final static String CPP_LINKAGE_ID= "C++"; //$NON-NLS-1$
	final static String FORTRAN_LINKAGE_ID= "Fortran"; //$NON-NLS-1$

	String getID();
}
