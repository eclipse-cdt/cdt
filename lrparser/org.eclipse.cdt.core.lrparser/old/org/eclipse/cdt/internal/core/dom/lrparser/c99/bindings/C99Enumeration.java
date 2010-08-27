/*******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.lrparser.c99.bindings;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.cdt.internal.core.dom.parser.c.CVisitor;
import org.eclipse.core.runtime.PlatformObject;

@SuppressWarnings("restriction")
public class C99Enumeration extends PlatformObject implements IC99Binding, IEnumeration, ITypeable {

	private List<IEnumerator> enumerators = new ArrayList<IEnumerator>();	
	private String name;
	
	private IScope scope;
	
	
	public C99Enumeration() {
	}
	
	public C99Enumeration(String name) {
		this.name = name;
	}
	
	public void addEnumerator(IEnumerator e) {
		enumerators.add(e);
	}
	
	public IEnumerator[] getEnumerators() {
		return enumerators.toArray(new IEnumerator[enumerators.size()]);
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	public char[] getNameCharArray() {
		return name.toCharArray();
	}


	public IType getType() {
		return this;
	}
	
	public boolean isSameType(IType type) {
		 if( type == this )
            return true;
        if( type instanceof ITypedef)
            return type.isSameType( this );

        return false;
	}

	@Override
	public C99Enumeration clone() {
		try {
			C99Enumeration clone = (C99Enumeration) super.clone();
			clone.enumerators = new ArrayList<IEnumerator>();	
			for(IEnumerator e : enumerators) {
				// TODO this is wrong, 
				// IEnumerator is not Cloneable so we are not returning a deep copy here
				clone.addEnumerator(e); 
			}
			return clone;
		} catch (CloneNotSupportedException e1) {
			assert false;
			return null;
		}
		
	}
	
	public ILinkage getLinkage()  {
		return Linkage.C_LINKAGE;
	}

	public IScope getScope() {
		return scope;
	}

	public void setScope(IScope scope) {
		this.scope = scope;
	}

	public IBinding getOwner() {
		if (scope != null) {
			return CVisitor.findEnclosingFunction((IASTNode) scope.getScopeName()); // local or global
		}
		return null;
	}

	public long getMinValue() {
		long minValue = Long.MAX_VALUE;
		IEnumerator[] enumerators = getEnumerators();
		for (IEnumerator enumerator : enumerators) {
			IValue value = enumerator.getValue();
			if (value != null) {
				Long val = value.numericalValue();
				if (val != null) {
					long v = val.longValue();
					if (v < minValue) {
						minValue = v;
					}
				}
			}
		}
		return minValue;
	}

	public long getMaxValue() {
		long maxValue = Long.MIN_VALUE;
		IEnumerator[] enumerators = getEnumerators();
		for (IEnumerator enumerator : enumerators) {
			IValue value = enumerator.getValue();
			if (value != null) {
				Long val = value.numericalValue();
				if (val != null) {
					long v = val.longValue();
					if (v > maxValue) {
						maxValue = v;
					}
				}
			}
		}
		return maxValue;
	}
}
