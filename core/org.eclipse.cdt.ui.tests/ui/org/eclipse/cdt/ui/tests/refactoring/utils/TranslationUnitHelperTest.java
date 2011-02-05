/*******************************************************************************
 * Copyright (c) 2008, 2011 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 * 	   Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.refactoring.utils;

import java.util.Collection;
import java.util.Properties;

import org.eclipse.core.resources.IFile;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.ui.tests.refactoring.RefactoringTest;
import org.eclipse.cdt.ui.tests.refactoring.TestSourceFile;

import org.eclipse.cdt.internal.ui.refactoring.utils.TranslationUnitHelper;

/**
 * @author Mirko Stocker
 */
public class TranslationUnitHelperTest extends RefactoringTest {
	private int offset;

	public TranslationUnitHelperTest(String name, Collection<TestSourceFile> files) {
		super(name, files);
	}

	@Override
	protected void runTest() throws Throwable {
		IFile file = project.getFile(fileName);
		IASTTranslationUnit unit = TranslationUnitHelper.loadTranslationUnit(file, false);
		IASTNode firstNode = TranslationUnitHelper.getFirstNode(unit);
		assertEquals(offset, firstNode.getNodeLocations()[0].getNodeOffset());
	}

	@Override
	protected void configureRefactoring(Properties refactoringProperties) {
		offset = new Integer(refactoringProperties.getProperty("offset", "0")).intValue();  //$NON-NLS-1$
	}
}
