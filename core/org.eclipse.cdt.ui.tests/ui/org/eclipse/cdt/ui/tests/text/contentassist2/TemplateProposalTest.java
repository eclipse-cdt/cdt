/*******************************************************************************
 * Copyright (c) 2012 Marc-Andre Laperle and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *     Marc-Andre Laperle - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.text.contentassist2;

import junit.framework.Test;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.persistence.TemplatePersistenceData;
import org.eclipse.jface.text.templates.persistence.TemplateStore;

import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.corext.template.c.CContextType;


public class TemplateProposalTest extends AbstractContentAssistTest {
	
	public TemplateProposalTest(String name) {
		super(name, true);
	}
	
	public TemplateProposalTest(String name, boolean isCpp) {
		super(name, isCpp);
	}
	
	public static Test suite() {
		return BaseTestCase.suite(TemplateProposalTest.class, "_");
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		// Remove all the default templates. Tests will add templates as necessary.
		TemplateStore templateStore = CUIPlugin.getDefault().getTemplateStore();
		TemplatePersistenceData[] templateData = templateStore.getTemplateData(false);
		for (TemplatePersistenceData templatePersistenceData : templateData) {
			templateStore.delete(templatePersistenceData);
		}
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		
		// Restore the default templates
		CUIPlugin.getDefault().getTemplateStore().restoreDefaults();
	}

	private static final String HEADER_FILE_NAME = "CompletionTest.h";
	private static final String SOURCE_FILE_NAME = "CompletionTest.cpp";
	private static final String SELECTION_START_TAG = "/*sel-start*/";
	private static final String SELECTION_END_TAG = "/*sel-end*/";
	
	private static final String TEMPLATE_NAME_WORD_SELECTION = "word selection template";
	private static final String TEMPLATE_NAME_WORD_SELECTION_DISP = TEMPLATE_NAME_WORD_SELECTION + " - ";
	private static final String TEMPLATE_NAME_LINE_SELECTION = "line selection template";
	private static final String TEMPLATE_NAME_LINE_SELECTION_DISP = TEMPLATE_NAME_LINE_SELECTION + " - ";
	
	protected int fSelectionOffset;
	protected int fSelectionLength;
	private IProject fProject;
	
	@Override
	protected IFile setUpProjectContent(IProject project) throws Exception {
		fProject= project;
		StringBuilder sourceContent= getContentsForTest(1)[0];
		fSelectionOffset= sourceContent.indexOf(SELECTION_START_TAG);
		assertTrue("No selection start specified", fSelectionOffset >= 0);
		sourceContent.delete(fSelectionOffset, fSelectionOffset + SELECTION_START_TAG.length());
		int selEndOffset = sourceContent.indexOf(SELECTION_END_TAG);
		
		if (selEndOffset >= 0) {
			sourceContent.delete(selEndOffset, selEndOffset + SELECTION_END_TAG.length());
			fSelectionLength = selEndOffset - fSelectionOffset;
		} else {
			fSelectionLength = 0;
		}
		
		return createFile(project, SOURCE_FILE_NAME, sourceContent.toString());
	}
	
	private void addWordSelectionTemplate() {
		Template newTemplate = new Template(TEMPLATE_NAME_WORD_SELECTION, "", CContextType.ID, "cout << ${word_selection};", true);
		TemplatePersistenceData data= new TemplatePersistenceData(newTemplate, true);
		CUIPlugin.getDefault().getTemplateStore().add(data);
	}
	
	private void addLineSelectionTemplate() {
		Template newTemplate = new Template(TEMPLATE_NAME_LINE_SELECTION, "", CContextType.ID, "cout << ${line_selection};", true);
		TemplatePersistenceData data= new TemplatePersistenceData(newTemplate, true);
		CUIPlugin.getDefault().getTemplateStore().add(data);
	}
	
	
	
	
	//void func() { 
	///*sel-start*/test foo bar/*sel-end*/
	//}
	public void testFullLineSelection() throws Exception {
		addLineSelectionTemplate();
		addWordSelectionTemplate();
		final String[] expected= {
			TEMPLATE_NAME_LINE_SELECTION_DISP
		};
		assertContentAssistResults(fSelectionOffset, fSelectionLength, expected, true, true, AbstractContentAssistTest.COMPARE_ID_STRINGS);
	}
	
	//void func() { 
	///*sel-start*/test foo bar
	//test foo bar/*sel-end*/
	//}
	public void testMultiLineSelection() throws Exception {
		addLineSelectionTemplate();
		addWordSelectionTemplate();
		final String[] expected= {
			TEMPLATE_NAME_LINE_SELECTION_DISP
		};
		assertContentAssistResults(fSelectionOffset, fSelectionLength, expected, true, true, AbstractContentAssistTest.COMPARE_ID_STRINGS);
	}
	
	//void func() { 
	//foo /*sel-start*/test/*sel-end*/
	//}
	public void testWordSelection() throws Exception {
		addLineSelectionTemplate();
		addWordSelectionTemplate();
		final String[] expected= {
			TEMPLATE_NAME_WORD_SELECTION_DISP
		};
		assertContentAssistResults(fSelectionOffset, fSelectionLength, expected, true, true, AbstractContentAssistTest.COMPARE_ID_STRINGS);
	}
	
	//void func() { 
	//foo/*sel-start*/test/*sel-end*/
	//}
	public void testPartialLineWordSelection() throws Exception {
		addLineSelectionTemplate();
		addWordSelectionTemplate();
		final String[] expected= {
			TEMPLATE_NAME_WORD_SELECTION_DISP
		};
		assertContentAssistResults(fSelectionOffset, fSelectionLength, expected, true, true, AbstractContentAssistTest.COMPARE_ID_STRINGS);
	}
	
	//void func() { 
	//test foo/*sel-start*/bar
	//test foo /*sel-end*/bar
	//}
	public void testWordSelectionOverMultiLine() throws Exception {
		addLineSelectionTemplate();
		addWordSelectionTemplate();
		final String[] expected= {
			TEMPLATE_NAME_WORD_SELECTION_DISP
		};
		assertContentAssistResults(fSelectionOffset, fSelectionLength, expected, true, true, AbstractContentAssistTest.COMPARE_ID_STRINGS);
	}
	
	//void func() { 
	//  /*sel-start*/test/*sel-end*/
	//}
	public void testBug298554_lineSelectedWithoutWhitespaces() throws Exception {
		addLineSelectionTemplate();
		final String[] expected= {
			TEMPLATE_NAME_LINE_SELECTION_DISP
		};
		assertContentAssistResults(fSelectionOffset, fSelectionLength, expected, true, true, AbstractContentAssistTest.COMPARE_ID_STRINGS);
	}
	
	//void func() { 
	//  /*sel-start*/test foo bar
	//test foo bar/*sel-end*/  
	//}
	public void testBug298554_multiLineSelectedWithoutWhitespaces() throws Exception {
		addWordSelectionTemplate();
		addLineSelectionTemplate();
		final String[] expected= {
			TEMPLATE_NAME_LINE_SELECTION_DISP
		};
		assertContentAssistResults(fSelectionOffset, fSelectionLength, expected, true, true, AbstractContentAssistTest.COMPARE_ID_STRINGS);
	}
	
	//void func() { 
	//  /*sel-start*/test/*sel-end*/
	//}
	public void testBug304482_onlyWordOnLine() throws Exception {
		addLineSelectionTemplate();
		addWordSelectionTemplate();
		final String[] expected= {
			TEMPLATE_NAME_LINE_SELECTION_DISP,
			TEMPLATE_NAME_WORD_SELECTION_DISP
		};
		assertContentAssistResults(fSelectionOffset, fSelectionLength, expected, true, true, AbstractContentAssistTest.COMPARE_ID_STRINGS);
	}
	
	//void func() { 
	///*sel-start*/test/*sel-end*/
	//}
	public void testBug304482_onlyWordOnLineStartOfLine() throws Exception {
		addLineSelectionTemplate();
		addWordSelectionTemplate();
		final String[] expected= {
			TEMPLATE_NAME_LINE_SELECTION_DISP,
			TEMPLATE_NAME_WORD_SELECTION_DISP
		};
		assertContentAssistResults(fSelectionOffset, fSelectionLength, expected, true, true, AbstractContentAssistTest.COMPARE_ID_STRINGS);
	}

}
