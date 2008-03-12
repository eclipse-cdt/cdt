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

package org.eclipse.cdt.internal.ui.refactoring.utils;

import org.eclipse.cdt.core.dom.CDOM;
import org.eclipse.cdt.core.dom.IASTServiceProvider.UnsupportedDialectException;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.CPPASTVisitor;
import org.eclipse.cdt.internal.ui.refactoring.Container;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class TranslationUnitHelper {

	public static IASTTranslationUnit loadTranslationUnit(String filename) {

		if (filename != null) {
			IPath path = new Path(filename);
			IFile tmpFile = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path);
			return loadTranslationUnit(tmpFile);
		}

		return null;
	}

	public static IASTTranslationUnit loadTranslationUnit(IFile tmpFile) {
		if (tmpFile != null) {
			try {
				IASTTranslationUnit fileUnit = CDOM.getInstance().getTranslationUnit(tmpFile, CDOM.getInstance().getCodeReaderFactory(CDOM.PARSE_SAVED_RESOURCES), true);
				return fileUnit;
			} catch (UnsupportedDialectException e) {
				return null;
			}
		}
		return null;
	}

	public static IASTName findNameInTranslationUnit(IASTTranslationUnit transUnit, IASTNode oldName) {
		final String oldFileName = oldName.getFileLocation().getFileName();
		final IASTFileLocation pos = oldName.getFileLocation();
		final Container<IASTName> nameCon = new Container<IASTName>();

		transUnit.accept(new CPPASTVisitor() {

			{
				shouldVisitNames = true;
			}

			@Override
			public int visit(IASTName locName) {
				IASTFileLocation locFileLocation = locName.getFileLocation();
				if (locFileLocation != null && oldFileName.equals(locFileLocation.getFileName()) && pos.getNodeOffset() == locFileLocation.getNodeOffset()
						&& pos.getNodeLength() == locFileLocation.getNodeLength()) {
					nameCon.setObject(locName);
					return PROCESS_ABORT;
				}
				return super.visit(locName);
			}

		});
		return nameCon.getObject();
	}
	
	public static IASTNode getFirstNode(IASTTranslationUnit unit) {
		IASTDeclaration firstNode = null;
		for (IASTDeclaration each : unit.getDeclarations()) {
			if(firstNode == null) {
				firstNode = each;
			} else if(each.getNodeLocations() != null && 
					each.getNodeLocations()[0].getNodeOffset() < firstNode.getNodeLocations()[0].getNodeOffset()) {
				firstNode = each;
			}
		}
		return firstNode;
	}
}
