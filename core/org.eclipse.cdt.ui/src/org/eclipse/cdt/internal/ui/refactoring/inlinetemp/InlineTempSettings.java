/*******************************************************************************
 * Copyright (c) 2013 Simon Taddiken
 * University of Bremen.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 *     Simon Taddiken (University of Bremen)
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.inlinetemp;

import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTName;

import java.util.Collection;

import org.eclipse.cdt.core.dom.ast.IBinding;

public class InlineTempSettings {

	public static enum SelectionType {
		DECLARATION, USAGE, INVALID;
	}

	private IBinding selected;
	private boolean inlineAll;
	private boolean removeDeclaration;
	private boolean alwaysAddParenthesis;
	private SelectionType selectionType;
	
	private IASTName selectedNode;
	private Collection<IASTName> references;
	private IASTDeclarator declarator;
	
	

	public boolean isAlwaysAddParenthesis() {
		return this.alwaysAddParenthesis;
	}

	public void setAlwaysAddParenthesis(boolean alwaysAddParenthesis) {
		this.alwaysAddParenthesis = alwaysAddParenthesis;
	}

	public IASTName getSelectedNode() {
		return this.selectedNode;
	}

	public void setSelectedNode(IASTName selectedNode) {
		this.selectedNode = selectedNode;
	}

	public Collection<IASTName> getReferences() {
		return this.references;
	}

	public void setReferences(Collection<IASTName> references) {
		this.references = references;
	}

	public IASTDeclarator getDeclarator() {
		return this.declarator;
	}

	public void setDeclarator(IASTDeclarator declarator) {
		this.declarator = declarator;
	}

	public IBinding getSelected() {
		return this.selected;
	}

	public boolean isInlineAll() {
		return this.inlineAll;
	}

	public boolean isRemoveDeclaration() {
		return this.removeDeclaration;
	}

	public SelectionType getSelectionType() {
		return this.selectionType;
	}

	public void setRemoveDeclaration(boolean removeDeclaration) {
		this.removeDeclaration = removeDeclaration;
	}

	public void setSelected(IBinding selected) {
		this.selected = selected;
	}

	public void setInlineAll(boolean inlineAll) {
		this.inlineAll = inlineAll;
	}

	public void setSelectionType(SelectionType selectionType) {
		this.selectionType = selectionType;
	}
}