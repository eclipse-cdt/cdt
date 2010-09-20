/*******************************************************************************
 * Copyright (c) 2009 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 * Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.extractfunction;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
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

		private final Vector<IASTNode> trail;
		private final IASTName name;
		private final List<IASTNode> stmts;
		private int i = 0;
		private NodeContainer similarContainer;
	private final List<IASTStatement> stmtToReplace = new ArrayList<IASTStatement>();

	private final ModificationCollector collector;

	SimilarFinderVisitor(ExtractFunctionRefactoring refactoring,
			ModificationCollector collector, Vector<IASTNode> trail, IFile file, IASTName name,
			List<IASTNode> stmts, String title) {
		this.refactoring = refactoring;
		this.trail = trail;
		this.name = name;
		this.stmts = stmts;
		this.collector = collector;
		this.similarContainer = new NodeContainer();
	}

		{
			shouldVisitStatements = true;
		}

		@Override
		public int visit(IASTStatement stmt) {
			
			boolean isAllreadyInMainRefactoring = isInSelection(stmt);
			
			if( (!isAllreadyInMainRefactoring)
					&& this.refactoring.isStatementInTrail(stmt, trail, this.refactoring.getIndex())){
				stmtToReplace.add(stmt);
				similarContainer.add(stmt);	
				++i;
				
				if(i==stmts.size()){
					//found similar code
					
					boolean similarOnReturnWays = true;
					for (NameInformation nameInfo : similarContainer.getAllAfterUsedNames()) {
						if(this.refactoring.names.containsKey(nameInfo.getDeclaration().getRawSignature())){
							Integer nameOrderNumber = this.refactoring.names.get(nameInfo.getDeclaration().getRawSignature());
							if(this.refactoring.nameTrail.containsValue(nameOrderNumber)){
								String orgName = null;
								boolean found = false;
								for (Entry<String, Integer> entry : this.refactoring.nameTrail.entrySet()) {
									if(entry.getValue().equals(nameOrderNumber)){
										orgName = entry.getKey();
									}
								}
								if(orgName != null){
									for (NameInformation orgNameInfo : this.refactoring.container.getAllAfterUsedNamesChoosenByUser()) {
										if( orgName.equals(orgNameInfo.getDeclaration().getRawSignature()) ){
											found = true;
										}
									}
								}
								
								if(!found){
									similarOnReturnWays = false;
								}
							}
						}
					}
										
					if(similarOnReturnWays){
						IASTNode call = refactoring.getMethodCall(name,
								this.refactoring.nameTrail, this.refactoring.names,
								this.refactoring.container, similarContainer);
						ASTRewrite rewrite = collector.rewriterForTranslationUnit(stmtToReplace.get(0)
								.getTranslationUnit());
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
				return super.visit(stmt);
			}
			
		}

		private boolean isInSelection(IASTStatement stmt) {
			List<IASTNode>nodes = this.refactoring.container.getNodesToWrite();
			for (IASTNode node : nodes) {
				if(node.equals(stmt)) {
					return true;
				}
			}
			return false;
		}

		private void clear() {
			i = 0;
			this.refactoring.names.clear();
			similarContainer = new NodeContainer();
			this.refactoring.namesCounter.setObject(ExtractFunctionRefactoring.NULL_INTEGER);
			this.refactoring.trailPos.setObject(ExtractFunctionRefactoring.NULL_INTEGER);
		stmtToReplace.clear();
		}
	}