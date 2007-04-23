/*******************************************************************************
 * Copyright (c) 2007 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 * Emanuel Graf - initial API and implementation 
 ******************************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2;

import org.eclipse.cdt.core.dom.ast.IASTPreprocessorElifStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorErrorStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfdefStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfndefStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorPragmaStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.parser.ParserLanguage;

/**
 * @author Emanuel Graf
 *
 */
public class DOMPreprocessorInformationTest extends AST2BaseTest {
	
	public void testPragma() throws Exception {
		String msg = "GCC poison printf sprintf fprintf";
		StringBuffer buffer = new StringBuffer( "#pragma " + msg + "\n" );  //$NON-NLS-1$
		IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP ); 
		IASTPreprocessorStatement[] st = tu.getAllPreprocessorStatements();
		assertEquals(1, st.length);
		assertTrue(st[0] instanceof IASTPreprocessorPragmaStatement);
		IASTPreprocessorPragmaStatement pragma = (IASTPreprocessorPragmaStatement) st[0];
		assertEquals(msg, new String(pragma.getMessage()));
	}
	
	public void testElIf() throws Exception {
		String cond = "2 == 2";
		StringBuffer buffer = new StringBuffer( "#if 1 == 2\n#elif " + cond + "\n#else\n#endif\n" );  //$NON-NLS-1$
		IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP ); 
		IASTPreprocessorStatement[] st = tu.getAllPreprocessorStatements();
		assertEquals(4, st.length);
		assertTrue(st[1] instanceof IASTPreprocessorElifStatement);
		IASTPreprocessorElifStatement pragma = (IASTPreprocessorElifStatement) st[1];
		assertEquals(cond, new String(pragma.getCondition()));
	}
	
	public void testIf() throws Exception {
		String cond = "2 == 2";
		StringBuffer buffer = new StringBuffer( "#if " + cond + "\n#endif\n" );  //$NON-NLS-1$
		IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP ); 
		IASTPreprocessorStatement[] st = tu.getAllPreprocessorStatements();
		assertEquals(2, st.length);
		assertTrue(st[0] instanceof IASTPreprocessorIfStatement);
		IASTPreprocessorIfStatement pragma = (IASTPreprocessorIfStatement) st[0];
		assertEquals(cond, new String(pragma.getCondition()));
	}
	
	public void testIfDef() throws Exception{
		String cond = "SYMBOL";
		StringBuffer buffer = new StringBuffer( "#ifdef " + cond + "\n#endif\n" );  //$NON-NLS-1$
		IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP ); 
		IASTPreprocessorStatement[] st = tu.getAllPreprocessorStatements();
		assertEquals(2, st.length);
		assertTrue(st[0] instanceof IASTPreprocessorIfdefStatement);
		IASTPreprocessorIfdefStatement pragma = (IASTPreprocessorIfdefStatement) st[0];
		assertEquals(cond, new String(pragma.getCondition()));
	}
	
	public void testIfnDef() throws Exception{
		String cond = "SYMBOL";
		StringBuffer buffer = new StringBuffer( "#ifndef " + cond + "\n#endif\n" );  //$NON-NLS-1$
		IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP ); 
		IASTPreprocessorStatement[] st = tu.getAllPreprocessorStatements();
		assertEquals(2, st.length);
		assertTrue(st[0] instanceof IASTPreprocessorIfndefStatement);
		IASTPreprocessorIfndefStatement pragma = (IASTPreprocessorIfndefStatement) st[0];
		assertEquals(cond, new String(pragma.getCondition()));
	}
	
	public void testError() throws Exception{
		String msg = "Message";
		StringBuffer buffer = new StringBuffer( "#error " + msg + "\n" );  //$NON-NLS-1$
		IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP, false, false ); 
		IASTPreprocessorStatement[] st = tu.getAllPreprocessorStatements();
		assertEquals(1, st.length);
		assertTrue(st[0] instanceof IASTPreprocessorErrorStatement);
		IASTPreprocessorErrorStatement pragma = (IASTPreprocessorErrorStatement) st[0];
		assertEquals(msg, new String(pragma.getMessage()));
	}
	
	public void testPragmaWithSpaces() throws Exception {
		String msg = "GCC poison printf sprintf fprintf";
		StringBuffer buffer = new StringBuffer( "#  pragma " + msg + "\n" );  //$NON-NLS-1$
		IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP ); 
		IASTPreprocessorStatement[] st = tu.getAllPreprocessorStatements();
		assertEquals(1, st.length);
		assertTrue(st[0] instanceof IASTPreprocessorPragmaStatement);
		IASTPreprocessorPragmaStatement pragma = (IASTPreprocessorPragmaStatement) st[0];
		assertEquals(msg, new String(pragma.getMessage()));
	}
	
	public void testElIfWithSpaces() throws Exception {
		String cond = "2 == 2";
		StringBuffer buffer = new StringBuffer( "#if 1 == 2\n#  elif " + cond + "\n#else\n#endif\n" );  //$NON-NLS-1$
		IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP ); 
		IASTPreprocessorStatement[] st = tu.getAllPreprocessorStatements();
		assertEquals(4, st.length);
		assertTrue(st[1] instanceof IASTPreprocessorElifStatement);
		IASTPreprocessorElifStatement pragma = (IASTPreprocessorElifStatement) st[1];
		assertEquals(cond, new String(pragma.getCondition()));
	}
	
	public void testIfWithSpaces() throws Exception {
		String cond = "2 == 2";
		StringBuffer buffer = new StringBuffer( "#  if " + cond + "\n#endif\n" );  //$NON-NLS-1$
		IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP ); 
		IASTPreprocessorStatement[] st = tu.getAllPreprocessorStatements();
		assertEquals(2, st.length);
		assertTrue(st[0] instanceof IASTPreprocessorIfStatement);
		IASTPreprocessorIfStatement pragma = (IASTPreprocessorIfStatement) st[0];
		assertEquals(cond, new String(pragma.getCondition()));
	}
	
	public void testIfDefWithSpaces() throws Exception{
		String cond = "SYMBOL";
		StringBuffer buffer = new StringBuffer( "#  ifdef " + cond + "\n#endif\n" );  //$NON-NLS-1$
		IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP ); 
		IASTPreprocessorStatement[] st = tu.getAllPreprocessorStatements();
		assertEquals(2, st.length);
		assertTrue(st[0] instanceof IASTPreprocessorIfdefStatement);
		IASTPreprocessorIfdefStatement pragma = (IASTPreprocessorIfdefStatement) st[0];
		assertEquals(cond, new String(pragma.getCondition()));
	}
	
	public void testIfnDefWithSpaces() throws Exception{
		String cond = "SYMBOL";
		StringBuffer buffer = new StringBuffer( "#  ifndef " + cond + "\n#endif\n" );  //$NON-NLS-1$
		IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP ); 
		IASTPreprocessorStatement[] st = tu.getAllPreprocessorStatements();
		assertEquals(2, st.length);
		assertTrue(st[0] instanceof IASTPreprocessorIfndefStatement);
		IASTPreprocessorIfndefStatement pragma = (IASTPreprocessorIfndefStatement) st[0];
		assertEquals(cond, new String(pragma.getCondition()));
	}
	
	public void testErrorWithSpaces() throws Exception{
		String msg = "Message";
		StringBuffer buffer = new StringBuffer( "#  error " + msg + "\n" );  //$NON-NLS-1$
		IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP, false, false ); 
		IASTPreprocessorStatement[] st = tu.getAllPreprocessorStatements();
		assertEquals(1, st.length);
		assertTrue(st[0] instanceof IASTPreprocessorErrorStatement);
		IASTPreprocessorErrorStatement pragma = (IASTPreprocessorErrorStatement) st[0];
		assertEquals(msg, new String(pragma.getMessage()));
	}
	
}
