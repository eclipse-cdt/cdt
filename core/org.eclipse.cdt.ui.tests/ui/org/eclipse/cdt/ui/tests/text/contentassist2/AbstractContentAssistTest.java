/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
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
 *     Thomas Corbat (IFS)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.text.contentassist2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.templates.TemplateProposal;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.testplugin.CTestPlugin;
import org.eclipse.cdt.ui.testplugin.EditorTestHelper;
import org.eclipse.cdt.ui.tests.BaseUITestCase;
import org.eclipse.cdt.ui.text.ICCompletionProposal;
import org.eclipse.cdt.ui.text.ICPartitions;
import org.eclipse.cdt.ui.text.contentassist.ContentAssistInvocationContext;

import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTNameBase;

import org.eclipse.cdt.internal.ui.text.contentassist.CCompletionProposal;
import org.eclipse.cdt.internal.ui.text.contentassist.CContentAssistProcessor;
import org.eclipse.cdt.internal.ui.text.contentassist.ContentAssistPreference;
import org.eclipse.cdt.internal.ui.text.contentassist.RelevanceConstants;

public abstract class AbstractContentAssistTest extends BaseUITestCase {
	public static enum CompareType {
		ID, DISPLAY, REPLACEMENT, CONTEXT, INFORMATION
	}

	protected ICProject fCProject;
	private IFile fCFile;
	protected ITextEditor fEditor;
	private final boolean fIsCpp;
	protected boolean fProcessorNeedsConfiguring;

	public AbstractContentAssistTest(String name, boolean isCpp) {
		super(name);
		fIsCpp= isCpp;
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (fIsCpp) {
			fCProject= CProjectHelper.createCCProject(getName(), "unused", IPDOMManager.ID_FAST_INDEXER);
		} else {
			fCProject= CProjectHelper.createCProject(getName(), "unused", IPDOMManager.ID_FAST_INDEXER);
		}
		fCFile= setUpProjectContent(fCProject.getProject());
		assertNotNull(fCFile);
		waitForIndexer(fCProject);
		fEditor= (ITextEditor) EditorTestHelper.openInEditor(fCFile, true);
		assertNotNull(fEditor);
		CPPASTNameBase.sAllowNameComputation= true;

//		EditorTestHelper.joinBackgroundActivities((AbstractTextEditor) fEditor);
	}

	/**
	 * Setup the project's content.
	 * @param project
	 * @return  the file to be opened in the editor
	 */
	protected abstract IFile setUpProjectContent(IProject project) throws Exception;

	@Override
	protected void tearDown() throws Exception {
		ContentAssistInvocationContext.assertNoUndisposedContexts();
		EditorTestHelper.closeEditor(fEditor);
		fEditor= null;
		CProjectHelper.delete(fCProject);
		fCProject= null;
		fCFile= null;
		super.tearDown();
	}

	protected static IPreferenceStore getPreferenceStore() {
		return CUIPlugin.getDefault().getPreferenceStore();
	}

	protected void assertContentAssistResults(int offset, int length, String[] expected,
			boolean isCompletion, boolean isTemplate, boolean filterResults, CompareType compareType) throws Exception {
		if (CTestPlugin.getDefault().isDebugging())  {
			System.out.println("\n\n\n\n\nTesting " + this.getClass().getName());
		}

		// Call the CContentAssistProcessor
		ISourceViewer sourceViewer= EditorTestHelper.getSourceViewer((AbstractTextEditor)fEditor);
		String contentType= TextUtilities.getContentType(sourceViewer.getDocument(), ICPartitions.C_PARTITIONING, offset, true);
		boolean isCode= IDocument.DEFAULT_CONTENT_TYPE.equals(contentType);
		ContentAssistant assistant = new ContentAssistant();
		CContentAssistProcessor processor = new CContentAssistProcessor(fEditor, assistant, contentType);
		assistant.setContentAssistProcessor(processor, contentType);
		if (fProcessorNeedsConfiguring) {
			ContentAssistPreference.configure(assistant, getPreferenceStore());
		}
		long startTime= System.currentTimeMillis();
		sourceViewer.setSelectedRange(offset, length);
		Object[] results = isCompletion ?
				(Object[]) processor.computeCompletionProposals(sourceViewer, offset) :
				(Object[]) processor.computeContextInformation(sourceViewer, offset);
		long endTime= System.currentTimeMillis();
		assertTrue(results != null);

		if (filterResults) {
			if (isTemplate) {
				results= filterResultsKeepTemplates(results);
			} else {
				results= filterResults(results, isCode);
			}
		}
		String[] resultStrings= toStringArray(results, compareType);
		Arrays.sort(expected);
		Arrays.sort(resultStrings);

		if (CTestPlugin.getDefault().isDebugging())  {
			System.out.println("Time: " + (endTime - startTime) + " ms");
			for (String proposal : resultStrings) {
				System.out.println("Result: " + proposal);
			}
		}

		boolean allFound = true;  // For the time being, let's be optimistic.

		for (String element : expected) {
			boolean found = false;
			for (String proposal : resultStrings) {
				if(element.equals(proposal)){
					found = true;
					if (CTestPlugin.getDefault().isDebugging())  {
						System.out.println("Lookup success for " + element);
					}
					break;
				}
			}
			if (!found)  {
				allFound = false;
				if (CTestPlugin.getDefault().isDebugging())  {
					System.out.println( "Lookup failed for " + element); //$NON-NLS-1$
				}
			}
		}

		if (!allFound) {
			assertEquals("Missing results!", toString(expected), toString(resultStrings));
		} else if (doCheckExtraResults())  {
			assertEquals("Extra results!", toString(expected), toString(resultStrings));
		}
	}

	protected void assertContentAssistResults(int offset, int length, String[] expected, boolean isCompletion, boolean isTemplate, CompareType compareType) throws Exception {
		assertContentAssistResults(offset, length, expected, isCompletion, isTemplate, true, compareType);
	}

	protected void assertContentAssistResults(int offset, String[] expected, boolean isCompletion, CompareType compareType) throws Exception {
		assertContentAssistResults(offset, 0, expected, isCompletion, false, compareType);
	}

	/**
	 * Filter out template and keyword proposals.
	 * @param results
	 * @param isCodeCompletion  completion is in code, not preprocessor, etc.
	 * @return filtered proposals
	 */
	private Object[] filterResults(Object[] results, boolean isCodeCompletion) {
		List<Object> filtered= new ArrayList<>();
		for (Object result : results) {
			if (result instanceof TemplateProposal) {
				continue;
			}
			if (result instanceof ICCompletionProposal) {
				if (isCodeCompletion) {
					// Check for keywords proposal.
					int relevance = ((ICCompletionProposal)result).getRelevance();
					if (relevance >= RelevanceConstants.CASE_MATCH_RELEVANCE) {
						relevance -= RelevanceConstants.CASE_MATCH_RELEVANCE;
					}
					if (relevance <= RelevanceConstants.KEYWORD_TYPE_RELEVANCE) {
						continue;
					}
				}
				filtered.add(result);
			} else if (result instanceof IContextInformation) {
				filtered.add(result);
			}
		}
		return filtered.toArray();
	}
	
	/**
	 * Filter out proposals, keep only templates
	 */
	private Object[] filterResultsKeepTemplates(Object[] results) {
		List<Object> filtered= new ArrayList<>();
		for (Object result : results) {
			if (result instanceof TemplateProposal) {
				filtered.add(result);
			}
		}
		return filtered.toArray();
	}
	
	private String[] toStringArray(Object[] results, CompareType type) {
		String[] strings = new String[results.length];

		for (int i = 0; i < results.length; i++) {
			Object result = results[i];
			switch (type) {
			case ID:
				if (result instanceof ICCompletionProposal) {
					strings[i] = ((ICCompletionProposal) result).getIdString();
				}
				break;

			case DISPLAY:
				if (result instanceof ICompletionProposal) {
					strings[i] = ((ICompletionProposal) result).getDisplayString();
				}
				break;

			case REPLACEMENT:
				if (result instanceof CCompletionProposal) {
					strings[i] = ((CCompletionProposal) result).getReplacementString();
				} else if (result instanceof ICCompletionProposal) {
					strings[i] = ((ICCompletionProposal) result).getDisplayString();
				}
				break;

			case CONTEXT:
				if (result instanceof ICompletionProposal) {
					result = ((CCompletionProposal) result).getContextInformation();
				}
				if (result instanceof IContextInformation) {
					strings[i] = ((IContextInformation) result).getContextDisplayString();
				}
				break;

			case INFORMATION:
				if (result instanceof IContextInformation) {
					strings[i] = ((IContextInformation) result).getInformationDisplayString();
				}
				break;
			}
		}
		return strings;
	}
	
	private String toString(String[] strings) {
		StringBuilder buf= new StringBuilder();
		for (String string : strings) {
			buf.append(string).append('\n');
		}
		return buf.toString();
	}

	/**
	 * Override to relax checking of extra results
	 */
	protected boolean doCheckExtraResults() {
		return true;
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

	protected void setCommaAfterFunctionParameter(String value) {
		fCProject.setOption(
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_METHOD_DECLARATION_PARAMETERS, value);
	}

	protected void setCommaAfterTemplateParameter(String value) {
		fCProject.setOption(
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_TEMPLATE_PARAMETERS, value);
	}
}
