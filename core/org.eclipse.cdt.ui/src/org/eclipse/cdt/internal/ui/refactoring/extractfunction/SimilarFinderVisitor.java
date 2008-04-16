/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule fuer Technik  
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

import java.util.List;
import java.util.Vector;
import java.util.Map.Entry;

import org.eclipse.core.resources.IFile;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.cpp.CPPASTVisitor;

import org.eclipse.cdt.internal.ui.refactoring.ChangeTreeSet;
import org.eclipse.cdt.internal.ui.refactoring.NodeContainer;
import org.eclipse.cdt.internal.ui.refactoring.NodeContainer.NameInformation;

final class SimilarFinderVisitor extends CPPASTVisitor {

	private final ExtractFunctionRefactoring extractFunctionRefactoring;

	// egtodo
	// private final ChangeTreeSet set;
//	private final IFile file;
//	private final IASTName name;

	private final Vector<IASTNode> trail;
	private final List<IASTNode> stmts;
	private int i = 0;
//	private int start;
	private NodeContainer similarContainer;
//	private final String title;

	SimilarFinderVisitor(ExtractFunctionRefactoring extractFunctionRefactoring, ChangeTreeSet set, Vector<IASTNode> trail, IFile file, IASTName name, List<IASTNode> stmts, String title) {
		this.extractFunctionRefactoring = extractFunctionRefactoring;
//		this.set = set;
		this.trail = trail;
//		this.file = file;
//		this.name = name;
		this.stmts = stmts;
//		this.title = title;

		this.similarContainer = new NodeContainer();
	}

	{
		shouldVisitStatements = true;
	}

	@Override
	public int visit(IASTStatement stmt) {

		boolean isAllreadyInMainRefactoring = isInSelection(stmt);

		if( (!isAllreadyInMainRefactoring)
				&& this.extractFunctionRefactoring.isStatementInTrail(stmt, trail)){
			if(i == 0){
//				start = stmt.getFileLocation().getNodeOffset();
			}
			similarContainer.add(stmt);	
			++i;

			if(i==stmts.size()){
				//found similar code

				boolean similarOnReturnWays = true;
				for (NameInformation nameInfo : similarContainer.getAllAfterUsedNames()) {
					if(this.extractFunctionRefactoring.names.containsKey(nameInfo.getDeclaration().getRawSignature())){
						Integer nameOrderNumber = this.extractFunctionRefactoring.names.get(nameInfo.getDeclaration().getRawSignature());
						if(this.extractFunctionRefactoring.nameTrail.containsValue(nameOrderNumber)){
							String orgName = null;
							boolean found = false;
							for (Entry<String, Integer> entry : this.extractFunctionRefactoring.nameTrail.entrySet()) {
								if(entry.getValue().equals(nameOrderNumber)){
									orgName = entry.getKey();
								}
							}
							if(orgName != null){
								for (NameInformation orgNameInfo : this.extractFunctionRefactoring.container.getAllAfterUsedNamesChoosenByUser()) {
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

				if (similarOnReturnWays) {
					// egtodo
//					CTextFileChange replace = new CTextFileChange(title, file);
//					IASTFileLocation loc = stmt.getFileLocation();
//					int end = loc.getNodeOffset() + loc.getNodeLength() - start;
//					try {
//						end = this.extractFunctionRefactoring
//								.getLengthWithNewLine(file, start, loc
//										.getNodeOffset()
//										+ loc.getNodeLength() - start);
//					} catch (CoreException e) {
//						// Keep current length
//					}
//					ReplaceEdit replaceEdit = new ReplaceEdit(start, end,
//							this.extractFunctionRefactoring.getMethodCall(name,
//									this.extractFunctionRefactoring.nameTrail,
//									this.extractFunctionRefactoring.names,
//									this.extractFunctionRefactoring.container,
//									similarContainer));
//					replace.setEdit(replaceEdit);
//					set.add(replace);
				}

				clear();
			}

			return PROCESS_SKIP;
		}
		clear();
		return super.visit(stmt);

	}

	private boolean isInSelection(IASTStatement stmt) {
		List<IASTNode>nodes = this.extractFunctionRefactoring.container.getNodesToWrite();
		for (IASTNode node : nodes) {
			if(node.equals(stmt)) {
				return true;
			}
		}
		return false;

		//			return container.getNodesToWrite().contains(stmt);
	}

	private void clear() {
		i = 0;
		this.extractFunctionRefactoring.names.clear();
		similarContainer = new NodeContainer();
		this.extractFunctionRefactoring.namesCounter.setObject(ExtractFunctionRefactoring.NULL_INTEGER);
		this.extractFunctionRefactoring.trailPos.setObject(ExtractFunctionRefactoring.NULL_INTEGER);
	}
}
