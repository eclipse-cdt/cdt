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
 * Created on Apr 5, 2005
 */
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateSpecialization;

/**
 * @author aniefer
 */
public class CPPClassTemplateSpecialization extends CPPClassTemplate implements
		ICPPTemplateSpecialization {

	private IASTNode [] arguments;
	/**
	 * @param name
	 */
	public CPPClassTemplateSpecialization(ICPPASTTemplateId name) {
		super(name);
		this.arguments = name.getTemplateArguments();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateSpecialization#getArguments()
	 */
	public IASTNode[] getArguments() {
		return arguments;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateSpecialization#isPartialSpecialization()
	 */
	public boolean isPartialSpecialization() {
		return getTemplateParameters().length > 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateSpecialization#getPrimaryTemplateDefinition()
	 */
	public ICPPTemplateDefinition getPrimaryTemplateDefinition() {
		ICPPASTTemplateId id = (ICPPASTTemplateId) getTemplateName();
		return (ICPPTemplateDefinition) id.getTemplateName().resolveBinding();
	}

}
