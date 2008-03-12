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
package org.eclipse.cdt.ui.tests.refactoring.utils;

import java.util.Properties;
import java.util.Vector;

import org.eclipse.core.resources.IFile;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.ui.tests.refactoring.RefactoringTest;
import org.eclipse.cdt.ui.tests.refactoring.TestSourceFile;

import org.eclipse.cdt.internal.ui.refactoring.utils.TranslationUnitHelper;

/**
 * @author Mirko Stocker
 *
 */
public class TranslationUnitHelperTest extends RefactoringTest {

	private int offset;

	public TranslationUnitHelperTest(String name,Vector<TestSourceFile> files) {
		super(name, files);
	}

	@Override
	protected void runTest() throws Throwable {
		IFile file = project.getFile(fileName);
		IASTTranslationUnit unit = TranslationUnitHelper.loadTranslationUnit(file);
		IASTNode firstNode = TranslationUnitHelper.getFirstNode(unit);
		assertEquals(offset, firstNode.getNodeLocations()[0].getNodeOffset());
	}

	@Override
	protected void configureRefactoring(Properties refactoringProperties) {
		String offsetKind = (System.getProperty("line.separator").equals("\n")) ? "offset_unix" : "offset_win"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		offset = new Integer(refactoringProperties.getProperty(offsetKind, "0")).intValue();  //$NON-NLS-1$
	}
}
