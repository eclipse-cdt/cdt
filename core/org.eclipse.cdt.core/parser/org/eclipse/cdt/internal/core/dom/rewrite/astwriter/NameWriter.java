/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 *     Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.rewrite.astwriter;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConversionName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTemplateTypeParameter;
import org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.NodeCommentMap;

/**
 * Generates source code of name nodes. The actual string operations are delegated
 * to the <code>Scribe</code> class.
 * 
 * @see Scribe
 * @see IASTName
 * @author Emanuel Graf IFS
 */
public class NameWriter extends NodeWriter {
	private static final String OPERATOR = "operator "; //$NON-NLS-1$

	/**
	 * @param scribe
	 * @param visitor
	 */
	public NameWriter(Scribe scribe, ASTWriterVisitor visitor, NodeCommentMap commentMap) {
		super(scribe, visitor, commentMap);
	}
	
	protected void writeName(IASTName name) {
		if (name instanceof ICPPASTTemplateId) {
			writeTempalteId((ICPPASTTemplateId) name);
		} else if (name instanceof ICPPASTConversionName) {
			scribe.print(OPERATOR);
			((ICPPASTConversionName) name).getTypeId().accept(visitor);
		} else if (name instanceof ICPPASTQualifiedName){
			writeQualifiedName((ICPPASTQualifiedName) name);
		} else {
			scribe.print(name.toString());
		}
		
		if (hasTrailingComments(name)) {
			writeTrailingComments(name);			
		}		
	}
	
	private void writeTempalteId(ICPPASTTemplateId tempId) {
		if (needsTemplateQualifier(tempId)) {
			scribe.print(TEMPLATE);
		}
		scribe.print(tempId.getTemplateName().toString());
		scribe.print('<');
		IASTNode[] nodes = tempId.getTemplateArguments();
		for (int i = 0; i < nodes.length; ++i) {
			nodes[i].accept(visitor);
			if (i + 1 < nodes.length) {
				scribe.print(',');
			}
		}
		scribe.print('>');
		if (isNestedTemplateId(tempId)) {
			scribe.printSpace();
		}
	}
	
	private boolean needsTemplateQualifier(ICPPASTTemplateId templId){
		if (templId.getParent() instanceof ICPPASTQualifiedName) {
			ICPPASTQualifiedName qName = (ICPPASTQualifiedName)  templId.getParent();
			return isDependentName(qName, templId);			
		}
		return false;
	}
	
	private boolean isDependentName(ICPPASTQualifiedName qname, ICPPASTTemplateId tempId) {
		IASTName[] names = qname.getNames();
		for (int i = 0; i < names.length; ++i){
			if (names[i] == tempId){
				return isDependentName(qname, tempId, i);
			}
		}
		return false;
	}

	private boolean isDependentName(ICPPASTQualifiedName qname, ICPPASTTemplateId tempId, int i) {
		if (i <= 0){
			return false;
		}
		if (qname.getNames()[i-1] instanceof ICPPASTTemplateId) {
			return true;
		}
		IBinding binding = qname.getNames()[i-1].resolveBinding();
		if (binding instanceof CPPTemplateTypeParameter) {
			return true;
		}
		return isDependentName(qname, tempId, i-1);
	}

	private boolean isNestedTemplateId(IASTNode node) {
		while ((node = node.getParent()) != null) {
			if (node instanceof ICPPASTTemplateId) {
				return true;
			}
		}
		return false;
	}

	private void writeQualifiedName(ICPPASTQualifiedName qname) {
		IASTName[] nodes = qname.getNames();
		for (int i = 0; i < nodes.length; ++i) {
			nodes[i].accept(visitor);
			if (i + 1 < nodes.length) {
				scribe.print(COLON_COLON);
			}
		}
	}
}
