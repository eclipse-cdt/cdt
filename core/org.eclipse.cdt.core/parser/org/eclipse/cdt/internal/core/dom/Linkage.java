/*******************************************************************************
 * Copyright (c) 2006, 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.dom;

import org.eclipse.cdt.core.dom.ILinkage;

public class Linkage implements ILinkage {

	public static final ILinkage NO_LINKAGE = new Linkage(NO_LINKAGE_ID, NO_LINKAGE_NAME);
	public static final ILinkage C_LINKAGE = new Linkage(C_LINKAGE_ID, C_LINKAGE_NAME);
	public static final ILinkage CPP_LINKAGE = new Linkage(CPP_LINKAGE_ID, CPP_LINKAGE_NAME);
	public static final ILinkage FORTRAN_LINKAGE = new Linkage(FORTRAN_LINKAGE_ID, FORTRAN_LINKAGE_NAME);
	
	private static final ILinkage[] LINKAGES= {C_LINKAGE, CPP_LINKAGE, FORTRAN_LINKAGE};
	
	public static final ILinkage[] getAllLinkages() {
		return LINKAGES;
	}
	
	private int fID;
	private String fName;
	private Linkage(int id, String name) {
		fID= id;
		fName= name;
	}
	public int getLinkageID() {
		return fID;
	}
	public String getLinkageName() {
		return fName;
	}
}
