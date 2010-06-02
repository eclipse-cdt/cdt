/*******************************************************************************
 * Copyright (c) 2008, 2010 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 * Institute for Software - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.refactoring.implementmethod;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

import org.eclipse.cdt.internal.ui.refactoring.utils.TranslationUnitHelper;

/**
 * Is returned when using the find-method of the MethodDefinitionInsertLocationFinder.
 * Contains all the infos needet to insert at the correct position.
 * 
 * @author Lukas Felber
 *
 */
public class InsertLocation {
	private IFile insertFile;
	private IASTNode nodeToInsertAfter;
	private IASTNode nodeToInsertBefore;
	private IASTTranslationUnit targetTranslationUnit;

	public boolean hasAnyNode() {
		return nodeToInsertAfter != null || nodeToInsertBefore != null;
	}
	
	public IASTNode getNodeToInsertBefore() {
		return nodeToInsertBefore;
	}
	
	public IASTNode getPartenOfNodeToInsertBefore() throws CoreException{
		IASTNode affectedNode = getAffectedNode();
		return (affectedNode != null) ? affectedNode.getParent() : getTargetTranslationUnit();
	}

	private IASTNode getAffectedNode() {
		IASTNode concernedNode = (nodeToInsertBefore != null) ? nodeToInsertBefore : nodeToInsertAfter;
		return concernedNode;
	}
	
	public IFile getInsertFile() {
		return insertFile;
	}

	public void setInsertFile(IFile insertFile) {
		this.insertFile = insertFile;
	}

	public void setNodeToInsertAfter(IASTNode nodeToInsertAfter) {
		this.nodeToInsertAfter = nodeToInsertAfter;
	}

	public void setNodeToInsertBefore(IASTNode nodeToInsertBefore) {
		this.nodeToInsertBefore = nodeToInsertBefore;
	}
	
	public boolean hasFile() {
		return insertFile != null;
	}
	
	public IASTTranslationUnit getTargetTranslationUnit() throws CoreException{
		if(targetTranslationUnit == null) {
			loadTargetTranslationUnit();
		}
		return targetTranslationUnit;
		
	}

	private void loadTargetTranslationUnit() throws CoreException{
		IASTNode affectedNode = getAffectedNode();
		if(affectedNode != null) {
			targetTranslationUnit = affectedNode.getTranslationUnit();
		} else if(hasFile()) {
			targetTranslationUnit = TranslationUnitHelper.loadTranslationUnit(insertFile, true);
		}
	}
	
	public int getInsertPosition() {
		if(nodeToInsertBefore != null) {
			return nodeToInsertBefore.getFileLocation().getNodeOffset();
		} else if (nodeToInsertAfter != null) {
			return nodeToInsertAfter.getFileLocation().getNodeOffset() + nodeToInsertAfter.getFileLocation().getNodeLength();
		} else {
			return 0;
		}
	}
}
