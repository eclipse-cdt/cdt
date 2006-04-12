/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.DOMSearchUtil;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.resources.IFile;

/**
 * @author johnc
 *
 */
public class DOMSelectionParseBaseTest extends DOMFileBasePluginTest {

	public DOMSelectionParseBaseTest(String name, Class className) {
		super(name, className);
	}

	protected IASTNode parse(String code, int offset1, int offset2) throws Exception {
		return parse( code, offset1, offset2, true );
	}

	protected IASTNode parse(String code, int offset1, int offset2, boolean expectedToPass) throws Exception {
		IFile file = importFile("temp.cpp", code); //$NON-NLS-1$
		return parse(file, offset1, offset2, expectedToPass);
	}
	
	protected IASTNode parse(IFile file, int offset1, int offset2, boolean expectedToPass) throws Exception {
		ITranslationUnit tu = (ITranslationUnit)CCorePlugin.getDefault().getCoreModel().create(file);
		IASTTranslationUnit ast = tu.getLanguage().getASTTranslationUnit(tu, 0);
		IASTName[] names = tu.getLanguage().getSelectedNames(ast, offset1, offset2 - offset1);
		
		if (!expectedToPass) return null;
		
		if (names.length == 0) {
			assertFalse(true);
		} else {
			return names[0];
		}		
		
		return null;
	}
	
	protected IASTName[] getDeclarationOffTU(IASTName name) {
        return DOMSearchUtil.getNamesFromDOM(name, DOMSearchUtil.DECLARATIONS);
	}
    
    protected IASTName[] getReferencesOffTU(IASTName name) {
        return DOMSearchUtil.getNamesFromDOM(name, DOMSearchUtil.REFERENCES);
    }
}
