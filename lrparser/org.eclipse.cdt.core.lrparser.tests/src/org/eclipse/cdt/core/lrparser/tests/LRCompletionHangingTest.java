/*******************************************************************************
 *  Copyright (c) 2010 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.lrparser.tests;





import junit.framework.TestSuite;

public class LRCompletionHangingTest extends AbstractLRHangingTest {

	

	public static TestSuite suite() {
		return new TestSuite(LRCompletionHangingTest.class);
	}
	
	
	
	
	
	//test c
	public void testCompletionDoWhileForC() throws Exception {

		String code = 
			"int main(int argc, char **argv) {" + 
				"do{" +
					CONTENT_ASIST_CURSOR +
		        "} while (i < 3);" +
		    "}";
		
		runTestCase(code, getCLanguage());
		
	}
	
	
	
	//test cpp
	public void testCompletionDoWhileForCPP() throws Exception {

		String code = 
			"int main(int argc, char **argv) {" + 
				"do{" +
		        	CONTENT_ASIST_CURSOR +
		        "} while (i < 3);" +
		    "}";
		runTestCase(code, getCPPLanguage());
	}
	
	public void testCompletionTryCatch() throws Exception {

		String code = 
			"int main(int argc, char **argv) {" + 
				"try {" +
					CONTENT_ASIST_CURSOR + 
				"}" + 
				
				"catch(x){}" + 
			"}";
		runTestCase(code, getCPPLanguage());
		
	}
	
	

	
	public void testCompletionTemplateFunctionForCPP() throws Exception {

		String code = 
			"template " + CONTENT_ASIST_CURSOR +"<class myType>" +
			"myType GetA (myType a) {" +
				"return a;" +
			"}" +
			
			"int main(int argc, char **argv) {" + 
				"int x=0;" +
				"int y = GetA " + CONTENT_ASIST_CURSOR + " <int> (x);" +
			"}";
		runTestCase(code, getCPPLanguage());
	}
	
	
	public void testCompletionIfForCPP() throws Exception {
		String code = 
			"int main(int argc, char **argv) {" + 
				"int x=0;" +
				"if" + CONTENT_ASIST_CURSOR +"(x>0){}" +
			"}";
		runTestCase(code, getCPPLanguage());
		
	}
	
	public void testCompletionTemplateClassForCPP() throws Exception {

		String code = 
		    "template" + CONTENT_ASIST_CURSOR +" <class T> " +
			"class Test {" + 
			    "T val;" +
			    "public:Test(T arg){"+
			      	"val = arg;" +
			    "}" +
			    "~" + CONTENT_ASIST_CURSOR +"Test();"+
			"};" +
			"Test::" + CONTENT_ASIST_CURSOR +"~" + CONTENT_ASIST_CURSOR +"Test(){}" +
			"int main(int argc, char **argv) {" + 
			    CONTENT_ASIST_CURSOR +
				"Test<" + CONTENT_ASIST_CURSOR + "int> t(1);" +
				"Test<" + CONTENT_ASIST_CURSOR +"double>" + CONTENT_ASIST_CURSOR + "* dt = new dt(1.0);" +
				"S* s = dynamic_cast<S*" + CONTENT_ASIST_CURSOR +">(dt);" +   
				"S* s = dynamic_cast" + CONTENT_ASIST_CURSOR +"<S*>(dt);" +   
			"}";
		runTestCase(code, getCPPLanguage());
	}
	
	
	
	
	public void testCompletionSimpleIfForCPP() throws Exception {
		String code = 
			"#" + CONTENT_ASIST_CURSOR + "include " + CONTENT_ASIST_CURSOR + "<iostream>" +
		   "using " + CONTENT_ASIST_CURSOR + "namespace std;" +
		   "if" + CONTENT_ASIST_CURSOR + "(i>0)){}" +
		   "int " + CONTENT_ASIST_CURSOR + "*" + CONTENT_ASIST_CURSOR + " i " + CONTENT_ASIST_CURSOR + "=" + CONTENT_ASIST_CURSOR + " &" + CONTENT_ASIST_CURSOR + "j;" +
		   "*" + CONTENT_ASIST_CURSOR + "j=0;" +
		   "*" + CONTENT_ASIST_CURSOR + "j++;"+
		   "*i" + CONTENT_ASIST_CURSOR + "++ = " + CONTENT_ASIST_CURSOR + "*j++;";
		runTestCase(code, getCPPLanguage());
	
	}
	
	public void testCompletionStructForCPP() throws Exception {
		String code = 
		    "struct " + CONTENT_ASIST_CURSOR + "p " + CONTENT_ASIST_CURSOR + "{" +
		      CONTENT_ASIST_CURSOR+
			  "int w;" +
			  "float p;" +
			"} " + CONTENT_ASIST_CURSOR + "a," + CONTENT_ASIST_CURSOR + "b,c;";
			 
		runTestCase(code, getCPPLanguage());
	
	}
	
	public void testCompletionGnuCPP() throws Exception {
		String code = 
		 "if a>b ? " + CONTENT_ASIST_CURSOR + "g" + CONTENT_ASIST_CURSOR + ":l;" +
		 "case 1..." + CONTENT_ASIST_CURSOR + "3 : ok; ";
		runTestCase(code, getCPPLanguage());
	}
	

	

}
