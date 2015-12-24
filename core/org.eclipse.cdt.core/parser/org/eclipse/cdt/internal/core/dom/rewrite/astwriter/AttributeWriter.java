/*******************************************************************************
 * Copyright (c) 2008, 2015 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Thomas Corbat (IFS) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.rewrite.astwriter;

import org.eclipse.cdt.core.dom.ast.IASTAlignmentSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTAttribute;
import org.eclipse.cdt.core.dom.ast.IASTAttributeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTToken;
import org.eclipse.cdt.core.dom.ast.IASTTokenList;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTAttribute;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTAttributeList;
import org.eclipse.cdt.core.dom.ast.gnu.IGCCASTAttributeList;
import org.eclipse.cdt.core.parser.GCCKeywords;
import org.eclipse.cdt.core.parser.Keywords;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTAttribute;
import org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.NodeCommentMap;

/**
 * Generates source code for attribute nodes. The actual string operations are delegated to the
 * <code>Scribe</code> class.
 *
 * @see Scribe
 * @see IASTAttribute
 */
public class AttributeWriter extends NodeWriter {

	public AttributeWriter(Scribe scribe, ASTWriterVisitor visitor, NodeCommentMap commentMap) {
		super(scribe, visitor, commentMap);
	}

	public void writeAttributeSpecifier(IASTAttributeSpecifier attribute) {
		if (attribute instanceof ICPPASTAttributeList) {
			writeAttributeSpecifier((ICPPASTAttributeList) attribute);
		} else if (attribute instanceof IGCCASTAttributeList) {
			writeGCCAttributeSpecifier((IGCCASTAttributeList) attribute);
		} else if (attribute instanceof IASTAlignmentSpecifier) {
			writeAlignmentSpecifier((IASTAlignmentSpecifier) attribute);
		}
	}
	
	private void writeAlignmentSpecifier(IASTAlignmentSpecifier specifier) {
		scribe.print(Keywords.ALIGNAS);
		scribe.print(OPENING_PARENTHESIS);
		if (specifier.getExpression() != null) {
			specifier.getExpression().accept(visitor);
		} else if (specifier.getTypeId() != null) {
			specifier.getTypeId().accept(visitor);
		}
		scribe.print(CLOSING_PARENTHESIS);
	}

	private void writeGCCAttributeSpecifier(IGCCASTAttributeList specifier) {
		scribe.print(GCCKeywords.__ATTRIBUTE__);
		scribe.print(OPENING_PARENTHESIS);
		scribe.print(OPENING_PARENTHESIS);
		IASTAttribute[] innerAttributes = specifier.getAttributes();
		for (int i = 0; i < innerAttributes.length; i++) {
			IASTAttribute innerAttribute = innerAttributes[i];
			writeAttribute((CPPASTAttribute)innerAttribute);
			if (i < innerAttributes.length - 1) {
				scribe.print(',');
				scribe.printSpace();
			}
		}
		scribe.print(CLOSING_PARENTHESIS);
		scribe.print(CLOSING_PARENTHESIS);
	}

	private void writeAttributeSpecifier(ICPPASTAttributeList specifier) {
		scribe.print(OPENING_SQUARE_BRACKET);
		scribe.print(OPENING_SQUARE_BRACKET);
		IASTAttribute[] innerAttributes = specifier.getAttributes();
		for (int i = 0; i < innerAttributes.length; i++) {
			IASTAttribute innerAttribute = innerAttributes[i];
			writeAttribute((ICPPASTAttribute)innerAttribute);
			if (i < innerAttributes.length - 1) {
				scribe.print(',');
				scribe.printSpace();
			}
		}
		scribe.print(CLOSING_SQUARE_BRACKET);
		scribe.print(CLOSING_SQUARE_BRACKET);
	}

	private void writeAttribute(ICPPASTAttribute attribute) {
		char[] scope = attribute.getScope();
		if (scope != null) {
			scribe.print(scope);
			scribe.print(COLON_COLON);
		}
		scribe.print(attribute.getName());

		IASTToken argumentClause = attribute.getArgumentClause();
		if (argumentClause != null) {
			scribe.print(OPENING_PARENTHESIS);
			printTokens(argumentClause);
			scribe.print(CLOSING_PARENTHESIS);
		}

		if (attribute.hasPackExpansion()) {
			scribe.printSpace();
			scribe.print(VAR_ARGS);
		}
	}

	protected void printTokens(IASTToken token) {
		if (token instanceof IASTTokenList) {
			for (IASTToken innerToken : ((IASTTokenList) token).getTokens()) {
				printTokens(innerToken);
			}
		} else {
			char[] tokenCharImage = token.getTokenCharImage();
			scribe.print(tokenCharImage);
		}
	}
}
