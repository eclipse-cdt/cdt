/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.core.parser.failedTests;

import java.io.StringWriter;
import java.io.Writer;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.parser.tests.DOMTests;
import org.eclipse.cdt.internal.core.dom.TranslationUnit;
import org.eclipse.cdt.internal.core.parser.ParserException;

/**
 * @author jcamelon
 */
public class DOMFailedTest extends DOMTests {

	public DOMFailedTest(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite();

		suite.addTest(new DOMFailedTest("testBug36689"));
		suite.addTest(new DOMFailedTest("testBug36690"));
		suite.addTest(new DOMFailedTest("testBug36691"));
		suite.addTest(new DOMFailedTest("testBug36692"));
		suite.addTest(new DOMFailedTest("testBug36693"));
		suite.addTest(new DOMFailedTest("testBug36696"));
		suite.addTest(new DOMFailedTest("testBug36699"));
		suite.addTest(new DOMFailedTest("testBug36703"));
		suite.addTest(new DOMFailedTest("testBug36704"));
		suite.addTest(new DOMFailedTest("testBug36707"));
		suite.addTest(new DOMFailedTest("testBug36708"));
		suite.addTest(new DOMFailedTest("testBug36713"));
		suite.addTest(new DOMFailedTest("testBug36714"));
		suite.addTest(new DOMFailedTest("testBug36717"));
		suite.addTest(new DOMFailedTest("testBug36730"));
		return suite;
	}

	public void testBug36689() {
		boolean testPassed = false;
		try {
			Writer code = new StringWriter();
			code.write("template\n");
			code.write("<\n");
			code.write("class AbstractFact,\n");
			code.write(
				"template <class, class> class Creator = OpNewFactoryUnit,\n");
			code.write("class TList = typename AbstractFact::ProductList\n");
			code.write(">\n");
			code.write("class ConcreteFactory\n");
			code.write(": public GenLinearHierarchy<\n");
			code.write(
				"typename TL::Reverse<TList>::Result, Creator, AbstractFact>\n");
			code.write("{\n");
			code.write("public:\n");
			code.write(
				"typedef typename AbstractFact::ProductList ProductList;\n");
			code.write("typedef TList ConcreteProductList;\n");
			code.write("};\n");
			TranslationUnit tu = parse(code.toString());
			testPassed = true;
		} catch (Throwable e) {
			if (!(e instanceof ParserException))
				fail("Unexpected Error: " + e.getMessage());

			if (testPassed)
				fail("The expected error did not occur.");
		}
	}

	public void testBug36690() {
		boolean testPassed = false;
		try {
			TranslationUnit tu =
				parse("Functor(const Functor& rhs) : spImpl_(Impl::Clone(rhs.spImpl_.get())){}");
			testPassed = true;
		} catch (Throwable e) {
			if (!(e instanceof ParserException))
				fail("Unexpected Error: " + e.getMessage());

			if (testPassed)
				fail("The expected error did not occur.");

		}
	}

	public void testBug36691() {
		boolean testPassed = false;
		try {
			Writer code = new StringWriter();
			code.write("template <class T, class H>\n");
			code.write(
				"typename H::template Rebind<T>::Result& Field(H& obj)\n");
			code.write("{	return obj;	}\n");
			TranslationUnit tu = parse(code.toString());
			testPassed = true;
		} catch (Throwable e) {
			if (!(e instanceof ParserException))
				fail("Unexpected Error: " + e.getMessage());

			if (testPassed)
				fail("The expected error did not occur.");
		}
	}

	public void testBug36692() {
		boolean testPassed = false;
		try {
			Writer code = new StringWriter();
			code.write("template <typename T, typename Destroyer>\n");
			code.write(
				"void SetLongevity(T* pDynObject, unsigned int longevity,\n");
			code.write("Destroyer d = Private::Deleter<T>::Delete){}\n");

			TranslationUnit tu = parse(code.toString());
			testPassed = true;
		} catch (Throwable e) {
			if (!(e instanceof ParserException))
				fail("Unexpected Error: " + e.getMessage());

			if (testPassed)
				fail("The expected error did not occur.");
		}

	}

	public void testBug36693() {
		boolean testPassed = false;
		try {
			TranslationUnit tu =
				parse("FixedAllocator::Chunk* FixedAllocator::VicinityFind(void* p){}");
			testPassed = true;
		} catch (Throwable e) {
			if (!(e instanceof ParserException))
				fail("Unexpected Error: " + e.getMessage());

			if (testPassed)
				fail("The expected error did not occur.");
		}
	}

	public void testBug36696() {
		boolean testPassed = false;
		try {
			Writer code = new StringWriter();
			code.write(
				"template <typename P1> RefCounted(const RefCounted<P1>& rhs)\n");
			code.write(
				": pCount_(reinterpret_cast<const RefCounted&>(rhs).pCount_) {}\n");
			TranslationUnit tu = parse(code.toString());
			testPassed = true;
		} catch (Throwable e) {
			if (!(e instanceof ParserException))
				fail("Unexpected Error: " + e.getMessage());

			if (testPassed)
				fail("The expected error did not occur.");
		}
	}

	public void testBug36699() {
		boolean testPassed = false;
		try {
			Writer code = new StringWriter();
			code.write(
				"template <	template <class> class ThreadingModel = DEFAULT_THREADING,\n");
			code.write("std::size_t chunkSize = DEFAULT_CHUNK_SIZE,\n");
			code.write(
				"std::size_t maxSmallObjectSize = MAX_SMALL_OBJECT_SIZE	>\n");
			code.write("class SmallObject : public ThreadingModel<\n");
			code.write(
				"SmallObject<ThreadingModel, chunkSize, maxSmallObjectSize> >\n");
			code.write("{};\n");
			TranslationUnit tu = parse(code.toString());
			testPassed = true;
		} catch (Throwable e) {
			if (!(e instanceof ParserException))
				fail("Unexpected Error: " + e.getMessage());

			if (testPassed)
				fail("The expected error did not occur.");
		}
	}

	public void testBug36703() {
		boolean testPassed = false;
		try {
			TranslationUnit tu = parse("const std::type_info& Get() const;");
			testPassed = true;
		} catch (Throwable e) {
			if (!(e instanceof ParserException))
				fail("Unexpected Error: " + e.getMessage());

			if (testPassed)
				fail("The expected error did not occur.");
		}
	}

	public void testBug36704() {
		boolean testPassed = false;
		try {
			TranslationUnit tu =
				parse("template <class T, class U> struct Length< Typelist<T, U> >	{ enum { value = 1 + Length<U>::value };};);");
			testPassed = true;
		} catch (Throwable e) {
			if (!(e instanceof ParserException))
				fail("Unexpected Error: " + e.getMessage());

			if (testPassed)
				fail("The expected error did not occur.");
		}
	}

	public void testBug36707() {
		boolean testPassed = false;
		try {
			TranslationUnit tu =
				parse("enum { exists = sizeof(typename H::Small) == sizeof((H::Test(H::MakeT()))) };");
			testPassed = true;
		} catch (Throwable e) {
			if (!(e instanceof ParserException))
				fail("Unexpected Error: " + e.getMessage());

			if (testPassed)
				fail("The expected error did not occur.");
		}
	}

	public void testBug36708() {
		boolean testPassed = false;
		try {
			TranslationUnit tu =
				parse("enum { isPointer = PointerTraits<T>::result };");
			testPassed = true;
		} catch (Throwable e) {
			if (!(e instanceof ParserException))
				fail("Unexpected Error: " + e.getMessage());

			if (testPassed)
				fail("The expected error did not occur.");
		}
	}
	
	public void testBug36713(){
		boolean testPassed = false;
		try{
			Writer code = new StringWriter();
			code.write("A (const * fPtr) (void *); \n");
			code.write("A (const * fPtr2) ( A * ); \n");
			code.write("A (const * fPtr3) ( A * ) = function\n");
			TranslationUnit tu = parse(code.toString());
			testPassed = true;
		} catch (Throwable e ) {
			if( ! (e instanceof ParserException))
				fail( "Unexpected Error: " + e.getMessage() );
		}
		if( testPassed )
			fail( "The expected error did not occur.");
	}

	public void testBug36714(){
		boolean testPassed = false;
		try{
			Writer code = new StringWriter();
			code.write("unsigned long a = 0UL;\n");
			code.write("unsigned long a2 = 0L; \n");

			TranslationUnit tu = parse(code.toString());
			testPassed = true;
		} catch (Throwable e ) {
			if( ! (e instanceof ParserException))
				fail( "Unexpected Error: " + e.getMessage() );
		}
		if( testPassed )
			fail( "The expected error did not occur.");
	}
	
	public void testBug36717(){
		boolean testPassed = false;
		try{
			TranslationUnit tu =
				parse("enum { eA = A::b };");
			
			testPassed = true;
		} catch (Throwable e ) {
			if( ! (e instanceof ParserException))
				fail( "Unexpected Error: " + e.getMessage() );
		}
		if( testPassed )
			fail( "The expected error did not occur.");
	}
	
	public void testBug36730(){
		boolean testPassed = false;
		try{
			TranslationUnit tu = parse("FUNCTION_MACRO( 1, a );\n	int i;");
		
			testPassed = true;
		} catch (Throwable e ) {
			if( ! (e instanceof ParserException))
				fail( "Unexpected Error: " + e.getMessage() );
		}
		if( testPassed )
			fail( "The expected error did not occur.");
	}
}
