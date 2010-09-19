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
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.utils;

import org.eclipse.core.runtime.Path;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndexName;

import org.eclipse.cdt.internal.ui.refactoring.Container;

/**
 * @author Guido Zgraggen IFS
 */
public class ExpressionFinder {	
	
	public static IASTName findExpressionInTranslationUnit(IASTTranslationUnit transUnit, final IIndexName indexName) {
		final Container<IASTName> expName = new Container<IASTName>();
		transUnit.accept(new ASTVisitor() {
			{
				shouldVisitNames = true;
			}

			@Override
			public int visit(IASTName name) {
				if (name.isReference() && name.getNodeLocations().length > 0) {
					IASTNodeLocation nodeLocation = name.getNodeLocations()[0];
					if (indexName.getNodeOffset() == nodeLocation.getNodeOffset() 
							&& indexName.getNodeLength() == nodeLocation.getNodeLength()
							&& new Path(indexName.getFileLocation().getFileName()).equals(new Path(nodeLocation.asFileLocation().getFileName()))) {
						expName.setObject(name);
						return ASTVisitor.PROCESS_ABORT;
					}
				}
				return ASTVisitor.PROCESS_CONTINUE;
			}

		});
		return expName.getObject();
	}
}
