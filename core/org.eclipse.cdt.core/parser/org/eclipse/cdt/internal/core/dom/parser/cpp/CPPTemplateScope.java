/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
/*
 * Created on Mar 11, 2005
 */
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateScope;

/**
 * @author aniefer
 */
public class CPPTemplateScope extends CPPScope implements ICPPTemplateScope {

	private ICPPTemplateDefinition primaryDefinition;
	/**
	 * @param physicalNode
	 */
	public CPPTemplateScope(IASTNode physicalNode) {
		super(physicalNode);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateScope#getTemplateDefinition()
	 */
	public ICPPTemplateDefinition getTemplateDefinition() throws DOMException {
		if( primaryDefinition == null ){
			//primaryDefinition = CPPTemplates.getTemplateDefinition( this );
			ICPPASTTemplateDeclaration template = (ICPPASTTemplateDeclaration) getPhysicalNode();
			IASTDeclaration decl = template.getDeclaration();
			return new CPPTemplateDefinition( decl );
		}
		return primaryDefinition;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPScope#getScopeName()
	 */
	public IASTName getScopeName() {
		// TODO Auto-generated method stub
		return null;
	}

}
