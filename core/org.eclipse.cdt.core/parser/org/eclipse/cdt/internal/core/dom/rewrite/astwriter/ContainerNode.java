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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;

/**
 * @author Emanuel Graf
 *
 */
public class ContainerNode extends ASTNode {
	
	private final IASTTranslationUnit tu = null;
	
	private final ArrayList<IASTNode> nodes = new ArrayList<IASTNode>();
	
	public ContainerNode(IASTNode... nodes) {
		for (IASTNode each : nodes) {
			addNode(each);
		}
	}
	
	public void addNode(IASTNode node) {
		nodes.add(node);
		if(node.getParent() == null) {
			node.setParent(tu);
		}
	}

	@Override
	public boolean accept(ASTVisitor visitor) {
		boolean ret = true;
		for (IASTNode node : nodes) {
			ret = node.accept(visitor);
		}
		return ret;
	}
	
	public IASTTranslationUnit getTu() {
		return tu;
	}
	
	public List<IASTNode> getNodes(){
		return Collections.unmodifiableList(nodes);
	}

}
