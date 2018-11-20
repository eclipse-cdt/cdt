/*******************************************************************************
 * Copyright (c) 2008, 2014 Institute for Software, HSR Hochschule fuer Technik
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
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.rewrite.changegenerator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

import java.io.IOException;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.model.CoreModelUtil;
import org.eclipse.cdt.core.parser.tests.rewrite.TestHelper;
import org.eclipse.cdt.core.testplugin.CTestPlugin;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
import org.eclipse.cdt.core.tests.BaseTestFramework;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNodeFactory;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModification;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModification.ModificationKind;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModificationStore;
import org.eclipse.cdt.internal.core.dom.rewrite.changegenerator.ChangeGenerator;
import org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.ASTCommenter;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.Document;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.TextFileChange;

public abstract class ChangeGeneratorTest extends BaseTestFramework {
	protected ASTModificationStore modStore;
	protected final ICPPNodeFactory factory = CPPNodeFactory.getDefault();

	public ChangeGeneratorTest() {
		super();
	}

	public ChangeGeneratorTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		modStore = new ASTModificationStore();
		CCorePlugin.getIndexManager().joinIndexer(IIndexManager.FOREVER, new NullProgressMonitor());
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		System.gc();
		fileManager.closeAllFiles();
		super.tearDown();
	}

	protected StringBuilder[] getTestSource(int sections) throws IOException {
		return TestSourceReader.getContentsForTest(CTestPlugin.getDefault().getBundle(), "parser", getClass(),
				getName(), sections);
	}

	protected void compareResult(ASTVisitor visitor) throws Exception {
		compareResult(visitor, false);
	}

	protected void compareResult(ASTVisitor visitor, boolean shouldValidateAST) throws Exception {
		final StringBuilder[] testSources = getTestSource(2);
		compareResult(visitor, testSources[0].toString(), testSources[1].toString(), shouldValidateAST);
	}

	protected void compareCopyResult(ASTVisitor visitor) throws Exception {
		compareCopyResult(visitor, true);
	}

	protected void compareCopyResult(ASTVisitor visitor, boolean shouldValidateAST) throws Exception {
		final StringBuilder[] testSources = getTestSource(1);
		compareResult(visitor, testSources[0].toString(), testSources[0].toString(), shouldValidateAST);
	}

	private void compareResult(ASTVisitor visitor, String source, String expected, boolean shouldValidateAST)
			throws Exception {
		final IFile testFile = importFile("source.h", source); //$NON-NLS-1$

		CCorePlugin.getIndexManager().reindex(cproject);

		ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, new NullProgressMonitor());

		waitForIndexer(cproject);

		final IASTTranslationUnit unit = CoreModelUtil.findTranslationUnit(testFile).getAST();

		if (shouldValidateAST) {
			ProblemNodeChecker validator = new ProblemNodeChecker();
			unit.accept(validator);
			assertFalse("Problem nodes found, AST is invalid.", validator.problemsFound());
		}

		final ChangeGenerator changeGenerator = new ChangeGenerator(modStore, ASTCommenter.getCommentedNodeMap(unit));
		unit.accept(visitor);

		changeGenerator.generateChange(unit);
		final Document doc = new Document(source);
		Change[] changes = ((CompositeChange) changeGenerator.getChange()).getChildren();
		assertThat("No changes found", changes.length, is(greaterThan(0)));
		for (Change change : changes) {
			if (change instanceof TextFileChange) {
				TextFileChange textChange = (TextFileChange) change;
				textChange.getEdit().apply(doc);
			}
		}
		assertEquals(TestHelper.unifyNewLines(expected), TestHelper.unifyNewLines(doc.get()));
	}

	protected ASTModification addModification(ASTModification parentMod, ModificationKind kind, IASTNode targetNode,
			IASTNode newNode) {
		ASTModification mod = new ASTModification(kind, targetNode, newNode, null);
		modStore.storeModification(parentMod, mod);
		return mod;
	}
}
