/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.preferences.formatter;
 
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import org.eclipse.cdt.core.formatter.DefaultCodeFormatterConstants;

public class BracesTabPage extends FormatterTabPage {
	
	/**
	 * Some C++ source code used for preview.
	 */
	private final static String PREVIEW=
		createPreviewHeader(FormatterMessages.BracesTabPage_preview_header) + 
		"#include <math.h>\n\n" + //$NON-NLS-1$
		"int digits[]= { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };" + //$NON-NLS-1$
		"\n\n" +  //$NON-NLS-1$
		"class Point {" +  //$NON-NLS-1$
		"public:" +  //$NON-NLS-1$
		"Point(double x, double y) : x(x), y(y) {}" + //$NON-NLS-1$ 
		"double distance(const Point& other) const;" + //$NON-NLS-1$
		"int compareX(const Point& other) const;" + //$NON-NLS-1$
		"double x;" +  //$NON-NLS-1$
		"double y;" +  //$NON-NLS-1$
		"};" +  //$NON-NLS-1$
		"\n\n" + //$NON-NLS-1$
		"double Point::distance(const Point& other) const {" + //$NON-NLS-1$
		"double dx = x - other.x;" + //$NON-NLS-1$
		"double dy = y - other.y;" + //$NON-NLS-1$
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
	
	private final String[] fBracePositions= {
	    DefaultCodeFormatterConstants.END_OF_LINE,
	    DefaultCodeFormatterConstants.NEXT_LINE,
	    DefaultCodeFormatterConstants.NEXT_LINE_SHIFTED
	};
	
	private final String[] fExtendedBracePositions= {
		DefaultCodeFormatterConstants.END_OF_LINE,
	    DefaultCodeFormatterConstants.NEXT_LINE,
	    DefaultCodeFormatterConstants.NEXT_LINE_SHIFTED, 
		DefaultCodeFormatterConstants.NEXT_LINE_ON_WRAP
	};
	
	private final String[] fBracePositionNames= {
	    FormatterMessages.BracesTabPage_position_same_line, 
	    FormatterMessages.BracesTabPage_position_next_line, 
	    FormatterMessages.BracesTabPage_position_next_line_indented
	};
	
	private final String[] fExtendedBracePositionNames= {
	    FormatterMessages.BracesTabPage_position_same_line, 
	    FormatterMessages.BracesTabPage_position_next_line, 
	    FormatterMessages.BracesTabPage_position_next_line_indented, 
		FormatterMessages.BracesTabPage_position_next_line_on_wrap
	};
	
	/**
	 * Create a new BracesTabPage.
	 * @param modifyDialog
	 * @param workingValues
	 */
	public BracesTabPage(ModifyDialog modifyDialog, Map<String, String> workingValues) {
		super(modifyDialog, workingValues);
	}
	
	@Override
	protected void doCreatePreferences(Composite composite, int numColumns) {
		final Group group= createGroup(numColumns, composite, FormatterMessages.BracesTabPage_group_brace_positions_title); 
		createExtendedBracesCombo(group, numColumns, FormatterMessages.BracesTabPage_option_class_declaration, DefaultCodeFormatterConstants.FORMATTER_BRACE_POSITION_FOR_TYPE_DECLARATION); 
		createExtendedBracesCombo(group, numColumns, FormatterMessages.BracesTabPage_option_namespace_declaration, DefaultCodeFormatterConstants.FORMATTER_BRACE_POSITION_FOR_NAMESPACE_DECLARATION); 
//		createExtendedBracesCombo(group, numColumns, FormatterMessages.BracesTabPage_option_constructor_declaration, DefaultCodeFormatterConstants.FORMATTER_BRACE_POSITION_FOR_CONSTRUCTOR_DECLARATION); 
		createExtendedBracesCombo(group, numColumns, FormatterMessages.BracesTabPage_option_function_declaration, DefaultCodeFormatterConstants.FORMATTER_BRACE_POSITION_FOR_METHOD_DECLARATION); 
//		createExtendedBracesCombo(group, numColumns, FormatterMessages.BracesTabPage_option_enum_declaration, DefaultCodeFormatterConstants.FORMATTER_BRACE_POSITION_FOR_ENUM_DECLARATION); 
//		createExtendedBracesCombo(group, numColumns, FormatterMessages.BracesTabPage_option_enumconst_declaration, DefaultCodeFormatterConstants.FORMATTER_BRACE_POSITION_FOR_ENUM_CONSTANT); 
//		createExtendedBracesCombo(group, numColumns, FormatterMessages.BracesTabPage_option_annotation_type_declaration, DefaultCodeFormatterConstants.FORMATTER_BRACE_POSITION_FOR_ANNOTATION_TYPE_DECLARATION); 
		createExtendedBracesCombo(group, numColumns, FormatterMessages.BracesTabPage_option_blocks, DefaultCodeFormatterConstants.FORMATTER_BRACE_POSITION_FOR_BLOCK); 
		createExtendedBracesCombo(group, numColumns, FormatterMessages.BracesTabPage_option_blocks_in_case, DefaultCodeFormatterConstants.FORMATTER_BRACE_POSITION_FOR_BLOCK_IN_CASE); 
		createBracesCombo(group, numColumns, FormatterMessages.BracesTabPage_option_switch_case, DefaultCodeFormatterConstants.FORMATTER_BRACE_POSITION_FOR_SWITCH); 
		
		ComboPreference arrayInitOption= createBracesCombo(group, numColumns, FormatterMessages.BracesTabPage_option_initializer_list, DefaultCodeFormatterConstants.FORMATTER_BRACE_POSITION_FOR_INITIALIZER_LIST); 
		final CheckboxPreference arrayInitCheckBox= createIndentedCheckboxPref(group, numColumns, FormatterMessages.BracesTabPage_option_keep_empty_initializer_list_on_one_line, DefaultCodeFormatterConstants.FORMATTER_KEEP_EMPTY_INITIALIZER_LIST_ON_ONE_LINE, FALSE_TRUE); 

		arrayInitOption.addObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				updateOptionEnablement((ComboPreference) o, arrayInitCheckBox);
			}
		});
		updateOptionEnablement(arrayInitOption, arrayInitCheckBox);
	}
	
	/**
	 * @param arrayInitOption
	 * @param arrayInitCheckBox
	 */
	protected final void updateOptionEnablement(ComboPreference arrayInitOption, CheckboxPreference arrayInitCheckBox) {
		arrayInitCheckBox.setEnabled(!arrayInitOption.hasValue(DefaultCodeFormatterConstants.END_OF_LINE));
	}

	@Override
	protected void initializePage() {
	    fPreview.setPreviewText(PREVIEW);
	}
	
	@Override
	protected CPreview doCreateCPreview(Composite parent) {
	    fPreview= new TranslationUnitPreview(fWorkingValues, parent);
	    return fPreview;
	}
	
	private ComboPreference createBracesCombo(Composite composite, int numColumns, String message, String key) {
		return createComboPref(composite, numColumns, message, key, fBracePositions, fBracePositionNames);
	}
	
	private ComboPreference createExtendedBracesCombo(Composite composite, int numColumns, String message, String key) {
		return createComboPref(composite, numColumns, message, key, fExtendedBracePositions, fExtendedBracePositionNames);
	}
	
	private CheckboxPreference createIndentedCheckboxPref(Composite composite, int numColumns, String message, String key, String[] values) {
		CheckboxPreference pref= createCheckboxPref(composite, numColumns, message, key, values);
		GridData data= (GridData) pref.getControl().getLayoutData();
		data.horizontalIndent= fPixelConverter.convertWidthInCharsToPixels(1);
		return pref;
	}

    @Override
	protected void doUpdatePreview() {
        fPreview.update();
    }
}
