/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *     Anton Leherbauer (Wind River Systems)
 *     Andrew Ferguson (Symbian)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.compare;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.compare.IEncodedStreamContentAccessor;
import org.eclipse.compare.ISharedDocumentAdapter;
import org.eclipse.compare.IStreamContentAccessor;
import org.eclipse.compare.ResourceNode;
import org.eclipse.compare.contentmergeviewer.IDocumentRange;
import org.eclipse.compare.structuremergeviewer.DocumentRangeNode;
import org.eclipse.compare.structuremergeviewer.IStructureComparator;
import org.eclipse.compare.structuremergeviewer.StructureCreator;
import org.eclipse.compare.structuremergeviewer.StructureRootNode;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.Position;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.FileContent;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IncludeFileContentProvider;
import org.eclipse.cdt.core.parser.ParserUtil;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.text.ICPartitions;
import org.eclipse.cdt.ui.text.doctools.IDocCommentOwner;

import org.eclipse.cdt.internal.ui.text.doctools.DocCommentOwnerManager;

/**
 * A structure creator for C/C++ translation units.
 */
public class CStructureCreator extends StructureCreator {

	private static final String NAME = "CStructureCreator.name"; //$NON-NLS-1$

	public CStructureCreator() {
	}

	@Override
	public String getName() {
		return CUIPlugin.getResourceString(NAME);
	}

	/*
	 * @see IStructureCreator#getContents
	 */
	@Override
	public String getContents(Object node, boolean ignoreWhitespace) {
		if (node instanceof IDocumentRange) {
			IDocumentRange documentRange= (IDocumentRange)node;
			final Position range = documentRange.getRange();
			try {
				return documentRange.getDocument().get(range.getOffset(), range.getLength());
			} catch (BadLocationException exc) {
			}
		}
		if (node instanceof IStreamContentAccessor) {
			IStreamContentAccessor sca = (IStreamContentAccessor) node;
			try {
				return readString(sca);
			} catch (CoreException ex) {
			}
		}
		return null;
	}

	/*
	 * @see org.eclipse.compare.structuremergeviewer.StructureCreator#createStructureComparator(java.lang.Object, org.eclipse.jface.text.IDocument, org.eclipse.compare.ISharedDocumentAdapter, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected IStructureComparator createStructureComparator(Object element,
			IDocument document, ISharedDocumentAdapter sharedDocumentAdapter,
			IProgressMonitor monitor) throws CoreException {

		DocumentRangeNode root= new StructureRootNode(document, element, this, sharedDocumentAdapter);

		// don't follow inclusions
		IncludeFileContentProvider contentProvider = IncludeFileContentProvider.getEmptyFilesProvider();
		
		// empty scanner info
		IScannerInfo scanInfo= new ScannerInfo();
		
		FileContent content = FileContent.create("<text>", document.get().toCharArray()); //$NON-NLS-1$
		
		// determine the language
		boolean isSource[]= {false};
		ILanguage language= determineLanguage(element, isSource);
		
		try {
			IASTTranslationUnit ast;
			int options= isSource[0] ? ILanguage.OPTION_IS_SOURCE_UNIT : 0;
			ast= language.getASTTranslationUnit(content, scanInfo, contentProvider, null, options, ParserUtil.getParserLogService());
			CStructureCreatorVisitor structureCreator= new CStructureCreatorVisitor(root);
			// build structure
			ast.accept(structureCreator);
		} catch (CoreException exc) {
			CUIPlugin.log(exc);
		}

		return root;
	}

	/**
	 * Try to determine the <code>ILanguage</code> for the given input element.
	 * 
	 * @param element
	 * @return a language instance
	 */
	private ILanguage determineLanguage(Object element, boolean[] isSource) {
		ILanguage language= null;
		if (element instanceof ResourceNode) {
			IResource resource= ((ResourceNode)element).getResource();
			if (resource.getType() == IResource.FILE) {
				ITranslationUnit tUnit= (ITranslationUnit)CoreModel.getDefault().create(resource);
				if (tUnit != null) {
					try {
						language= tUnit.getLanguage();
						isSource[0]= tUnit.isSourceUnit();
					} catch (CoreException exc) {
						// silently ignored
					}
				}
			}
		}
		if (language == null) {
			language= GPPLanguage.getDefault();
		}
		return language;
	}

	@Override
	protected String getDocumentPartitioning() {
		return ICPartitions.C_PARTITIONING;
	}
	
	@Override
	protected IDocumentPartitioner getDocumentPartitioner() {
		// use workspace default for highlighting doc comments in compare viewer
		IDocCommentOwner owner= DocCommentOwnerManager.getInstance().getWorkspaceCommentOwner();
		return CUIPlugin.getDefault().getTextTools().createDocumentPartitioner(owner);
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
