package org.eclipse.cdt.core.model.tests;

/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * Rational Software - Initial API and implementation
***********************************************************************/
import java.io.FileInputStream;
import java.util.ArrayList;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IEnumeration;
import org.eclipse.cdt.core.model.IEnumerator;
import org.eclipse.cdt.core.model.IField;
import org.eclipse.cdt.core.model.IFunction;
import org.eclipse.cdt.core.model.IFunctionDeclaration;
import org.eclipse.cdt.core.model.IInclude;
import org.eclipse.cdt.core.model.IMacro;
import org.eclipse.cdt.core.model.IMember;
import org.eclipse.cdt.core.model.IMethod;
import org.eclipse.cdt.core.model.IMethodDeclaration;
import org.eclipse.cdt.core.model.INamespace;
import org.eclipse.cdt.core.model.IParent;
import org.eclipse.cdt.core.model.IStructure;
import org.eclipse.cdt.core.model.ITypeDef;
import org.eclipse.cdt.core.model.IVariable;
import org.eclipse.cdt.core.model.IVariableDeclaration;
import org.eclipse.cdt.internal.core.model.ClassTemplate;
import org.eclipse.cdt.internal.core.model.FunctionTemplate;
import org.eclipse.cdt.internal.core.model.MethodTemplate;
import org.eclipse.cdt.internal.core.model.TranslationUnit;
import org.eclipse.cdt.testplugin.CProjectHelper;
import org.eclipse.cdt.testplugin.TestPluginLauncher;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

public class CModelElementsTests extends TestCase {	
	private ICProject fCProject;
	private IFile headerFile;
	private NullProgressMonitor monitor;
		
	public static void main(String[] args) {
		TestPluginLauncher.run(TestPluginLauncher.getLocationFromProperties(), WorkingCopyTests.class, args);
	}
		
	public static Test suite() {
		TestSuite suite= new TestSuite();
		suite.addTest(new CModelElementsTests("testCModelElements"));
		return suite;
	}		
		
	public CModelElementsTests(String name) {
		super(name);
	}
		
	protected void setUp() throws Exception {
		monitor = new NullProgressMonitor();
		String pluginRoot=org.eclipse.core.runtime.Platform.getPlugin("org.eclipse.cdt.ui.tests").find(new Path("/")).getFile();
	
		fCProject= CProjectHelper.createCProject("TestProject1", "bin");
		headerFile = fCProject.getProject().getFile("CModelElementsTest.h");
		if (!headerFile.exists()) {
			try{
				FileInputStream fileIn = new FileInputStream(pluginRoot+ "model/org/eclipse/cdt/core/model/tests/resources/cfiles/CModelElementsTestStart.h"); 
				headerFile.create(fileIn,false, monitor);        
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		if (!fCProject.getProject().hasNature(CCProjectNature.CC_NATURE_ID)) {
			addNatureToProject(fCProject.getProject(), CCProjectNature.CC_NATURE_ID, null);
		}

		CCorePlugin.getDefault().setUseNewParser(true);
	}

	private static void addNatureToProject(IProject proj, String natureId, IProgressMonitor monitor) throws CoreException {
		IProjectDescription description = proj.getDescription();
		String[] prevNatures= description.getNatureIds();
		String[] newNatures= new String[prevNatures.length + 1];
		System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
		newNatures[prevNatures.length]= natureId;
		description.setNatureIds(newNatures);
		proj.setDescription(description, monitor);
	}
	
	protected void tearDown() throws Exception {
		CProjectHelper.delete(fCProject);
	}	
			
	public void testCModelElements(){
		TranslationUnit tu = new TranslationUnit(fCProject, headerFile);
		// parse the translation unit to get the elements tree		
		tu.parse();
		
		// tu ---> include
		checkInclude(tu);
		
		// tu ---> macro
		checkMacro(tu);
		
		// tu ---> namespace: MyPackage
		ArrayList tuPackages = tu.getChildrenOfType(ICElement.C_NAMESPACE);
		INamespace namespace = (INamespace) tuPackages.get(0);
		assertEquals(namespace.getElementName(), new String("MyPackage"));
		
		checkClass(namespace);
				
		checkEnums(namespace);	
		
		checkVariables(namespace);		

		checkVariableDeclarations(namespace);		
		
		checkFunctions(namespace);
		
		checkStructs(namespace);
		
		checkTemplates(namespace);
		
		checkArrays(tu);
	}

	private void checkInclude(IParent tu){
		ArrayList tuIncludes = tu.getChildrenOfType(ICElement.C_INCLUDE);
		IInclude inc1 = (IInclude) tuIncludes.get(0);
		assertEquals(inc1.getElementName(), new String("stdio.h"));
	}
	
	private void checkMacro(IParent tu){
		ArrayList tuMacros = tu.getChildrenOfType(ICElement.C_MACRO);
		IMacro mac1 = (IMacro) tuMacros.get(0);
		assertEquals(mac1.getElementName(), new String("PRINT"));
	}
	
	private void checkClass(IParent namespace){
		// MyPackage ---> class: Hello
		ArrayList nsClasses = namespace.getChildrenOfType(ICElement.C_CLASS);		
		IStructure classHello = (IStructure) nsClasses.get(0);
		assertEquals(classHello.getElementName(), new String("Hello"));
		
		// Hello --> field: int x
		ArrayList helloFields = classHello.getChildrenOfType(ICElement.C_FIELD);
		IField intX = (IField) helloFields.get(0);
		assertEquals(intX.getElementName(), new String("x"));
		assertEquals(intX.getTypeName(), new String("int"));
		int xVisibility = intX.getVisibility(); 
		if (xVisibility != IMember.V_PROTECTED)
			fail("visibility should be protected!");
		
		// Hello ---> method: void setX(int X)
		ArrayList helloMethods = classHello.getChildrenOfType(ICElement.C_METHOD);
		IMethod setX = (IMethod) helloMethods.get(0);
		assertEquals(setX.getElementName(), new String("setX"));
		assertEquals(setX.getReturnType(), new String("void"));
		int setXNumOfParam = setX.getNumberOfParameters();
		if(setXNumOfParam != 1)
			fail("setX should have one parameter!");
		String[] setXParamTypes = setX.getParameterTypes();
		String firstParamType = setXParamTypes[0];
		assertEquals(firstParamType, new String("int"));
		// TODO : check for the inline here
		
		checkNestedNamespace(classHello);
	}
	private void checkNestedNamespace(IParent classHello){
		// Hello ---> namespace: MyNestedPackage 
		ArrayList helloNamespaces = classHello.getChildrenOfType(ICElement.C_NAMESPACE);
		INamespace myNestedPackage = (INamespace) helloNamespaces.get(0);
		assertEquals(myNestedPackage.getElementName(), new String("MyNestedPackage"));
	
		checkParentNestedClass(myNestedPackage);	
		checkDerivedNestedClass(myNestedPackage);
	}
	private void checkParentNestedClass(IParent myNestedPackage){
		// MyNestedPackage ---> class: Y  
		ArrayList nestedClasses = myNestedPackage.getChildrenOfType(ICElement.C_CLASS);		
		IStructure classY = (IStructure) nestedClasses.get(0);
		assertEquals(classY.getElementName(), new String("Y"));
		
		// Y ---> constructor: Y
		ArrayList yMethods = classY.getChildrenOfType(ICElement.C_METHOD_DECLARATION);
		IMethodDeclaration constructor  = (IMethodDeclaration) yMethods.get(0);
		assertEquals(constructor.getElementName(), new String("Y"));
		assertTrue (constructor.isConstructor());
		
		// Y ---> destructor: ~Y
		IMethodDeclaration destructor  = (IMethodDeclaration) yMethods.get(1);
		assertEquals(destructor.getElementName(), new String("~Y"));
		assertTrue (destructor.isDestructor());
		// TODO: check for virtual on destructors
		
	}
	
	private void checkDerivedNestedClass(IParent myNestedPackage){
		// MyNestedPackage ---> class: X public Y 
		ArrayList nestedClasses = myNestedPackage.getChildrenOfType(ICElement.C_CLASS);		
		IStructure classX = (IStructure) nestedClasses.get(1);
		assertEquals(classX.getElementName(), new String("X"));
		// TODO : Check for base classes here
		
		// X --> field: B b
		ArrayList xFieldChildren = classX.getChildrenOfType(ICElement.C_FIELD);
		IField bB = (IField) xFieldChildren.get(0);
		assertEquals(bB.getElementName(), new String("b"));
		assertEquals(bB.getTypeName(), new String("B"));
		int bVisibility = bB.getVisibility(); 
		if (bVisibility != IMember.V_PRIVATE)
			fail("visibility should be private!");
		
		// X ---> constructor chain: X 
		ArrayList xMethodChildren = classX.getChildrenOfType(ICElement.C_METHOD);
		IMethod xconstructor  = (IMethod) xMethodChildren.get(0);
		assertEquals(xconstructor.getElementName(), new String("X"));
		assertTrue (xconstructor.isConstructor());

		// X ---> method declaration: doNothing
		ArrayList xMethodDeclarations = classX.getChildrenOfType(ICElement.C_METHOD_DECLARATION);
		IMethodDeclaration xDoNothing = (IMethodDeclaration) xMethodDeclarations.get(0);
		assertEquals(xDoNothing.getElementName(), new String("doNothing"));
		assertEquals(xDoNothing.getReturnType(), new String("int"));						
	}
	
	private void checkEnums(IParent namespace){
		// MyPackage ---> enum: Noname
		ArrayList nsEnums = namespace.getChildrenOfType(ICElement.C_ENUMERATION);		
		IEnumeration enum = (IEnumeration) nsEnums.get(0);
		assertEquals(enum.getElementName(), new String(""));
	
		// 	enum ---> enumerator: first
		ArrayList enumEnumerators = enum.getChildrenOfType(ICElement.C_ENUMERATOR);
		IEnumerator first = (IEnumerator) enumEnumerators.get(0);
		assertEquals(first.getElementName(), new String("first"));
		// 	enum ---> enumerator: second
		IEnumerator second = (IEnumerator) enumEnumerators.get(1);
		assertEquals(second.getElementName(), new String("second"));
		// 	enum ---> enumerator: third
		IEnumerator third = (IEnumerator) enumEnumerators.get(2);
		assertEquals(third.getElementName(), new String("third"));		

		// MyPackage ---> enum: MyEnum
		IEnumeration myEnum = (IEnumeration) nsEnums.get(1);
		assertEquals(myEnum.getElementName(), new String("MyEnum"));
	
		// 	enum ---> enumerator: first
		ArrayList myEnumEnumerators = myEnum.getChildrenOfType(ICElement.C_ENUMERATOR);
		IEnumerator f = (IEnumerator) myEnumEnumerators.get(0);
		assertEquals(f.getElementName(), new String("f"));
		// 	enum ---> enumerator: second
		IEnumerator s = (IEnumerator) myEnumEnumerators.get(1);
		assertEquals(s.getElementName(), new String("s"));
		// 	enum ---> enumerator: third
		IEnumerator t = (IEnumerator) myEnumEnumerators.get(2);
		assertEquals(t.getElementName(), new String("t"));
	}

	private void checkVariables(IParent namespace){
		// MyPackage ---> int v
		ArrayList nsVars = namespace.getChildrenOfType(ICElement.C_VARIABLE);
		IVariable var1 = (IVariable) nsVars.get(0);
		assertEquals(var1.getElementName(), new String("v"));
		assertEquals(var1.getTypeName(), new String("int"));
		
		// MyPackage ---> unsigned long vuLong
		IVariable var2 = (IVariable) nsVars.get(1);
		assertEquals(var2.getElementName(), new String("vuLong"));
		assertEquals(var2.getTypeName(), new String("unsigned long "));
		
		// MyPackage ---> unsigned short vuShort
		IVariable var3 = (IVariable) nsVars.get(2);
		assertEquals(var3.getElementName(), new String("vuShort"));
		assertEquals(var3.getTypeName(), new String("unsigned short "));
	}

	private void checkVariableDeclarations(IParent namespace){
		// MyPackage ---> extern int evar
		ArrayList nsVarDecls = namespace.getChildrenOfType(ICElement.C_VARIABLE_DECLARATION);
		IVariableDeclaration vDecl1 = (IVariableDeclaration) nsVarDecls.get(0);
		assertEquals(vDecl1.getElementName(), new String("evar"));
		assertEquals(vDecl1.getTypeName(), new String("int"));

		// MyPackage ---> function pointer: orig_malloc_hook
		IVariableDeclaration vDecl2 = (IVariableDeclaration) nsVarDecls.get(1);
		assertEquals(vDecl2.getElementName(), new String("orig_malloc_hook"));
		assertEquals(vDecl2.getTypeName(), new String ("void*(*)(const char*, int, size_t)"));
	}
	
	private void checkFunctions(IParent namespace){
		//	MyPackage ---> function: void foo()
		ArrayList nsFunctionDeclarations = namespace.getChildrenOfType(ICElement.C_FUNCTION_DECLARATION);
		IFunctionDeclaration f1 = (IFunctionDeclaration) nsFunctionDeclarations.get(0);
		assertEquals(f1.getElementName(), new String("foo"));
		assertEquals(f1.getReturnType(), new String("void"));
		
		//	MyPackage ---> function: char* foo(int&, char**)
		IFunctionDeclaration f2 = (IFunctionDeclaration) nsFunctionDeclarations.get(1);
		assertEquals(f2.getElementName(), new String("foo"));
		assertEquals(f2.getReturnType(), new String("char*"));
		int fooNumOfParam = f2.getNumberOfParameters();
		if(fooNumOfParam != 2)
			fail("foo should have two parameter!");
		String[] paramTypes = f2.getParameterTypes();
		assertEquals(paramTypes[0], new String("int&"));
		assertEquals(paramTypes[1], new String("char**"));
	
		//	MyPackage ---> function: void boo() {}		
		ArrayList nsFunctions = namespace.getChildrenOfType(ICElement.C_FUNCTION);
		IFunction f3 = (IFunction) nsFunctions.get(0);		
		assertEquals(f3.getElementName(), new String("boo"));
		assertEquals(f3.getReturnType(), new String("void"));
	}

	private void checkStructs(IParent namespace){
		// struct with name
		ArrayList nsStructs = namespace.getChildrenOfType(ICElement.C_STRUCT);
		IStructure struct1 = (IStructure) nsStructs.get(0);
		assertEquals(struct1.getElementName(), new String ("MyStruct"));
		ArrayList struct1Fields = struct1.getChildrenOfType(ICElement.C_FIELD);
		IField field1 = (IField) struct1Fields.get(0);
		assertEquals(field1.getElementName(), new String("sint"));
		assertEquals(field1.getTypeName(), new String("int"));
		if(field1.getVisibility() != IMember.V_PUBLIC)
			fail("field visibility should be public!");
		
		// struct no name
		IStructure struct2 = (IStructure) nsStructs.get(1);
		assertEquals(struct2.getElementName(), new String (""));
		ArrayList struct2Fields = struct2.getChildrenOfType(ICElement.C_FIELD);
		IField field2 = (IField) struct2Fields.get(0);
		assertEquals(field2.getElementName(), new String("ss"));
		assertEquals(field2.getTypeName(), new String("int"));
		if(field2.getVisibility() != IMember.V_PUBLIC)
			fail("field visibility should be public!");
		
		// typedefs
		ArrayList nsTypeDefs = namespace.getChildrenOfType(ICElement.C_TYPEDEF);
		ITypeDef td1 = (ITypeDef) nsTypeDefs.get(0);
		assertEquals(td1.getElementName(), new String ("myStruct"));
		assertEquals(td1.getTypeName(), new String ("struct MyStruct"));
		ITypeDef td2 = (ITypeDef) nsTypeDefs.get(1);
		assertEquals(td2.getElementName(), new String ("myTypedef"));
		assertEquals(td2.getTypeName(), new String (""));

		// union
		ArrayList nsUnions = namespace.getChildrenOfType(ICElement.C_UNION);
		IStructure u0 = (IStructure) nsUnions.get(0);
		assertEquals(u0.getElementName(), new String("U"));		
		ArrayList u0Fields = u0.getChildrenOfType(ICElement.C_FIELD);
		IField field3 = (IField) u0Fields.get(0);
		assertEquals(field3.getElementName(), new String("U1"));
		assertEquals(field3.getTypeName(), new String("int"));
		if(field3.getVisibility() != IMember.V_PUBLIC)
			fail("field visibility should be public!");
	}

	private void checkTemplates(IParent namespace){
		// template function
		ArrayList functionTemplates = namespace.getChildrenOfType(ICElement.C_TEMPLATE_FUNCTION);
		FunctionTemplate ft = (FunctionTemplate)functionTemplates.get(0);
		assertEquals(ft.getElementName(), new String("aTemplatedFunction"));
		assertEquals(ft.getTemplateSignature(), new String("aTemplatedFunction<A, B>(B) : A"));
		
		// template method
		ArrayList nsClasses = namespace.getChildrenOfType(ICElement.C_CLASS);
		IStructure enclosingClass = (IStructure) nsClasses.get(1);
		ArrayList methodTemplates = enclosingClass.getChildrenOfType(ICElement.C_TEMPLATE_METHOD);
		MethodTemplate mt = (MethodTemplate)methodTemplates.get(0);
		assertEquals(mt.getElementName(), new String("aTemplatedMethod"));
		assertEquals(mt.getTemplateSignature(), new String("aTemplatedMethod<A, B>(B) : A"));
		assertEquals(mt.getVisibility(), IMember.V_PUBLIC);
		
		// template class
		ArrayList classTemplates = namespace.getChildrenOfType(ICElement.C_TEMPLATE_CLASS);
		ClassTemplate ct = (ClassTemplate)classTemplates.get(0);
		assertEquals(ct.getElementName(), new String("myarray"));
		assertEquals(ct.getTemplateSignature(), new String("myarray<T, Tibor>"));
	}
	
	private void checkArrays(IParent tu){
		// array variable
		ArrayList variables = tu.getChildrenOfType(ICElement.C_VARIABLE);
		IVariable arrayVar = (IVariable) variables.get(0);
		assertEquals(arrayVar.getElementName(), new String("myArray"));
		assertEquals(arrayVar.getTypeName(), new String("int[][]"));
		
		// array parameter in function main
		ArrayList functions = tu.getChildrenOfType(ICElement.C_FUNCTION);
		IFunction mainFunction  = (IFunction) functions.get(0);
		assertEquals(mainFunction.getElementName(), new String("main"));
		assertEquals(mainFunction.getReturnType(), new String("int"));
		int NumOfParam = mainFunction.getNumberOfParameters();
		if(NumOfParam != 2)
			fail("main should have two parameter!");
		String[] paramTypes = mainFunction.getParameterTypes();
		assertEquals(paramTypes[0], new String("int"));
		assertEquals(paramTypes[1], new String("char*[]"));
		
	}
	

}
