/*******************************************************************************
 * Copyright (c) 2013, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Eidsness - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.ast.tag;

import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.tag.ITag;
import org.eclipse.cdt.core.dom.ast.tag.ITagReader;
import org.eclipse.cdt.core.dom.ast.tag.ITagWriter;
import org.eclipse.cdt.core.dom.ast.tag.IWritableTag;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding;

public class NonCachedTaggable implements ITagReader, ITagWriter {
	private final IBinding binding;
	private IASTName ast;

	public NonCachedTaggable(IBinding binding) {
		this.binding = binding;
	}

	@Override
	public IWritableTag createTag(String id, int len) {
		return new Tag(id, len);
	}

	@Override
	public ITag getTag(String id) {
		return TagManager.getInstance().process(id, this, binding, getAST());
	}

	@Override
	public Iterable<ITag> getTags() {
		return TagManager.getInstance().process(this, binding, getAST());
	}

	@Override
	public boolean setTags(Iterable<ITag> tags) {
		// This non-caching implementation has nothing to set, the tags will be regenerated
		// when they are queried.
		return true;
	}

	private IASTName getAST() {
		if (ast != null)
			return ast;

		if (!(binding instanceof ICPPInternalBinding))
			return null;

		IASTNode node = getPhysicalNode((ICPPInternalBinding) binding);
		if (node == null)
			return null;

		return ast = getName(node);
	}

	private static IASTNode getPhysicalNode(ICPPInternalBinding binding) {
		IASTNode node = binding.getDefinition();
		if (node != null)
			return node;

		IASTNode[] nodes = binding.getDeclarations();
		if (nodes == null || nodes.length <= 0)
			return null;
		return nodes[0];
	}

	private static IASTName getName(IASTNode node) {
		if (node instanceof IASTName)
			return (IASTName) node;
		if (node instanceof ICPPASTCompositeTypeSpecifier)
			return ((ICPPASTCompositeTypeSpecifier) node).getName();
		if (node instanceof IASTDeclarator)
			return ((IASTDeclarator) node).getName();
		return null;
	}
}
