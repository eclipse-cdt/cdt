/*******************************************************************************
 * Copyright (c) 2006, 2013 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.callhierarchy;

import java.util.ArrayList;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;

/**
 * Visitor to find references in a given range of the source code.
 * @since 4.0
 */
class ReferenceVisitor extends ASTVisitor {
	private ArrayList<IASTName> fReferences = new ArrayList<>();
	private int fOffset;
	private int fEndOffset;
	private String fFileName;

	ReferenceVisitor(String fileName, int offset, int length) {
		shouldVisitNames = true;
		shouldVisitDeclarations = true;

		fFileName = fileName;
		fOffset = offset;
		fEndOffset = offset + length;
	}

	public IASTName[] getReferences() {
		return fReferences.toArray(new IASTName[fReferences.size()]);
	}

	@Override
	public int visit(IASTName name) {
		if (name.isReference()) {
			IASTFileLocation loc = name.getFileLocation();
			if (!loc.getFileName().equals(fFileName)) {
				return PROCESS_SKIP;
			}
			int offset = loc.getNodeOffset();
			if (fOffset <= offset && offset + loc.getNodeLength() <= fEndOffset) {
				fReferences.add(name);
			}
		}
		return super.visit(name);
	}

	@Override
	public int visit(IASTDeclaration declaration) {
		IASTFileLocation loc = declaration.getFileLocation();
		if (loc != null) {
			if (!loc.getFileName().equals(fFileName)) {
				return PROCESS_SKIP;
			}
			int offset = loc.getNodeOffset();
			if (offset + loc.getNodeLength() <= fOffset || fEndOffset <= offset) {
				return PROCESS_SKIP;
			}
		}
		return PROCESS_CONTINUE;
	}
}
