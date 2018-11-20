/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.xlc.tests;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.lrparser.tests.ParseHelper;
import org.eclipse.cdt.core.lrparser.xlc.XlcCLanguage;
import org.eclipse.cdt.core.lrparser.xlc.XlcCPPLanguage;
import org.eclipse.cdt.core.model.ILanguage;

import junit.framework.TestCase;

public class XlcTestBase extends TestCase {

	public XlcTestBase() {
	}

	public XlcTestBase(String name) {
		super(name);
	}

	protected IASTTranslationUnit parse(String code, ILanguage language, boolean checkBindings) {
		ParseHelper.Options options = new ParseHelper.Options();
		options.setCheckSyntaxProblems(true);
		options.setCheckPreprocessorProblems(true);
		options.setCheckBindings(checkBindings);
		return ParseHelper.parse(code, language, options);
	}

	protected XlcCLanguage getCLanguage() {
		return XlcCLanguage.getDefault();
	}

	protected XlcCPPLanguage getCPPLanguage() {
		return XlcCPPLanguage.getDefault();
	}

}
