/*******************************************************************************
 * Copyright (c) 2004, 2013 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Niefer (IBM Corporation) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.IASTLabelStatement;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ILabel;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.core.runtime.PlatformObject;

public class CPPLabel extends PlatformObject implements ILabel, ICPPInternalBinding {
	private IASTName name;

	public CPPLabel(IASTName name) {
		this.name = name;
		name.setBinding(this);
	}

	@Override
	public IASTNode[] getDeclarations() {
		return null;
	}

	@Override
	public IASTNode getDefinition() {
		return name;
	}

	@Override
	public IASTLabelStatement getLabelStatement() {
		IASTNode statement = name.getParent();
		if (statement instanceof IASTLabelStatement)
			return (IASTLabelStatement) statement;

		// TODO find label statement
		return null;
	}

	@Override
	public String getName() {
		return new String(getNameCharArray());
	}

	@Override
	public char[] getNameCharArray() {
		return name.getSimpleID();
	}

	@Override
	public IScope getScope() {
		return CPPVisitor.getContainingScope(name);
	}

	public IASTNode getPhysicalNode() {
		return name;
	}

	public void setLabelStatement(IASTName labelStatement) {
		name = labelStatement;
	}

	@Override
	public String[] getQualifiedName() {
		return new String[] { getName() };
	}

	@Override
	public char[][] getQualifiedNameCharArray() {
		return new char[][] { getNameCharArray() };
	}

	@Override
	public boolean isGloballyQualified() {
		return false;
	}

	@Override
	public void addDefinition(IASTNode node) {
	}

	@Override
	public void addDeclaration(IASTNode node) {
	}

	@Override
	public ILinkage getLinkage() {
		return Linkage.CPP_LINKAGE;
	}

	@Override
	public IBinding getOwner() {
		return CPPVisitor.findEnclosingFunction(name);
	}

	@Override
	public String toString() {
		return getName();
	}
}
