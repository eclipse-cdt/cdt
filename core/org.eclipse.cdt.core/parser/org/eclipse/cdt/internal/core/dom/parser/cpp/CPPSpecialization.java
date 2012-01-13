/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrew Niefer (IBM) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameterPackType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.core.runtime.PlatformObject;

/**
 * Base class for all specializations in the AST. Note the specialization may also be created on behalf
 * of the index. The index may be concurrently be accessed (read-only) from different threads. So there
 * is a need to synchronize non-final members.
 */
public abstract class CPPSpecialization extends PlatformObject implements ICPPSpecialization, ICPPInternalBinding {
	private IBinding owner;
	private IBinding specialized;
	private ICPPTemplateParameterMap argumentMap;
	protected IASTNode definition;
	private IASTNode[] declarations;
	
	public CPPSpecialization(IBinding specialized, IBinding owner, ICPPTemplateParameterMap argumentMap) {
		this.specialized = specialized;
		this.owner = owner;
		this.argumentMap = argumentMap;
	}

	public IType specializeType(IType type) {
		return CPPTemplates.instantiateType(type, getTemplateParameterMap(), -1, getSpecializationContext());
	}

	protected ICPPClassSpecialization getSpecializationContext() {
		if (owner instanceof ICPPClassSpecialization) {
			ICPPClassSpecialization within = (ICPPClassSpecialization) owner;
			ICPPClassType orig = within.getSpecializedBinding();
			for(;;) {
				IBinding o1 = within.getOwner();
				IBinding o2 = orig.getOwner();
				if (!(o1 instanceof ICPPClassSpecialization && o2 instanceof ICPPClassType)) 
					return within;
				ICPPClassSpecialization nextWithin = (ICPPClassSpecialization) o1;
				orig= (ICPPClassType) o2;
				if (orig.isSameType(nextWithin)) 
					return within;
				within= nextWithin;
			}
		}		
		return null;
	}
	
	public IType[] specializeTypePack(ICPPParameterPackType type) {
		if (owner instanceof ICPPClassSpecialization) {
			return CPPTemplates.instantiateTypes(new IType[]{type}, getTemplateParameterMap(), -1, (ICPPClassSpecialization) owner);
		} else {
			return CPPTemplates.instantiateTypes(new IType[]{type}, getTemplateParameterMap(), -1, null);
		}
	}

	public IValue specializeValue(IValue value, int maxdepth) {
		if (owner instanceof ICPPClassSpecialization) {
			return CPPTemplates.instantiateValue(value, getTemplateParameterMap(), -1, (ICPPClassSpecialization) owner, maxdepth);
		} else {
			return CPPTemplates.instantiateValue(value, getTemplateParameterMap(), -1, null, maxdepth);
		}
	}

	@Override
	public IBinding getSpecializedBinding() {
		return specialized;
	}

	@Override
	public IASTNode[] getDeclarations() {
		return declarations;
	}

	@Override
	public IASTNode getDefinition() {
		return definition;
	}

	@Override
	public void addDefinition(IASTNode node) {
		definition = node;
	}

	@Override
	public void addDeclaration(IASTNode node) {
		if (declarations == null) {
	        declarations = new IASTNode[] { node };
		} else {
	        // keep the lowest offset declaration in [0]
			if (declarations.length > 0 &&
					((ASTNode) node).getOffset() < ((ASTNode) declarations[0]).getOffset()) {
				declarations = ArrayUtil.prepend(IASTNode.class, declarations, node);
			} else {
				declarations = ArrayUtil.append(IASTNode.class, declarations, node);
			}
	    }
	}

	@Override
	public String getName() {
		return specialized.getName();
	}

	@Override
	public char[] getNameCharArray() {
		return specialized.getNameCharArray();
	}

	@Override
	public IBinding getOwner() {
		return owner;
	}
	
	@Override
	public IScope getScope() throws DOMException {
		if (owner instanceof ICPPClassType) {
			return ((ICPPClassType) owner).getCompositeScope();
		} else if (owner instanceof ICPPNamespace) {
			return ((ICPPNamespace) owner).getNamespaceScope();
		} else if (owner instanceof ICPPFunction) {
			return ((ICPPFunction) owner).getFunctionScope();
		}
		if (definition != null) 
			return CPPVisitor.getContainingScope(definition);
		if (declarations != null && declarations.length > 0) 
			return CPPVisitor.getContainingScope(declarations[0]);
		
		return specialized.getScope();
	}

	@Override
	public String[] getQualifiedName() {
		return CPPVisitor.getQualifiedName(this);
	}

	@Override
	public char[][] getQualifiedNameCharArray() {
		return CPPVisitor.getQualifiedNameCharArray(this);
	}

	@Override
	public boolean isGloballyQualified() throws DOMException {
		if (specialized instanceof ICPPBinding)
			return ((ICPPBinding) specialized).isGloballyQualified();
		return false;
	}

	@Override
	public ILinkage getLinkage() {
		return Linkage.CPP_LINKAGE;
	}

	@Override
	@Deprecated
	public ObjectMap getArgumentMap() {
		return CPPTemplates.getArgumentMap(this, getTemplateParameterMap());
	}
	
	@Override
	public ICPPTemplateParameterMap getTemplateParameterMap() {
		return argumentMap;
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder(getName());
		if (argumentMap != null) {
			result.append(" "); //$NON-NLS-1$
			result.append(argumentMap.toString());
		}
		return result.toString();
	}
}
