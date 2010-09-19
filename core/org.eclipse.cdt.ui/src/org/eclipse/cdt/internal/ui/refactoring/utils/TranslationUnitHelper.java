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

import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.CDOM;
import org.eclipse.cdt.core.dom.IASTServiceProvider.UnsupportedDialectException;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.refactoring.Container;

/**
 * A collection of methods that deal with IASTTranslationUnits.
 * 
 * @author Mirko Stocker
 *
 */
public class TranslationUnitHelper {
	private static final int AST_STYLE = ITranslationUnit.AST_CONFIGURE_USING_SOURCE_CONTEXT | ITranslationUnit.AST_SKIP_INDEXED_HEADERS;

	/**
	 * @param filename to load the translation unit from
	 * @return the translation unit for the file or null
	 * @throws CoreException 
	 */
	public static IASTTranslationUnit loadTranslationUnit(String filename, boolean useIndex) throws CoreException{
		if (filename != null) {
			IFile[] tmpFile = null;

			tmpFile = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(
					URIUtil.toURI(filename));

			return loadTranslationUnit(tmpFile[0], useIndex);
		}

		return null;
	}
	
	/**
	 * @param file to load the translation unit from
	 * @return the translation unit for the file or null
	 * @throws CoreException 
	 */
	public static IASTTranslationUnit loadTranslationUnit(IFile file, boolean useIndex) throws CoreException {
		if (file == null) {
			return null;
		}
		if (useIndex) {
			return loadIndexBasedTranslationUnit(file);
		} else {
			return loadFileBasedTranslationUnit(file);
		}
	}

	private static IASTTranslationUnit loadFileBasedTranslationUnit(IFile file) {
		try {
			IASTTranslationUnit fileUnit = CDOM.getInstance().getTranslationUnit(file, CDOM.getInstance().getCodeReaderFactory(CDOM.PARSE_SAVED_RESOURCES), true);
			return fileUnit;
		} catch (UnsupportedDialectException e) {
			return null;
		}
	}

	private static IASTTranslationUnit loadIndexBasedTranslationUnit(IFile file) throws CoreException {
		IIndex index = null;
		try {
			index = lockIndex();
			ITranslationUnit tu = (ITranslationUnit) CCorePlugin.getDefault().getCoreModel().create(file);
			return tu.getAST(index, AST_STYLE);
		} catch (InterruptedException e) {
			CUIPlugin.log(e);
		} finally {
			unlockIndex(index);
		}
		return null;
	}

	/**
	 * Visits all names in the TU to find the specified name
	 */
	public static IASTName findNameInTranslationUnit(IASTTranslationUnit transUnit, IASTNode oldName) {
		final String oldFileName = oldName.getFileLocation().getFileName();
		final IASTFileLocation pos = oldName.getFileLocation();
		final Container<IASTName> nameCon = new Container<IASTName>();

		transUnit.accept(new ASTVisitor() {

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
	
	/**
	 * @return the first node in the translation unit or null
	 */
	public static IASTNode getFirstNode(IASTTranslationUnit unit) {
		IASTDeclaration firstNode = null;
		for (IASTDeclaration each : unit.getDeclarations()) {
			if (firstNode == null) {
				firstNode = each;
			} else if (each.getNodeLocations() != null && 
					each.getNodeLocations()[0].getNodeOffset() < firstNode.getNodeLocations()[0].getNodeOffset()) {
				firstNode = each;
			}
		}
		return firstNode;
	}
	
	private static IIndex lockIndex() throws CoreException, InterruptedException {
		IIndex index;
		ICProject[] projects= CoreModel.getDefault().getCModel().getCProjects();
		index= CCorePlugin.getIndexManager().getIndex(projects);
		try {
			index.acquireReadLock();
		} catch (InterruptedException e) {
			// no lock was acquired
			index= null; 
			throw e;
		}
		return index;
	}
	
	private static void unlockIndex(IIndex index) {
		if (index != null) {
			index.releaseReadLock();
		}
	}
}
