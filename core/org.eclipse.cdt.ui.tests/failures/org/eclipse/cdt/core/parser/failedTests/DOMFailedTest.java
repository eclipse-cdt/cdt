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

import org.eclipse.cdt.core.parser.tests.BaseDOMTest;

/**
 * @author jcamelon
 */
public class DOMFailedTest extends BaseDOMTest  {

	public DOMFailedTest(String name) {
		super(name);
	}

	public void testBug36704() throws Exception {
		failTest("template <class T, class U> struct Length< Typelist<T, U> >	{ enum { value = 1 + Length<U>::value };};);");
	}

	public void testBug36691() throws Exception {
		Writer code = new StringWriter();
		code.write("template <class T, class H>\n");
		code.write(
			"typename H::template Rebind<T>::Result& Field(H& obj)\n");
		code.write("{	return obj;	}\n");
		failTest(code.toString());
	}

	public void testBug36699() throws Exception {
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
		failTest(code.toString());
	}

	
	public void testBug36714() throws Exception {
		Writer code = new StringWriter();
		code.write("unsigned long a = 0UL;\n");
		code.write("unsigned long a2 = 0L; \n");

		failTest(code.toString());
	}
	
	public void testBug36730(){
		failTest("FUNCTION_MACRO( 1, a );\n	int i;");
	}

	public void testBug36702() throws Exception
	{
		Writer code = new StringWriter(); 
		code.write( "void mad_decoder_init(struct mad_decoder *, void *,\n" ); 
		code.write( "			  enum mad_flow (*)(void *, struct mad_stream *),\n" ); 
		code.write( "			  enum mad_flow (*)(void *, struct mad_header const *),\n" ); 
		code.write( "			  enum mad_flow (*)(void *,\n" ); 
		code.write( "					struct mad_stream const *,\n" ); 
		code.write( "					struct mad_frame *),\n" ); 
		code.write( "			  enum mad_flow (*)(void *,\n" ); 
		code.write( "					struct mad_header const *,\n" ); 
		code.write( "					struct mad_pcm *),\n" ); 
		code.write( "			  enum mad_flow (*)(void *,\n" ); 
		code.write( "					struct mad_stream *,\n" ); 
		code.write( "					struct mad_frame *),\n" ); 
		code.write( "			  enum mad_flow (*)(void *, void *, unsigned int *)\n" ); 
		code.write( ");\n" );  
		
		failTest( code.toString() );
		
	}

}
