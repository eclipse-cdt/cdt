/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     istvan@benedek-home.de - 103706 [formatter] indent empty lines
 *     Sergey Prigogin, Google
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.preferences.formatter;

import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import org.eclipse.jface.text.Assert;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.formatter.DefaultCodeFormatterConstants;


public class IndentationTabPage extends ModifyDialogTabPage {
	
//	private final String PREVIEW=
//	createPreviewHeader(FormatterMessages.IndentationTabPage_preview_header) + 
//	"class Example {" +	//$NON-NLS-1$
//	"  int [] myArray= {1,2,3,4,5,6};" + //$NON-NLS-1$
//	"  int theInt= 1;" + //$NON-NLS-1$
//	"  String someString= \"Hello\";" + //$NON-NLS-1$
//	"  double aDouble= 3.0;" + //$NON-NLS-1$
//	"  void foo(int a, int b, int c, int d, int e, int f) {" + //$NON-NLS-1$
//	"    switch(a) {" + //$NON-NLS-1$
//	"    case 0: " + //$NON-NLS-1$
//	"      Other.doFoo();" + //$NON-NLS-1$
//	"      break;" + //$NON-NLS-1$
//	"    default:" + //$NON-NLS-1$
//	"      Other.doBaz();" + //$NON-NLS-1$
//	"    }" + //$NON-NLS-1$
//	"  }" + //$NON-NLS-1$
//	"  void bar(List v) {" + //$NON-NLS-1$
//	"    for (int i= 0; i < 10; i++) {" + //$NON-NLS-1$
// 	"      v.add(new Integer(i));" + //$NON-NLS-1$
// 	"    }" + //$NON-NLS-1$
//	"  }" + //$NON-NLS-1$
//	"}" + //$NON-NLS-1$
//	"\n" + //$NON-NLS-1$
//	"enum MyEnum {" + //$NON-NLS-1$
//	"    UNDEFINED(0) {" + //$NON-NLS-1$
//	"        void foo() {}" + //$NON-NLS-1$
//	"    }" + //$NON-NLS-1$
//	"}" + //$NON-NLS-1$
//	"@interface MyAnnotation {" + //$NON-NLS-1$
//	"    int count() default 1;" + //$NON-NLS-1$
//	"}";//$NON-NLS-1$
	
//	private CompilationUnitPreview fPreview;
	private String fOldTabChar= null;
	
	public IndentationTabPage(ModifyDialog modifyDialog, Map workingValues) {
		super(modifyDialog, workingValues);
	}

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
		
		String tabchar= (String) fWorkingValues.get(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR);
		updateTabPreferences(tabchar, tabSize, indentSize, onlyForLeading);
		tabPolicy.addObserver(new Observer() {
			public void update(Observable o, Object arg) {
				updateTabPreferences((String) arg, tabSize, indentSize, onlyForLeading);
			}
		});
		tabSize.addObserver(new Observer() {
			public void update(Observable o, Object arg) {
				indentSize.updateWidget();
			}
		});
		
//		final Group typeMemberGroup= createGroup(numColumns, composite, FormatterMessages.IndentationTabPage_field_alignment_group_title); 
//		createCheckboxPref(typeMemberGroup, numColumns, FormatterMessages.IndentationTabPage_field_alignment_group_align_fields_in_columns, DefaultCodeFormatterConstants.FORMATTER_ALIGN_TYPE_MEMBERS_ON_COLUMNS, FALSE_TRUE); 
		
		final Group classGroup = createGroup(numColumns, composite, FormatterMessages.IndentationTabPage_indent_group_title); 
		createCheckboxPref(classGroup, numColumns, FormatterMessages.IndentationTabPage_class_group_option_indent_access_specifiers_within_class_body, DefaultCodeFormatterConstants.FORMATTER_INDENT_ACCESS_SPECIFIER_COMPARE_TO_TYPE_HEADER, FALSE_TRUE); 
		createCheckboxPref(classGroup, numColumns, FormatterMessages.IndentationTabPage_class_group_option_indent_declarations_compare_to_access_specifiers, DefaultCodeFormatterConstants.FORMATTER_INDENT_BODY_DECLARATIONS_COMPARE_TO_ACCESS_SPECIFIER, FALSE_TRUE); 
		createCheckboxPref(classGroup, numColumns, FormatterMessages.IndentationTabPage_class_group_option_indent_declarations_within_enum_decl, DefaultCodeFormatterConstants.FORMATTER_INDENT_BODY_DECLARATIONS_COMPARE_TO_ENUM_DECLARATION_HEADER, FALSE_TRUE);
		createCheckboxPref(classGroup, numColumns, FormatterMessages.IndentationTabPage_class_group_option_indent_declarations_within_enum_const, DefaultCodeFormatterConstants.FORMATTER_INDENT_BODY_DECLARATIONS_COMPARE_TO_ENUM_CONSTANT_HEADER, FALSE_TRUE); 

		createCheckboxPref(classGroup, numColumns, FormatterMessages.IndentationTabPage_block_group_option_indent_statements_compare_to_body, DefaultCodeFormatterConstants.FORMATTER_INDENT_STATEMENTS_COMPARE_TO_BODY, FALSE_TRUE); 
		createCheckboxPref(classGroup, numColumns, FormatterMessages.IndentationTabPage_block_group_option_indent_statements_compare_to_block, DefaultCodeFormatterConstants.FORMATTER_INDENT_STATEMENTS_COMPARE_TO_BLOCK, FALSE_TRUE); 

		createCheckboxPref(classGroup, numColumns, FormatterMessages.IndentationTabPage_switch_group_option_indent_statements_within_switch_body, DefaultCodeFormatterConstants.FORMATTER_INDENT_SWITCHSTATEMENTS_COMPARE_TO_SWITCH, FALSE_TRUE); 
		createCheckboxPref(classGroup, numColumns, FormatterMessages.IndentationTabPage_switch_group_option_indent_statements_within_case_body, DefaultCodeFormatterConstants.FORMATTER_INDENT_SWITCHSTATEMENTS_COMPARE_TO_CASES, FALSE_TRUE); 
		createCheckboxPref(classGroup, numColumns, FormatterMessages.IndentationTabPage_switch_group_option_indent_break_statements, DefaultCodeFormatterConstants.FORMATTER_INDENT_BREAKS_COMPARE_TO_CASES, FALSE_TRUE); 
        createCheckboxPref(classGroup, numColumns, FormatterMessages.IndentationTabPage_indent_empty_lines, DefaultCodeFormatterConstants.FORMATTER_INDENT_EMPTY_LINES, FALSE_TRUE); 
	}
	
	public void initializePage() {
//	    fPreview.setPreviewText(PREVIEW);
	}
	
    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.ui.preferences.formatter.ModifyDialogTabPage#doCreateCPreview(org.eclipse.swt.widgets.Composite)
     */
    protected CPreview doCreateCPreview(Composite parent) {
//        fPreview= new CompilationUnitPreview(fWorkingValues, parent);
//        return fPreview;
    	return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.ui.preferences.formatter.ModifyDialogTabPage#doUpdatePreview()
     */
    protected void doUpdatePreview() {
//        fPreview.update();
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
		Object tabSize= fWorkingValues.get(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE);
		Object indentSize= fWorkingValues.get(DefaultCodeFormatterConstants.FORMATTER_INDENTATION_SIZE);
		fWorkingValues.put(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, indentSize);
		fWorkingValues.put(DefaultCodeFormatterConstants.FORMATTER_INDENTATION_SIZE, tabSize);
	}
}
