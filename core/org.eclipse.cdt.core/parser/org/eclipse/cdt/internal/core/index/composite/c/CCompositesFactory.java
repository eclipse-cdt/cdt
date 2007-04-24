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
package org.eclipse.cdt.internal.core.index.composite.c;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IQualifierType;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.c.ICCompositeTypeScope;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.IIndexScope;
import org.eclipse.cdt.internal.core.index.IIndexType;
import org.eclipse.cdt.internal.core.index.composite.AbstractCompositeFactory;
import org.eclipse.cdt.internal.core.index.composite.CompositeArrayType;
import org.eclipse.cdt.internal.core.index.composite.CompositeFunctionType;
import org.eclipse.cdt.internal.core.index.composite.CompositePointerType;
import org.eclipse.cdt.internal.core.index.composite.CompositeQualifierType;
import org.eclipse.cdt.internal.core.index.composite.CompositingNotImplementedError;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

public class CCompositesFactory extends AbstractCompositeFactory implements ICompositesFactory {
	
	public CCompositesFactory(IIndex index) {
		super(index);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.index.composite.cpp.ICompositesFactory#getCompositeScope(org.eclipse.cdt.core.index.IIndex, org.eclipse.cdt.core.dom.ast.IScope)
	 */
	public IScope getCompositeScope(IIndexScope rscope) {
		if(rscope==null)
			return null;
		if(rscope instanceof ICCompositeTypeScope) {
			try {
				ICCompositeTypeScope cscope = (ICCompositeTypeScope) rscope;
				IIndexFragmentBinding rbinding = (IIndexFragmentBinding) cscope.getCompositeType();
				return ((ICompositeType)getCompositeBinding(rbinding)).getCompositeScope();
			} catch(DOMException de) {
				CCorePlugin.log(de);
			}
		}
		throw new CompositingNotImplementedError();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.index.composite.cpp.ICompositesFactory#getCompositeType(org.eclipse.cdt.core.index.IIndex, org.eclipse.cdt.core.dom.ast.IType)
	 */
	public IType getCompositeType(IIndexType rtype) throws DOMException {
		IType result;
		
		if(rtype==null) {
			result = null;
		} else if(rtype instanceof IFunctionType) {
			result = new CompositeFunctionType((IFunctionType)rtype, this);
		} else if(rtype instanceof ICompositeType) {
			result = (ICompositeType) getCompositeBinding((IIndexFragmentBinding) rtype);
		} else if (rtype instanceof IEnumeration) {
			result = (IEnumeration) getCompositeBinding((IIndexFragmentBinding) rtype);
		} else if(rtype instanceof IPointerType) {
			result = new CompositePointerType((IPointerType)rtype, this);
		} else if(rtype instanceof IQualifierType) {
			result = new CompositeQualifierType((IQualifierType) rtype, this);
		} else if(rtype instanceof IArrayType) {
			result = new CompositeArrayType((IArrayType) rtype, this);
		} else if(rtype instanceof ITypedef) {
			result = new CompositeCTypedef(this, (IIndexFragmentBinding) rtype);
		} else if(rtype instanceof IBasicType) {
			result = rtype; // no context required its a leaf with no way to traverse upward
		} else {
			throw new CompositingNotImplementedError();
		}

		return result;
	}


	/* (non-Javadoc)
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
			result = new CompositeCStructure(this, findOneDefinition(rbinding));
		} else if(rbinding instanceof IEnumeration) {
			result = new CompositeCEnumeration(this, findOneDefinition(rbinding));
		} else if(rbinding instanceof IFunction) {
			result = new CompositeCFunction(this, rbinding);				
		} else if(rbinding instanceof IEnumerator) {
			result = new CompositeCEnumerator(this, rbinding);
		} else if(rbinding instanceof ITypedef) {
			result = new CompositeCTypedef(this, rbinding);
		} else {
			throw new CompositingNotImplementedError("composite binding unavailable for "+rbinding+" "+rbinding.getClass()); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		return result;
	}
}
