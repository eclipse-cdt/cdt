/*******************************************************************************
 * Copyright (c) 2008, 2015 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 *     Institute for Software - initial API and implementation
 *     Thomas Corbat (IFS)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.rewrite.astwriter;

import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConversionName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNameSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.parser.Keywords;
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
			scribe.printStringSpace(Keywords.TEMPLATE);
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
			return !isPartOfFunctionDeclarator(qName) && isDependentName(qName, templId);
		}
		return false;
	}

	private boolean isPartOfFunctionDeclarator(ICPPASTQualifiedName qName) {
		return qName.getParent() instanceof IASTFunctionDeclarator;
	}

	private boolean isDependentName(ICPPASTQualifiedName qname, ICPPASTTemplateId tempId) {
		ICPPASTNameSpecifier[] segments = qname.getAllSegments();
		for (int i = 0; i < segments.length; ++i){
			if (segments[i] == tempId){
				return isDependentName(qname, tempId, i);
			}
		}
		return false;
	}

	private boolean isDependentName(ICPPASTQualifiedName qname, ICPPASTTemplateId tempId, int i) {
		if (i <= 0){
			return false;
		}
		if (qname.getQualifier()[i - 1] instanceof ICPPASTTemplateId) {
			return true;
		}
		IBinding binding = qname.getQualifier()[i - 1].resolveBinding();
		if (binding instanceof CPPTemplateTypeParameter) {
			return true;
		}
		return isDependentName(qname, tempId, i - 1);
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
		if (qname.isFullyQualified()) {
			scribe.print(COLON_COLON);
		}
		for (ICPPASTNameSpecifier segment : qname.getQualifier()) {
			segment.accept(visitor);
			scribe.print(COLON_COLON);
		}
		qname.getLastName().accept(visitor);
	}
}
