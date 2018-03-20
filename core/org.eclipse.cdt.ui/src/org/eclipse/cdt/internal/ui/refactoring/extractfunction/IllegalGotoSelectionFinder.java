/*******************************************************************************
 * Copyright (c) 2018 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 *     Institute for Software - initial API and implementation 
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.extractfunction;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTGotoStatement;
import org.eclipse.cdt.core.dom.ast.IASTLabelStatement;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;

import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;

/**
 * @author Daniel Marty IFS
 */
class IllegalGotoSelectionFinder extends ASTVisitor {
	private Set<String> setLabel = new HashSet<String>();

	private Map<String, Integer> mapGotoInSelection = new HashMap<String, Integer>();
	private Map<String, Integer> mapGotoInFunction = new HashMap<String, Integer>();
	private IASTFunctionDefinition functionDefinition;

	{
		shouldVisitStatements = true;
	}

	@Override
	public int visit(IASTStatement statement) {
		functionDefinition = ASTQueries.findAncestorWithType(statement, IASTFunctionDefinition.class);
		if (statement instanceof IASTGotoStatement) {
			IASTGotoStatement gotoStatement = (IASTGotoStatement) statement;
			for (IASTNode child : gotoStatement.getChildren()) {
				if (child instanceof IASTName) {
					IASTName name = (IASTName) child;
					if (mapGotoInSelection.containsKey(name.toString())) {
						int count = mapGotoInSelection.get(name.toString());
						mapGotoInSelection.put(name.toString(), count + 1);
					} else {
						mapGotoInSelection.put(name.toString(), 1);
					}
				}
			}
		} else if (statement instanceof IASTLabelStatement) {
			IASTLabelStatement labelStatement = (IASTLabelStatement) statement;
			for (IASTNode child : labelStatement.getChildren()) {
				if (child instanceof IASTName) {
					IASTName name = (IASTName) child;
					setLabel.add(name.toString());
				}
			}
		}
		return ASTVisitor.PROCESS_CONTINUE;
	}

	public boolean containsIllegalSelection() {

		if (functionDefinition == null) {
			return false;
		}

		functionDefinition.accept(new ASTVisitor() {

			{
				shouldVisitStatements = true;
			}

			@Override
			public int visit(IASTStatement statement) {
				if (statement instanceof IASTGotoStatement) {
					for (IASTNode child : statement.getChildren()) {
						if (child instanceof IASTName) {
							IASTName name = (IASTName) child;
							if (mapGotoInFunction.containsKey(name.toString())) {
								int count = mapGotoInFunction.get(name.toString());
								mapGotoInFunction.put(name.toString(), count + 1);
							} else {
								mapGotoInFunction.put(name.toString(), 1);
							}
						}
					}
				}
				return PROCESS_CONTINUE;
			}
		});

		for (Map.Entry<String, Integer> entry : mapGotoInSelection.entrySet()) {
			if (!setLabel.contains(entry.getKey())) {
				return true;
			}
		}

		for (String label : setLabel) {
			if (!mapGotoInSelection.containsKey(label)
					|| !mapGotoInSelection.get(label).equals(mapGotoInFunction.get(label))) {
				return true;
			}
		}
		return false;
	}
}
