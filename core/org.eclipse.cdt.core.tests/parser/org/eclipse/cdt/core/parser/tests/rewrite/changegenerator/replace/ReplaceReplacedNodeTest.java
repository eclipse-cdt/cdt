/*******************************************************************************
 * Copyright (c) 2014 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Thomas Corbat (IFS) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.rewrite.changegenerator.replace;

import junit.framework.Test;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.INodeFactory;
import org.eclipse.cdt.core.parser.tests.rewrite.changegenerator.ChangeGeneratorTest;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModification;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModificationStore;

import static org.eclipse.cdt.internal.core.dom.rewrite.ASTModification.ModificationKind.*;

public class ReplaceReplacedNodeTest extends ChangeGeneratorTest {

	public ReplaceReplacedNodeTest() {
		super("ReplaceReplacedNodeTest"); //$NON-NLS-1$
	}

	@Override
	protected void setUp() throws Exception {
		source =
				"void foo() {\n" +
				"}";
		expectedSource = 
				"void bar() {\n" +
				"}";
		super.setUp();
	}

	public static Test suite() {
		return new ReplaceReplacedNodeTest();
	}

	@Override
	protected ASTVisitor createModificator(final ASTModificationStore modStore) {
		return new ASTVisitor() {
			{
				shouldVisitNames = true;
			}

			@Override
			public int visit(IASTName name) {
				INodeFactory factory = name.getTranslationUnit().getASTNodeFactory();

				IASTName intermediateName = factory.newName("intermediate".toCharArray());
				ASTModification replaceMod = new ASTModification(REPLACE, name, intermediateName, null);
				modStore.storeModification(null, replaceMod);

				IASTName finalName = factory.newName("bar".toCharArray());
				ASTModification replaceReplacementMod = new ASTModification(REPLACE, intermediateName, finalName, null);
				modStore.storeModification(replaceMod, replaceReplacementMod);

				return PROCESS_ABORT;
			}
		};
	}
}
