/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.corext.util;

import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ToolFactory;
import org.eclipse.cdt.core.formatter.CodeFormatter;
import org.eclipse.cdt.core.formatter.CodeFormatterConstants;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DefaultPositionUpdater;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.Position;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;

public class CodeFormatterUtil {

//	/**
//	 * Creates a string that represents the given number of indents (can be spaces or tabs..)
//	 */
//	public static String createIndentString(int indent) {
//		String str= format(CodeFormatter.K_EXPRESSION, "x", indent, null, "", (Map) null); //$NON-NLS-1$ //$NON-NLS-2$
//		return str.substring(0, str.indexOf('x'));
//	} 

	/**
	 * Gets the current tab width.
	 * 
	 * @param project The project where the source is used, used for project
	 *        specific options or <code>null</code> if the project is unknown
	 *        and the workspace default should be used
	 * @return The tab width
	 */
	public static int getTabWidth(ICProject project) {
		/*
		 * If the tab-char is SPACE, FORMATTER_INDENTATION_SIZE is not used
		 * by the core formatter.
		 * We piggy back the visual tab length setting in that preference in
		 * that case.
		 */
		String key;
		if (CCorePlugin.SPACE.equals(getCoreOption(project, CodeFormatterConstants.FORMATTER_TAB_CHAR)))
			key= CodeFormatterConstants.FORMATTER_INDENTATION_SIZE;
		else
			key= CodeFormatterConstants.FORMATTER_TAB_SIZE;
		
		return getCoreOption(project, key, 4);
	}

	/**
	 * Returns the current indent width.
	 * 
	 * @param project the project where the source is used or <code>null</code>
	 *        if the project is unknown and the workspace default should be used
	 * @return the indent width
	 */
	public static int getIndentWidth(ICProject project) {
		String key;
		if (CodeFormatterConstants.MIXED.equals(getCoreOption(project, CodeFormatterConstants.FORMATTER_TAB_CHAR)))
			key= CodeFormatterConstants.FORMATTER_INDENTATION_SIZE;
		else
			key= CodeFormatterConstants.FORMATTER_TAB_SIZE;
		
		return getCoreOption(project, key, 4);
	}

	/**
	 * Returns the possibly <code>project</code>-specific core preference
	 * defined under <code>key</code>.
	 * 
	 * @param project the project to get the preference from, or
	 *        <code>null</code> to get the global preference
	 * @param key the key of the preference
	 * @return the value of the preference
	 */
	private static String getCoreOption(ICProject project, String key) {
		if (project == null)
			return CCorePlugin.getOption(key);
		return project.getOption(key, true);
	}

	/**
	 * Returns the possibly <code>project</code>-specific core preference
	 * defined under <code>key</code>, or <code>def</code> if the value is
	 * not a integer.
	 * 
	 * @param project the project to get the preference from, or
	 *        <code>null</code> to get the global preference
	 * @param key the key of the preference
	 * @param def the default value
	 * @return the value of the preference
	 */
	private static int getCoreOption(ICProject project, String key, int def) {
		try {
			return Integer.parseInt(getCoreOption(project, key));
		} catch (NumberFormatException e) {
			return def;
		}
	}

	/**
	 * Evaluates the edit on the given string.
	 * @throws IllegalArgumentException If the positions are not inside the string, a
	 *  IllegalArgumentException is thrown.
	 */
	public static String evaluateFormatterEdit(String string, TextEdit edit, Position[] positions) {
		try {
			Document doc= createDocument(string, positions);
			edit.apply(doc, 0);
			if (positions != null) {
				for (int i= 0; i < positions.length; i++) {
					Assert.isTrue(!positions[i].isDeleted, "Position got deleted"); //$NON-NLS-1$
				}
			}
			return doc.get();
		} catch (BadLocationException e) {
			CUIPlugin.getDefault().log(e); // bug in the formatter
			Assert.isTrue(false, "Fromatter created edits with wrong positions: " + e.getMessage()); //$NON-NLS-1$
		}
		return null;
	}
	
	/**
	 * Creates edits that describe how to format the given string. Returns <code>null</code> if the code could not be formatted for the given kind.
	 * @throws IllegalArgumentException If the offset and length are not inside the string, a
	 *  IllegalArgumentException is thrown.
	 */
	public static TextEdit format(int kind, String source, int offset, int length, int indentationLevel, String lineSeparator, Map options) {
		if (offset < 0 || length < 0 || offset + length > source.length()) {
			throw new IllegalArgumentException("offset or length outside of string. offset: " + offset + ", length: " + length + ", string size: " + source.length());   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
		}
		CodeFormatter formatter = ToolFactory.createCodeFormatter(options);
		if (formatter != null) {
			return formatter.format(kind, source, offset, length, indentationLevel, lineSeparator);
		}
		return null;
	}
	
	public static TextEdit format(int kind, String source, int indentationLevel, String lineSeparator, Map options) {
		return format(kind, source, 0, source.length(), indentationLevel, lineSeparator, options);
	}
	
			
//	private static TextEdit shifEdit(TextEdit oldEdit, int diff) {
//		TextEdit newEdit;
//		if (oldEdit instanceof ReplaceEdit) {
//			ReplaceEdit edit= (ReplaceEdit) oldEdit;
//			newEdit= new ReplaceEdit(edit.getOffset() - diff, edit.getLength(), edit.getText());
//		} else if (oldEdit instanceof InsertEdit) {
//			InsertEdit edit= (InsertEdit) oldEdit;
//			newEdit= new InsertEdit(edit.getOffset() - diff,  edit.getText());
//		} else if (oldEdit instanceof DeleteEdit) {
//			DeleteEdit edit= (DeleteEdit) oldEdit;
//			newEdit= new DeleteEdit(edit.getOffset() - diff,  edit.getLength());
//		} else if (oldEdit instanceof MultiTextEdit) {
//			newEdit= new MultiTextEdit();			
//		} else {
//			return null; // not supported
//		}
//		TextEdit[] children= oldEdit.getChildren();
//		for (int i= 0; i < children.length; i++) {
//			TextEdit shifted= shifEdit(children[i], diff);
//			if (shifted != null) {
//				newEdit.addChild(shifted);
//			}
//		}
//		return newEdit;
//	}
		
	private static Document createDocument(String string, Position[] positions) throws IllegalArgumentException {
		Document doc= new Document(string);
		try {
			if (positions != null) {
				final String POS_CATEGORY= "myCategory"; //$NON-NLS-1$
				
				doc.addPositionCategory(POS_CATEGORY);
				doc.addPositionUpdater(new DefaultPositionUpdater(POS_CATEGORY) {
					protected boolean notDeleted() {
						if (fOffset < fPosition.offset && (fPosition.offset + fPosition.length < fOffset + fLength)) {
							fPosition.offset= fOffset + fLength; // deleted positions: set to end of remove
							return false;
						}
						return true;
					}
				});
				for (int i= 0; i < positions.length; i++) {
					try {
						doc.addPosition(POS_CATEGORY, positions[i]);
					} catch (BadLocationException e) {
						throw new IllegalArgumentException("Position outside of string. offset: " + positions[i].offset + ", length: " + positions[i].length + ", string size: " + string.length());   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
					}
				}
			}
		} catch (BadPositionCategoryException cannotHappen) {
			// can not happen: category is correctly set up
		}
		return doc;
	}
	
	public static int getTabWidth() {
		IPreferenceStore store = CUIPlugin.getDefault().getCombinedPreferenceStore();
		return store.getInt(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH);
	}

}
