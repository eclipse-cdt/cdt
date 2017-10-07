/******************************************************************************* 
 * Copyright (c) 2017 Pavel Marek 
 * All rights reserved. This program and the accompanying materials  
 * are made available under the terms of the Eclipse Public License v1.0  
 * which accompanies this distribution, and is available at  
 * http://www.eclipse.org/legal/epl-v10.html   
 *  
 * Contributors:  
 *      Pavel Marek - initial API and implementation 
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.overridemethods;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.parser.FileContent;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IncludeFileContentProvider;
import org.eclipse.cdt.core.parser.ParserUtil;
import org.eclipse.cdt.core.parser.ScannerInfo;

/**
 * Represent a class that parses methods (Strings) to IASTNodes.
 * Parsing code copied from CCodeFormatter.
 */
public class CodeParser {
	private IncludeFileContentProvider includeContentProvider;
	private IScannerInfo scannerInfo;
	private ILanguage language;
	
	public CodeParser() {
		includeContentProvider= IncludeFileContentProvider.getEmptyFilesProvider();
		scannerInfo= new ScannerInfo();
		language= GPPLanguage.getDefault();
	}
	
	public IASTTranslationUnit parse(String code) {
		FileContent content= FileContent.create("<text>", code.toCharArray()); //$NON-NLS-1$
		IASTTranslationUnit ast= null;
		
		try {
			ast= language.getASTTranslationUnit(content, scannerInfo, includeContentProvider, null, 0,
					ParserUtil.getParserLogService());
		} catch (CoreException e) {
			e.printStackTrace();
		}
		
		return ast;
	}

	/**
	 * Convert ICPPMethod class wrapper (Method) to IASTNode.
	 * @param method
	 */
	public IASTNode parse(Method method) {
		StringBuilder structStringBuilder= new StringBuilder("struct A{"); //$NON-NLS-1$
		
		String methodToPrint= method.print();
		// We need to wrap methodToPrint in a struct, otherwise the ast may
		// contain ProblemNodes (for example when methodToPrint looks like this:
		// "virtual void m() const").
		structStringBuilder.append(methodToPrint);
		structStringBuilder.append("};"); //$NON-NLS-1$
		
		IASTTranslationUnit ast= parse(structStringBuilder.toString());
		// "Unpack" method from the struct
		IASTDeclaration structDecl= ast.getDeclarations()[0];
		ICPPASTCompositeTypeSpecifier compTypeSpecifier=
				(ICPPASTCompositeTypeSpecifier) structDecl.getChildren()[0];
		return compTypeSpecifier.getMembers()[0];
	}
}
