/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 * Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.rewrite.astwriter;

import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplatedTypeTemplateParameter;
import org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.NodeCommentMap;

/**
 * Generates source code of template parameter nodes. The actual string operations are delegated
 * to the <code>Scribe</code> class.
 * 
 * @see Scribe
 * @see ICPPASTTemplateParameter
 * @author Emanuel Graf IFS
 */
public class TemplateParameterWriter extends NodeWriter {
	private static final String GREATER_THAN_CLASS = "> class"; //$NON-NLS-1$
	private static final String TEMPLATE_LESS_THAN = "template <"; //$NON-NLS-1$

	/**
	 * @param scribe
	 * @param visitor
	 */
	public TemplateParameterWriter(Scribe scribe, ASTWriterVisitor visitor, NodeCommentMap commentMap) {
		super(scribe, visitor, commentMap);
	}
	
	protected void writeTemplateParameter(ICPPASTTemplateParameter parameter) {
		if (parameter instanceof ICPPASTParameterDeclaration) {
			((IASTParameterDeclaration)((ICPPASTParameterDeclaration) parameter)).accept(visitor);
		} else if (parameter instanceof ICPPASTSimpleTypeTemplateParameter) {
			writeSimpleTypeTemplateParameter((ICPPASTSimpleTypeTemplateParameter) parameter);
		} else if (parameter instanceof ICPPASTTemplatedTypeTemplateParameter) {
			writeTemplatedTypeTemplateParameter((ICPPASTTemplatedTypeTemplateParameter) parameter);
		}
	}

	private void writeTemplatedTypeTemplateParameter(ICPPASTTemplatedTypeTemplateParameter templated) {
		scribe.print(TEMPLATE_LESS_THAN);
		ICPPASTTemplateParameter[] params = templated.getTemplateParameters();
		writeNodeList(params);
		
		scribe.print(GREATER_THAN_CLASS);
		
		if (templated.getName()!=null){
			scribe.printSpace();
			templated.getName().accept(visitor);
		}
		
		if (templated.getDefaultValue() != null){
			scribe.print(EQUALS);
			templated.getDefaultValue().accept(visitor);
		}
	}

	private void writeSimpleTypeTemplateParameter(ICPPASTSimpleTypeTemplateParameter simple) {
		switch (simple.getParameterType()) {
		case ICPPASTSimpleTypeTemplateParameter.st_class:
			scribe.print(CLASS_SPACE);
			break;
		case ICPPASTSimpleTypeTemplateParameter.st_typename:
			scribe.print(TYPENAME);
			break;
		}
					
		visitNodeIfNotNull(simple.getName());
		
		if (simple.getDefaultType() != null){
			scribe.print(EQUALS);
			simple.getDefaultType().accept(visitor);
		}
	}
}
