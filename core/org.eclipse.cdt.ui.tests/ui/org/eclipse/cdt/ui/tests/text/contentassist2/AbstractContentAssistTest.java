/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Rational Software - Initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *     Bryan Wilkinson (QNX)
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.text.contentassist2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.templates.TemplateProposal;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.parser.KeywordSetKey;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.ui.testplugin.CTestPlugin;
import org.eclipse.cdt.ui.tests.BaseUITestCase;
import org.eclipse.cdt.ui.tests.text.EditorTestHelper;
import org.eclipse.cdt.ui.text.ICCompletionProposal;

import org.eclipse.cdt.internal.ui.text.contentassist.CCompletionProposal;
import org.eclipse.cdt.internal.ui.text.contentassist.CContentAssistProcessor;

public abstract class AbstractContentAssistTest extends BaseUITestCase {

	public static final int COMPARE_ID_STRINGS = 0;
	public static final int COMPARE_DISP_STRINGS = 1;
	public static final int COMPARE_REP_STRINGS = 2;
	
	private ICProject fCProject;
	private IFile fCFile;
	protected ITextEditor fEditor;

	private final static Set fgAllKeywords= new HashSet();
	
	static {
		fgAllKeywords.addAll(ParserFactory.getKeywordSet(KeywordSetKey.KEYWORDS, ParserLanguage.C));
		fgAllKeywords.addAll(ParserFactory.getKeywordSet(KeywordSetKey.TYPES, ParserLanguage.C));
		fgAllKeywords.addAll(ParserFactory.getKeywordSet(KeywordSetKey.KEYWORDS, ParserLanguage.CPP));
		fgAllKeywords.addAll(ParserFactory.getKeywordSet(KeywordSetKey.TYPES, ParserLanguage.CPP));
	}
	public AbstractContentAssistTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		fCProject= CProjectHelper.createCCProject(getName(), "unused", IPDOMManager.ID_FAST_INDEXER);
		fCFile= setUpProjectContent(fCProject.getProject());
		assertNotNull(fCFile);
		fEditor= (ITextEditor)EditorTestHelper.openInEditor(fCFile, true);
		assertNotNull(fEditor);
		EditorTestHelper.joinBackgroundActivities((AbstractTextEditor)fEditor);
	}

	/**
	 * Setup the project's content.
	 * @param project
	 * @return  the file to be opened in the editor
	 * @throws Exception 
	 */
	protected abstract IFile setUpProjectContent(IProject project) throws Exception;

	protected void tearDown() throws Exception {
		EditorTestHelper.closeEditor(fEditor);
		fEditor= null;
		CProjectHelper.delete(fCProject);
		fCProject= null;
		fCFile= null;
		super.tearDown();
	}

	protected void assertContentAssistResults(int offset, String[] expected, boolean isCompletion, int compareType) throws Exception {

		if (CTestPlugin.getDefault().isDebugging())  {
			System.out.println("\n\n\n\n\nTesting "+this.getClass().getName());
		}

		//Call the CContentAssistProcessor
		ISourceViewer sourceViewer= EditorTestHelper.getSourceViewer((AbstractTextEditor)fEditor);
		String contentType = sourceViewer.getDocument().getContentType(offset);
		ContentAssistant assistant = new ContentAssistant();
		CContentAssistProcessor processor = new CContentAssistProcessor(fEditor, sourceViewer, assistant, contentType);
		long startTime= System.currentTimeMillis();
		Object[] results = isCompletion
			? (Object[]) processor.computeCompletionProposals(sourceViewer, offset)
			: (Object[]) processor.computeContextInformation(sourceViewer, offset);
		long endTime= System.currentTimeMillis();
		assertTrue(results != null);

		results= filterResults(results);
		String[] resultStrings= toStringArray(results, compareType);
		Arrays.sort(expected);
		Arrays.sort(resultStrings);

		if (CTestPlugin.getDefault().isDebugging())  {
			System.out.println("Time (ms): " + (endTime-startTime));
			for (int i = 0; i < resultStrings.length; i++) {
				String proposal = resultStrings[i];
				System.out.println("Result: " + proposal);
			}
		}

		boolean allFound = true ;  // for the time being, let's be optimistic

		for (int i = 0; i< expected.length; i++){
			boolean found = false;
			for(int j = 0; j< resultStrings.length; j++){
				String proposal = resultStrings[j];
				if(expected[i].equals(proposal)){
					found = true;
					if (CTestPlugin.getDefault().isDebugging())  {
						System.out.println("Lookup success for " + expected[i]);
					}
					break;
				}
			}
			if (!found)  {
				allFound = false ;
				if (CTestPlugin.getDefault().isDebugging())  {
					System.out.println( "Lookup failed for " + expected[i]); //$NON-NLS-1$
				}
			}
		}

		if (!allFound) {
			assertEquals("Missing results!", toString(expected), toString(resultStrings));
		} else if (doCheckExtraResults())  {
			assertEquals("Extra results!", toString(expected), toString(resultStrings));
		}

	}

	/**
	 * Filter out template and keyword proposals.
	 * @param results
	 * @return filtered proposals
	 */
	private Object[] filterResults(Object[] results) {
		List filtered= new ArrayList();
		for (int i = 0; i < results.length; i++) {
			Object result = results[i];
			if (result instanceof TemplateProposal) {
				continue;
			}
			if (result instanceof ICCompletionProposal) {
				// check for keywords proposal
				if (fgAllKeywords.contains(((ICCompletionProposal)result).getDisplayString())) {
					continue;
				}
			}
			filtered.add(result);
		}
		return filtered.toArray();
	}
	
	private String[] toStringArray(Object[] results, int compareType) {
		String[] strings= new String[results.length];
		for(int i=0; i< results.length; i++){
			Object result = results[i];
			if (result instanceof ICompletionProposal) {
				if (compareType == COMPARE_ID_STRINGS) {
					strings[i]= ((CCompletionProposal)result).getIdString();
				} else if (compareType == COMPARE_DISP_STRINGS) {
					strings[i]= ((CCompletionProposal)result).getDisplayString();
				} else {
					strings[i]= ((CCompletionProposal)result).getReplacementString();
				}
			} else {
				strings[i]= ((IContextInformation)result).getContextDisplayString();
			}
		}
		return strings;
	}
	
	private String toString(String[] strings) {
		StringBuffer buf= new StringBuffer();
		for(int i=0; i< strings.length; i++){
			buf.append(strings[i]).append('\n');
		}
		return buf.toString();
	}

	/**
	 * Override to relax checking of extra results
	 */
	protected boolean doCheckExtraResults() {
		return true ;
	}

	/**
	 * @return  the content of the editor buffer
	 */
	protected String getBuffer() {
		return getDocument().get();
	}
	
	/**
	 * @return  the editor document
	 */
	protected IDocument getDocument() {
		return EditorTestHelper.getDocument(fEditor);
	}

}
