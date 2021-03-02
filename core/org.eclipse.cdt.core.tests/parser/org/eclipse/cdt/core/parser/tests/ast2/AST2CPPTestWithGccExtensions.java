package org.eclipse.cdt.core.parser.tests.ast2;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.parser.IScannerExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.ISourceCodeParser;
import org.eclipse.cdt.core.dom.parser.cpp.GPPParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.cpp.GPPScannerExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.cpp.ICPPParserExtensionConfiguration;
import org.eclipse.cdt.core.parser.FileContent;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IncludeFileContentProvider;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.internal.core.dom.parser.cpp.GNUCPPSourceParser;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.parser.scanner.CPreprocessor;

public class AST2CPPTestWithGccExtensions extends AST2CPPTestBase {

	protected static final int SIZEOF_EXTENSION = 0x1;
	protected static final int FUNCTION_STYLE_ASM = 0x2;
	protected static final int SLASH_PERCENT_COMMENT = 0x4;

	public AST2CPPTestWithGccExtensions(String name) {
		super(name);
	}

	@Override
	protected IASTTranslationUnit parseAndCheckBindings() throws Exception {
		return parseAndCheckBindings(getAboveComment());
	}

	@Override
	protected IASTTranslationUnit parseAndCheckBindings(String code) throws Exception {
		IASTTranslationUnit tu = parseCPPWithExtension(code, SIZEOF_EXTENSION);
		NameCollector col = new NameCollector();
		tu.accept(col);
		assertNoProblemBindings(col);

		return tu;
	}

	protected IASTTranslationUnit parseAndCheckBindingsHasErrors(String code) throws Exception {
		IASTTranslationUnit tu = parseCPPWithExtension(code, SIZEOF_EXTENSION);
		NameCollector col = new NameCollector();
		tu.accept(col);
		assertProblemBindings(col);

		return tu;
	}

	protected IASTTranslationUnit parse(ISourceCodeParser parser) {
		IASTTranslationUnit tu = parser.parse();
		assertFalse(parser.encounteredError());
		assertEquals(0, tu.getPreprocessorProblemsCount());
		assertEquals(0, CPPVisitor.getProblems(tu).length);
		return tu;
	}

	protected IASTTranslationUnit parse(String code, IScannerExtensionConfiguration sext,
			ICPPParserExtensionConfiguration pext) throws Exception {
		FileContent codeReader = FileContent.create("<test-code>", code.toCharArray());
		IScanner scanner = new CPreprocessor(codeReader, new ScannerInfo(), ParserLanguage.CPP, NULL_LOG, sext,
				IncludeFileContentProvider.getSavedFilesProvider());
		GNUCPPSourceParser parser = new GNUCPPSourceParser(scanner, ParserMode.COMPLETE_PARSE, NULL_LOG, pext);
		return parse(parser);
	}

	protected IASTTranslationUnit parseCPPWithExtension(String code, final int extensions) throws Exception {
		return parse(code, new GPPScannerExtensionConfiguration(version(10, 0)) {
			@Override
			public boolean supportSlashPercentComments() {
				return (extensions & SLASH_PERCENT_COMMENT) != 0;
			}
		}, new GPPParserExtensionConfiguration() {
			@Override
			public boolean supportExtendedSizeofOperator() {
				return (extensions & SIZEOF_EXTENSION) != 0;
			}

			@Override
			public boolean supportFunctionStyleAssembler() {
				return (extensions & FUNCTION_STYLE_ASM) != 0;
			}
		});
	}

	protected static int version(int major, int minor) {
		return (major << 16) + minor;
	}
}
