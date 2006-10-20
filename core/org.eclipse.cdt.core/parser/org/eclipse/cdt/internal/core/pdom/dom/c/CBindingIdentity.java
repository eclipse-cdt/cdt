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
package org.eclipse.cdt.internal.core.pdom.dom.c;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.internal.core.Util;
import org.eclipse.cdt.internal.core.dom.bid.AbstractCLocalBindingIdentity;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.core.runtime.CoreException;

public class CBindingIdentity extends AbstractCLocalBindingIdentity {
	public CBindingIdentity(IBinding binding, PDOMLinkage linkage) {
		super(binding, linkage);
	}
	public int getTypeConstant() throws CoreException {
		if(binding instanceof PDOMBinding) {
			return ((PDOMBinding) binding).getNodeType();
		} else {
			return ((PDOMCLinkage)linkage).getBindingType(binding);
		}
	}
	
	public String getExtendedType() throws CoreException {
		if(binding instanceof IFunction) {
			IFunction f = (IFunction) binding;
			try {
				String mangled = ASTTypeUtil.getParameterTypeString(f.getType());
				return mangled;
			} catch(DOMException e) {
				throw new CoreException(Util.createStatus(e));
			}
		} 
		return ""; //$NON-NLS-1$
	}
}
