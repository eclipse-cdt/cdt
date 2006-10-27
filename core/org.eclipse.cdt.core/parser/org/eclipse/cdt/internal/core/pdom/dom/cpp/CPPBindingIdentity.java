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
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.internal.core.Util;
import org.eclipse.cdt.internal.core.dom.bid.AbstractCLocalBindingIdentity;
import org.eclipse.cdt.internal.core.dom.bid.ICLocalBindingIdentity;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.core.runtime.CoreException;

public class CPPBindingIdentity extends AbstractCLocalBindingIdentity {
	public CPPBindingIdentity(IBinding binding, PDOMLinkage linkage) {
		super(binding, linkage);
	}
	
	public int getTypeConstant() throws CoreException {
		if(binding instanceof PDOMBinding) {
			return ((PDOMBinding) binding).getNodeType();
		} else {
			return ((PDOMCPPLinkage)linkage).getBindingType(binding);
		}
	}
	
	public String getExtendedType() throws CoreException {
		try {
			if(binding instanceof ICPPFunction) {
				return renderFunctionType(((ICPPFunction)binding).getType());
			} else if(binding instanceof ICPPMethod) {
				return renderFunctionType(((ICPPMethod)binding).getType());
			} else {
				return ""; //$NON-NLS-1$
			}
		} catch(DOMException e) {
			throw new CoreException(Util.createStatus(e));
		}
	}
	
	protected static String renderTypes(IType[] types) throws DOMException {
		if(types.length==1) {
			if(types[0] instanceof IBasicType) {
				if(((IBasicType)types[0]).getType()==IBasicType.t_void) {
					types = new IType[0];
				}
			}
		}
		
		StringBuffer result = new StringBuffer();
		result.append('(');
		for(int i=0; i<types.length; i++) {
			if (i>0) {
				result.append(',');
			}
			result.append(ASTTypeUtil.getType(types[i]));
		}
		result.append(')');
		return result.toString();
	}
	
	private String renderFunctionType(IFunctionType type) throws DOMException {
		IType[] params = type.getParameterTypes();	
		return renderTypes(params);
	}
	
	public static class Holder implements ICLocalBindingIdentity {
		String name;
		int type;
		String mangledExtendedType;
		
		public Holder(String name, int type, IType[] types) throws DOMException {
			this.name = name;
			this.type = type;
			mangledExtendedType = renderTypes(types);
		}
		
		public int getTypeConstant() throws CoreException {
			return type;
		}
		
		public String getName() throws CoreException {
			return name;
		}
		
		public String getExtendedType() throws CoreException {
			return mangledExtendedType;
		}
		
		public String toString() {
			return name+" "+type+" "+mangledExtendedType; //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		public char[] getNameCharArray() throws CoreException {
			return name.toCharArray();
		}
	}
}
