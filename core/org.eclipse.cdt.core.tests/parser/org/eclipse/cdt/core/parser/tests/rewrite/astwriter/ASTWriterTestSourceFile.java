/*******************************************************************************
 * Copyright (c) 2008, 2011 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Institute for Software - initial API and implementation
 ******************************************************************************/
package org.eclipse.cdt.core.parser.tests.rewrite.astwriter;

import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.tests.ast2.AST2TestBase.ScannerKind;
import org.eclipse.cdt.core.parser.tests.rewrite.TestSourceFile;

/**
 * @author Guido Zgraggen IFS
 */
public class ASTWriterTestSourceFile extends TestSourceFile {
	private ParserLanguage parserLanguage = ParserLanguage.CPP;
	private ScannerKind scannerKind = ScannerKind.STD;

	public ASTWriterTestSourceFile(String name) {
		super(name);
	}

	public void setParserLanguage(ParserLanguage lang) {
		this.parserLanguage = lang;
	}

	public ParserLanguage getParserLanguage() {
		return parserLanguage;
	}

	public ScannerKind getScannerKind() {
		return scannerKind;
	}

	public void setScannerKind(ScannerKind scannerKind) {
		this.scannerKind = scannerKind;
	}
}
