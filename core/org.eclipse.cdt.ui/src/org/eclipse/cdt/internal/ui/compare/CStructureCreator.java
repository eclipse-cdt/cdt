/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.compare;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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
import org.eclipse.compare.IEditableContent;
import org.eclipse.compare.IStreamContentAccessor;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.compare.structuremergeviewer.IDiffContainer;
import org.eclipse.compare.structuremergeviewer.IStructureComparator;
import org.eclipse.compare.structuremergeviewer.IStructureCreator;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
/**
 * 
 */
public class CStructureCreator implements IStructureCreator {

	private static final String NAME = "CStructureCreator.name"; //$NON-NLS-1$

	public CStructureCreator() {
	}

	/**
	 * @see IStructureCreator#getTitle
	 */
	public String getName() {
		return CUIPlugin.getResourceString(NAME);
	}

	/**
	 * @see IStructureCreator#getStructure
	 */
	public IStructureComparator getStructure(Object input) {

		String s = null;
		if (input instanceof IStreamContentAccessor) {
			try {
				s = readString(((IStreamContentAccessor) input).getContents());
			} catch (CoreException ex) {
			}
		}

		if (s == null) {
			s = new String();
		}
		Document doc = new Document(s);

		CNode root = new CNode(null, ICElement.C_UNIT, "root", doc, 0, 0); //$NON-NLS-1$

		ISourceElementRequestor builder = new CParseTreeBuilder(root, doc);
		try {
			//Using the CPP parser (was implicit before, now its explicit).  If there 
			//are bugs while parsing C files, we might want to create a separate Structure
			//compare for c files, but we'll never be completely right about .h files
			IScanner scanner =
				ParserFactory.createScanner(new CodeReader(s.toCharArray()), new ScannerInfo(), ParserMode.QUICK_PARSE, ParserLanguage.CPP, builder, new NullLogService(), null); //$NON-NLS-1$
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

	/**
	 * @see IStructureCreator#canSave
	 */
	public boolean canSave() {
		return true;
	}

	/**
	 * @see IStructureCreator#locate
	 */
	public IStructureComparator locate(Object path, Object source) {
		return null;
	}

	/**
	 * @see IStructureCreator#canRewriteTree
	 */
	public boolean canRewriteTree() {
		return false;
	}

	/**
	 * @see IStructureCreator#rewriteTree
	 */
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
				return readString(sca.getContents());
			} catch (CoreException ex) {
			}
		}
		return null;
	}

	/**
	 * Returns null if an error occurred.
	 */
	private static String readString(InputStream is) {
		if (is == null)
			return null;
		BufferedReader reader = null;
		try {
			StringBuffer buffer = new StringBuffer();
			char[] part = new char[2048];
			int read = 0;
			reader = new BufferedReader(new InputStreamReader(is));

			while ((read = reader.read(part)) != -1)
				buffer.append(part, 0, read);

			return buffer.toString();

		} catch (IOException ex) {
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException ex) {
				}
			}
		}
		return null;
	}

}
