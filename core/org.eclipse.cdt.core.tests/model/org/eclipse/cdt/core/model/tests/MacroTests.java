/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.model.tests;

import java.util.Stack;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IBuffer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultLineTracker;
import org.eclipse.jface.text.ILineTracker;


/**
 * @author bnicolle
 *
 */
public class MacroTests extends IntegratedCModelTest {
	/**
	 * @param name
	 */
	public MacroTests(String name) {
		super(name);
	}
	
	/**
	 * @see org.eclipse.cdt.internal.core.model.IntegratedCModelTest
	 */
	@Override
	public String getSourcefileSubdir() {
		return "resources/cmodel/";
	}

	/**
	 * @see org.eclipse.cdt.internal.core.model.IntegratedCModelTest
	 */
	@Override
	public String getSourcefileResource() {
		return "MacroTests.cpp";
	}
	
	/**
	 * @returns a test suite named after this class
	 *          containing all its public members named "test*"
	 */
	public static Test suite() {
		TestSuite suite= new TestSuite(MacroTests.class);
		return suite;
	}

	public void testBug40759 () throws CModelException, BadLocationException {
		/* This is a list of elements in the test .c file. It will be used 
		 * in a number of places in the tests
		 */
		String[] expectedStringList= {"Z", "X", "Y", 
				"SomeName", "A::BCD", "DEFA", "DB", "B::SomeName", 
				"PINT", "myPINT", "foobar"};
		int[]  expectedOffsets={  8,26,39,55,89,114,130,152,187,212,227};
		int[]  expectedLengths={  1, 1, 1, 1, 8,  4,  2, 18,  4,  6,  6};
		/* This is a list of that the types of the above list of elements is 
		 * expected to be.
		 */
		int[] expectedTypes= { ICElement.C_MACRO, ICElement.C_MACRO, 
				ICElement.C_MACRO, ICElement.C_STRUCT, 
				ICElement.C_VARIABLE, ICElement.C_MACRO, 
				ICElement.C_MACRO, ICElement.C_VARIABLE, ICElement.C_MACRO,
				ICElement.C_VARIABLE, ICElement.C_FUNCTION_DECLARATION};

		ITranslationUnit myTranslationUnit = getTU();

		// fix offsets in case source file is not in windows format
		IBuffer buffer= myTranslationUnit.getBuffer();
		ILineTracker lineTracker= new DefaultLineTracker();
		lineTracker.set(buffer.getContents());
		if (lineTracker.getLineDelimiter(0).length() == 1) {
			lineTracker.set(buffer.getContents().replaceAll("[\r\n]","\r\n"));
			for (int i = 0; i < expectedOffsets.length; i++) {
				expectedOffsets[i] -= lineTracker.getLineNumberOfOffset(expectedOffsets[i]);
			}
		}

		ICElement myElement;
		Stack missing=new Stack();
		int x;

		for (x=0;x<expectedStringList.length;x++) {
			myElement=myTranslationUnit.getElement(expectedStringList[x]);
			if (myElement==null)
				missing.push(expectedStringList[x]);
			else {
				assertTrue("Expected:" + expectedStringList[x] + " Got:" + myElement.getElementName(),
						expectedStringList[x].equals(myElement.getElementName()));

				assertTrue("Expected type for '" + expectedStringList[x] + "':" + expectedTypes[x] + " Got:" + myElement.getElementType(),
						expectedTypes[x] == myElement.getElementType());

				int offset = -1;
				int length = -1;

				if (myElement instanceof ISourceReference) {
					ISourceRange range = ((ISourceReference)myElement).getSourceRange();
					offset = range.getIdStartPos();
					length = range.getIdLength();
				}

				assertTrue("Expected offset for '" + expectedStringList[x] + "':" + expectedOffsets[x] + " Got:" + offset,
						expectedOffsets[x] == offset);

				assertTrue( "Expected length for '" + expectedStringList[x] + "':" + expectedLengths[x] + " Got:" + length,
						expectedLengths[x] == length);
			}

		}
		if (!missing.empty()) {
			String output=new String("Could not get elements: ");
			while (!missing.empty())
				output+=missing.pop() + " ";
			assertTrue(output, false);
		}

	}

}
