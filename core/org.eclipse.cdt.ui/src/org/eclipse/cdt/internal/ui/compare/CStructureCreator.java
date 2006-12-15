/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.compare;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.compare.CompareUI;
import org.eclipse.compare.IEditableContent;
import org.eclipse.compare.IEncodedStreamContentAccessor;
import org.eclipse.compare.IStreamContentAccessor;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.compare.structuremergeviewer.IDiffContainer;
import org.eclipse.compare.structuremergeviewer.IStructureComparator;
import org.eclipse.compare.structuremergeviewer.IStructureCreator;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IParser;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.NullLogService;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ParserUtil;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.editor.CDocumentSetupParticipant;
/**
 * 
 */
public class CStructureCreator implements IStructureCreator {

	private static final String NAME = "CStructureCreator.name"; //$NON-NLS-1$

	public CStructureCreator() {
	}

	public String getName() {
		return CUIPlugin.getResourceString(NAME);
	}

	public IStructureComparator getStructure(Object input) {

		IDocument doc= CompareUI.getDocument(input);
		if (doc == null) {
			if (input instanceof IStreamContentAccessor) {
				String s = null;
				try {
					s = readString((IStreamContentAccessor) input);
				} catch (CoreException ex) {
				}
				if (s != null) {
					doc = new Document(s);
					new CDocumentSetupParticipant().setup(doc);
				}
			}
		}
		if (doc == null) {
			return null;
		}
		CNode root = new CNode(null, ICElement.C_UNIT, "root", doc, 0, 0); //$NON-NLS-1$

		ISourceElementRequestor builder = new CParseTreeBuilder(root, doc);
		try {
			//Using the CPP parser (was implicit before, now its explicit).  If there 
			//are bugs while parsing C files, we might want to create a separate Structure
			//compare for c files, but we'll never be completely right about .h files
			IScanner scanner =
				ParserFactory.createScanner(new CodeReader(doc.get().toCharArray()), new ScannerInfo(), ParserMode.QUICK_PARSE, ParserLanguage.CPP, builder, new NullLogService(), null); 
			IParser parser = ParserFactory.createParser(scanner, builder, ParserMode.QUICK_PARSE, ParserLanguage.CPP, ParserUtil.getParserLogService() );
			parser.parse();
		} catch (Exception e) {
			// What to do when error ?
			// The CParseTreeBuilder will throw CParseTreeBuilder.ParseError
			// for acceptProblem.
			
			//TODO : New : ParserFactoryError gets thrown by ParserFactory primitives
		}

		return root;
	}

	public boolean canSave() {
		return true;
	}

	public IStructureComparator locate(Object path, Object source) {
		return null;
	}

	public boolean canRewriteTree() {
		return false;
	}

	public void rewriteTree(Differencer differencer, IDiffContainer root) {
	}

	/**
	 * @see IStructureCreator#save
	 */
	public void save(IStructureComparator structure, Object input) {
		if (input instanceof IEditableContent && structure instanceof CNode) {
			IDocument doc = ((CNode) structure).getDocument();
			IEditableContent bca = (IEditableContent) input;
			String c = doc.get();
			bca.setContent(c.getBytes());
		}
	}

	/**
	 * @see IStructureCreator#getContents
	 */
	public String getContents(Object node, boolean ignoreWhitespace) {
		if (node instanceof IStreamContentAccessor) {
			IStreamContentAccessor sca = (IStreamContentAccessor) node;
			try {
				return readString(sca);
			} catch (CoreException ex) {
			}
		}
		return null;
	}

	private static String readString(IStreamContentAccessor sa) throws CoreException {
		InputStream is= sa.getContents();
		if (is != null) {
			String encoding= null;
			if (sa instanceof IEncodedStreamContentAccessor) {
				try {
					encoding= ((IEncodedStreamContentAccessor) sa).getCharset();
				} catch (Exception e) {
				}
			}
			if (encoding == null)
				encoding= ResourcesPlugin.getEncoding();
			return readString(is, encoding);
		}
		return null;
	}

	private static String readString(InputStream is, String encoding) {
		if (is == null)
			return null;
		BufferedReader reader= null;
		try {
			StringBuffer buffer= new StringBuffer();
			char[] part= new char[2048];
			int read= 0;
			reader= new BufferedReader(new InputStreamReader(is, encoding));

			while ((read= reader.read(part)) != -1)
				buffer.append(part, 0, read);
			
			return buffer.toString();
			
		} catch (IOException ex) {
			// NeedWork
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException ex) {
					// silently ignored
				}
			}
		}
		return null;
	}
}
