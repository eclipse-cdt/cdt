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

import org.eclipse.core.resources.IFile;
import org.eclipse.text.edits.TextEditGroup;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;

import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.cdt.internal.ui.refactoring.NodeContainer;
import org.eclipse.cdt.internal.ui.refactoring.NodeContainer.NameInformation;

final class SimilarFinderVisitor extends ASTVisitor {
	private final ExtractFunctionRefactoring refactoring;

	private final List<IASTNode> trail;
	private final IASTName name;
	private final List<IASTNode> statements;
	private int statementCount;
	private NodeContainer similarContainer;
	private final List<IASTStatement> stmtToReplace = new ArrayList<IASTStatement>();

	private final ModificationCollector collector;

	SimilarFinderVisitor(ExtractFunctionRefactoring refactoring, ModificationCollector collector,
			List<IASTNode> trail, IFile file, IASTName name, List<IASTNode> statements,
			String title) {
		this.refactoring = refactoring;
		this.trail = trail;
		this.name = name;
		this.statements = statements;
		this.collector = collector;
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
					if (refactoring.names.containsKey(nameInfo.getDeclaration().getRawSignature())) {
						Integer nameOrderNumber = refactoring.names.get(nameInfo.getDeclaration().getRawSignature());
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
								for (NameInformation orgNameInfo : refactoring.container.getParameterCandidates()) {
									if (orgName.equals(orgNameInfo.getDeclaration().getRawSignature()) &&
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
					IASTNode call = refactoring.getMethodCall(name,	refactoring.nameTrail,
							refactoring.names, refactoring.container, similarContainer);
					ASTRewrite rewrite =
							collector.rewriterForTranslationUnit(stmtToReplace.get(0).getTranslationUnit());
					TextEditGroup editGroup = new TextEditGroup(Messages.SimilarFinderVisitor_replaceDuplicateCode);
					rewrite.replace(stmtToReplace.get(0), call, editGroup);
					if (stmtToReplace.size() > 1) {
						for (int i = 1; i < stmtToReplace.size(); ++i) {
							rewrite.remove(stmtToReplace.get(i), editGroup);
						}
					}
				}
				clear();
			}
			return PROCESS_SKIP;
		} else {
			clear();
			return super.visit(statement);
		}
	}

	private boolean isInSelection(IASTStatement stmt) {
		List<IASTNode>nodes = refactoring.container.getNodesToWrite();
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