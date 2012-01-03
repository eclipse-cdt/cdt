/*******************************************************************************
 * Copyright (c) 2006, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.core.runtime.CoreException;

public class Linkage implements ILinkage {
	public static final ILinkage NO_LINKAGE = new Linkage(NO_LINKAGE_ID, NO_LINKAGE_NAME);
	public static final ILinkage C_LINKAGE = new Linkage(C_LINKAGE_ID, C_LINKAGE_NAME);
	public static final ILinkage CPP_LINKAGE = new Linkage(CPP_LINKAGE_ID, CPP_LINKAGE_NAME);
	public static final ILinkage FORTRAN_LINKAGE = new Linkage(FORTRAN_LINKAGE_ID, FORTRAN_LINKAGE_NAME);
	public static final ILinkage OBJC_LINKAGE = new Linkage(OBJC_LINKAGE_ID, OBJC_LINKAGE_NAME);
	
	private static final ILinkage[] LINKAGES= { C_LINKAGE, CPP_LINKAGE, FORTRAN_LINKAGE, OBJC_LINKAGE };
	private static final ILinkage[] INDEX_LINKAGES= { C_LINKAGE, CPP_LINKAGE, FORTRAN_LINKAGE };
	
	public static final ILinkage[] getIndexerLinkages() {
		return INDEX_LINKAGES;
	}

	public static final ILinkage[] getAllLinkages() {
		return LINKAGES;
	}

	public static String getLinkageName(int linkageID) throws CoreException {
		switch(linkageID) {
		case NO_LINKAGE_ID: return NO_LINKAGE_NAME;
		case C_LINKAGE_ID: return C_LINKAGE_NAME;
		case CPP_LINKAGE_ID: return CPP_LINKAGE_NAME;
		case FORTRAN_LINKAGE_ID: return FORTRAN_LINKAGE_NAME;
		case OBJC_LINKAGE_ID: return OBJC_LINKAGE_NAME;
		}
		throw new CoreException(CCorePlugin.createStatus("Unsupported linkage id: " + linkageID)); //$NON-NLS-1$
	}

	private int fID;
	private String fName;

	private Linkage(int id, String name) {
		fID= id;
		fName= name;
	}

	@Override
	public int getLinkageID() {
		return fID;
	}

	@Override
	public String getLinkageName() {
		return fName;
	}
}
