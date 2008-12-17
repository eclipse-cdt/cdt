/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTExplicitTemplateInstantiation;

/**
 * @author jcamelon
 */
public class GPPASTExplicitTemplateInstantiation extends
        CPPASTExplicitTemplateInstantiation implements
        IGPPASTExplicitTemplateInstantiation {

    public GPPASTExplicitTemplateInstantiation() {
		super();
	}

	public GPPASTExplicitTemplateInstantiation(IASTDeclaration declaration) {
		super(declaration);
	}

	private int mod;

	
	@Override
	public GPPASTExplicitTemplateInstantiation copy() {
		GPPASTExplicitTemplateInstantiation copy = new GPPASTExplicitTemplateInstantiation();
		IASTDeclaration declaration = getDeclaration();
		copy.setDeclaration(declaration == null ? null : declaration.copy());
		copy.mod = mod;
		copy.setOffsetAndLength(this);
		return copy;
	}
	
    public int getModifier() {
        return mod;
    }

    public void setModifier(int value) {
        this.mod = value;
    }

}
