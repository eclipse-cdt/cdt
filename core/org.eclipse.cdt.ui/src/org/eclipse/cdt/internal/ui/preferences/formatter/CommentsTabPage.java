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

/**
 * Tab page for the comment formatter settings.
 */
public class CommentsTabPage extends FormatterTabPage {

	//	private static abstract class Controller implements Observer {
	//		private final Collection<CheckboxPreference> fMasters;
	//		private final Collection<Object> fSlaves;
	//
	//		public Controller(Collection<CheckboxPreference> masters, Collection<Object> slaves) {
	//			fMasters= masters;
	//			fSlaves= slaves;
	//			for (CheckboxPreference pref : fMasters) {
	//				pref.addObserver(this);
	//			}
	//		}
	//
	//		public void update(Observable o, Object arg) {
	//			boolean enabled= areSlavesEnabled();
	//
	//			for (Object slave : fSlaves) {
	//				if (slave instanceof Preference) {
	//					((Preference) slave).setEnabled(enabled);
	//				} else if (slave instanceof Control) {
	//					((Group) slave).setEnabled(enabled);
	//				}
	//			}
	//		}
	//
	//		public Collection<CheckboxPreference> getMasters() {
	//			return fMasters;
	//		}
	//
	//		protected abstract boolean areSlavesEnabled();
	//	}
	//
	//	private final static class OrController extends Controller {
	//		public OrController(Collection<CheckboxPreference> masters, Collection<Object> slaves) {
	//			super(masters, slaves);
	//			update(null, null);
	//		}
	//
	//		@Override
	//		protected boolean areSlavesEnabled() {
	//			for (CheckboxPreference pref : getMasters()) {
	//				if (pref.getChecked())
	//					return true;
	//			}
	//			return false;
	//		}
	//	}

	private final String PREVIEW = createPreviewHeader(FormatterMessages.CommentsTabPage_preview_header)
			+ "int gVariable = 100;    \t\t// line 1 of comment\n" + //$NON-NLS-1$
			// needs as many tabs as indent size, consider case when tab indent size is 1
			"\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t// line 2 of comment\n" + //$NON-NLS-1$
			"\n" + //$NON-NLS-1$
			"void lineComments() {\n" + //$NON-NLS-1$
			"\tprintf(\"%d\\n\", 1234);   \t\t// Integer number\n" + //$NON-NLS-1$
			"\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t// More here\n" + //$NON-NLS-1$
			"\t/*\n\t * Another block\n\t * comment\n\t*/\n" + //$NON-NLS-1$
			"\tprintf(\"%.5g\\n\", 12.34);\t\t// Floating point number\n" + //$NON-NLS-1$
			"}\n"; //$NON-NLS-1$

	private TranslationUnitPreview fPreview;

	public CommentsTabPage(ModifyDialog modifyDialog, Map<String, String> workingValues) {
		super(modifyDialog, workingValues);
	}

	@Override
	protected void doCreatePreferences(Composite composite, int numColumns) {
		//		final int indent= fPixelConverter.convertWidthInCharsToPixels(4);

		// Global group
		//		final Group globalGroup= createGroup(numColumns, composite, FormatterMessages.CommentsTabPage_group1_title);
		//		createPrefFalseTrue(globalGroup, numColumns, FormatterMessages.CommentsTabPage_do_not_join_lines, DefaultCodeFormatterConstants.FORMATTER_JOIN_LINES_IN_COMMENTS, true);

		// Line comment group
		final Group commentGroup = createGroup(numColumns, composite, FormatterMessages.CommentsTabPage_group1_title);
		//		final CheckboxPreference singleLineCommentsOnFirstColumn= createPrefFalseTrue(lineCommentGroup, numColumns, FormatterMessages.CommentsTabPage_format_line_comments_on_first_column, DefaultCodeFormatterConstants.FORMATTER_COMMENT_FORMAT_LINE_COMMENT_STARTING_ON_FIRST_COLUMN, false);
		//		((GridData) singleLineCommentsOnFirstColumn.getControl().getLayoutData()).horizontalIndent= indent;
		createPrefFalseTrue(commentGroup, numColumns, FormatterMessages.CommentsTabPage_block_comment,
				DefaultCodeFormatterConstants.FORMATTER_COMMENT_BLOCK, false);
		createPrefFalseTrue(commentGroup, numColumns, FormatterMessages.CommentsTabPage_line_comment,
				DefaultCodeFormatterConstants.FORMATTER_COMMENT_LINE, false);
		createPrefFalseTrue(commentGroup, numColumns, FormatterMessages.CommentsTabPage_header_comment,
				DefaultCodeFormatterConstants.FORMATTER_COMMENT_HEADER, false);
		createPrefFalseTrue(commentGroup, numColumns,
				FormatterMessages.CommentsTabPage_preserve_white_space_before_line_comment,
				DefaultCodeFormatterConstants.FORMATTER_COMMENT_PRESERVE_WHITE_SPACE_BETWEEN_CODE_AND_LINE_COMMENT,
				false);
		createPrefFalseTrue(commentGroup, numColumns,
				FormatterMessages.CommentsTabPage_line_up_line_comment_in_blocks_on_first_column,
				DefaultCodeFormatterConstants.FORMATTER_COMMENT_LINE_UP_LINE_COMMENT_IN_BLOCKS_ON_FIRST_COLUMN, false);
		createNumberPref(commentGroup, numColumns, FormatterMessages.CommentsTabPage_line_width,
				DefaultCodeFormatterConstants.FORMATTER_COMMENT_MIN_DISTANCE_BETWEEN_CODE_AND_LINE_COMMENT, 0, 9999);
		//		final CheckboxPreference singleLineComments= createPrefFalseTrue(lineCommentGroup, numColumns, FormatterMessages.CommentsTabPage_enable_line_comment_formatting, DefaultCodeFormatterConstants.FORMATTER_COMMENT_FORMAT_LINE_COMMENT, false);
		//		createPrefFalseTrue(lineCommentGroup, numColumns, FormatterMessages.CommentsTabPage_never_indent_line_comments_on_first_column, DefaultCodeFormatterConstants.FORMATTER_NEVER_INDENT_LINE_COMMENTS_ON_FIRST_COLUMN, false);

		// Block comment settings
		//		final Group blockSettingsGroup= createGroup(numColumns, composite, FormatterMessages.CommentsTabPage_group4_title);
		//		final CheckboxPreference header= createPrefFalseTrue(blockSettingsGroup, numColumns, FormatterMessages.CommentsTabPage_format_header, DefaultCodeFormatterConstants.FORMATTER_COMMENT_FORMAT_HEADER, false);
		//		GridData spacerData= new GridData(0, 0);
		//		spacerData.horizontalSpan= numColumns;
		//		final CheckboxPreference blockComment= createPrefFalseTrue(blockSettingsGroup, numColumns, FormatterMessages.CommentsTabPage_enable_block_comment_formatting, DefaultCodeFormatterConstants.FORMATTER_COMMENT_FORMAT_BLOCK_COMMENT, false);
		//		final CheckboxPreference nlBoundariesBlock= createPrefFalseTrue(blockSettingsGroup, numColumns, FormatterMessages.CommentsTabPage_new_lines_at_comment_boundaries, DefaultCodeFormatterConstants.FORMATTER_COMMENT_NEW_LINES_AT_BLOCK_BOUNDARIES, false);
		//		final CheckboxPreference blankLinesBlock= createPrefFalseTrue(blockSettingsGroup, numColumns, FormatterMessages.CommentsTabPage_remove_blank_block_comment_lines, DefaultCodeFormatterConstants.FORMATTER_COMMENT_CLEAR_BLANK_LINES_IN_BLOCK_COMMENT, false);

		// Doxygen comment formatting settings
		//		final Group settingsGroup= createGroup(numColumns, composite, FormatterMessages.CommentsTabPage_group2_title);
		//		final CheckboxPreference doxygen= createPrefFalseTrue(globalGroup, numColumns, FormatterMessages.commentsTabPage_enable_javadoc_comment_formatting, DefaultCodeFormatterConstants.FORMATTER_COMMENT_FORMAT_JAVADOC_COMMENT, false);
		//		final CheckboxPreference html= createPrefFalseTrue(settingsGroup, numColumns, FormatterMessages.CommentsTabPage_format_html, DefaultCodeFormatterConstants.FORMATTER_COMMENT_FORMAT_HTML, false);
		//		final CheckboxPreference code= createPrefFalseTrue(settingsGroup, numColumns, FormatterMessages.CommentsTabPage_format_code_snippets, DefaultCodeFormatterConstants.FORMATTER_COMMENT_FORMAT_SOURCE, false);
		//		final CheckboxPreference blankDoxygen= createPrefInsert(settingsGroup, numColumns, FormatterMessages.CommentsTabPage_blank_line_before_doxygen_tags, DefaultCodeFormatterConstants.FORMATTER_COMMENT_INSERT_EMPTY_LINE_BEFORE_ROOT_TAGS);
		//		final CheckboxPreference indentDoxygen= createPrefFalseTrue(settingsGroup, numColumns, FormatterMessages.CommentsTabPage_indent_doxygen_tags, DefaultCodeFormatterConstants.FORMATTER_COMMENT_INDENT_ROOT_TAGS, false);
		//		final CheckboxPreference indentDesc= createPrefFalseTrue(settingsGroup, numColumns, FormatterMessages.CommentsTabPage_indent_description_after_param, DefaultCodeFormatterConstants.FORMATTER_COMMENT_INDENT_PARAMETER_DESCRIPTION, false);
		//		((GridData) indentDesc.getControl().getLayoutData()).horizontalIndent= indent;
		//		final CheckboxPreference nlParam= createPrefInsert(settingsGroup, numColumns, FormatterMessages.CommentsTabPage_new_line_after_param_tags, DefaultCodeFormatterConstants.FORMATTER_COMMENT_INSERT_NEW_LINE_FOR_PARAMETER);
		//		final CheckboxPreference nlBoundariesDoxygen= createPrefFalseTrue(settingsGroup, numColumns, FormatterMessages.CommentsTabPage_new_lines_at_doxygen_boundaries, DefaultCodeFormatterConstants.FORMATTER_COMMENT_NEW_LINES_AT_JAVADOC_BOUNDARIES, false);
		//		final CheckboxPreference blankLinesDoxygen= createPrefFalseTrue(settingsGroup, numColumns, FormatterMessages.CommentsTabPage_clear_blank_lines, DefaultCodeFormatterConstants.FORMATTER_COMMENT_CLEAR_BLANK_LINES_IN_JAVADOC_COMMENT, false);

		// Line width settings
		//		final Group widthGroup= createGroup(numColumns, composite, FormatterMessages.CommentsTabPage_group3_title);
		//		final NumberPreference lineWidth= createNumberPref(widthGroup, numColumns, FormatterMessages.CommentsTabPage_line_width, DefaultCodeFormatterConstants.FORMATTER_COMMENT_LINE_LENGTH, 0, 9999);

		//		ArrayList<CheckboxPreference> lineFirstColumnMasters= new ArrayList<CheckboxPreference>();
		//		lineFirstColumnMasters.add(singleLineComments);
		//
		//		ArrayList<Object> lineFirstColumnSlaves= new ArrayList<Object>();
		//		lineFirstColumnSlaves.add(singleLineCommentsOnFirstColumn);
		//
		//		new Controller(lineFirstColumnMasters, lineFirstColumnSlaves) {
		//			@Override
		//			protected boolean areSlavesEnabled() {
		//				return singleLineComments.getChecked();
		//            }
		//		}.update(null, null);
		//
		//		ArrayList<CheckboxPreference> doxygenMaster= new ArrayList<CheckboxPreference>();
		//		doxygenMaster.add(doxygen);
		//		doxygenMaster.add(header);
		//
		//		ArrayList<Object> doxygenSlaves= new ArrayList<Object>();
		//		doxygenSlaves.add(settingsGroup);
		//		doxygenSlaves.add(html);
		//		doxygenSlaves.add(code);
		//		doxygenSlaves.add(blankDoxygen);
		//		doxygenSlaves.add(indentDoxygen);
		//		doxygenSlaves.add(nlParam);
		//		doxygenSlaves.add(nlBoundariesDoxygen);
		//		doxygenSlaves.add(blankLinesDoxygen);
		//
		//		new OrController(doxygenMaster, doxygenSlaves);
		//
		//		ArrayList<CheckboxPreference> indentMasters= new ArrayList<CheckboxPreference>();
		//		indentMasters.add(doxygen);
		//		indentMasters.add(header);
		//		indentMasters.add(indentDoxygen);
		//
		//		ArrayList<Object> indentSlaves= new ArrayList<Object>();
		//		indentSlaves.add(indentDesc);
		//
		//		new Controller(indentMasters, indentSlaves) {
		//			@Override
		//			protected boolean areSlavesEnabled() {
		//				return (doxygen.getChecked() || header.getChecked()) && indentDoxygen.getChecked();
		//            }
		//		}.update(null, null);
		//
		//		ArrayList<CheckboxPreference> blockMasters= new ArrayList<CheckboxPreference>();
		//		blockMasters.add(blockComment);
		//		blockMasters.add(header);
		//
		//		ArrayList<Object> blockSlaves= new ArrayList<Object>();
		//		blockSlaves.add(blockSettingsGroup);
		//		blockSlaves.add(nlBoundariesBlock);
		//		blockSlaves.add(blankLinesBlock);
		//
		//		new OrController(blockMasters, blockSlaves);
		//
		//		ArrayList<CheckboxPreference> lineWidthMasters= new ArrayList<CheckboxPreference>();
		//		lineWidthMasters.add(doxygen);
		//		lineWidthMasters.add(blockComment);
		//		lineWidthMasters.add(singleLineComments);
		//		lineWidthMasters.add(header);
		//
		//		ArrayList<Object> lineWidthSlaves= new ArrayList<Object>();
		//		lineWidthSlaves.add(widthGroup);
		//		lineWidthSlaves.add(lineWidth);
		//
		//		new OrController(lineWidthMasters, lineWidthSlaves);
	}

	@Override
	protected void initializePage() {
		fPreview.setPreviewText(PREVIEW);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.ui.preferences.formatter.ModifyDialogTabPage#doCreateCPreview(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected CPreview doCreateCPreview(Composite parent) {
		fPreview = new TranslationUnitPreview(fWorkingValues, parent);
		return fPreview;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.ui.preferences.formatter.ModifyDialogTabPage#doUpdatePreview()
	 */
	@Override
	protected void doUpdatePreview() {
		super.doUpdatePreview();
		fPreview.update();
	}

	private CheckboxPreference createPrefFalseTrue(Composite composite, int numColumns, String text, String key,
			boolean invertPreference) {
		if (invertPreference)
			return createCheckboxPref(composite, numColumns, text, key, TRUE_FALSE);
		return createCheckboxPref(composite, numColumns, text, key, FALSE_TRUE);
	}

	//    private CheckboxPreference createPrefInsert(Composite composite, int numColumns, String text, String key) {
	//        return createCheckboxPref(composite, numColumns, text, key, DO_NOT_INSERT_INSERT);
	//    }
}
