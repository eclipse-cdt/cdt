/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 * Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.rewrite.changegenerator;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.CDOM;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.CPPASTVisitor;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.parser.tests.rewrite.TestHelper;
import org.eclipse.cdt.core.tests.BaseTestFramework;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModificationStore;
import org.eclipse.cdt.internal.core.dom.rewrite.changegenerator.CTextFileChange;
import org.eclipse.cdt.internal.core.dom.rewrite.changegenerator.ChangeGenerator;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.Document;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;

public abstract class ChangeGeneratorTest extends BaseTestFramework {

	protected String source;
	protected String expectedSource;

	public ChangeGeneratorTest() {
		super();
	}

	@Override
	protected void setUp() throws Exception {
		CCorePlugin.getIndexManager().joinIndexer(IIndexManager.FOREVER, new NullProgressMonitor());
		super.setUp();
	}

	@Override
	public void runTest() {
		final ASTModificationStore modStore = new ASTModificationStore();
		final ChangeGenerator changegenartor = new ChangeGenerator(modStore);
		try {
			importFile("source.h", source); //$NON-NLS-1$
	
			IASTTranslationUnit unit = CDOM.getInstance().getTranslationUnit(
					project.getFile(new Path("source.h")), //$NON-NLS-1$
			        CDOM.getInstance().getCodeReaderFactory(
			              CDOM.PARSE_SAVED_RESOURCES),
			        true);
			CPPASTVisitor visitor = createModificator(modStore);
		
			unit.accept(visitor);
			

			//			assertEquals(expectedSource, changegenartor.write(unit));
			changegenartor.generateChange(unit);
			Document doc = new Document(source);
			for(Change curChange : ((CompositeChange)changegenartor.getChange()).getChildren()){
				if (curChange instanceof CTextFileChange) {
					CTextFileChange textChange = (CTextFileChange) curChange;
					textChange.getEdit().apply(doc);
				}
			}
			assertEquals(TestHelper.unifyNewLines(expectedSource), TestHelper.unifyNewLines(doc.get()));
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	protected abstract CPPASTVisitor createModificator(ASTModificationStore modStore);

	public ChangeGeneratorTest(String name) {
		super(name);
	}

	@Override
	protected void tearDown() throws Exception {
		System.gc();
		fileManager.closeAllFiles();
		super.tearDown();
	}

}
