/*******************************************************************************
 *  Copyright (c) 2004, 2011 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     John Camelon (IBM) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTypenameExpression;

@Deprecated
public class CPPASTTypenameExpression extends CPPASTSimpleTypeConstructorExpression implements ICPPASTTypenameExpression {

    public CPPASTTypenameExpression() {
	}

	public CPPASTTypenameExpression(IASTName name, IASTExpression expr) {
		setName(name);
		setInitialValue(expr);
	}

	@Override
	public CPPASTTypenameExpression copy() {
		return copy(CopyStyle.withoutLocations);
	}
	
	@Override
	public CPPASTTypenameExpression copy(CopyStyle style) {
		super.copy(style);
		CPPASTTypenameExpression copy = new CPPASTTypenameExpression();
		ICPPASTDeclSpecifier declSpec = getDeclSpecifier();
		IASTInitializer init = getInitializer();
		copy.setDeclSpecifier(declSpec == null ? null : declSpec.copy(style));
		copy.setInitializer(init == null ? null : init.copy(style));
		copy.setOffsetAndLength(this);
		if (style == CopyStyle.withLocations) {
			copy.setCopyLocation(this);
		}
		return copy;
	}

    @Override
	public void setName(IASTName name) {
    	CPPASTNamedTypeSpecifier spec= new CPPASTNamedTypeSpecifier(name);
    	spec.setOffsetAndLength(this);
    	setDeclSpecifier(spec);
    }

    @Override
	public IASTName getName() {
    	IASTDeclSpecifier spec= getDeclSpecifier();
    	if (spec instanceof ICPPASTNamedTypeSpecifier) {
    		return ((ICPPASTNamedTypeSpecifier) spec).getName();
    	}
    	return null;
    }

	@Override
	public int getRoleForName(IASTName n) {
		if (n == getName())
			return r_reference;
		return r_unclear;
	}

	@Override
	@Deprecated
	public void setIsTemplate(boolean val) {
    }

	@Override
	@Deprecated
    public boolean isTemplate() {
        return false;
    }
}
