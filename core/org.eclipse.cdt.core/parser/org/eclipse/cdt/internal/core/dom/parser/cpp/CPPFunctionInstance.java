/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;

/**
 * @author aniefer
 */
public class CPPFunctionInstance extends CPPFunctionSpecialization implements ICPPTemplateInstance {
	private IType[] arguments;

	public CPPFunctionInstance(IBinding owner, ICPPFunction orig, ObjectMap argMap, IType[] args) {
		super(orig, owner, argMap);
		this.arguments = args;
	}

	public ICPPTemplateDefinition getTemplateDefinition() {
		return (ICPPTemplateDefinition) getSpecializedBinding();
	}

	public IType[] getArguments() {
		return arguments;
	}

	/* (non-Javadoc)
	 * For debug purposes only
	 */
	@Override
	public String toString() {
		return getName() + " <" + ASTTypeUtil.getTypeListString(arguments) + ">"; //$NON-NLS-1$ //$NON-NLS-2$
	}
	
    @Override
	public boolean equals(Object obj) {
    	if( (obj instanceof ICPPTemplateInstance) && (obj instanceof ICPPFunction)){
    		try {
    			ICPPFunctionType ct1= (ICPPFunctionType) ((ICPPFunction)getSpecializedBinding()).getType();
    			ICPPFunctionType ct2= (ICPPFunctionType) ((ICPPFunction)((ICPPTemplateInstance)obj).getTemplateDefinition()).getType();
    			if(!ct1.isSameType(ct2))
    				return false;

    			ObjectMap m1 = getArgumentMap(), m2 = ((ICPPTemplateInstance)obj).getArgumentMap();
    			if( m1 == null || m2 == null || m1.size() != m2.size())
    				return false;
    			for( int i = 0; i < m1.size(); i++ ){
    				IType t1 = (IType) m1.getAt( i );
    				IType t2 = (IType) m2.getAt( i );
    				if(!CPPTemplates.isSameTemplateArgument(t1, t2))
    					return false;
    			}
    			return true;
    		} catch(DOMException de) {
    			CCorePlugin.log(de);
    		}
    	}

    	return false;
    }
}
