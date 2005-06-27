/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.model.tests;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IEnumeration;
import org.eclipse.cdt.core.model.IEnumerator;
import org.eclipse.cdt.core.model.IField;
import org.eclipse.cdt.core.model.IFunction;
import org.eclipse.cdt.core.model.IFunctionDeclaration;
import org.eclipse.cdt.core.model.IFunctionTemplateDeclaration;
import org.eclipse.cdt.core.model.IInclude;
import org.eclipse.cdt.core.model.IMacro;
import org.eclipse.cdt.core.model.IMethod;
import org.eclipse.cdt.core.model.IMethodDeclaration;
import org.eclipse.cdt.core.model.IMethodTemplateDeclaration;
import org.eclipse.cdt.core.model.INamespace;
import org.eclipse.cdt.core.model.IParent;
import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.IStructure;
import org.eclipse.cdt.core.model.IStructureTemplate;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.ITypeDef;
import org.eclipse.cdt.core.model.IVariable;
import org.eclipse.cdt.core.model.IVariableDeclaration;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.CTestPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

public class StructuralCModelElementsTests extends TestCase {	
	private ICProject fCProject;
	private IFile headerFile;
	private IFile includedFile;
	private NullProgressMonitor monitor;
		
	public static Test suite() {
		TestSuite suite= new TestSuite(StructuralCModelElementsTests.class.getName());
		suite.addTest(new StructuralCModelElementsTests("testCModelElements")); //$NON-NLS-1$
		return suite;
	}		
		
	public StructuralCModelElementsTests(String name) {
		super(name);
	}
		
	protected void setUp() throws Exception {
		monitor = new NullProgressMonitor();
		fCProject= CProjectHelper.createCCProject("TestProject1", "bin"); //$NON-NLS-1$ //$NON-NLS-2$
		headerFile = fCProject.getProject().getFile("CModelElementsTest.h"); //$NON-NLS-1$
		includedFile = fCProject.getProject().getFile("included.h"); //$NON-NLS-1$
		if (!headerFile.exists()) {
			try{
				FileInputStream fileIn = new FileInputStream(
						CTestPlugin.getDefault().getFileInPlugin(new Path("resources/cfiles/CModelElementsTestStart.h")));  //$NON-NLS-1$
				headerFile.create(fileIn,false, monitor);        
				FileInputStream includedFileIn = new FileInputStream(
						CTestPlugin.getDefault().getFileInPlugin(new Path("resources/cfiles/included.h")));  //$NON-NLS-1$
				includedFile.create(includedFileIn,false, monitor);        
			} catch (CoreException e) {
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	
	protected void tearDown() {
		  CProjectHelper.delete(fCProject);
	}	
			
	public void testCModelElements() throws CModelException{
		ITranslationUnit tu = (ITranslationUnit)CoreModel.getDefault().create(headerFile);
		//ITranslationUnit included = (ITranslationUnit)CoreModel.getDefault().create(includedFile);
		// turn on the structural parse mode
		CCorePlugin.getDefault().setStructuralParseMode(true);
		
		// parse the translation unit to get the elements tree		
		tu.parse(); 
		
		// tu ---> include
		checkInclude(tu);
		
		// tu ---> macro
		checkMacro(tu);
		
		// tu ---> namespace: MyPackage
		List tuPackages = tu.getChildrenOfType(ICElement.C_NAMESPACE);
		INamespace namespace = (INamespace) tuPackages.get(0);
		assertEquals(namespace.getElementName(), new String("MyPackage")); //$NON-NLS-1$
		checkElementOffset(namespace);
		checkLineNumbers(namespace, 8, 130);
		checkClass(namespace);
				
		checkEnums(namespace);	
		
		checkVariables(namespace);		

		checkVariableDeclarations(namespace);		
		
		checkFunctions(namespace);
		
		checkStructs(namespace);
		
		checkTemplates(namespace);
		
		checkArrays(tu);

		// turn off the structural parse mode
		CCorePlugin.getDefault().setStructuralParseMode(false);

	}

	private void checkInclude(IParent tu) throws CModelException{
		List tuIncludes = tu.getChildrenOfType(ICElement.C_INCLUDE);
		IInclude inc1 = (IInclude) tuIncludes.get(0);
		assertEquals(inc1.getElementName(), new String("included.h")); //$NON-NLS-1$
		checkElementOffset(inc1);
		checkLineNumbers(inc1, 2, 2);
	}
	
	private void checkMacro(IParent tu) throws CModelException{
		List tuMacros = tu.getChildrenOfType(ICElement.C_MACRO);
		IMacro mac1 = (IMacro) tuMacros.get(0);
		assertEquals(mac1.getElementName(), new String("PRINT")); //$NON-NLS-1$
		checkElementOffset(mac1);
		checkLineNumbers(mac1, 5, 5);
	}
	
	private void checkClass(IParent namespace) throws CModelException{
		// MyPackage ---> class: Hello
		List nsClasses = namespace.getChildrenOfType(ICElement.C_CLASS);		
		IStructure classHello = (IStructure) nsClasses.get(0);
		assertEquals(classHello.getElementName(), new String("Hello")); //$NON-NLS-1$
		checkElementOffset(classHello);
		checkLineNumbers(classHello, 12, 53);
		
		// Hello --> field: int x
		List helloFields = classHello.getChildrenOfType(ICElement.C_FIELD);
		IField intX = (IField) helloFields.get(0);
		assertEquals(intX.getElementName(), new String("x")); //$NON-NLS-1$
		checkElementOffset(intX);
		assertEquals(intX.getTypeName(), new String("int")); //$NON-NLS-1$
		checkLineNumbers(intX, 17, 17);
		
		ASTAccessVisibility xVisibility = intX.getVisibility(); 
		if (xVisibility != ASTAccessVisibility.PROTECTED)
			fail("visibility should be protected!"); //$NON-NLS-1$
		
		// Hello ---> method: void setX(int X)
		List helloMethods = classHello.getChildrenOfType(ICElement.C_METHOD);
		IMethod setX = (IMethod) helloMethods.get(0);
		assertEquals(setX.getElementName(), new String("setX")); //$NON-NLS-1$
		checkElementOffset(setX);
		assertEquals(setX.getReturnType(), new String("void")); //$NON-NLS-1$
		checkLineNumbers(setX, 19, 22);
		int setXNumOfParam = setX.getNumberOfParameters();
		if(setXNumOfParam != 1)
			fail("setX should have one parameter!"); //$NON-NLS-1$
		String[] setXParamTypes = setX.getParameterTypes();
		String firstParamType = setXParamTypes[0];
		assertEquals(firstParamType, new String("int")); //$NON-NLS-1$
		// TODO : check for the inline here
		
		checkNestedNamespace(classHello);
	}
	private void checkNestedNamespace(IParent classHello) throws CModelException{
		// Hello ---> namespace: MyNestedPackage 
		List helloNamespaces = classHello.getChildrenOfType(ICElement.C_NAMESPACE);
		INamespace myNestedPackage = (INamespace) helloNamespaces.get(0);
		assertEquals(myNestedPackage.getElementName(), new String("MyNestedPackage")); //$NON-NLS-1$
		checkElementOffset(myNestedPackage);
		checkLineNumbers(myNestedPackage, 25, 52);

		checkParentNestedClass(myNestedPackage);	
		checkDerivedNestedClass(myNestedPackage);
	}
	private void checkParentNestedClass(IParent myNestedPackage) throws CModelException{
		// MyNestedPackage ---> class: Y  
		List nestedClasses = myNestedPackage.getChildrenOfType(ICElement.C_CLASS);		
		IStructure classY = (IStructure) nestedClasses.get(0);
		assertEquals(classY.getElementName(), new String("Y")); //$NON-NLS-1$
		checkElementOffset(classY);
		checkLineNumbers(classY, 28, 35);
		
		// Y ---> constructor: Y
		List yMethods = classY.getChildrenOfType(ICElement.C_METHOD_DECLARATION);
		IMethodDeclaration constructor  = (IMethodDeclaration) yMethods.get(0);
		assertEquals(constructor.getElementName(), new String("Y")); //$NON-NLS-1$
		checkElementOffset(constructor);
		assertTrue (constructor.isConstructor());
		checkLineNumbers(constructor, 32, 32);
		
		// Y ---> destructor: ~Y
		IMethodDeclaration destructor  = (IMethodDeclaration) yMethods.get(1);
		assertEquals(destructor.getElementName(), new String("~Y")); //$NON-NLS-1$
		checkElementOffset(destructor);
		assertTrue (destructor.isDestructor());
		checkLineNumbers(destructor, 34, 34);
		// TODO: check for virtual on destructors
		
	}
	
	private void checkDerivedNestedClass(IParent myNestedPackage) throws CModelException{
		// MyNestedPackage ---> class: X public Y 
		List nestedClasses = myNestedPackage.getChildrenOfType(ICElement.C_CLASS);		
		IStructure classX = (IStructure) nestedClasses.get(1);
		assertEquals(classX.getElementName(), new String("X")); //$NON-NLS-1$
		checkElementOffset(classX);
		checkLineNumbers(classX, 38, 51);
		// TODO : Check for base classes here
		
		// X --> field: B b
		List xFieldChildren = classX.getChildrenOfType(ICElement.C_FIELD);
		IField bB = (IField) xFieldChildren.get(0);
		assertEquals(bB.getElementName(), new String("b")); //$NON-NLS-1$
		checkElementOffset(bB);
		assertEquals(bB.getTypeName(), new String("B")); //$NON-NLS-1$
		checkLineNumbers(bB, 42, 42);
		ASTAccessVisibility bVisibility = bB.getVisibility(); 
		if (bVisibility != ASTAccessVisibility.PRIVATE)
			fail("visibility should be private!"); //$NON-NLS-1$
		
		// X ---> constructor chain: X 
		List xMethodChildren = classX.getChildrenOfType(ICElement.C_METHOD);
		IMethod xconstructor  = (IMethod) xMethodChildren.get(0);
		assertEquals(xconstructor.getElementName(), new String("X")); //$NON-NLS-1$
		checkElementOffset(xconstructor);
		assertTrue (xconstructor.isConstructor());
		checkLineNumbers(xconstructor, 46, 48);

		// X ---> method declaration: doNothing
		List xMethodDeclarations = classX.getChildrenOfType(ICElement.C_METHOD_DECLARATION);
		IMethodDeclaration xDoNothing = (IMethodDeclaration) xMethodDeclarations.get(0);
		assertEquals(xDoNothing.getElementName(), new String("doNothing")); //$NON-NLS-1$
		checkElementOffset(xDoNothing);
		assertEquals(xDoNothing.getReturnType(), new String("int"));						 //$NON-NLS-1$
		checkLineNumbers(xDoNothing, 50, 50);
	}
	
	private void checkEnums(IParent namespace) throws CModelException{
		// MyPackage ---> enum: Noname
		List nsEnums = namespace.getChildrenOfType(ICElement.C_ENUMERATION);		
		IEnumeration enumarate = (IEnumeration) nsEnums.get(0);
		assertEquals(enumarate.getElementName(), new String("")); //$NON-NLS-1$
		checkElementOffset(enumarate);
		checkLineNumbers(enumarate, 57, 61);
	
		// 	enum ---> enumerator: first = 1
		List enumEnumerators = enumarate.getChildrenOfType(ICElement.C_ENUMERATOR);
		IEnumerator first = (IEnumerator) enumEnumerators.get(0);
		assertEquals(first.getElementName(), new String("first")); //$NON-NLS-1$
		assertEquals("1", first.getConstantExpression()); //$NON-NLS-1$
		checkElementOffset(first);
		// 	enum ---> enumerator: second
		IEnumerator second = (IEnumerator) enumEnumerators.get(1);
		assertEquals(second.getElementName(), new String("second")); //$NON-NLS-1$
		checkElementOffset(second);
		// 	enum ---> enumerator: third
		IEnumerator third = (IEnumerator) enumEnumerators.get(2);
		checkElementOffset(third);
		assertEquals(third.getElementName(), new String("third"));		 //$NON-NLS-1$
		checkElementOffset(third);
		
		// MyPackage ---> enum: MyEnum
		IEnumeration myEnum = (IEnumeration) nsEnums.get(1);
		assertEquals(myEnum.getElementName(), new String("MyEnum")); //$NON-NLS-1$
		checkElementOffset(myEnum);
		checkLineNumbers(myEnum, 64, 67);
	
		// 	enum ---> enumerator: first
		List myEnumEnumerators = myEnum.getChildrenOfType(ICElement.C_ENUMERATOR);
		IEnumerator f = (IEnumerator) myEnumEnumerators.get(0);
		assertEquals(f.getElementName(), new String("f")); //$NON-NLS-1$
		checkElementOffset(f);
		// 	enum ---> enumerator: second
		IEnumerator s = (IEnumerator) myEnumEnumerators.get(1);
		assertEquals(s.getElementName(), new String("s")); //$NON-NLS-1$
		checkElementOffset(s);
		// 	enum ---> enumerator: third
		IEnumerator t = (IEnumerator) myEnumEnumerators.get(2);
		assertEquals(t.getElementName(), new String("t")); //$NON-NLS-1$
		checkElementOffset(t);
	}

	private void checkVariables(IParent namespace) throws CModelException{
		// MyPackage ---> int v
		List nsVars = namespace.getChildrenOfType(ICElement.C_VARIABLE);
		IVariable var1 = (IVariable) nsVars.get(0);
		assertEquals(var1.getElementName(), new String("v")); //$NON-NLS-1$
		checkElementOffset(var1);
		assertEquals(var1.getTypeName(), new String("int")); //$NON-NLS-1$
		checkLineNumbers(var1, 71, 71);
		
		// MyPackage ---> unsigned long vuLong
		IVariable var2 = (IVariable) nsVars.get(1);
		assertEquals(var2.getElementName(), new String("vuLong")); //$NON-NLS-1$
		checkElementOffset(var2);
		assertEquals(var2.getTypeName(), new String("unsigned long")); //$NON-NLS-1$
		checkLineNumbers(var2, 73, 73);

		// MyPackage ---> unsigned short vuShort
		IVariable var3 = (IVariable) nsVars.get(2);
		assertEquals(var3.getElementName(), new String("vuShort")); //$NON-NLS-1$
		checkElementOffset(var3);
		assertEquals(var3.getTypeName(), new String("unsigned short")); //$NON-NLS-1$
		checkLineNumbers(var3, 75, 75);
		
		// MyPackage ---> function pointer: orig_malloc_hook
		IVariable vDecl2 = (IVariable) nsVars.get(3);
		assertEquals(vDecl2.getElementName(), new String("orig_malloc_hook")); //$NON-NLS-1$
		checkElementOffset(vDecl2);
		assertEquals(vDecl2.getTypeName(), new String ("void*(*)(const char*, int, int)")); //$NON-NLS-1$
		checkLineNumbers(vDecl2, 81, 81);
		
	}

	private void checkVariableDeclarations(IParent namespace) throws CModelException{
		// MyPackage ---> extern int evar
		List nsVarDecls = namespace.getChildrenOfType(ICElement.C_VARIABLE_DECLARATION);
		IVariableDeclaration vDecl1 = (IVariableDeclaration) nsVarDecls.get(0);
		assertEquals(vDecl1.getElementName(), new String("evar")); //$NON-NLS-1$
		checkElementOffset(vDecl1);
		assertEquals(vDecl1.getTypeName(), new String("int")); //$NON-NLS-1$
		checkLineNumbers(vDecl1, 79, 79);
	}
	
	private void checkFunctions(IParent namespace) throws CModelException{
		List nsFunctionDeclarations = namespace.getChildrenOfType(ICElement.C_FUNCTION_DECLARATION);

		//	MyPackage ---> function: void foo()
		IFunctionDeclaration f1 = (IFunctionDeclaration) nsFunctionDeclarations.get(0);
		assertEquals(f1.getElementName(), new String("foo")); //$NON-NLS-1$
		checkElementOffset(f1);
		assertEquals(f1.getReturnType(), new String("void")); //$NON-NLS-1$
		checkLineNumbers(f1, 85, 85);
		
		//	MyPackage ---> function: char* foo(int&, char**)
		IFunctionDeclaration f2 = (IFunctionDeclaration) nsFunctionDeclarations.get(1);
		assertEquals(f2.getElementName(), new String("foo")); //$NON-NLS-1$
		checkElementOffset(f2);
		assertEquals(f2.getReturnType(), new String("char*")); //$NON-NLS-1$
		checkLineNumbers(f2, 87, 88);
		int fooNumOfParam = f2.getNumberOfParameters();
		if(fooNumOfParam != 2)
			fail("foo should have two parameter!"); //$NON-NLS-1$
		String[] paramTypes = f2.getParameterTypes();
		assertEquals(paramTypes[0], new String("int&")); //$NON-NLS-1$
		assertEquals(paramTypes[1], new String("char**")); //$NON-NLS-1$
	
		//	MyPackage ---> function: void boo() {}		
		List nsFunctions = namespace.getChildrenOfType(ICElement.C_FUNCTION);
		IFunction f3 = (IFunction) nsFunctions.get(0);		
		assertEquals(f3.getElementName(), new String("boo")); //$NON-NLS-1$
		checkElementOffset(f3);
		assertEquals(f3.getReturnType(), new String("void")); //$NON-NLS-1$
		checkLineNumbers(f3, 90, 92);
	}

	private void checkStructs(IParent namespace) throws CModelException{
		// struct with name
		List nsStructs = namespace.getChildrenOfType(ICElement.C_STRUCT);
		IStructure struct1 = (IStructure) nsStructs.get(0);
		assertEquals(struct1.getElementName(), new String ("MyStruct")); //$NON-NLS-1$
		checkElementOffset(struct1);
		checkLineNumbers(struct1, 95, 97);
		List struct1Fields = struct1.getChildrenOfType(ICElement.C_FIELD);
		IField field1 = (IField) struct1Fields.get(0);
		assertEquals(field1.getElementName(), new String("sint")); //$NON-NLS-1$
		checkElementOffset(field1);
		assertEquals(field1.getTypeName(), new String("int")); //$NON-NLS-1$
		checkLineNumbers(field1, 96, 96);
		
		if(field1.getVisibility() != ASTAccessVisibility.PUBLIC)
			fail("field visibility should be public!"); //$NON-NLS-1$
		
		// struct no name
		IStructure struct2 = (IStructure) nsStructs.get(1);
		assertEquals(struct2.getElementName(), new String ("")); //$NON-NLS-1$
		checkElementOffset(struct2);
		checkLineNumbers(struct2, 101, 103);
		List struct2Fields = struct2.getChildrenOfType(ICElement.C_FIELD);
		IField field2 = (IField) struct2Fields.get(0);
		assertEquals(field2.getElementName(), new String("ss")); //$NON-NLS-1$
		checkElementOffset(field2);
		assertEquals(field2.getTypeName(), new String("int")); //$NON-NLS-1$
		checkLineNumbers(field2, 102, 102);
		if(field2.getVisibility() != ASTAccessVisibility.PUBLIC)
			fail("field visibility should be public!"); //$NON-NLS-1$
		
		// typedefs
		List nsTypeDefs = namespace.getChildrenOfType(ICElement.C_TYPEDEF);
		ITypeDef td1 = (ITypeDef) nsTypeDefs.get(0);
		assertEquals(td1.getElementName(), new String ("myStruct")); //$NON-NLS-1$
		checkElementOffset(td1);
		assertEquals(td1.getTypeName(), new String ("struct MyStruct")); //$NON-NLS-1$
		checkLineNumbers(td1, 99, 99);
		ITypeDef td2 = (ITypeDef) nsTypeDefs.get(1);
		assertEquals(td2.getElementName(), new String ("myTypedef")); //$NON-NLS-1$
		checkElementOffset(td2);
		assertEquals(td2.getTypeName(), new String ("")); //$NON-NLS-1$
		checkLineNumbers(td2, 101, 103);

		// union
		List nsUnions = namespace.getChildrenOfType(ICElement.C_UNION);
		IStructure u0 = (IStructure) nsUnions.get(0);
		assertEquals(u0.getElementName(), new String("U"));		 //$NON-NLS-1$
		checkElementOffset(u0);
		checkLineNumbers(u0, 105, 107);
		List u0Fields = u0.getChildrenOfType(ICElement.C_FIELD);
		IField field3 = (IField) u0Fields.get(0);
		assertEquals(field3.getElementName(), new String("U1")); //$NON-NLS-1$
		checkElementOffset(field3);
		assertEquals(field3.getTypeName(), new String("int")); //$NON-NLS-1$
		checkLineNumbers(field3, 106, 106);
		if(field3.getVisibility() != ASTAccessVisibility.PUBLIC)
			fail("field visibility should be public!"); //$NON-NLS-1$
	}

	private void checkTemplates(IParent namespace) throws CModelException{
		// template function
		List functionTemplates = namespace.getChildrenOfType(ICElement.C_TEMPLATE_FUNCTION_DECLARATION);
		IFunctionTemplateDeclaration ft = (IFunctionTemplateDeclaration)functionTemplates.get(0);
		assertEquals(ft.getElementName(), new String("aTemplatedFunction")); //$NON-NLS-1$
		checkElementOffset(ft);
		ft.getTemplateSignature();
		assertEquals(ft.getTemplateSignature(), new String("aTemplatedFunction<A, B>(B) : A")); //$NON-NLS-1$
		checkLineNumbers(ft, 112, 113);
		
		// template method
		List nsClasses = namespace.getChildrenOfType(ICElement.C_CLASS);
		IStructure enclosingClass = (IStructure) nsClasses.get(1);
		checkLineNumbers(enclosingClass, 115, 120);
		List methodTemplates = enclosingClass.getChildrenOfType(ICElement.C_TEMPLATE_METHOD_DECLARATION);
		IMethodTemplateDeclaration mt = (IMethodTemplateDeclaration)methodTemplates.get(0);
		assertEquals(mt.getElementName(), new String("aTemplatedMethod")); //$NON-NLS-1$
		checkElementOffset(mt);
		assertEquals(mt.getTemplateSignature(), new String("aTemplatedMethod<A, B>(B) : A")); //$NON-NLS-1$
		checkLineNumbers(mt, 118, 119 );
		assertEquals(mt.getVisibility(), ASTAccessVisibility.PUBLIC);
		
		// template class
		List classTemplates = namespace.getChildrenOfType(ICElement.C_TEMPLATE_CLASS);
		IStructureTemplate ct = (IStructureTemplate)classTemplates.get(0);
		assertEquals(ct.getElementName(), new String("myarray")); //$NON-NLS-1$
		checkElementOffset(ct);
		assertEquals(ct.getTemplateSignature(), new String("myarray<T, Tibor>")); //$NON-NLS-1$
		checkLineNumbers(ct, 122, 123);

		// template struct
		List structTemplates = namespace.getChildrenOfType(ICElement.C_TEMPLATE_STRUCT);
		IStructureTemplate st = (IStructureTemplate)structTemplates.get(0);
		assertEquals(st.getElementName(), new String("mystruct")); //$NON-NLS-1$
		checkElementOffset(st);
		assertEquals(st.getTemplateSignature(), new String("mystruct<T, Tibor>")); //$NON-NLS-1$
		checkLineNumbers(st, 125, 126);

		// moved to failed tests
		// also commented in the source file
		// template variable
//		ArrayList variableTemplates = namespace.getChildrenOfType(ICElement.C_TEMPLATE_VARIABLE);
//		VariableTemplate vt = (VariableTemplate)variableTemplates.get(0);
//		assertEquals(vt.getElementName(), new String("default_alloc_template<__threads,__inst>::_S_start_free"));
//		checkElementOffset((CElement)mac1);
//		assertEquals(vt.getTemplateSignature(), new String("default_alloc_template<__threads,__inst>::_S_start_free<bool, int> : char*"));
//		checkLineNumbers((CElement)vt, 128, 129);
	}
	
	private void checkArrays(IParent tu) throws CModelException{
		// array variable
		List variables = tu.getChildrenOfType(ICElement.C_VARIABLE);
		IVariable arrayVar = (IVariable) variables.get(0);
		assertEquals(arrayVar.getElementName(), new String("myArray")); //$NON-NLS-1$
		checkElementOffset(arrayVar);
		assertEquals(arrayVar.getTypeName(), new String("int[][]")); //$NON-NLS-1$
		checkLineNumbers(arrayVar, 133, 133);
		
		// array parameter in function main
		List functions = tu.getChildrenOfType(ICElement.C_FUNCTION);
		IFunction mainFunction  = (IFunction) functions.get(0);
		assertEquals(mainFunction.getElementName(), new String("main")); //$NON-NLS-1$
		checkElementOffset(mainFunction);
		assertEquals(mainFunction.getReturnType(), new String("int")); //$NON-NLS-1$
		checkLineNumbers(mainFunction, 134, 136);
		int NumOfParam = mainFunction.getNumberOfParameters();
		if(NumOfParam != 2)
			fail("main should have two parameter!"); //$NON-NLS-1$
		String[] paramTypes = mainFunction.getParameterTypes();
		assertEquals(paramTypes[0], new String("int")); //$NON-NLS-1$
		assertEquals(paramTypes[1], new String("char*[]")); //$NON-NLS-1$
		
	}
	private void checkLineNumbers(ICElement element, int startLine, int endLine) throws CModelException {
		ISourceRange range = ((ISourceReference)element).getSourceRange();
		assertEquals(startLine, range.getStartLine());
		assertEquals(endLine, range.getEndLine());		 		
	}
	
	private void checkElementOffset(ICElement element) throws CModelException{
		String name = element.getElementName();
		ISourceRange range = ((ISourceReference)element).getSourceRange();
		if(name.length() > 0 ){
			assertTrue (range.getStartPos() <= range.getIdStartPos());
			assertEquals (range.getIdLength(), name.length());
		} else {
			assertEquals (range.getStartPos(), range.getIdStartPos());
			if (element instanceof ITypeDef)
				assertEquals (range.getIdLength(), ((ITypeDef)element).getTypeName().length());
			else if(element instanceof IEnumeration)
				assertEquals (range.getIdLength(), ((IEnumeration)element).getTypeName().length());
			else if(element instanceof IStructure)
				assertEquals (range.getIdLength(), ((IStructure)element).getTypeName().length());
		}
	}
}
