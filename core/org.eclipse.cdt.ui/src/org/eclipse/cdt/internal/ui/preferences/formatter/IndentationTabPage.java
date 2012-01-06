/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     istvan@benedek-home.de - 103706 [formatter] indent empty lines
 *     Sergey Prigogin (Google)
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.preferences.formatter;

import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.formatter.DefaultCodeFormatterConstants;

public class IndentationTabPage extends FormatterTabPage {
	
	/**
	 * Some C++ source code used for preview.
	 */
	private final static String PREVIEW=
		createPreviewHeader(FormatterMessages.IndentationTabPage_preview_header) + 
		"#include <math.h>\n\n" + //$NON-NLS-1$
		"class Point {" +  //$NON-NLS-1$
		"public:" +  //$NON-NLS-1$
		"Point(double x, double y) : x(x), y(y) {}" + //$NON-NLS-1$ 
		"\n\n" +  //$NON-NLS-1$
		"double distance(const Point& other) const;" + //$NON-NLS-1$
		"int compareX(const Point& other) const;" + //$NON-NLS-1$
		"double x;" +  //$NON-NLS-1$
		"double y;" +  //$NON-NLS-1$
		"};" + //$NON-NLS-1$
		"\n\n" +  //$NON-NLS-1$
		"double Point::distance(const Point& other) const {" + //$NON-NLS-1$
		"double dx = x - other.x;" + //$NON-NLS-1$
		"double dy = y - other.y;" + //$NON-NLS-1$
		"\n\n" +  //$NON-NLS-1$
		"return sqrt(dx * dx + dy * dy);" + //$NON-NLS-1$
		"}"+ //$NON-NLS-1$
		"\n\n" +  //$NON-NLS-1$
		"int Point::compareX(const Point& other) const {" + //$NON-NLS-1$
		"if(x < other.x) {" + //$NON-NLS-1$
		"return -1;" + //$NON-NLS-1$
		"} else if(x > other.x){" + //$NON-NLS-1$
		"return 1;" + //$NON-NLS-1$
		"} else {" + //$NON-NLS-1$
		"return 0;" + //$NON-NLS-1$
		"}"+ //$NON-NLS-1$
		"}"+ //$NON-NLS-1$
		"\n\n" +  //$NON-NLS-1$
		"namespace FOO {"+ //$NON-NLS-1$
		"int foo(int bar) const {" + //$NON-NLS-1$
		"switch(bar) {" + //$NON-NLS-1$
		"case 0:" + //$NON-NLS-1$
		"++bar;" + //$NON-NLS-1$
		"break;" + //$NON-NLS-1$
		"case 1:" + //$NON-NLS-1$
		"--bar;" + //$NON-NLS-1$
		"default: {" + //$NON-NLS-1$
		"bar += bar;" + //$NON-NLS-1$
		"break;" + //$NON-NLS-1$
		"}"+ //$NON-NLS-1$
		"}"+ //$NON-NLS-1$
		"}"+ //$NON-NLS-1$
		"} // end namespace FOO"; //$NON-NLS-1$

	private TranslationUnitPreview fPreview;
	private String fOldTabChar= null;
	
	public IndentationTabPage(ModifyDialog modifyDialog, Map<String,String> workingValues) {
		super(modifyDialog, workingValues);
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.preferences.formatter.ModifyDialogTabPage#doCreatePreferences(org.eclipse.swt.widgets.Composite, int)
	 */
	@Override
	protected void doCreatePreferences(Composite composite, int numColumns) {

		final Group generalGroup= createGroup(numColumns, composite, FormatterMessages.IndentationTabPage_general_group_title); 
		
		final String[] tabPolicyValues= new String[] {CCorePlugin.SPACE, CCorePlugin.TAB, DefaultCodeFormatterConstants.MIXED};
		final String[] tabPolicyLabels= new String[] {
				FormatterMessages.IndentationTabPage_general_group_option_tab_policy_SPACE, 
				FormatterMessages.IndentationTabPage_general_group_option_tab_policy_TAB, 
				FormatterMessages.IndentationTabPage_general_group_option_tab_policy_MIXED
		};
		final ComboPreference tabPolicy= createComboPref(generalGroup, numColumns, FormatterMessages.IndentationTabPage_general_group_option_tab_policy, DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, tabPolicyValues, tabPolicyLabels);
		final CheckboxPreference onlyForLeading= createCheckboxPref(generalGroup, numColumns, FormatterMessages.IndentationTabPage_use_tabs_only_for_leading_indentations, DefaultCodeFormatterConstants.FORMATTER_USE_TABS_ONLY_FOR_LEADING_INDENTATIONS, FALSE_TRUE);
		final NumberPreference indentSize= createNumberPref(generalGroup, numColumns, FormatterMessages.IndentationTabPage_general_group_option_indent_size, DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, 0, 32); 
		final NumberPreference tabSize= createNumberPref(generalGroup, numColumns, FormatterMessages.IndentationTabPage_general_group_option_tab_size, DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, 0, 32);
		
		String tabchar= fWorkingValues.get(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR);
		updateTabPreferences(tabchar, tabSize, indentSize, onlyForLeading);
		tabPolicy.addObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				updateTabPreferences((String) arg, tabSize, indentSize, onlyForLeading);
			}
		});
		tabSize.addObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				indentSize.updateWidget();
			}
		});
		
//		final Group typeMemberGroup= createGroup(numColumns, composite, FormatterMessages.IndentationTabPage_field_alignment_group_title); 
//		createCheckboxPref(typeMemberGroup, numColumns, FormatterMessages.IndentationTabPage_field_alignment_group_align_fields_in_columns, DefaultCodeFormatterConstants.FORMATTER_ALIGN_TYPE_MEMBERS_ON_COLUMNS, FALSE_TRUE); 
		
		final Group classGroup = createGroup(numColumns, composite, FormatterMessages.IndentationTabPage_indent_group_title); 
		createCheckboxPref(classGroup, numColumns, FormatterMessages.IndentationTabPage_class_group_option_indent_access_specifiers_within_class_body, DefaultCodeFormatterConstants.FORMATTER_INDENT_ACCESS_SPECIFIER_COMPARE_TO_TYPE_HEADER, FALSE_TRUE); 
		createCheckboxPref(classGroup, numColumns, FormatterMessages.IndentationTabPage_class_group_option_indent_declarations_compare_to_access_specifiers, DefaultCodeFormatterConstants.FORMATTER_INDENT_BODY_DECLARATIONS_COMPARE_TO_ACCESS_SPECIFIER, FALSE_TRUE); 

		createCheckboxPref(classGroup, numColumns, FormatterMessages.IndentationTabPage_block_group_option_indent_statements_compare_to_body, DefaultCodeFormatterConstants.FORMATTER_INDENT_STATEMENTS_COMPARE_TO_BODY, FALSE_TRUE); 
		createCheckboxPref(classGroup, numColumns, FormatterMessages.IndentationTabPage_block_group_option_indent_statements_compare_to_block, DefaultCodeFormatterConstants.FORMATTER_INDENT_STATEMENTS_COMPARE_TO_BLOCK, FALSE_TRUE); 

		createCheckboxPref(classGroup, numColumns, FormatterMessages.IndentationTabPage_switch_group_option_indent_statements_within_switch_body, DefaultCodeFormatterConstants.FORMATTER_INDENT_SWITCHSTATEMENTS_COMPARE_TO_SWITCH, FALSE_TRUE); 
		createCheckboxPref(classGroup, numColumns, FormatterMessages.IndentationTabPage_switch_group_option_indent_statements_within_case_body, DefaultCodeFormatterConstants.FORMATTER_INDENT_SWITCHSTATEMENTS_COMPARE_TO_CASES, FALSE_TRUE); 
		createCheckboxPref(classGroup, numColumns, FormatterMessages.IndentationTabPage_switch_group_option_indent_break_statements, DefaultCodeFormatterConstants.FORMATTER_INDENT_BREAKS_COMPARE_TO_CASES, FALSE_TRUE); 

		createCheckboxPref(classGroup, numColumns, FormatterMessages.IndentationTabPage_namespace_group_option_indent_declarations_within_namespace, DefaultCodeFormatterConstants.FORMATTER_INDENT_BODY_DECLARATIONS_COMPARE_TO_NAMESPACE_HEADER, FALSE_TRUE); 

		createCheckboxPref(classGroup, numColumns, FormatterMessages.IndentationTabPage_indent_empty_lines, DefaultCodeFormatterConstants.FORMATTER_INDENT_EMPTY_LINES, FALSE_TRUE); 
	}
	
	/*
	 * @see org.eclipse.cdt.internal.ui.preferences.formatter.ModifyDialogTabPage#initializePage()
	 */
	@Override
	public void initializePage() {
	    fPreview.setPreviewText(PREVIEW);
	}

    /*
     * @see org.eclipse.cdt.internal.ui.preferences.formatter.ModifyDialogTabPage#doCreateCPreview(org.eclipse.swt.widgets.Composite)
     */
    @Override
	protected CPreview doCreateCPreview(Composite parent) {
        fPreview= new TranslationUnitPreview(fWorkingValues, parent);
        return fPreview;
    }

    /*
     * @see org.eclipse.cdt.internal.ui.preferences.formatter.ModifyDialogTabPage#doUpdatePreview()
     */
    @Override
	protected void doUpdatePreview() {
        fPreview.update();
    }

	private void updateTabPreferences(String tabPolicy, NumberPreference tabPreference, NumberPreference indentPreference, CheckboxPreference onlyForLeading) {
		/*
		 * If the tab-char is SPACE (or TAB), INDENTATION_SIZE
		 * preference is not used by the core formatter. We piggy back the
		 * visual tab length setting in that preference in that case. If the
		 * user selects MIXED, we use the previous TAB_SIZE preference as the
		 * new INDENTATION_SIZE (as this is what it really is) and set the 
		 * visual tab size to the value piggy backed in the INDENTATION_SIZE
		 * preference. See also CodeFormatterUtil. 
		 */
		if (DefaultCodeFormatterConstants.MIXED.equals(tabPolicy)) {
			if (CCorePlugin.SPACE.equals(fOldTabChar) || CCorePlugin.TAB.equals(fOldTabChar))
				swapTabValues();
			tabPreference.setEnabled(true);
			tabPreference.setKey(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE);
			indentPreference.setEnabled(true);
			indentPreference.setKey(DefaultCodeFormatterConstants.FORMATTER_INDENTATION_SIZE);
			onlyForLeading.setEnabled(true);
		} else if (CCorePlugin.SPACE.equals(tabPolicy)) {
			if (DefaultCodeFormatterConstants.MIXED.equals(fOldTabChar))
				swapTabValues();
			tabPreference.setEnabled(true);
			tabPreference.setKey(DefaultCodeFormatterConstants.FORMATTER_INDENTATION_SIZE);
			indentPreference.setEnabled(true);
			indentPreference.setKey(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE);
			onlyForLeading.setEnabled(false);
		} else if (CCorePlugin.TAB.equals(tabPolicy)) {
			if (DefaultCodeFormatterConstants.MIXED.equals(fOldTabChar))
				swapTabValues();
			tabPreference.setEnabled(true);
			tabPreference.setKey(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE);
			indentPreference.setEnabled(false);
			indentPreference.setKey(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE);
			onlyForLeading.setEnabled(true);
		} else {
			Assert.isTrue(false);
		}
		fOldTabChar= tabPolicy;
	}

	private void swapTabValues() {
		String tabSize= fWorkingValues.get(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE);
		String indentSize= fWorkingValues.get(DefaultCodeFormatterConstants.FORMATTER_INDENTATION_SIZE);
		fWorkingValues.put(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, indentSize);
		fWorkingValues.put(DefaultCodeFormatterConstants.FORMATTER_INDENTATION_SIZE, tabSize);
	}
}
