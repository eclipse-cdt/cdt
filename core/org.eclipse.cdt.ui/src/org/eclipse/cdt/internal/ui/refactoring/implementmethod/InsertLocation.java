/*******************************************************************************
 * Copyright (c) 2008, 2011 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Institute for Software - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.implementmethod;

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

/**
 * Is returned when using the find method of the MethodDefinitionInsertLocationFinder.
 * Contains all the information needed to insert at the correct position.
 *
 * @author Lukas Felber
 */
public class InsertLocation {
	private IASTNode nodeToInsertAfter;
	private IASTNode nodeToInsertBefore;
	private IASTNode parentNode;
	private ITranslationUnit tu;

	public InsertLocation() {
	}

	public boolean hasAnyNode() {
		return nodeToInsertAfter != null || nodeToInsertBefore != null;
	}

	public IASTNode getNodeToInsertBefore() {
		return nodeToInsertBefore;
	}

	public IASTNode getParentOfNodeToInsertBefore() throws CoreException {
		IASTNode node = nodeToInsertBefore != null ? nodeToInsertBefore : nodeToInsertAfter;
		return node != null ? node.getParent() : parentNode;
	}

	public ITranslationUnit getTranslationUnit() {
		return tu;
	}

	public IFile getFile() {
		return tu != null ? (IFile) tu.getResource() : null;
	}

	public int getInsertPosition() {
		if (nodeToInsertBefore != null) {
			return nodeToInsertBefore.getFileLocation().getNodeOffset();
		} else if (nodeToInsertAfter != null) {
			IASTFileLocation fileLocation = nodeToInsertAfter.getFileLocation();
			return fileLocation.getNodeOffset() + fileLocation.getNodeLength();
		}
		return 0;
	}

	public void setNodeToInsertAfter(IASTNode nodeToInsertAfter, ITranslationUnit tu) {
		this.nodeToInsertAfter = nodeToInsertAfter;
		this.tu = tu;
	}

	public void setNodeToInsertBefore(IASTNode nodeToInsertBefore, ITranslationUnit tu) {
		this.nodeToInsertBefore = nodeToInsertBefore;
		this.tu = tu;
	}

	public void setParentNode(IASTNode parentNode, ITranslationUnit tu) {
		this.parentNode = parentNode;
		this.tu = tu;
	}
}
