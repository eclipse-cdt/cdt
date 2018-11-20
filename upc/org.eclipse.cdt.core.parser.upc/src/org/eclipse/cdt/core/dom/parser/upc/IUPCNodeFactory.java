/*******************************************************************************
 * Copyright (c) 2009, 2011 IBM Corporation and others.
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
package org.eclipse.cdt.core.dom.parser.upc;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.c.ICNodeFactory;
import org.eclipse.cdt.core.dom.upc.ast.IUPCASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.upc.ast.IUPCASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.upc.ast.IUPCASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.upc.ast.IUPCASTForallStatement;
import org.eclipse.cdt.core.dom.upc.ast.IUPCASTKeywordExpression;
import org.eclipse.cdt.core.dom.upc.ast.IUPCASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.upc.ast.IUPCASTSynchronizationStatement;
import org.eclipse.cdt.core.dom.upc.ast.IUPCASTTypedefNameSpecifier;

public interface IUPCNodeFactory extends ICNodeFactory {

	public void setUseUPCSizeofExpressions(int op);

	public void setUseC99SizeofExpressions();

	public IUPCASTKeywordExpression newKeywordExpression(int keywordKind);

	public IUPCASTSynchronizationStatement newSyncronizationStatment(IASTExpression barrierExpression,
			int statmentKind);

	public IUPCASTForallStatement newForallStatement(IASTStatement init, IASTExpression condition,
			IASTExpression iterationExpression, IASTStatement body, IASTExpression affinity);

	@Override
	public IUPCASTSimpleDeclSpecifier newSimpleDeclSpecifier();

	@Override
	public IUPCASTCompositeTypeSpecifier newCompositeTypeSpecifier(int key, IASTName name);

	@Override
	public IUPCASTElaboratedTypeSpecifier newElaboratedTypeSpecifier(int kind, IASTName name);

	@Override
	public IUPCASTEnumerationSpecifier newEnumerationSpecifier(IASTName name);

	@Override
	public IUPCASTTypedefNameSpecifier newTypedefNameSpecifier(IASTName name);

}