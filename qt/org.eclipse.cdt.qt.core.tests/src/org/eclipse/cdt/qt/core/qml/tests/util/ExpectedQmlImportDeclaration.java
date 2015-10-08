/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.qt.core.qml.tests.util;

import static org.junit.Assert.*;

import org.antlr.v4.runtime.ParserRuleContext;
import org.eclipse.cdt.internal.qt.core.qml.parser.QMLParser.QmlHeaderItemContext;
import org.eclipse.cdt.internal.qt.core.qml.parser.QMLParser.QmlImportDeclarationContext;
import org.junit.Assert;

public class ExpectedQmlImportDeclaration extends ExpectedQmlElement {

	private final String id;
	private final String version;
	private final String asIdentifier;

	public ExpectedQmlImportDeclaration(String qualifiedId, String version, String asIdentifier) {
		this.id = qualifiedId;
		this.version = version;
		this.asIdentifier = asIdentifier;
	}

	@Override
	public void assertEquals(ParserRuleContext context, int index) {
		String baseMsg = getBaseMessage(context, index);

		if (!(context instanceof QmlHeaderItemContext))
			fail(baseMsg + " is not a Qml Import Declaration"); //$NON-NLS-1$

		QmlImportDeclarationContext importCtx = ((QmlHeaderItemContext) context).qmlImportDeclaration();
		if (importCtx == null)
			fail(baseMsg + " is not a Qml Import Declaration"); //$NON-NLS-1$

		// 'import' <qmlQualifiedId>|<StringLiteral>
		if (importCtx.qmlQualifiedId() != null) {
			Assert.assertEquals(baseMsg + " has incorrect import id", id, //$NON-NLS-1$
					importCtx.qmlQualifiedId().getText());
		} else {
			Assert.assertEquals(baseMsg + " has incorrect import id", id, //$NON-NLS-1$
					importCtx.StringLiteral().getText());
		}

		// <DecimalLiteral>
		if (importCtx.DecimalLiteral() != null) {
			Assert.assertEquals(baseMsg + " has incorrect version", version, //$NON-NLS-1$
					importCtx.DecimalLiteral().getText());
		} else if (version != null) {
			Assert.assertEquals(baseMsg + " has incorrect version", version, new String()); //$NON-NLS-1$
		}

		// 'as' <Identifier>
		if (importCtx.Identifier() != null) {
			Assert.assertEquals(baseMsg + " has incorrect import as", asIdentifier, importCtx.Identifier().getText()); //$NON-NLS-1$
		} else if (asIdentifier != null) {
			Assert.assertEquals(baseMsg + " has incorrect import as", asIdentifier, new String()); //$NON-NLS-1$
		}
	}

}
