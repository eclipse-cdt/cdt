/*
 * Copyright (c) 2013 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.cdt.internal.qt.core.parser;

import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTypeId;
import org.eclipse.cdt.core.dom.parser.cpp.GPPParserExtensionConfiguration;
import org.eclipse.cdt.core.parser.EndOfFileException;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.NullLogService;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.internal.core.dom.parser.BacktrackException;
import org.eclipse.cdt.internal.core.dom.parser.DeclarationOptions;
import org.eclipse.cdt.internal.core.dom.parser.cpp.GNUCPPSourceParser;

/**
 * A parser that use a special StringScanner to extract small sections of C++ syntax that
 * are used in Qt macro expansions.
 *
 * @see StringScanner
 */
@SuppressWarnings("restriction")
public class QtParser extends GNUCPPSourceParser {

	private QtParser(String str) {
		super(new StringScanner(str), ParserMode.QUICK_PARSE, new NullLogService(),
				GPPParserExtensionConfiguration.getInstance());
	}

	/**
	 * The argument String is the expansion parameter for SIGNAL and SLOT macros.  The text
	 * is parsed and the function declarator is returned if possible.  Returns null if the
	 * string is not a valid function declarator reference.
	 */
	public static ICPPASTFunctionDeclarator parseQtMethodReference(String str) {
		// Reject strings that have embedded line terminators.  This is needed to properly check that
		// one that is about to be added.
		if (str == null || str.contains(";"))
			return null;

		QtParser parser = new QtParser(str + ';');
		try {
			IASTDeclarator declarator = parser.declarator(GNUCPPSourceParser.DtorStrategy.PREFER_FUNCTION,
					DeclarationOptions.CPP_MEMBER);
			if (!(declarator instanceof ICPPASTFunctionDeclarator))
				return null;

			// JI 439374: Make sure the ; was the last token read to prevent errors where extra strings
			//            appear in the expansion parameter.
			if (parser.lastTokenFromScanner == null || parser.lastTokenFromScanner.getType() != IToken.tSEMI)
				return null;

			// JI 439374: Make sure the ; was the last token read to prevent errors where extra strings
			//            appear in the expansion parameter.
			if (parser.lastTokenFromScanner == null || parser.lastTokenFromScanner.getType() != IToken.tSEMI)
				return null;

			// make sure this is a legal declarator for a Qt method reference
			ICPPASTFunctionDeclarator function = (ICPPASTFunctionDeclarator) declarator;

			// 1) parameters must not have names
			for (ICPPASTParameterDeclaration param : function.getParameters()) {
				ICPPASTDeclarator decltor = param.getDeclarator();
				if (decltor == null)
					continue;

				IASTName paramName = decltor.getName();
				if (paramName == null)
					continue;

				char[] name = paramName.getSimpleID();
				if (name == null || name.length <= 0)
					continue;

				// The Qt normalization code treats a reference with a trailing const as a special case (this
				// seems to be a bug in the way they normalize const pointers.  We could support this case by
				// allowing reference parameters to be named 'const'.  However, since this seems to be a bug
				// in Qt they will likely fix it at some point, and there doesn't seem to be a case where the
				// user would need to reference the Qt method in this way.

				// the parameter has a non-empty name, so reject the declarator
				return null;
			}

			// All tests have passed, so return this declarator.
			return function;
		} catch (BacktrackException e) {
			return null;
		} catch (EndOfFileException e) {
			return null;
		}
	}

	public static ICPPASTTypeId parseTypeId(String str) {
		QtParser parser = new QtParser(str);
		try {
			return parser.typeId(new DeclarationOptions(DeclarationOptions.NO_INITIALIZER));
		} catch (BacktrackException e) {
			return null;
		} catch (EndOfFileException e) {
			return null;
		}
	}
}
