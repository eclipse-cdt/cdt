/*******************************************************************************
 * Copyright (c) 2009,2013 QNX Software Systems
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems (Alena Laskavaia)  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.tests;

import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.cdt.codan.core.CodanRuntime;
import org.eclipse.cdt.codan.core.model.IChecker;
import org.eclipse.cdt.codan.core.model.ICheckerInvocationContext;
import org.eclipse.cdt.codan.core.model.IProblemLocation;
import org.eclipse.cdt.codan.core.model.IProblemReporter;
import org.eclipse.cdt.codan.core.model.IRunnableInEditorChecker;
import org.eclipse.cdt.codan.internal.core.CheckerInvocationContext;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.parser.ISourceCodeParser;
import org.eclipse.cdt.core.dom.parser.c.ANSICParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.c.GCCParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.c.ICParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.cpp.ANSICPPParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.cpp.GPPParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.cpp.ICPPParserExtensionConfiguration;
import org.eclipse.cdt.core.parser.FileContent;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.NullLogService;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.core.parser.tests.ast2.AST2TestBase;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
import org.eclipse.cdt.internal.core.dom.parser.c.CVisitor;
import org.eclipse.cdt.internal.core.dom.parser.c.GNUCSourceParser;
import org.eclipse.cdt.internal.core.dom.parser.cpp.GNUCPPSourceParser;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;

import junit.framework.TestCase;

/**
 * TODO: add description
 */
@SuppressWarnings({ "nls", "restriction" })
public abstract class CodanFastCxxAstTestCase extends TestCase {
	protected IASTTranslationUnit tu;

	protected String getAboveComment() {
		return getContents(1)[0].toString();
	}

	protected StringBuilder[] getContents(int sections) {
		try {
			CodanCoreTestActivator plugin = CodanCoreTestActivator.getDefault();
			return TestSourceReader.getContentsForTest(plugin == null ? null : plugin.getBundle(), "src", getClass(),
					getName(), sections);
		} catch (IOException e) {
			fail(e.getMessage());
			return null;
		}
	}

	public boolean isCpp() {
		return false;
	}

	private static final NullLogService NULL_LOG = new NullLogService();

	/**
	 * @return
	 *
	 */
	public IASTTranslationUnit parse(String code) {
		return parse(code, isCpp() ? ParserLanguage.CPP : ParserLanguage.C, true);
	}

	protected IASTTranslationUnit parse(String code, ParserLanguage lang, boolean gcc) {
		FileContent codeReader = FileContent.create("code.c", code.toCharArray());
		IScannerInfo scannerInfo = new ScannerInfo();
		IScanner scanner = AST2TestBase.createScanner(codeReader, lang, ParserMode.COMPLETE_PARSE, scannerInfo);
		ISourceCodeParser parser2 = null;
		if (lang == ParserLanguage.CPP) {
			ICPPParserExtensionConfiguration config = null;
			if (gcc)
				config = new GPPParserExtensionConfiguration();
			else
				config = new ANSICPPParserExtensionConfiguration();
			parser2 = new GNUCPPSourceParser(scanner, ParserMode.COMPLETE_PARSE, NULL_LOG, config);
		} else {
			ICParserExtensionConfiguration config = null;
			if (gcc)
				config = new GCCParserExtensionConfiguration();
			else
				config = new ANSICParserExtensionConfiguration();
			parser2 = new GNUCSourceParser(scanner, ParserMode.COMPLETE_PARSE, NULL_LOG, config);
		}
		IASTTranslationUnit tu = parser2.parse();
		if (parser2.encounteredError() && !hasCodeErrors())
			fail("PARSE FAILURE");
		if (!hasCodeErrors()) {
			if (lang == ParserLanguage.C) {
				IASTProblem[] problems = CVisitor.getProblems(tu);
				assertEquals(problems.length, 0);
			} else if (lang == ParserLanguage.CPP) {
				IASTProblem[] problems = CPPVisitor.getProblems(tu);
				assertEquals(problems.length, 0);
			}
		}
		this.tu = tu;
		return tu;
	}

	/**
	 * Override if any of code that test tried to parse has errors, otherwise
	 * parse method would assert
	 *
	 * @return
	 */
	protected boolean hasCodeErrors() {
		return false;
	}

	public class ProblemInstance {
		String id;
		IProblemLocation loc;
		Object[] args;

		/**
		 * @param id
		 * @param loc
		 * @param args
		 */
		public ProblemInstance(String id, IProblemLocation loc, Object[] args) {
			this.id = id;
			this.loc = loc;
			this.args = args;
		}
	}

	private ArrayList<ProblemInstance> codanproblems = new ArrayList<>();

	void runCodan(String code) {
		tu = parse(code);
		runCodan(tu);
	}

	void runCodan(IASTTranslationUnit tu) {
		IProblemReporter problemReporter = CodanRuntime.getInstance().getProblemReporter();
		CodanRuntime.getInstance().setProblemReporter(new IProblemReporter() {
			@Override
			public void reportProblem(String problemId, IProblemLocation loc, Object... args) {
				codanproblems.add(new ProblemInstance(problemId, loc, args));
			}
		});
		ICheckerInvocationContext context = new CheckerInvocationContext(null);
		try {
			IChecker checker = getChecker();
			((IRunnableInEditorChecker) checker).processModel(tu, context);
		} finally {
			CodanRuntime.getInstance().setProblemReporter(problemReporter);
			context.dispose();
		}
	}

	/**
	 * @return
	 */
	public abstract IChecker getChecker();

	protected int line2offset(int linePar, String code) throws IOException {
		byte[] bytes = code.getBytes();
		int line = 1;
		for (int j = 0; j < bytes.length; j++) {
			byte c = bytes[j];
			if (line >= linePar)
				return j;
			if (c == '\n')
				line++;
		}
		return 0;
	}
}
