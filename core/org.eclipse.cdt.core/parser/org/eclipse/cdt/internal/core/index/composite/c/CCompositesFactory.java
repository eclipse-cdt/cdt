/*******************************************************************************
 * Copyright (c) 2007, 2010 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.c;

import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.c.ICArrayType;
import org.eclipse.cdt.core.dom.ast.c.ICCompositeTypeScope;
import org.eclipse.cdt.core.dom.ast.c.ICPointerType;
import org.eclipse.cdt.core.dom.ast.c.ICQualifierType;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexMacroContainer;
import org.eclipse.cdt.internal.core.dom.parser.c.CArrayType;
import org.eclipse.cdt.internal.core.dom.parser.c.CFunctionType;
import org.eclipse.cdt.internal.core.dom.parser.c.CPointerType;
import org.eclipse.cdt.internal.core.dom.parser.c.CQualifierType;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.IIndexScope;
import org.eclipse.cdt.internal.core.index.composite.AbstractCompositeFactory;
import org.eclipse.cdt.internal.core.index.composite.CompositeMacroContainer;
import org.eclipse.cdt.internal.core.index.composite.CompositingNotImplementedError;

public class CCompositesFactory extends AbstractCompositeFactory {
	
	public CCompositesFactory(IIndex index) {
		super(index);
	}
	
	/* 
	 * @see org.eclipse.cdt.internal.core.index.composite.cpp.ICompositesFactory#getCompositeScope(org.eclipse.cdt.core.index.IIndex, org.eclipse.cdt.core.dom.ast.IScope)
	 */
	public IIndexScope getCompositeScope(IIndexScope rscope) {
		if(rscope==null)
			return null;
		if(rscope instanceof ICCompositeTypeScope) {
			ICCompositeTypeScope cscope = (ICCompositeTypeScope) rscope;
			IIndexFragmentBinding rbinding = (IIndexFragmentBinding) cscope.getCompositeType();
			return (IIndexScope) ((ICompositeType)getCompositeBinding(rbinding)).getCompositeScope();
		}
		throw new CompositingNotImplementedError();
	}

	/* 
	 * @see org.eclipse.cdt.internal.core.index.composite.cpp.ICompositesFactory#getCompositeType(org.eclipse.cdt.core.index.IIndex, org.eclipse.cdt.core.dom.ast.IType)
	 */
	public IType getCompositeType(IType rtype) {

		if (rtype instanceof IIndexFragmentBinding) {
			return (IType) getCompositeBinding((IIndexFragmentBinding) rtype);
		} 
		if (rtype instanceof IFunctionType) {
			IFunctionType ft= (IFunctionType) rtype;
			IType r= ft.getReturnType();
			IType r2= getCompositeType(r);
			IType[] p= ft.getParameterTypes();
			IType[] p2= getCompositeTypes(p);
			if (r != r2 || p != p2) {
				return new CFunctionType(r2, p2);
			}
			return ft;
		} 
		if (rtype instanceof ICPointerType) {
			ICPointerType pt= (ICPointerType) rtype;
			IType r= pt.getType();
			IType r2= getCompositeType(r);
			if (r != r2) {
				int q= 0;
				if (pt.isConst())
					q |= CPointerType.IS_CONST;
				if (pt.isVolatile())
					q |= CPointerType.IS_VOLATILE;
				if (pt.isRestrict())
					q |= CPointerType.IS_RESTRICT;
				return new CPointerType(r2, q);
			}
			return pt;
		}
		if (rtype instanceof ICQualifierType) {
			ICQualifierType qt= (ICQualifierType) rtype;
			IType r= qt.getType();
			IType r2= getCompositeType(r);
			if (r != r2) {
				return new CQualifierType(r2, qt.isConst(), qt.isVolatile(), qt.isRestrict());
			}
			return qt;
		} 
		if (rtype instanceof ICArrayType) {
			ICArrayType at= (ICArrayType) rtype;
			IType r= at.getType();
			IType r2= getCompositeType(r);
			IValue v= at.getSize();
			IValue v2= getCompositeValue(v);
			if (r != r2 || v != v2) {
				CArrayType at2 = new CArrayType(r2, at.isConst(), at.isVolatile(), at.isRestrict(), v2);
				at2.setIsStatic(at.isStatic());
				at2.setIsVariableLength(at.isVariableLength());
			}
			return at;
		} 
		if (rtype instanceof IBasicType || rtype == null) {
			return rtype;
		} 
		
		throw new CompositingNotImplementedError();
	}

	public IValue getCompositeValue(IValue v) {
		return v;
	}

	/* 
	 * @see org.eclipse.cdt.internal.core.index.composite.cpp.ICompositesFactory#getCompositeBinding(org.eclipse.cdt.core.index.IIndex, org.eclipse.cdt.core.dom.ast.IBinding)
	 */
	public IIndexBinding getCompositeBinding(IIndexFragmentBinding rbinding) {
		IIndexBinding result;
		
		if(rbinding==null) {
			result = null; 
		} else if(rbinding instanceof IParameter) {
			result = new CompositeCParameter(this, rbinding);
		} else if(rbinding instanceof IField) {
			result = new CompositeCField(this, rbinding);
		} else if(rbinding instanceof IVariable) {
			result = new CompositeCVariable(this, rbinding);
		} else if(rbinding instanceof ICompositeType) {
			result = new CompositeCStructure(this, findOneBinding(rbinding, false));
		} else if(rbinding instanceof IEnumeration) {
			result = new CompositeCEnumeration(this, findOneBinding(rbinding, false));
		} else if(rbinding instanceof IFunction) {
			result = new CompositeCFunction(this, rbinding);				
		} else if(rbinding instanceof IEnumerator) {
			result = new CompositeCEnumerator(this, rbinding);
		} else if(rbinding instanceof ITypedef) {
			result = new CompositeCTypedef(this, rbinding);
		} else if(rbinding instanceof IIndexMacroContainer) {
			result= new CompositeMacroContainer(this, rbinding);
		} else {
			throw new CompositingNotImplementedError("composite binding unavailable for "+rbinding+" "+rbinding.getClass()); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		return result;
	}
}
