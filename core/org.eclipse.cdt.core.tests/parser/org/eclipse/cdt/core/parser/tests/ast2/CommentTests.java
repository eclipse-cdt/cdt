/*******************************************************************************
 * Copyright (c) 2008, 2009 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 * Emanuel Graf & Guido Zgraggen - initial API and implementation 
 ******************************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2;

import junit.framework.TestSuite;

import org.eclipse.cdt.core.dom.ast.IASTComment;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.internal.core.parser.ParserException;

/**
 * @author Guido Zgraggen
 * 
 */
public class CommentTests extends AST2BaseTest {
	
	public static TestSuite suite() {
		return suite(CommentTests.class);
	}

	public void testCountCommentsInHeaderFile() throws ParserException{
		IASTTranslationUnit tu = parse(getHSource(), ParserLanguage.CPP, false, true);
		IASTComment[] comments = tu.getComments();

		assertEquals(9, comments.length);
	}
	
	public void testCommentsInHeaderFile() throws ParserException{
		IASTTranslationUnit tu = parse(getHSource(), ParserLanguage.CPP, false, true);
		IASTComment[] comments = tu.getComments();

		assertEquals("/* A very cool class\n * isn't it?\n */", new String(comments[0].getComment()));
		assertEquals("//the Hallo", new String(comments[1].getComment()));
		assertEquals("// the 2. Hallo", new String(comments[2].getComment()));
		assertEquals("// comment im h", new String(comments[3].getComment()));
		assertEquals("// comment before ", new String(comments[4].getComment()));
		assertEquals("//Great", new String(comments[5].getComment()));
		assertEquals("//once more", new String(comments[6].getComment()));
		assertEquals("//value field", new String(comments[7].getComment()));
		assertEquals("//Endcomment h", new String(comments[8].getComment()));
	}
	
	public void testCountCommentsInCPPFile() throws ParserException{
		IASTTranslationUnit tu = parse(getCppSource(), ParserLanguage.CPP, false, true);
		IASTComment[] comments = tu.getComments();
		
		assertEquals(10, comments.length);
	}

	public void testCommentsInCPPFile() throws ParserException{
		IASTTranslationUnit tu = parse(getCppSource(), ParserLanguage.CPP, false, true);
		IASTComment[] comments = tu.getComments();
		
		assertEquals("// Comment in cpp", new String(comments[0].getComment()));
		assertEquals("/*The magic 5 */", new String(comments[1].getComment()));
		assertEquals("// Another comment", new String(comments[2].getComment()));
		assertEquals("/* A blockcomment \n* over multiple lines */", new String(comments[3].getComment()));
		assertEquals("//Toplevel comment", new String(comments[4].getComment()));
		assertEquals("//A little bit code", new String(comments[5].getComment()));
		assertEquals("//Trailing comment", new String(comments[6].getComment()));
		assertEquals("//Comment on newline", new String(comments[7].getComment()));
		assertEquals("//Last comment in cpp", new String(comments[8].getComment()));
		assertEquals("//An integer", new String(comments[9].getComment()));
	}
	
	public void testCountCommentsInCFile() throws ParserException{
		IASTTranslationUnit tu = parse(getCSource(), ParserLanguage.C, false, true);
		IASTComment[] comments = tu.getComments();
		
		assertEquals(4, comments.length);
	}
	
	public void testCommentsInCFile() throws ParserException{
		IASTTranslationUnit tu = parse(getCSource(), ParserLanguage.C, false, true);
		IASTComment[] comments = tu.getComments();
		
		assertEquals("//A little input/output programm", new String(comments[0].getComment()));
		assertEquals("//Read the number", new String(comments[1].getComment()));
		assertEquals("/*\n			 * That is the answer ;-)\n			 */", new String(comments[2].getComment()));
		assertEquals("//The end", new String(comments[3].getComment()));
	}

	private String getHSource() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("#ifndef CPPCLASS_H_\n");
		buffer.append("#define CPPCLASS_H_\n");
		buffer.append("/* A very cool class\n");
		buffer.append(" * isn't it?\n");
		buffer.append(" */\n");
		buffer.append("class CppClass\n");
		buffer.append("{\n");
		buffer.append("public:\n");
		buffer.append("	CppClass(int hallo, //the Hallo\n");
		buffer.append("		int hallo2) // the 2. Hallo\n");
		buffer.append("	const;\n");
		buffer.append("	// comment im h\n");
		buffer.append("	virtual ~CppClass();\n");
		buffer.append("	// comment before \n");
		buffer.append("	void doIrgendwas(); //Great\n");
		buffer.append("private:\n");
		buffer.append("	void privateMethode(); //once more\n");
		buffer.append("	//value field\n");
		buffer.append("	int value;\n");
		buffer.append("	//Endcomment h\n");
		buffer.append("};\n");
		buffer.append("#endif\n");
		return buffer.toString();
	}	
	
	private String getCppSource() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("void CppClass()\n");
		buffer.append("{\n");
		buffer.append("   // Comment in cpp\n");
		buffer.append("   int value = 1 + /*The magic 5 */5 * 6;\n");
		buffer.append("   // Another comment\n");
		buffer.append("   value++;\n");
		buffer.append("}\n");
		buffer.append("/* A blockcomment \n");
		buffer.append("* over multiple lines */\n");
		buffer.append("//Toplevel comment\n");
		buffer.append("void doIrgendwas(){\n");
		buffer.append("   //A little bit code\n");
		buffer.append("   int i = 3; //Trailing comment\n");
		buffer.append("		;\n");
		buffer.append("		switch(i){\n");
		buffer.append("			case 1:\n");
		buffer.append("				++i;\n");
		buffer.append("				break;\n");
		buffer.append("			default:\n");
		buffer.append("				++i;\n");
		buffer.append("				break;\n");
		buffer.append("		}\n");
		buffer.append("		do {\n");
		buffer.append("			++i;\n");
		buffer.append("		} while (i < 10);\n");
		buffer.append("		while (i < 20){\n");
		buffer.append("			++i;\n");
		buffer.append("		}\n");
		buffer.append("   //Comment on newline\n");
		buffer.append("   int n = i++ +5;\n");
		buffer.append("  //Last comment in cpp\n");
		buffer.append("}\n");
		buffer.append("int globaleFuntktion(){\n");
		buffer.append("//An integer\n");
		buffer.append("int i;\n");
		buffer.append("}\n");
		buffer.append("enum hue { red, blue, green };\n");
		buffer.append("enum hue col, *cp;\n");
		buffer.append("void f() {\n");
		buffer.append("   col = blue;\n");
		buffer.append("   cp = &col;\n");
		buffer.append("   if( *cp != red )\n");
		buffer.append("      return;\n");
		buffer.append("}\n");
		return buffer.toString();
	}
	
	private String getCSource() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("//A little input/output programm\n");
		buffer.append("int main(void){\n");
		buffer.append("	int number = -1;\n");
		buffer.append("\n");
		buffer.append("	printf(\"Please enter a number: \");\n");
		buffer.append("	scanf(\"%d\", &number); //Read the number\n");
		buffer.append("\n");
		buffer.append("	if(number < 10){\n");
		buffer.append("		printf(\"You aren't a fan of big things? :-)\");\n");
		buffer.append("	}\n");
		buffer.append("	else{\n");
		buffer.append("		if(number == 42){\n");
		buffer.append("			/*\n");
		buffer.append("			 * That is the answer ;-)\n");
		buffer.append("			 */\n");
		buffer.append("			printf(\"Great!!! Thats the answer!!!\");\n");
		buffer.append("		}\n");
		buffer.append("		else{\n");
		buffer.append("			printf(\"You tipped: %d\",  number);\n");
		buffer.append("		}\n");
		buffer.append("	}\n");
		buffer.append("	return 0; //The end\n");
		buffer.append("}\n");
		return buffer.toString();
	}	
	
	// #ifdef xxx
	// // comment1
	// #else 
	// // comment2
	// #endif
	public void testCommentsInInactiveCode_bug183930() throws Exception {
		StringBuffer code= getContents(1)[0];
		IASTTranslationUnit tu = parse(code.toString(), ParserLanguage.CPP, false, true);
		IASTComment[] comments = tu.getComments();
		
		assertEquals(2, comments.length);
		assertEquals("// comment1", new String(comments[0].getComment()));
		assertEquals("// comment2", new String(comments[1].getComment()));
	}
	
	// //comment
	public void testCommentLocation_bug186337() throws Exception{
		StringBuffer code= getContents(1)[0];
		IASTTranslationUnit tu = parse(code.toString(), ParserLanguage.CPP, false, true);
		IASTComment[] comments = tu.getComments();
		
		assertEquals(1, comments.length);
		assertNotNull(comments[0].getFileLocation());
		assertNotNull(comments[0].getNodeLocations());

		tu = parse(code.toString(), ParserLanguage.C, false, true);
		comments = tu.getComments();
		
		assertEquals(1, comments.length);
		assertNotNull(comments[0].getFileLocation());
		assertNotNull(comments[0].getNodeLocations());
	}
	
	// // TODO: shows up in task list 
	// #include "somefile.h"  // TODO: ignored
    //
	// #ifdef WHATEVA // TODO: ignored
	// #endif // TODO: ignored
	// // TODO: shows up in task list

	public void testCommentInDirectives_bug192546() throws Exception {
		StringBuffer code= getContents(1)[0];
		IASTTranslationUnit tu = parse(code.toString(), ParserLanguage.CPP, false, false);
		IASTComment[] comments = tu.getComments();
		
		assertEquals(5, comments.length);
		assertNotNull(comments[0].getFileLocation());
		assertNotNull(comments[0].getNodeLocations());
		for (IASTComment comment : comments) {
			IASTFileLocation loc= comment.getFileLocation();
			int idx= loc.getNodeOffset() + comment.getRawSignature().indexOf("TODO");
			assertEquals("TODO", code.substring(idx, idx+4));			
		}
	}
}
