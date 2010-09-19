/*******************************************************************************
 * Copyright (c) 2008, 2009 Institute for Software, HSR Hochschule fuer Technik  
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

import java.util.TreeMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.refactoring.Container;

/**
 * Helper class to find definitions.
 * 
 * @author Lukas Felber
 *
 */
public class DefinitionFinder {

	public static IASTName getDefinition(IASTSimpleDeclaration simpleDeclaration, IFile file) throws CoreException{
		IASTDeclarator declarator = simpleDeclaration.getDeclarators()[0];
		IBinding resolveBinding = declarator.getName().resolveBinding();
		return DefinitionFinder.getDefinition(declarator.getName(), resolveBinding, file);
	}

	public static IASTName getDefinition(IASTName methodName, IBinding bind, IFile file) throws CoreException {
		TreeMap<String, IASTTranslationUnit> parsedFiles = new TreeMap<String, IASTTranslationUnit>();

		ITranslationUnit tu = (ITranslationUnit) CCorePlugin.getDefault().getCoreModel().create(file);
		IIndex index = CCorePlugin.getIndexManager().getIndex(tu.getCProject());
		IIndexName[] pdomref = null;

		try {
			index.acquireReadLock();
		} catch (InterruptedException e) {
			IStatus status = new Status(IStatus.WARNING, CUIPlugin.PLUGIN_ID, IStatus.OK, e.getMessage(), e);
			CUIPlugin.log(status);
			return null;
		} 
		try {
			pdomref= index.findDefinitions(bind);
		}
		finally {
			index.releaseReadLock();
		}

		if (pdomref==null || pdomref.length < 1) {
			return null;
		}

		IASTTranslationUnit transUnit;
		if (!parsedFiles.containsKey(pdomref[0].getFileLocation().getFileName())) {
			String filename = pdomref[0].getFileLocation().getFileName();
			transUnit = TranslationUnitHelper.loadTranslationUnit(filename, false);
		} else {
			transUnit = parsedFiles.get(pdomref[0].getFileLocation().getFileName());
		}
		return findDefinitionInTranslationUnit(transUnit, pdomref[0]);
	}

	private static IASTName findDefinitionInTranslationUnit(IASTTranslationUnit transUnit, final IIndexName indexName) {
		final Container<IASTName> defName = new Container<IASTName>();
		transUnit.accept(new ASTVisitor() {
			{
				shouldVisitNames = true;
			}

			@Override
			public int visit(IASTName name) {
				if (name.isDefinition() && name.getNodeLocations().length > 0) {
					IASTNodeLocation nodeLocation = name.getNodeLocations()[0];
					if (indexName.getNodeOffset() == nodeLocation.getNodeOffset() 
							&& indexName.getNodeLength() == nodeLocation.getNodeLength()
							&& new Path(indexName.getFileLocation().getFileName()).equals(new Path(nodeLocation.asFileLocation().getFileName()))) {
						defName.setObject(name);
						return ASTVisitor.PROCESS_ABORT;
					}
				}
				return ASTVisitor.PROCESS_CONTINUE;
			}

		});
		return defName.getObject();
	}
}
