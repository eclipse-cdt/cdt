/*******************************************************************************
 * Copyright (c) 2005, 2013 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    IBM Rational Software - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.resources.IFile;

/**
 * @author johnc
 *
 */
public class DOMSelectionParseTestBase extends DOMFileBasePluginTest {

	public DOMSelectionParseTestBase() {
	}

	public DOMSelectionParseTestBase(String name) {
		super(name);
	}

	public DOMSelectionParseTestBase(String name, Class className) {
		super(name, className);
	}

	protected IASTNode parse(String code, int offset1, int offset2) throws Exception {
		return parse(code, offset1, offset2, true);
	}

	protected IASTNode parse(String code, int offset1, int offset2, boolean expectedToPass) throws Exception {
		IFile file = importFile("temp.cpp", code); //$NON-NLS-1$
		return parse(file, offset1, offset2, expectedToPass);
	}

	protected IASTNode parse(IFile file, int offset1, int offset2, boolean expectedToPass) throws Exception {
		ITranslationUnit tu = (ITranslationUnit) CCorePlugin.getDefault().getCoreModel().create(file);
		IASTTranslationUnit ast = tu.getAST();
		IASTName name = ast.getNodeSelector(null).findName(offset1, offset2 - offset1);

		if (!expectedToPass)
			return null;

		assertNotNull(name);
		return name;
	}

	protected IName[] getDeclarationOffTU(IASTName name) {
		return DOMSearchUtil.getNamesFromDOM(name, DOMSearchUtil.DECLARATIONS);
	}

	protected IName[] getReferencesOffTU(IASTName name) {
		return DOMSearchUtil.getNamesFromDOM(name, DOMSearchUtil.REFERENCES);
	}
}
