package org.eclipse.cdt.core.lrparser.tests.cpp;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.lrparser.BaseExtensibleLanguage;
import org.eclipse.cdt.core.dom.lrparser.c99.C99Language;
import org.eclipse.cdt.core.dom.lrparser.cpp.ISOCPPLanguage;
import org.eclipse.cdt.core.lrparser.tests.ParseHelper;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.tests.ast2.AST2CPPSpecTest;
import org.eclipse.cdt.internal.core.parser.ParserException;

@SuppressWarnings("restriction")
public class ISOCPPSpecTest extends AST2CPPSpecTest {
	
	public ISOCPPSpecTest() { } 
	public ISOCPPSpecTest(String name) { super(name); }

	
	@Override
	protected void parseCandCPP( String code, boolean checkBindings, int expectedProblemBindings ) throws ParserException {
		parse(code, ParserLanguage.C,   checkBindings, expectedProblemBindings);
		parse(code, ParserLanguage.CPP, checkBindings, expectedProblemBindings);
	}
		
	@Override
	protected IASTTranslationUnit parse( String code, ParserLanguage lang, boolean checkBindings, int expectedProblemBindings ) throws ParserException {
		ILanguage language = lang.isCPP() ? getCPPLanguage() : getCLanguage();
		return ParseHelper.parse(code, language, true, checkBindings, expectedProblemBindings );
    }
	
	protected BaseExtensibleLanguage getCLanguage() {
		return C99Language.getDefault();
	}
	
	protected BaseExtensibleLanguage getCPPLanguage() {
		return ISOCPPLanguage.getDefault();
	}
	
	
}
