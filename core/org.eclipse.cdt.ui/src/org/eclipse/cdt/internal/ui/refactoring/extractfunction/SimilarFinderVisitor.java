/*******************************************************************************
 * Copyright (c) 2009 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 *     Institute for Software - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.extractfunction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;

import org.eclipse.cdt.internal.ui.refactoring.NameInformation;
import org.eclipse.cdt.internal.ui.refactoring.NodeContainer;

abstract class SimilarFinderVisitor extends ASTVisitor {
	protected final ExtractFunctionRefactoring refactoring;
	protected final NodeContainer extractedNodes;
	protected NodeContainer similarContainer;
	protected final List<IASTStatement> stmtToReplace = new ArrayList<IASTStatement>();
	private final List<IASTNode> trail;
	private final List<IASTNode> statements;
	private int statementCount;

	SimilarFinderVisitor(ExtractFunctionRefactoring refactoring, NodeContainer extractedNodes,
			List<IASTNode> trail, List<IASTNode> statements) {
		this.refactoring = refactoring;
		this.extractedNodes = extractedNodes;
		this.trail = trail;
		this.statements = statements;
		this.similarContainer = new NodeContainer();
		shouldVisitStatements = true;
	}

	@Override
	public int visit(IASTStatement statement) {
		if (!isInSelection(statement) &&
				refactoring.isStatementInTrail(statement, trail, refactoring.getIndex())) {
			stmtToReplace.add(statement);
			similarContainer.add(statement);	
			++statementCount;

			if (statementCount == statements.size()) {
				// Found similar code
				boolean similarOnReturnWays = true;
				for (NameInformation nameInfo : similarContainer.getParameterCandidates()) {
					if (refactoring.names.containsKey(nameInfo.getDeclarationName().getRawSignature())) {
						Integer nameOrderNumber = refactoring.names.get(nameInfo.getDeclarationName().getRawSignature());
						if (refactoring.nameTrail.containsValue(nameOrderNumber)) {
							String orgName = null;
							boolean found = false;
							for (Entry<String, Integer> entry : refactoring.nameTrail.entrySet()) {
								if (entry.getValue().equals(nameOrderNumber)) {
									orgName = entry.getKey();
									break;
								}
							}
							if (orgName != null) {
								for (NameInformation orgNameInfo : extractedNodes.getParameterCandidates()) {
									if (orgName.equals(orgNameInfo.getDeclarationName().getRawSignature()) &&
											(orgNameInfo.isOutput() || !nameInfo.isOutput())) {
										found = true;
										break;
									}
								}
							}

							if (!found) {
								similarOnReturnWays = false;
							}
						}
					}
				}

				if (similarOnReturnWays) {
					foundSimilar();
				}
				clear();
			}
			return PROCESS_SKIP;
		} else {
			clear();
			return super.visit(statement);
		}
	}

	protected abstract void foundSimilar();

	private boolean isInSelection(IASTStatement stmt) {
		List<IASTNode>nodes = extractedNodes.getNodesToWrite();
		for (IASTNode node : nodes) {
			if (node.equals(stmt)) {
				return true;
			}
		}
		return false;
	}

	private void clear() {
		statementCount = 0;
		refactoring.names.clear();
		similarContainer = new NodeContainer();
		refactoring.namesCounter.setObject(ExtractFunctionRefactoring.NULL_INTEGER);
		refactoring.trailPos.setObject(ExtractFunctionRefactoring.NULL_INTEGER);
		stmtToReplace.clear();
	}
}