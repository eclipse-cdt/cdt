/*******************************************************************************
 *  Copyright (c) 2000, 2010 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *     Andrew Ferguson (Symbian)
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.text;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.IDocumentPartitioningListener;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TypedRegion;

import org.eclipse.cdt.ui.text.ICPartitions;

import org.eclipse.cdt.internal.ui.text.CTextTools;



/**
 * Tests to verify the C partitioning.
 * Derived from JavaPartitionerTest.
 */
public class CPartitionerTest extends TestCase {

	private CTextTools fTextTools;
	private Document fDocument;
	protected boolean fDocumentPartitioningChanged;


	public CPartitionerTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() {

		fTextTools= new CTextTools();

		fDocument= new Document();
		IDocumentPartitioner partitioner= fTextTools.createDocumentPartitioner(null);
		partitioner.connect(fDocument);
		fDocument.setDocumentPartitioner(partitioner);
		fDocument.set("xxx\n/*xxx*/\nxxx\n/**xxx*/\nxxx\n/**/\nxxx\n/***/\nxxx");

		fDocumentPartitioningChanged= false;
		fDocument.addDocumentPartitioningListener(new IDocumentPartitioningListener() {
			public void documentPartitioningChanged(IDocument document) {
				fDocumentPartitioningChanged= true;
			}
		});
	}

	public static Test suite() {
		return new TestSuite(CPartitionerTest.class);
	}

	@Override
	protected void tearDown () {
		fTextTools.dispose();
		fTextTools= null;

		IDocumentPartitioner partitioner= fDocument.getDocumentPartitioner();
		partitioner.disconnect();
		fDocument= null;
	}

	protected String print(ITypedRegion r) {
		return "[" + r.getOffset() + "," + r.getLength() + "," + r.getType() + "]";
	}

	protected void checkPartitioning(ITypedRegion[] expectation, ITypedRegion[] result) {

		assertEquals("invalid number of partitions", expectation.length, result.length);

		for (int i= 0; i < expectation.length; i++) {
			ITypedRegion e= expectation[i];
			ITypedRegion r= result[i];
			assertTrue(print(r) + " != " + print(e), r.equals(e));
		}

	}

	public void testInitialPartitioning() {
		try {

			//	"xxx\n/*xxx*/\nxxx\n/**xxx*/\nxxx\n/**/\nxxx\n/***/\nxxx"

			ITypedRegion[] result= fDocument.computePartitioning(0, fDocument.getLength());
			TypedRegion[] expectation= {
				new TypedRegion(0,  4, IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(4,  7, ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(11, 5, IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(16, 8, ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(24, 5, IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(29, 4, ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(33, 5, IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(38, 5, ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(43, 4, IDocument.DEFAULT_CONTENT_TYPE)
			};

			checkPartitioning(expectation, result);
		} catch (BadLocationException x) {
			assertTrue(false);
		}
	}

	public void testIntraPartitionChange() {
		try {

			fDocument.replace(34, 3, "y");
			//	"xxx\n/*xxx*/\nxxx\n/**xxx*/\nxxx\n/**/\ny\n/***/\nxxx");

			assertTrue(!fDocumentPartitioningChanged);

			ITypedRegion[] result= fDocument.computePartitioning(0, fDocument.getLength());
			TypedRegion[] expectation= {
				new TypedRegion(0,  4, IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(4,  7, ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(11, 5, IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(16, 8, ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(24, 5, IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(29, 4, ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(33, 3, IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(36, 5, ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(41, 4, IDocument.DEFAULT_CONTENT_TYPE)
			};

			checkPartitioning(expectation, result);
		} catch (BadLocationException x) {
			assertTrue(false);
		}
	}

	public void testIntraPartitionChange2() {
		try {

			fDocument.replace(41, 0, "yyy");
			//	"xxx\n/*xxx*/\nxxx\n/**xxx*/\nxxx\n/**/\nxxx\n/**yyy*/\nxxx");

			// assertTrue(!fDocumentPartitioningChanged);

			ITypedRegion[] result= fDocument.computePartitioning(0, fDocument.getLength());
			TypedRegion[] expectation= {
				new TypedRegion(0,  4, IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(4,  7, ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(11, 5, IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(16, 8, ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(24, 5, IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(29, 4, ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(33, 5, IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(38, 8, ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(46, 4, IDocument.DEFAULT_CONTENT_TYPE)
			};

			checkPartitioning(expectation, result);
		} catch (BadLocationException x) {
			assertTrue(false);
		}
	}
	public void testInsertNewPartition() {
		try {

			fDocument.replace(35, 1, "/***/");
			//	"xxx\n/*xxx*/\nxxx\n/**xxx*/\nxxx\n/**/\nx/***/x\n/***/\nxxx");

			assertTrue(fDocumentPartitioningChanged);

			ITypedRegion[] result= fDocument.computePartitioning(0, fDocument.getLength());
			TypedRegion[] expectation= {
				new TypedRegion(0,  4, IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(4,  7, ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(11, 5, IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(16, 8, ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(24, 5, IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(29, 4, ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(33, 2, IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(35, 5, ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(40, 2, IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(42, 5, ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(47, 4, IDocument.DEFAULT_CONTENT_TYPE)
			};

			checkPartitioning(expectation, result);
		} catch (BadLocationException x) {
			assertTrue(false);
		}
	}
	public void testInsertStringPartition() {
		try {

			fDocument.replace(35, 1, "\"yyy\"");
			//	"xxx\n/*xxx*/\nxxx\n/**xxx*/\nxxx\n/**/\nx\"yyy\"x\n/***/\nxxx");

			assertTrue(fDocumentPartitioningChanged);

			ITypedRegion[] result= fDocument.computePartitioning(0, fDocument.getLength());
			TypedRegion[] expectation= {
				new TypedRegion(0,  4, IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(4,  7, ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(11, 5, IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(16, 8, ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(24, 5, IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(29, 4, ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(33, 2, IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(35, 5, ICPartitions.C_STRING),
				new TypedRegion(40, 2, IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(42, 5, ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(47, 4, IDocument.DEFAULT_CONTENT_TYPE)
			};

			checkPartitioning(expectation, result);
		} catch (BadLocationException x) {
			assertTrue(false);
		}
	}
	public void testInsertCharacterPartition() {
		try {

			fDocument.replace(35, 1, "'y'");
			//	"xxx\n/*xxx*/\nxxx\n/**xxx*/\nxxx\n/**/\nx\"yyy\"x\n/***/\nxxx");

			assertTrue(fDocumentPartitioningChanged);

			ITypedRegion[] result= fDocument.computePartitioning(0, fDocument.getLength());
			TypedRegion[] expectation= {
				new TypedRegion(0,  4, IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(4,  7, ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(11, 5, IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(16, 8, ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(24, 5, IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(29, 4, ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(33, 2, IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(35, 3, ICPartitions.C_CHARACTER),
				new TypedRegion(38, 2, IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(40, 5, ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(45, 4, IDocument.DEFAULT_CONTENT_TYPE)
			};

			checkPartitioning(expectation, result);
		} catch (BadLocationException x) {
			assertTrue(false);
		}
	}
	public void testInsertPreprocessorPartition() {
		try {

			fDocument.replace(4, 0, " # include <x.h>\n");
			//	"xxx\n/*xxx*/\nxxx\n/**xxx*/\nxxx\n/**/\nx\"yyy\"x\n/***/\nxxx");
			//	"xxx\n # include <x.h>\n/*xxx*/\nxxx\n/**xxx*/\nxxx\n/**/\nx\"yyy\"x\n/***/\nxxx");

			assertTrue(fDocumentPartitioningChanged);

			ITypedRegion[] result= fDocument.computePartitioning(0, fDocument.getLength());
			TypedRegion[] expectation= {
				new TypedRegion(0,  4, IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(4, 17, ICPartitions.C_PREPROCESSOR),
				new TypedRegion(21, 7, ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(28, 5, IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(33, 8, ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(41, 5, IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(46, 4, ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(50, 5, IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(55, 5, ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(60, 4, IDocument.DEFAULT_CONTENT_TYPE)
			};

			checkPartitioning(expectation, result);
		} catch (BadLocationException x) {
			assertTrue(false);
		}
	}

	public void testRemovePartition1() {
		try {

			fDocument.replace(13, 16, null);
			//	"xxx\n/*xxx*/\nx/**/\nxxx\n/***/\nxxx");

			assertTrue(fDocumentPartitioningChanged);


			ITypedRegion[] result= fDocument.computePartitioning(0, fDocument.getLength());
			TypedRegion[] expectation= {
				new TypedRegion(0,  4, IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(4,  7, ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(11, 2, IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(13, 4, ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(17, 5, IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(22, 5, ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(27, 4, IDocument.DEFAULT_CONTENT_TYPE)
			};

			checkPartitioning(expectation, result);
		} catch (BadLocationException x) {
			assertTrue(false);
		}
	}

	public void testRemovePartition2() {

		testJoinPartition3();
		fDocumentPartitioningChanged= false;

		try {

			fDocument.replace(5, 2, null);
			//	"xxx\nxxx\nxxx\n/**xxx*/\nxxx\n/**/\nxxx\n/***/\nxxx"

			assertTrue(fDocumentPartitioningChanged);

			ITypedRegion[] result= fDocument.computePartitioning(0, fDocument.getLength());
			TypedRegion[] expectation= {
				new TypedRegion(0,  12, IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(12,  8, ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(20, 5, IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(25, 4, ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(29, 5, IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(34, 5, ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(39, 4, IDocument.DEFAULT_CONTENT_TYPE)
			};

			checkPartitioning(expectation, result);
		} catch (BadLocationException x) {
			assertTrue(false);
		}
	}


	public void testJoinPartitions1() {
		try {

			fDocument.replace(31, 1, null);
			//	"xxx\n/*xxx*/\nxxx\n/**xxx*/\nxxx\n/*/\nxxx\n/***/\nxxx"

			assertTrue(fDocumentPartitioningChanged);

			ITypedRegion[] result= fDocument.computePartitioning(0, fDocument.getLength());
			TypedRegion[] expectation= {
				new TypedRegion(0,  4, IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(4,  7, ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(11, 5, IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(16, 8, ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(24, 5, IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(29, 13, ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(42, 4, IDocument.DEFAULT_CONTENT_TYPE)
			};

			checkPartitioning(expectation, result);
		} catch (BadLocationException x) {
			assertTrue(false);
		}
	}

	public void testJoinPartitions2() {
		try {

			fDocument.replace(32, 1, null);
			//	"xxx\n/*xxx*/\nxxx\n/**xxx*/\nxxx\n/**\nxxx\n/***/\nxxx"

			assertTrue(fDocumentPartitioningChanged);

			ITypedRegion[] result= fDocument.computePartitioning(0, fDocument.getLength());
			TypedRegion[] expectation= {
				new TypedRegion(0,  4, IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(4,  7, ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(11, 5, IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(16, 8, ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(24, 5, IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(29, 13, ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(42, 4, IDocument.DEFAULT_CONTENT_TYPE)
			};

			checkPartitioning(expectation, result);
		} catch (BadLocationException x) {
			assertTrue(false);
		}
	}

	public void testJoinPartition3() {
		try {

			fDocument.replace(9, 2, null);
			//	"xxx\n/*xxx\nxxx\n/**xxx*/\nxxx\n/**/\nxxx\n/***/\nxxx"

			assertTrue(fDocumentPartitioningChanged);

			ITypedRegion[] result= fDocument.computePartitioning(0, fDocument.getLength());
			TypedRegion[] expectation= {
				new TypedRegion(0,  4, IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(4,  18, ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(22, 5, IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(27, 4, ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(31, 5, IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(36, 5, ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(41, 4, IDocument.DEFAULT_CONTENT_TYPE)
			};

			checkPartitioning(expectation, result);
		} catch (BadLocationException x) {
			assertTrue(false);
		}
	}


	public void testSplitPartition1() {

		testJoinPartitions1();
		fDocumentPartitioningChanged= false;


		try {

			//	"xxx\n/*xxx*/\nxxx\n/**xxx*/\nxxx\n/*/\nxxx\n/***/\nxxx"
			fDocument.replace(31, 0, "*");
			//	"xxx\n/*xxx*/\nxxx\n/**xxx*/\nxxx\n/**/\nxxx\n/***/\nxxx"

			assertTrue(fDocumentPartitioningChanged);


		} catch (BadLocationException x) {
			assertTrue(false);
		}

		testInitialPartitioning();
	}

	public void testSplitPartition2() {

		testJoinPartitions2();
		fDocumentPartitioningChanged= false;

		try {

			//	"xxx\n/*xxx*/\nxxx\n/**xxx*/\nxxx\n/**\nxxx\n/***/\nxxx"
			fDocument.replace(32, 0, "/");
			//	"xxx\n/*xxx*/\nxxx\n/**xxx*/\nxxx\n/**/\nxxx\n/***/\nxxx"

			assertTrue(fDocumentPartitioningChanged);

		} catch (BadLocationException x) {
			assertTrue(false);
		}

		testInitialPartitioning();
	}

	public void testSplitPartition3() {

		fDocumentPartitioningChanged= false;

		try {

			//	"xxx\n/*xxx*/\nxxx\n/**xxx*/\nxxx\n/**/\nxxx\n/***/\nxxx"
			fDocument.replace(12, 9, "");
			//	"xxx\n/*xxx*/\nx*/\nxxx\n/**/\nxxx\n/***/\nxxx"

			assertTrue(fDocumentPartitioningChanged);

			ITypedRegion[] result= fDocument.computePartitioning(0, fDocument.getLength());
			TypedRegion[] expectation= {
				new TypedRegion(0,  4, IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(4,  7, ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(11, 9, IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(20, 4, ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(24, 5, IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(29, 5, ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(34, 4, IDocument.DEFAULT_CONTENT_TYPE)
			};

			checkPartitioning(expectation, result);
		} catch (BadLocationException x) {
			assertTrue(false);
		}
	}

	public void testCorruptPartitioning1() {
		try {

			fDocument.replace(0, fDocument.getLength(), "/***/\n/***/");

			ITypedRegion[] result= fDocument.computePartitioning(0, fDocument.getLength());
			TypedRegion[] expectation= {
				new TypedRegion(0, 5, ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(5, 1, IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(6, 5, ICPartitions.C_MULTI_LINE_COMMENT)
			};

			checkPartitioning(expectation, result);

			fDocument.replace(6, 0, "*/\n/***/\n/*");
			// "/***/\n*/\n/***/\n/*/***/"

			result= fDocument.computePartitioning(0, fDocument.getLength());
			expectation= new TypedRegion[] {
				new TypedRegion(0, 5, ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(5, 4, IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(9, 5, ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(14, 1, IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(15, 7, ICPartitions.C_MULTI_LINE_COMMENT)
			};

		} catch (BadLocationException x) {
			assertTrue(false);
		}
	}

	public void testCorruptPartitioning2() {
		try {

			fDocument.replace(0, fDocument.getLength(), "/***/\n/***/\n/***/");

			ITypedRegion[] result= fDocument.computePartitioning(0, fDocument.getLength());
			TypedRegion[] expectation= {
				new TypedRegion(0, 5, ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(5, 1, IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(6, 5, ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(11, 1, IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(12, 5, ICPartitions.C_MULTI_LINE_COMMENT)
			};

			checkPartitioning(expectation, result);

			fDocument.replace(6, 0, "*/\n/***/\n/*");
			// "/***/\n*/\n/***/\n/*/***/\n/***/"

			result= fDocument.computePartitioning(0, fDocument.getLength());
			expectation= new TypedRegion[] {
				new TypedRegion(0, 5, ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(5, 4, IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(9, 5, ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(14, 1, IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(15, 7, ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(22, 1, IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(23, 5, ICPartitions.C_MULTI_LINE_COMMENT)
			};

		} catch (BadLocationException x) {
			assertTrue(false);
		}
	}

	public void testCorruptPartitioning3() {
		try {

			fDocument.replace(0, fDocument.getLength(), "/***/\n/**/");

			ITypedRegion[] result= fDocument.computePartitioning(0, fDocument.getLength());
			TypedRegion[] expectation= {
				new TypedRegion(0, 5, ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(5, 1, IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(6, 4, ICPartitions.C_MULTI_LINE_COMMENT)
			};

			checkPartitioning(expectation, result);

			fDocument.replace(0, 9, "/***/\n/***/\n/***/\n/**");
			// "/***/\n/***/\n/***/\n/***/"

			result= fDocument.computePartitioning(0, fDocument.getLength());
			expectation= new TypedRegion[] {
				new TypedRegion(0, 5, ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(5, 1, IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(6, 5, ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(11, 1, IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(12, 5, ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(17, 1, IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(18, 5, ICPartitions.C_MULTI_LINE_COMMENT)
			};

		} catch (BadLocationException x) {
			assertTrue(false);
		}
	}

	public void testOpenPartition1() {
		try {

			fDocument.replace(42, 1, null);
			//	"xxx\n/*xxx*/\nxxx\n/**xxx*/\nxxx\n/**/\nxxx\n/***\nxxx"

			assertTrue(fDocumentPartitioningChanged);


			ITypedRegion[] result= fDocument.computePartitioning(0, fDocument.getLength());
			TypedRegion[] expectation= {
				new TypedRegion(0,  4, IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(4,  7, ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(11, 5, IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(16, 8, ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(24, 5, IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(29, 4, ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(33, 5, IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(38, 8, ICPartitions.C_MULTI_LINE_COMMENT)
			};

			checkPartitioning(expectation, result);
		} catch (BadLocationException x) {
			assertTrue(false);
		}
	}

	public void testOpenPartition2() {
		try {

			fDocument.replace(47, 0, "/*");
			//	"xxx\n/*xxx*/\nxxx\n/**xxx*/\nxxx\n/**/\nxxx\n/***/\nxxx/*"

			assertTrue(fDocumentPartitioningChanged);


			ITypedRegion[] result= fDocument.computePartitioning(0, fDocument.getLength());
			TypedRegion[] expectation= {
				new TypedRegion(0,  4, IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(4,  7, ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(11, 5, IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(16, 8, ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(24, 5, IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(29, 4, ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(33, 5, IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(38, 5, ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(43, 4, IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(47, 2, ICPartitions.C_MULTI_LINE_COMMENT)
			};

			checkPartitioning(expectation, result);
		} catch (BadLocationException x) {
			assertTrue(false);
		}
	}


	public void testPartitionFinder() {
		try {

			ITypedRegion[] partitioning= fDocument.computePartitioning(0, fDocument.getLength());

			for (int i= 0; i < partitioning.length; i++) {
				ITypedRegion expected= partitioning[i];
				for (int j= 0; j < expected.getLength(); j++) {
					ITypedRegion result= fDocument.getPartition(expected.getOffset() + j);
					assertTrue(expected.equals(result));
				}
			}

		} catch (BadLocationException x) {
			assertTrue(false);
		}
	}

	public void testExtendPartition() {
		try {

			fDocument.replace(0, fDocument.getLength(), "/*");

			ITypedRegion[] result= fDocument.computePartitioning(0, fDocument.getLength());
			TypedRegion[] expectation= {
				new TypedRegion(0,  2, ICPartitions.C_MULTI_LINE_COMMENT)
			};

			checkPartitioning(expectation, result);

			fDocument.replace(2, 0, " ");
			result= fDocument.computePartitioning(0, fDocument.getLength());
			expectation= new TypedRegion[] {
				new TypedRegion(0,  3, ICPartitions.C_MULTI_LINE_COMMENT)
			};

			checkPartitioning(expectation, result);

		} catch (BadLocationException x) {
			assertTrue(false);
		}
	}

	public void testTogglePartition() {
		try {

			fDocument.replace(0, fDocument.getLength(), "\t/*\n\tx\n\t/*/\n\ty\n//\t*/");

			ITypedRegion[] result= fDocument.computePartitioning(0, fDocument.getLength());
			TypedRegion[] expectation1= {
				new TypedRegion(0,  1, IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(1,  10, ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(11, 4, IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(15, 5, ICPartitions.C_SINGLE_LINE_COMMENT)
			};
			checkPartitioning(expectation1, result);

			fDocumentPartitioningChanged= false;
			fDocument.replace(0, 0, "//"); // "//\t/*\n\tx\n\t/*/\n\ty\n//\t*/"
			assertTrue(fDocumentPartitioningChanged);

			result= fDocument.computePartitioning(0, fDocument.getLength());
			TypedRegion[] expectation2= {
				new TypedRegion(0,  6, ICPartitions.C_SINGLE_LINE_COMMENT),
				new TypedRegion(6,  4,  IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(10,  12, ICPartitions.C_MULTI_LINE_COMMENT)
			};
			checkPartitioning(expectation2, result);

		} catch (BadLocationException x) {
			assertTrue(false);
		}
	}

	public void testEditing1() {
		try {

			fDocument.replace(0, fDocument.getLength(), "");

			ITypedRegion[] result= fDocument.computePartitioning(0, fDocument.getLength());
			TypedRegion[] expectation= {
				new TypedRegion(0,  0,  IDocument.DEFAULT_CONTENT_TYPE)
			};
			checkPartitioning(expectation, result);

			fDocument.replace(fDocument.getLength(), 0, "/");
			fDocument.replace(fDocument.getLength(), 0, "*");
			fDocument.replace(fDocument.getLength(), 0, "*");
			fDocument.replace(fDocument.getLength(), 0, "/");

			result= fDocument.computePartitioning(0, fDocument.getLength());
			expectation= new TypedRegion[] {
				new TypedRegion(0,  fDocument.getLength(),  ICPartitions.C_MULTI_LINE_COMMENT)
			};
			checkPartitioning(expectation, result);


			fDocument.replace(fDocument.getLength(), 0, "\r\n");
			result= fDocument.computePartitioning(0, fDocument.getLength());
			expectation= new TypedRegion[] {
				new TypedRegion(0,  4,  ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(4, 2, IDocument.DEFAULT_CONTENT_TYPE)
			};
			checkPartitioning(expectation, result);


		} catch (BadLocationException x) {
			assertTrue(false);
		}
	}

	public void testEditing2() {
		try {

			fDocument.replace(0, fDocument.getLength(), "");

			ITypedRegion[] result= fDocument.computePartitioning(0, fDocument.getLength());
			TypedRegion[] expectation= {
				new TypedRegion(0,  0,  IDocument.DEFAULT_CONTENT_TYPE)
			};
			checkPartitioning(expectation, result);

			fDocument.replace(fDocument.getLength(), 0, "/");
			fDocument.replace(fDocument.getLength(), 0, "*");
			result= fDocument.computePartitioning(0, fDocument.getLength());
			expectation= new TypedRegion[] {
				new TypedRegion(0,  fDocument.getLength(),  ICPartitions.C_MULTI_LINE_COMMENT)
			};
			checkPartitioning(expectation, result);

			fDocument.replace(fDocument.getLength(), 0, "\r\n");
			result= fDocument.computePartitioning(0, fDocument.getLength());
			expectation= new TypedRegion[] {
				new TypedRegion(0,  fDocument.getLength(),  ICPartitions.C_MULTI_LINE_COMMENT)
			};
			checkPartitioning(expectation, result);

			fDocument.replace(fDocument.getLength(), 0, "*");
			result= fDocument.computePartitioning(0, fDocument.getLength());
			expectation= new TypedRegion[] {
				new TypedRegion(0,  fDocument.getLength(),  ICPartitions.C_MULTI_LINE_COMMENT)
			};
			checkPartitioning(expectation, result);

			fDocument.replace(fDocument.getLength(), 0, "*");
			result= fDocument.computePartitioning(0, fDocument.getLength());
			expectation= new TypedRegion[] {
				new TypedRegion(0,  fDocument.getLength(),  ICPartitions.C_MULTI_LINE_COMMENT)
			};
			checkPartitioning(expectation, result);

		} catch (BadLocationException x) {
			assertTrue(false);
		}
	}

	public void testEditing3() {
		try {

			fDocument.replace(0, fDocument.getLength(), "");

			ITypedRegion[] result= fDocument.computePartitioning(0, fDocument.getLength());
			TypedRegion[] expectation= {
				new TypedRegion(0,  0,  IDocument.DEFAULT_CONTENT_TYPE)
			};
			checkPartitioning(expectation, result);

			fDocument.replace(fDocument.getLength(), 0, "/");
			fDocument.replace(fDocument.getLength(), 0, "*");
			fDocument.replace(fDocument.getLength(), 0, "*");
			fDocument.replace(fDocument.getLength(), 0, "\r\n *");
			fDocument.replace(fDocument.getLength(), 0, "/");


			result= fDocument.computePartitioning(0, fDocument.getLength());
			expectation= new TypedRegion[] {
				new TypedRegion(0,  fDocument.getLength(),  ICPartitions.C_MULTI_LINE_COMMENT)
			};
			checkPartitioning(expectation, result);

			fDocument.replace(fDocument.getLength(), 0, "*");
			result= fDocument.computePartitioning(0, fDocument.getLength());
			expectation= new TypedRegion[] {
				new TypedRegion(0,  8,  ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(8, 1, IDocument.DEFAULT_CONTENT_TYPE)
			};
			checkPartitioning(expectation, result);

		} catch (BadLocationException x) {
			assertTrue(false);
		}
	}

	public void testEditingString() {
		try {

			fDocument.replace(0, fDocument.getLength(), "");

			ITypedRegion[] result= fDocument.computePartitioning(0, fDocument.getLength());
			TypedRegion[] expectation= {
				new TypedRegion(0,  0,  IDocument.DEFAULT_CONTENT_TYPE)
			};
			checkPartitioning(expectation, result);

			fDocument.replace(fDocument.getLength(), 0, "\"");
			fDocument.replace(fDocument.getLength(), 0, "y");
			fDocument.replace(fDocument.getLength(), 0, "y");
			fDocument.replace(fDocument.getLength(), 0, "\"");

			result= fDocument.computePartitioning(0, fDocument.getLength());
			expectation= new TypedRegion[] {
				new TypedRegion(0,  fDocument.getLength(),  ICPartitions.C_STRING)
			};
			checkPartitioning(expectation, result);

			fDocument.replace(fDocument.getLength(), 0, "y");
			result= fDocument.computePartitioning(0, fDocument.getLength());
			expectation= new TypedRegion[] {
				new TypedRegion(0,  4,  ICPartitions.C_STRING),
				new TypedRegion(4, 1, IDocument.DEFAULT_CONTENT_TYPE)
			};
			checkPartitioning(expectation, result);

			// insert escaped double quote
			fDocument.replace(2, 0, "\\\"");
			result= fDocument.computePartitioning(0, fDocument.getLength());
			expectation= new TypedRegion[] {
				new TypedRegion(0,  6,  ICPartitions.C_STRING),
				new TypedRegion(6, 1, IDocument.DEFAULT_CONTENT_TYPE)
			};
			checkPartitioning(expectation, result);

			// insert closing and opening double quote
			fDocument.replace(2, 2, "\"\"");
			result= fDocument.computePartitioning(0, fDocument.getLength());
			expectation= new TypedRegion[] {
				new TypedRegion(0,  3,  ICPartitions.C_STRING),
				new TypedRegion(3,  3,  ICPartitions.C_STRING),
				new TypedRegion(6, 1, IDocument.DEFAULT_CONTENT_TYPE)
			};
			checkPartitioning(expectation, result);

			// insert line continuation and newline
			fDocument.replace(2, 2, "\\\r\n");
			result= fDocument.computePartitioning(0, fDocument.getLength());
			expectation= new TypedRegion[] {
				new TypedRegion(0,  7,  ICPartitions.C_STRING),
				new TypedRegion(7, 1, IDocument.DEFAULT_CONTENT_TYPE)
			};
			checkPartitioning(expectation, result);

			// delete line continuation
			fDocument.replace(2, 1, "");
			result= fDocument.computePartitioning(0, fDocument.getLength());
			expectation= new TypedRegion[] {
				new TypedRegion(0, 4,  ICPartitions.C_STRING),
				new TypedRegion(4, 1, IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(5, 2,  ICPartitions.C_STRING)
			};
			checkPartitioning(expectation, result);

		} catch (BadLocationException x) {
			assertTrue(false);
		}
	}

	public void testEditingCharacter() {
		try {

			fDocument.replace(0, fDocument.getLength(), "");

			ITypedRegion[] result= fDocument.computePartitioning(0, fDocument.getLength());
			TypedRegion[] expectation= {
				new TypedRegion(0,  0,  IDocument.DEFAULT_CONTENT_TYPE)
			};
			checkPartitioning(expectation, result);

			fDocument.replace(fDocument.getLength(), 0, "'");
			fDocument.replace(fDocument.getLength(), 0, "y");
			fDocument.replace(fDocument.getLength(), 0, "y");
			fDocument.replace(fDocument.getLength(), 0, "'");

			result= fDocument.computePartitioning(0, fDocument.getLength());
			expectation= new TypedRegion[] {
				new TypedRegion(0,  fDocument.getLength(),  ICPartitions.C_CHARACTER)
			};
			checkPartitioning(expectation, result);

			fDocument.replace(fDocument.getLength(), 0, "y");
			result= fDocument.computePartitioning(0, fDocument.getLength());
			expectation= new TypedRegion[] {
				new TypedRegion(0,  4,  ICPartitions.C_CHARACTER),
				new TypedRegion(4, 1, IDocument.DEFAULT_CONTENT_TYPE)
			};
			checkPartitioning(expectation, result);

			// insert escaped single quote
			fDocument.replace(2, 0, "\\'");
			result= fDocument.computePartitioning(0, fDocument.getLength());
			expectation= new TypedRegion[] {
				new TypedRegion(0,  6,  ICPartitions.C_CHARACTER),
				new TypedRegion(6, 1, IDocument.DEFAULT_CONTENT_TYPE)
			};
			checkPartitioning(expectation, result);

			// insert closing and opening quote
			fDocument.replace(2, 2, "''");
			result= fDocument.computePartitioning(0, fDocument.getLength());
			expectation= new TypedRegion[] {
				new TypedRegion(0,  3,  ICPartitions.C_CHARACTER),
				new TypedRegion(3,  3,  ICPartitions.C_CHARACTER),
				new TypedRegion(6, 1, IDocument.DEFAULT_CONTENT_TYPE)
			};
			checkPartitioning(expectation, result);

			// insert line continuation and newline
			fDocument.replace(2, 2, "\\\r\n");
			result= fDocument.computePartitioning(0, fDocument.getLength());
			expectation= new TypedRegion[] {
				new TypedRegion(0,  7,  ICPartitions.C_CHARACTER),
				new TypedRegion(7, 1, IDocument.DEFAULT_CONTENT_TYPE)
			};
			checkPartitioning(expectation, result);

			// delete line continuation
			fDocument.replace(2, 1, "");
			result= fDocument.computePartitioning(0, fDocument.getLength());
			expectation= new TypedRegion[] {
				new TypedRegion(0, 4,  ICPartitions.C_CHARACTER),
				new TypedRegion(4, 1, IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(5, 2,  ICPartitions.C_CHARACTER)
			};
			checkPartitioning(expectation, result);

		} catch (BadLocationException x) {
			assertTrue(false);
		}
	}

	public void testEditingPreprocessor() {
		try {

			fDocument.replace(0, fDocument.getLength(), "");

			ITypedRegion[] result= fDocument.computePartitioning(0, fDocument.getLength());
			TypedRegion[] expectation= {
				new TypedRegion(0,  0,  IDocument.DEFAULT_CONTENT_TYPE)
			};
			checkPartitioning(expectation, result);

			fDocument.replace(fDocument.getLength(), 0, "#");
			fDocument.replace(fDocument.getLength(), 0, " ");
			fDocument.replace(fDocument.getLength(), 0, "\t");
			fDocument.replace(fDocument.getLength(), 0, "include <float.h> ");

			result= fDocument.computePartitioning(0, fDocument.getLength());
			expectation= new TypedRegion[] {
				new TypedRegion(0,  fDocument.getLength(),  ICPartitions.C_PREPROCESSOR)
			};
			checkPartitioning(expectation, result);

			fDocument.replace(fDocument.getLength(), 0, "//   ");
			result= fDocument.computePartitioning(0, fDocument.getLength());
			expectation= new TypedRegion[] {
				new TypedRegion(0,  21,  ICPartitions.C_PREPROCESSOR),
				new TypedRegion(21, 5, ICPartitions.C_SINGLE_LINE_COMMENT),
			};
			checkPartitioning(expectation, result);

			fDocument.replace(21, 5, "/* */");
			result= fDocument.computePartitioning(0, fDocument.getLength());
			expectation= new TypedRegion[] {
				new TypedRegion(0,  21,  ICPartitions.C_PREPROCESSOR),
				new TypedRegion(21, 5, ICPartitions.C_MULTI_LINE_COMMENT),
			};
			checkPartitioning(expectation, result);

			fDocument.replace(fDocument.getLength(), 0, "\nz");
			result= fDocument.computePartitioning(0, fDocument.getLength());
			expectation= new TypedRegion[] {
				new TypedRegion(0,  21,  ICPartitions.C_PREPROCESSOR),
				new TypedRegion(21, 5, ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(26, 1,  ICPartitions.C_PREPROCESSOR),
				new TypedRegion(27, 1, IDocument.DEFAULT_CONTENT_TYPE)
			};
			checkPartitioning(expectation, result);

			// insert escaped backslash
			fDocument.replace(2, 0, "\\\\");
			result= fDocument.computePartitioning(0, fDocument.getLength());
			expectation= new TypedRegion[] {
				new TypedRegion(0,  23,  ICPartitions.C_PREPROCESSOR),
				new TypedRegion(23, 5, ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(28, 1,  ICPartitions.C_PREPROCESSOR),
				new TypedRegion(29, 1, IDocument.DEFAULT_CONTENT_TYPE)
			};
			checkPartitioning(expectation, result);

			// replace one backslash with a newline
			fDocument.replace(3, 1, "\n");
			result= fDocument.computePartitioning(0, fDocument.getLength());
			expectation= new TypedRegion[] {
				new TypedRegion(0,  23,  ICPartitions.C_PREPROCESSOR),
				new TypedRegion(23, 5, ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(28, 1,  ICPartitions.C_PREPROCESSOR),
				new TypedRegion(29, 1, IDocument.DEFAULT_CONTENT_TYPE)
			};
			checkPartitioning(expectation, result);

			// insert backslash and newline inside multiline comment
			fDocument.replace(26, 0, "\\\r\n");
			result= fDocument.computePartitioning(0, fDocument.getLength());
			expectation= new TypedRegion[] {
				new TypedRegion(0,  23,  ICPartitions.C_PREPROCESSOR),
				new TypedRegion(23, 8, ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(31, 1,  ICPartitions.C_PREPROCESSOR),
				new TypedRegion(32, 1, IDocument.DEFAULT_CONTENT_TYPE)
			};
			checkPartitioning(expectation, result);

			// delete NL leaving only CR
			fDocument.replace(28, 1, "");
			result= fDocument.computePartitioning(0, fDocument.getLength());
			expectation= new TypedRegion[] {
				new TypedRegion(0,  23,  ICPartitions.C_PREPROCESSOR),
				new TypedRegion(23, 7, ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(30, 1,  ICPartitions.C_PREPROCESSOR),
				new TypedRegion(31, 1, IDocument.DEFAULT_CONTENT_TYPE)
			};
			checkPartitioning(expectation, result);

			// delete backslash
			fDocument.replace(26, 1, "");
			result= fDocument.computePartitioning(0, fDocument.getLength());
			expectation= new TypedRegion[] {
				new TypedRegion(0,  23,  ICPartitions.C_PREPROCESSOR),
				new TypedRegion(23, 6, ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(29, 1,  ICPartitions.C_PREPROCESSOR),
				new TypedRegion(30, 1, IDocument.DEFAULT_CONTENT_TYPE)
			};
			checkPartitioning(expectation, result);

			// insert white space before #
			fDocument.replace(0, 0, "  \t");
			result= fDocument.computePartitioning(0, fDocument.getLength());
			expectation= new TypedRegion[] {
				new TypedRegion(0,  26,  ICPartitions.C_PREPROCESSOR),
				new TypedRegion(26, 6, ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(32, 1,  ICPartitions.C_PREPROCESSOR),
				new TypedRegion(33, 1, IDocument.DEFAULT_CONTENT_TYPE)
			};
			checkPartitioning(expectation, result);

		} catch (BadLocationException x) {
			assertTrue(false);
		}
	}

	public void testLineSplicing_Bug124113() {
		try {

			fDocument.replace(0, fDocument.getLength(), "// comment... \\\\\ncontinued");

			ITypedRegion[] result= fDocument.computePartitioning(0, fDocument.getLength());
			TypedRegion[] expectation= {
				new TypedRegion(0,  fDocument.getLength(),  ICPartitions.C_SINGLE_LINE_COMMENT)
			};
			checkPartitioning(expectation, result);

			fDocument.replace(0, fDocument.getLength(), "#define D \\\\\ncontinued");

			result= fDocument.computePartitioning(0, fDocument.getLength());
			expectation= new TypedRegion[] {
				new TypedRegion(0,  fDocument.getLength(),  ICPartitions.C_PREPROCESSOR)
			};
			checkPartitioning(expectation, result);

			fDocument.replace(0, fDocument.getLength(), "\"str\\\\\ncontinued\"");

			result= fDocument.computePartitioning(0, fDocument.getLength());
			expectation= new TypedRegion[] {
				new TypedRegion(0,  fDocument.getLength(),  ICPartitions.C_STRING)
			};
			checkPartitioning(expectation, result);

			fDocument.replace(0, fDocument.getLength(), "'\\\\\nc'");

			result= fDocument.computePartitioning(0, fDocument.getLength());
			expectation= new TypedRegion[] {
				new TypedRegion(0,  fDocument.getLength(),  ICPartitions.C_CHARACTER)
			};
			checkPartitioning(expectation, result);

		} catch (BadLocationException x) {
			assertTrue(false);
		}
	}

	public void testCommentInPreprocessorString() {
		try {

			fDocument.replace(0, fDocument.getLength(), "#define S \"http://www.foo.bar\"");

			ITypedRegion[] result= fDocument.computePartitioning(0, fDocument.getLength());
			TypedRegion[] expectation= {
				new TypedRegion(0,  fDocument.getLength(),  ICPartitions.C_PREPROCESSOR)
			};
			checkPartitioning(expectation, result);

			fDocument.replace(0, fDocument.getLength(), "#define S \"http:/* */\"");

			result= fDocument.computePartitioning(0, fDocument.getLength());
			expectation= new TypedRegion[] {
				new TypedRegion(0,  fDocument.getLength(),  ICPartitions.C_PREPROCESSOR)
			};
			checkPartitioning(expectation, result);

		} catch (BadLocationException x) {
			assertTrue(false);
		}
	}

	public void testString1() {
		try {

			fDocument.replace(0, fDocument.getLength(), "\"[string]\"");
			ITypedRegion[] result= fDocument.computePartitioning(0, fDocument.getLength());
			TypedRegion[] expectation= {
				new TypedRegion(0,  fDocument.getLength(),  ICPartitions.C_STRING)
			};
			checkPartitioning(expectation, result);

			fDocument.replace(0, fDocument.getLength(), "\"string1\" \"string2\"");
			result= fDocument.computePartitioning(0, fDocument.getLength());
			expectation= new TypedRegion[] {
				new TypedRegion(0,  9,  ICPartitions.C_STRING),
				new TypedRegion(9,  1,  IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(10,  9,  ICPartitions.C_STRING)
			};
			checkPartitioning(expectation, result);

		} catch (BadLocationException x) {
			assertTrue(false);
		}
	}

	public void testRawString1() {
		try {

			fDocument.replace(0, fDocument.getLength(), "R\"(line 1\n/*line 2*/\nline 3\n)\"");
			ITypedRegion[] result= fDocument.computePartitioning(0, fDocument.getLength());
			TypedRegion[] expectation= {
				new TypedRegion(0,  1,  IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(1,  fDocument.getLength() - 1,  ICPartitions.C_STRING)
			};
			checkPartitioning(expectation, result);

			fDocument.replace(0, fDocument.getLength(), "R\"()\"//comment");
			result= fDocument.computePartitioning(0, fDocument.getLength());
			expectation= new TypedRegion[] {
				new TypedRegion(0,  1,  IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(1,  fDocument.getLength() - 10,  ICPartitions.C_STRING),
				new TypedRegion(fDocument.getLength() - 9,  9,  ICPartitions.C_SINGLE_LINE_COMMENT),
			};
			checkPartitioning(expectation, result);

			fDocument.replace(0, fDocument.getLength(), "R\"delimiter(line 1\n()delimitex\nline 3\n)delimiter\"");
			result= fDocument.computePartitioning(0, fDocument.getLength());
			expectation= new TypedRegion[] {
				new TypedRegion(0,  1,  IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(1,  fDocument.getLength() - 1,  ICPartitions.C_STRING)
			};
			checkPartitioning(expectation, result);

		} catch (BadLocationException x) {
			assertTrue(false);
		}
	}

	public void testRawString2() {
		try {

			fDocument.replace(0, fDocument.getLength(), "/***/R\"(line 1\nline 2\nline 3\n)\"");
			ITypedRegion[] result= fDocument.computePartitioning(0, fDocument.getLength());
			TypedRegion[] expectation= {
				new TypedRegion(0,  5,  ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(5,  1,  IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(6,  fDocument.getLength() - 6,  ICPartitions.C_STRING)
			};
			checkPartitioning(expectation, result);

			fDocument.replace(0, fDocument.getLength(), "#define X x\nR\"delimiter(line 1\n()delimitex\nline 3\n)delimiter\"");
			result= fDocument.computePartitioning(0, fDocument.getLength());
			expectation= new TypedRegion[] {
				new TypedRegion(0,  12,  ICPartitions.C_PREPROCESSOR),
				new TypedRegion(12,  1,  IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(13,  fDocument.getLength() - 13,  ICPartitions.C_STRING)
			};
			checkPartitioning(expectation, result);

		} catch (BadLocationException x) {
			assertTrue(false);
		}
	}

	public void testRawString3() {
		try {

			fDocument.replace(0, fDocument.getLength(), "/***/R\"(line 1\nline 2\nline 3\n)\" \"str\"");
			ITypedRegion[] result= fDocument.computePartitioning(0, fDocument.getLength());
			TypedRegion[] expectation= {
				new TypedRegion(0,  5,  ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(5,  1,  IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(6,  fDocument.getLength() - 12,  ICPartitions.C_STRING),
				new TypedRegion(fDocument.getLength() - 6, 1,  IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(fDocument.getLength() - 5, 5,  ICPartitions.C_STRING),
			};
			checkPartitioning(expectation, result);

			fDocument.replace(0, fDocument.getLength(), "#define X x\nR\"delimiter(line 1\n()delimitex\nline 3\n)del)delimiter\" \"str\"");
			result= fDocument.computePartitioning(0, fDocument.getLength());
			expectation= new TypedRegion[] {
				new TypedRegion(0,  12,  ICPartitions.C_PREPROCESSOR),
				new TypedRegion(12,  1,  IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(13,  fDocument.getLength() - 19,  ICPartitions.C_STRING),
				new TypedRegion(fDocument.getLength() - 6, 1,  IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(fDocument.getLength() - 5, 5,  ICPartitions.C_STRING),
			};
			checkPartitioning(expectation, result);

		} catch (BadLocationException x) {
			assertTrue(false);
		}
	}

	public void testEditingRawString1() {
		try {

			fDocument.replace(0, fDocument.getLength(), "/***/R\"(line 1\nline 2\nline 3\n)\" \"str\"");
			ITypedRegion[] result= fDocument.computePartitioning(0, fDocument.getLength());
			TypedRegion[] expectation= {
				new TypedRegion(0,  5,  ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(5,  1,  IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(6,  fDocument.getLength() - 12,  ICPartitions.C_STRING),
				new TypedRegion(fDocument.getLength() - 6, 1,  IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(fDocument.getLength() - 5, 5,  ICPartitions.C_STRING),
			};
			checkPartitioning(expectation, result);

			// insert line
			fDocument.replace(8, 0, "line 0\n");
			result= fDocument.computePartitioning(0, fDocument.getLength());
			expectation= new TypedRegion[] {
				new TypedRegion(0,  5,  ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(5,  1,  IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(6,  fDocument.getLength() - 12,  ICPartitions.C_STRING),
				new TypedRegion(fDocument.getLength() - 6, 1,  IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(fDocument.getLength() - 5, 5,  ICPartitions.C_STRING),
			};
			checkPartitioning(expectation, result);

			// delete text
			fDocument.replace(12, 8, "");
			result= fDocument.computePartitioning(0, fDocument.getLength());
			expectation= new TypedRegion[] {
				new TypedRegion(0,  5,  ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(5,  1,  IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(6,  fDocument.getLength() - 12,  ICPartitions.C_STRING),
				new TypedRegion(fDocument.getLength() - 6, 1,  IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(fDocument.getLength() - 5, 5,  ICPartitions.C_STRING),
			};
			checkPartitioning(expectation, result);

		} catch (BadLocationException x) {
			assertTrue(false);
		}
	}

	public void testEditingRawString2() {
		try {

			fDocument.replace(0, fDocument.getLength(), "/***/R\"(line 1\nline 2\nline 3\n)\" \"str\"");
			ITypedRegion[] result= fDocument.computePartitioning(0, fDocument.getLength());
			TypedRegion[] expectation= {
				new TypedRegion(0,  5,  ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(5,  1,  IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(6,  fDocument.getLength() - 12,  ICPartitions.C_STRING),
				new TypedRegion(fDocument.getLength() - 6, 1,  IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(fDocument.getLength() - 5, 5,  ICPartitions.C_STRING),
			};
			checkPartitioning(expectation, result);

			// insert opening delimiter
			fDocument.replace(7, 0, "***");
			result= fDocument.computePartitioning(0, fDocument.getLength());
			expectation= new TypedRegion[] {
				new TypedRegion(0,   5,  ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(5,  1,  IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(6,  fDocument.getLength() - 6,  ICPartitions.C_STRING)
			};
			checkPartitioning(expectation, result);

			// insert closing delimiter
			fDocument.replace(fDocument.getLength() - 7, 0, "***");
			result= fDocument.computePartitioning(0, fDocument.getLength());
			expectation= new TypedRegion[] {
				new TypedRegion(0,  5,  ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(5,  1,  IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(6,  fDocument.getLength() - 12,  ICPartitions.C_STRING),
				new TypedRegion(fDocument.getLength() - 6, 1,  IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(fDocument.getLength() - 5, 5,  ICPartitions.C_STRING),
			};
			checkPartitioning(expectation, result);

			// invalidate closing delimiter
			fDocument.replace(fDocument.getLength() - 7, 1, "");
			result= fDocument.computePartitioning(0, fDocument.getLength());
			expectation= new TypedRegion[] {
				new TypedRegion(0,  5,  ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(5,  1,  IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(6,  fDocument.getLength() - 6,  ICPartitions.C_STRING),
			};
			checkPartitioning(expectation, result);

		} catch (BadLocationException x) {
			assertTrue(false);
		}
	}

	public void testEditingRawString3() {
		try {

			fDocument.replace(0, fDocument.getLength(), "/***/R\"(line 1\nline 2\nline 3\n)\" \"str\"");
			ITypedRegion[] result= fDocument.computePartitioning(0, fDocument.getLength());
			TypedRegion[] expectation= {
				new TypedRegion(0,  5,  ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(5,  1,  IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(6,  fDocument.getLength() - 12,  ICPartitions.C_STRING),
				new TypedRegion(fDocument.getLength() - 6, 1,  IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(fDocument.getLength() - 5, 5,  ICPartitions.C_STRING),
			};
			checkPartitioning(expectation, result);

			// insert text after closing quote
			fDocument.replace(fDocument.getLength() - 6, 0, " ");
			result= fDocument.computePartitioning(0, fDocument.getLength());
			expectation= new TypedRegion[] {
				new TypedRegion(0,  5,  ICPartitions.C_MULTI_LINE_COMMENT),
				new TypedRegion(5,  1,  IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(6,  fDocument.getLength() - 13,  ICPartitions.C_STRING),
				new TypedRegion(fDocument.getLength() - 7, 2,  IDocument.DEFAULT_CONTENT_TYPE),
				new TypedRegion(fDocument.getLength() - 5, 5,  ICPartitions.C_STRING),
			};
			checkPartitioning(expectation, result);

		} catch (BadLocationException x) {
			assertTrue(false);
		}
	}
}
