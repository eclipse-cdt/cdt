/*******************************************************************************
 * Copyright (c) 2008, 2009 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrew Ferguson (Symbian) - Initial Implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTAmbiguousTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.internal.core.dom.parser.ASTAmbiguousNode;
import org.eclipse.cdt.internal.core.parser.ParserMessages;

/**
 * Ambiguity node for deciding between type-id and id-expression in a template argument.
 */
public class CPPASTAmbiguousTemplateArgument extends ASTAmbiguousNode implements ICPPASTAmbiguousTemplateArgument {

	private List<IASTNode> fNodes;
	
	/**
	 * @param nodes  nodes of type {@link IASTTypeId} or {@link IASTIdExpression}
	 */
	/*
	 * We can replace this with a version taking ICPPASTTemplateArgument...
	 * in the future
	 */
	public CPPASTAmbiguousTemplateArgument(IASTNode... nodes) {
		fNodes= new ArrayList<IASTNode>(2);
		for(IASTNode node : nodes) {
			if(node instanceof IASTTypeId || node instanceof IASTIdExpression) {
				fNodes.add(node);
			} else {
				String ns= node == null ? "null" : node.getClass().getName(); //$NON-NLS-1$
				String msg= MessageFormat.format(ParserMessages.getString("CPPASTAmbiguousTemplateArgument_InvalidConstruction"), ns); //$NON-NLS-1$
				throw new IllegalArgumentException(msg);
			}
		}
	}

	public IASTNode copy() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public IASTNode[] getNodes() {
		return fNodes.toArray(new IASTNode[fNodes.size()]);
	}

	public void addTypeId(IASTTypeId typeId) {
        assertNotFrozen();
		addNode(typeId);
	}
	
	public void addIdExpression(IASTIdExpression idExpression) {
        assertNotFrozen();
		addNode(idExpression);
	}
	
	private void addNode(IASTNode node) {
		fNodes.add(node);
		node.setParent(this);
		node.setPropertyInParent(ICPPASTTemplateId.TEMPLATE_ID_ARGUMENT);
	}
}

