/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.preferences.formatter;

import java.util.Map;

import org.eclipse.cdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

public class NewLinesTabPage extends FormatterTabPage {

	private final String PREVIEW = createPreviewHeader(FormatterMessages.NewLinesTabPage_preview_header)
			+ "class Point {" + //$NON-NLS-1$
			"public:" + //$NON-NLS-1$
			"Point(double x, double y) : x(x), y(y) {" + //$NON-NLS-1$
			"label: for (int i = 0; i < 10; i++)" + //$NON-NLS-1$
			"goto label;" + //$NON-NLS-1$
			"}\n\n" + //$NON-NLS-1$
			"private:" + //$NON-NLS-1$
			"double x;" + //$NON-NLS-1$
			"double y;" + //$NON-NLS-1$
			"};\n\n\n" + //$NON-NLS-1$
			"void bar() {\n" + //$NON-NLS-1$
			"for (int i = 0; i < 10; i++);\n" + //$NON-NLS-1$
			"}"; //$NON-NLS-1$

	private TranslationUnitPreview fPreview;

	public NewLinesTabPage(ModifyDialog modifyDialog, Map<String, String> workingValues) {
		super(modifyDialog, workingValues);
	}

	@Override
	protected void doCreatePreferences(Composite composite, int numColumns) {
		final Group newlinesGroup = createGroup(numColumns, composite,
				FormatterMessages.NewLinesTabPage_newlines_group_title);
		createPref(newlinesGroup, numColumns,
				FormatterMessages.NewLinesTabPage_newlines_group_option_after_colon_in_constructor_initializer_list,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_AFTER_COLON_IN_CONSTRUCTOR_INITIALIZER_LIST,
				DO_NOT_INSERT_INSERT);
		createPref(newlinesGroup, numColumns,
				FormatterMessages.NewLinesTabPage_newlines_group_option_before_colon_in_constructor_initializer_list,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_BEFORE_COLON_IN_CONSTRUCTOR_INITIALIZER_LIST,
				DO_NOT_INSERT_INSERT);
		createPref(newlinesGroup, numColumns, FormatterMessages.NewLinesTabPage_newlines_emtpy_statement_on_new_line,
				DefaultCodeFormatterConstants.FORMATTER_PUT_EMPTY_STATEMENT_ON_NEW_LINE, FALSE_TRUE);
		createPref(newlinesGroup, numColumns,
				FormatterMessages.NewLinesTabPage_newlines_before_identifier_in_function_declaration,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_BEFORE_IDENTIFIER_IN_FUNCTION_DECLARATION,
				DO_NOT_INSERT_INSERT);
		createPref(newlinesGroup, numColumns, FormatterMessages.NewLinesTabPage_newlines_after_label,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_AFTER_LABEL, DO_NOT_INSERT_INSERT);
		//		createPref(newlinesGroup, numColumns, FormatterMessages.NewLinesTabPage_newlines_group_option_empty_class_body, DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_EMPTY_TYPE_DECLARATION, DO_NOT_INSERT_INSERT);
		//		createPref(newlinesGroup, numColumns, FormatterMessages.NewLinesTabPage_newlines_group_option_empty_anonymous_class_body, DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_EMPTY_ANONYMOUS_TYPE_DECLARATION, DO_NOT_INSERT_INSERT);
		//		createPref(newlinesGroup, numColumns, FormatterMessages.NewLinesTabPage_newlines_group_option_empty_method_body, DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_EMPTY_METHOD_BODY, DO_NOT_INSERT_INSERT);
		//		createPref(newlinesGroup, numColumns, FormatterMessages.NewLinesTabPage_newlines_group_option_empty_block, DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_EMPTY_BLOCK, DO_NOT_INSERT_INSERT);
		//		createPref(newlinesGroup, numColumns, FormatterMessages.NewLinesTabPage_newlines_group_option_empty_label, DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_AFTER_LABEL, DO_NOT_INSERT_INSERT);
		//		createPref(newlinesGroup, numColumns, FormatterMessages.NewLinesTabPage_newlines_group_option_empty_enum_declaration, DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_EMPTY_ENUM_DECLARATION, DO_NOT_INSERT_INSERT);
		//		createPref(newlinesGroup, numColumns, FormatterMessages.NewLinesTabPage_newlines_group_option_empty_enum_constant, DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_EMPTY_ENUM_CONSTANT, DO_NOT_INSERT_INSERT);
		//		createPref(newlinesGroup, numColumns, FormatterMessages.NewLinesTabPage_newlines_group_option_empty_annotation_decl_body, DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_EMPTY_ANNOTATION_DECLARATION, DO_NOT_INSERT_INSERT);
		//		createPref(newlinesGroup, numColumns, FormatterMessages.NewLinesTabPage_newlines_group_option_empty_end_of_file, DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_AT_END_OF_FILE_IF_MISSING, DO_NOT_INSERT_INSERT);

		//		final Group arrayInitializerGroup= createGroup(numColumns, composite, FormatterMessages.NewLinesTabPage_arrayInitializer_group_title);
		//		createPref(arrayInitializerGroup, numColumns, FormatterMessages.NewLinesTabPage_array_group_option_after_opening_brace_of_array_initializer, DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_AFTER_OPENING_BRACE_IN_ARRAY_INITIALIZER, DO_NOT_INSERT_INSERT);
		//		createPref(arrayInitializerGroup, numColumns, FormatterMessages.NewLinesTabPage_array_group_option_before_closing_brace_of_array_initializer, DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_BEFORE_CLOSING_BRACE_IN_ARRAY_INITIALIZER, DO_NOT_INSERT_INSERT);

		//		final Group emptyStatementsGroup= createGroup(numColumns, composite, FormatterMessages.NewLinesTabPage_empty_statement_group_title);
		//		createPref(emptyStatementsGroup, numColumns, FormatterMessages.NewLinesTabPage_emtpy_statement_group_option_empty_statement_on_new_line, DefaultCodeFormatterConstants.FORMATTER_PUT_EMPTY_STATEMENT_ON_NEW_LINE, FALSE_TRUE);

		//		final Group annotationsGroup= createGroup(numColumns, composite, FormatterMessages.NewLinesTabPage_annotations_group_title);
		//		createPref(annotationsGroup, numColumns, FormatterMessages.NewLinesTabPage_annotations_group_packages, DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_AFTER_ANNOTATION_ON_PACKAGE, DO_NOT_INSERT_INSERT);
		//		createPref(annotationsGroup, numColumns, FormatterMessages.NewLinesTabPage_annotations_group_types, DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_AFTER_ANNOTATION_ON_TYPE, DO_NOT_INSERT_INSERT);
		//		createPref(annotationsGroup, numColumns, FormatterMessages.NewLinesTabPage_annotations_group_fields, DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_AFTER_ANNOTATION_ON_FIELD, DO_NOT_INSERT_INSERT);
		//		createPref(annotationsGroup, numColumns, FormatterMessages.NewLinesTabPage_annotations_group_methods, DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_AFTER_ANNOTATION_ON_METHOD, DO_NOT_INSERT_INSERT);
		//		createPref(annotationsGroup, numColumns, FormatterMessages.NewLinesTabPage_annotations_group_paramters, DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_AFTER_ANNOTATION_ON_PARAMETER, DO_NOT_INSERT_INSERT);
		//		createPref(annotationsGroup, numColumns, FormatterMessages.NewLinesTabPage_annotations_group_local_variables, DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_AFTER_ANNOTATION_ON_LOCAL_VARIABLE, DO_NOT_INSERT_INSERT);
	}

	@Override
	protected void initializePage() {
		fPreview.setPreviewText(PREVIEW);
	}

	@Override
	protected CPreview doCreateCPreview(Composite parent) {
		fPreview = new TranslationUnitPreview(fWorkingValues, parent);
		return fPreview;
	}

	@Override
	protected void doUpdatePreview() {
		super.doUpdatePreview();
		fPreview.update();
	}

	private CheckboxPreference createPref(Composite composite, int numColumns, String message, String key,
			String[] values) {
		return createCheckboxPref(composite, numColumns, message, key, values);
	}
}
