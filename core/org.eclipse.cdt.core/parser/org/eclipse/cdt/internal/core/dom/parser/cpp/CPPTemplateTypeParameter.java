/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Niefer (IBM Corporation) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTypeParameter;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;

/**
 * The standard template parameter (template<typename T> or template<class T>).
 */
public class CPPTemplateTypeParameter extends CPPTemplateParameter implements
		ICPPTemplateTypeParameter, ICPPUnknownType, ICPPUnknownBinding {
	private ICPPScope unknownScope;
	private final boolean fIsParameterPack;

	public CPPTemplateTypeParameter(IASTName name, boolean isPack) {
		super(name);
		fIsParameterPack= isPack;
	}

	public final boolean isParameterPack() {
		return fIsParameterPack;
	}

	public ICPPScope asScope() {
	    if (unknownScope == null) {
	    	IASTName n = null;
	    	IASTNode[] nodes = getDeclarations();
	    	if (nodes != null && nodes.length > 0)
	    		n = (IASTName) nodes[0];
	        unknownScope = new CPPUnknownScope(this, n);
	    }
	    return unknownScope;
	}

	public IType getDefault() {
		IASTName[] nds = getDeclarations();
		if (nds == null || nds.length == 0)
		    return null;
		for (IASTName nd : nds) {
			if (nd != null) {
				IASTNode parent = nd.getParent();
				assert parent instanceof ICPPASTSimpleTypeTemplateParameter;
				if (parent instanceof ICPPASTSimpleTypeTemplateParameter) {
					ICPPASTSimpleTypeTemplateParameter simple = (ICPPASTSimpleTypeTemplateParameter) parent;
					IASTTypeId typeId = simple.getDefaultType();
					if (typeId != null)
						return CPPVisitor.createType(typeId);
				}
			}
		}
		return null;
	}
	
	public ICPPTemplateArgument getDefaultValue() {
		IType t= getDefault();
		if (t == null)
			return null;
		
		return new CPPTemplateArgument(t);
	}

    public boolean isSameType(IType type) {
        if (type == this)
            return true;
        if (type instanceof ITypedef)
            return type.isSameType(this);
        if (!(type instanceof ICPPTemplateTypeParameter))
        	return false;
        
        return getParameterID() == ((ICPPTemplateParameter) type).getParameterID();
    }

	public IASTName getUnknownName() {
		return new CPPASTName(getNameCharArray());
	}
}
