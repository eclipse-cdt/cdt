/*******************************************************************************
 * Copyright (c) 2006 Symbian Software and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.bid;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.core.runtime.CoreException;

/**
 * An implementation of ILocalBindingIdentityComparator for CLocalBinding objects
 */
public class CLocalBindingIdentityComparator implements ILocalBindingIdentityComparator {
	protected IBindingIdentityFactory factory;
	
	public CLocalBindingIdentityComparator(IBindingIdentityFactory factory) {
		this.factory = factory;
	}
	
	public int compare(IBinding a, IBinding b) throws CoreException {
		ICLocalBindingIdentity aID = (ICLocalBindingIdentity) factory.getLocalBindingIdentity(a);
		ICLocalBindingIdentity bID = (ICLocalBindingIdentity) factory.getLocalBindingIdentity(b);
		return compare(aID, bID);
	}
		
	public int compare(ILocalBindingIdentity aID, IBinding b) throws CoreException {
		ICLocalBindingIdentity bID = (ICLocalBindingIdentity) factory.getLocalBindingIdentity(b);
		return compare((ICLocalBindingIdentity) aID, bID);
	}
	
	public int compare(IBinding a, ILocalBindingIdentity bID) throws CoreException {
		ICLocalBindingIdentity aID = (ICLocalBindingIdentity) factory.getLocalBindingIdentity(a);
		return compare(aID, bID);
	}
	
	public int compare(ILocalBindingIdentity aID, ILocalBindingIdentity bID) throws CoreException {
		return compare((ICLocalBindingIdentity) aID, (ICLocalBindingIdentity) bID);
	}
	
	public int compare(ICLocalBindingIdentity aID, ICLocalBindingIdentity bID) throws CoreException {
		int cmp = CharArrayUtils.compare(aID.getNameCharArray(), bID.getNameCharArray());
		if(cmp!=0) return cmp;
		
		int tyConA = aID.getTypeConstant();
		int tyConB = bID.getTypeConstant();
		if(tyConA != tyConB) {
			return tyConA < tyConB ? -1 : 1;
		}
		
		cmp = aID.getExtendedType().compareTo(bID.getExtendedType());
		return cmp;
	}
	
	public int compareNameAndConstOnly(IBinding a, char[] bName, int bConst) throws CoreException {
		ICLocalBindingIdentity aID = (ICLocalBindingIdentity) factory.getLocalBindingIdentity(a);
		
		int cmp = CharArrayUtils.compare(aID.getNameCharArray(), bName);
		if(cmp!=0) {
			return cmp;
		}
		
		int tyCon = aID.getTypeConstant();
		if(tyCon != bConst) {
			return tyCon < bConst ? -1 : 1;
		}
		
		return 0;
	}
}

