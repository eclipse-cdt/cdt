/*******************************************************************************
 * Copyright (c) 2008, 2011 Institute for Software, HSR Hochschule fuer Technik  
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;

/**
 * This class serves as a container to pass several nodes to the <code>ASTWriter</code>.
 * This container is used if source code for several sibling nodes but for their common parent
 * node should be generated. 
 * 
 * @author Emanuel Graf IFS
 */
public class ContainerNode extends ASTNode {
	private final IASTTranslationUnit tu = null;
	
	private final ArrayList<IASTNode> nodes = new ArrayList<IASTNode>();
	
	public ContainerNode(IASTNode... nodes) {
		for (IASTNode each : nodes) {
			addNode(each);
		}
	}
	
	@Override
	public ContainerNode copy() {
		return copy(CopyStyle.withoutLocations);
	}
	
	@Override
	public ContainerNode copy(CopyStyle style) {
		ContainerNode copy = new ContainerNode();
		for (IASTNode node : getNodes())
			copy.addNode(node == null ? null : node.copy(style));
		copy.setOffsetAndLength(this);
		if (style == CopyStyle.withLocations) {
			copy.setCopyLocation(this);
		}
		return copy;
	}

	public void addNode(IASTNode node) {
		nodes.add(node);
		if (node.getParent() == null) {
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
