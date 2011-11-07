/*******************************************************************************
 * Copyright (c) 2000, 2011 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Sergey Prigogin (Google)
 *     Anton Leherbauer (Wind River Systems)
 *     IBM Corporation
 *******************************************************************************/
package org.eclipse.cdt.internal.formatter;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.formatter.CodeFormatter;
import org.eclipse.cdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.FileContent;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IncludeFileContentProvider;
import org.eclipse.cdt.core.parser.ParserUtil;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.jface.text.IRegion;

public class CCodeFormatter extends CodeFormatter {
	private DefaultCodeFormatterOptions preferences;
	private Map<String, ?> options;

	public CCodeFormatter() {
		this(DefaultCodeFormatterOptions.getDefaultSettings());
	}

	public CCodeFormatter(DefaultCodeFormatterOptions preferences) {
		this(preferences, null);
	}

	public CCodeFormatter(DefaultCodeFormatterOptions defaultCodeFormatterOptions, Map<String, ?> options) {
		setOptions(options);
		if (defaultCodeFormatterOptions != null) {
			preferences.set(defaultCodeFormatterOptions.getMap());
		}
	}

	public CCodeFormatter(Map<String, ?> options) {
		this(null, options);
	}

	@Override
	public String createIndentationString(final int indentationLevel) {
		if (indentationLevel < 0) {
			throw new IllegalArgumentException();
		}

		int tabs= 0;
		int spaces= 0;
		switch (preferences.tab_char) {
		case DefaultCodeFormatterOptions.SPACE:
			spaces= indentationLevel * preferences.tab_size;
			break;

		case DefaultCodeFormatterOptions.TAB:
			tabs= indentationLevel;
			break;

		case DefaultCodeFormatterOptions.MIXED:
			int tabSize= preferences.tab_size;
			int spaceEquivalents= indentationLevel * preferences.indentation_size;
			tabs= spaceEquivalents / tabSize;
			spaces= spaceEquivalents % tabSize;
			break;

		default:
			return EMPTY_STRING;
		}

		if (tabs == 0 && spaces == 0) {
			return EMPTY_STRING;
		}
		StringBuffer buffer= new StringBuffer(tabs + spaces);
		for (int i= 0; i < tabs; i++) {
			buffer.append('\t');
		}
		for (int i= 0; i < spaces; i++) {
			buffer.append(' ');
		}
		return buffer.toString();
	}

	@Override
	public void setOptions(Map<String, ?> options) {
		if (options != null) {
			this.options= options;
			Map<String, String> formatterPrefs= new HashMap<String, String>(options.size());
			for (String key : options.keySet()) {
				Object value= options.get(key);
				if (value instanceof String) {
					formatterPrefs.put(key, (String) value);
				}
			}
			preferences= new DefaultCodeFormatterOptions(formatterPrefs);
		} else {
			this.options= CCorePlugin.getOptions();
			preferences= DefaultCodeFormatterOptions.getDefaultSettings();
		}
	}

	@Override
	public TextEdit format(int kind, String source, int offset, int length, int indentationLevel, String lineSeparator) {
		TextEdit edit= null;
		ITranslationUnit tu= (ITranslationUnit) options.get(DefaultCodeFormatterConstants.FORMATTER_TRANSLATION_UNIT);
		if (tu == null) {
			IFile file= (IFile) options.get(DefaultCodeFormatterConstants.FORMATTER_CURRENT_FILE);
			if (file != null) {
				tu= (ITranslationUnit) CoreModel.getDefault().create(file);
			}
		}
		if (lineSeparator != null) {
			preferences.line_separator = lineSeparator;
		} else {
			preferences.line_separator = System.getProperty("line.separator"); //$NON-NLS-1$
		}
		preferences.initial_indentation_level = indentationLevel;

		if (tu != null) {
			IIndex index;
			try {
				index = CCorePlugin.getIndexManager().getIndex(tu.getCProject());
				index.acquireReadLock();
			} catch (CoreException e) {
				throw new AbortFormatting(e);
			} catch (InterruptedException e) {
				return null;
			}
			IASTTranslationUnit ast;
			try {
				try {
					ast= tu.getAST(index, ITranslationUnit.AST_SKIP_ALL_HEADERS);
				} catch (CoreException exc) {
					throw new AbortFormatting(exc);
				}
				CodeFormatterVisitor codeFormatter = new CodeFormatterVisitor(preferences, offset, length);
				edit= codeFormatter.format(source, ast);
				IStatus status= codeFormatter.getStatus();
				if (!status.isOK()) {
					CCorePlugin.log(status);
				}
			} finally {
				index.releaseReadLock();
			}
		} else {
			IncludeFileContentProvider contentProvider = IncludeFileContentProvider.getSavedFilesProvider();
			IScannerInfo scanInfo = new ScannerInfo();
			FileContent content = FileContent.create("<text>", source.toCharArray()); //$NON-NLS-1$
			
			ILanguage language= (ILanguage) options.get(DefaultCodeFormatterConstants.FORMATTER_LANGUAGE);
			if (language == null) {
				language= GPPLanguage.getDefault();
			}
			IASTTranslationUnit ast;
			try {
				ast= language.getASTTranslationUnit(content, scanInfo, contentProvider, null, 0,
						ParserUtil.getParserLogService());
				CodeFormatterVisitor codeFormatter = new CodeFormatterVisitor(preferences, offset, length);
				edit= codeFormatter.format(source, ast);
				IStatus status= codeFormatter.getStatus();
				if (!status.isOK()) {
					CCorePlugin.log(status);
				}
			} catch (CoreException e) {
				throw new AbortFormatting(e);
			}
		}
		return edit;
	}

	@Override
	public TextEdit[] format(int kind, String source, IRegion[] regions, String lineSeparator) {
		TextEdit[] edits= new TextEdit[regions.length];
		ITranslationUnit tu= (ITranslationUnit) options.get(DefaultCodeFormatterConstants.FORMATTER_TRANSLATION_UNIT);
		if (tu == null) {
			IFile file= (IFile) options.get(DefaultCodeFormatterConstants.FORMATTER_CURRENT_FILE);
			if (file != null) {
				tu= (ITranslationUnit) CoreModel.getDefault().create(file);
			}
		}
		if (lineSeparator != null) {
			preferences.line_separator = lineSeparator;
		} else {
			preferences.line_separator = System.getProperty("line.separator"); //$NON-NLS-1$
		}

		if (tu != null) {
			IIndex index;
			try {
				index = CCorePlugin.getIndexManager().getIndex(tu.getCProject());
				index.acquireReadLock();
			} catch (CoreException e) {
				throw new AbortFormatting(e);
			} catch (InterruptedException e) {
				return null;
			}
			IASTTranslationUnit ast;
			try {
				try {
					ast= tu.getAST(index, ITranslationUnit.AST_SKIP_ALL_HEADERS);
				} catch (CoreException exc) {
					throw new AbortFormatting(exc);
				}
				for (int i = 0; i < regions.length; i++) {
					IRegion region = regions[i];
					CodeFormatterVisitor codeFormatter =
							new CodeFormatterVisitor(preferences, region.getOffset(), region.getLength());
					edits[i] = codeFormatter.format(source, ast);
					IStatus status= codeFormatter.getStatus();
					if (!status.isOK()) {
						CCorePlugin.log(status);
					}
				}
			} finally {
				index.releaseReadLock();
			}
		} else {
			IncludeFileContentProvider contentProvider = IncludeFileContentProvider.getSavedFilesProvider();
			IScannerInfo scanInfo = new ScannerInfo();
			FileContent content = FileContent.create("<text>", source.toCharArray()); //$NON-NLS-1$
			
			ILanguage language= (ILanguage) options.get(DefaultCodeFormatterConstants.FORMATTER_LANGUAGE);
			if (language == null) {
				language= GPPLanguage.getDefault();
			}
			IASTTranslationUnit ast;
			try {
				ast= language.getASTTranslationUnit(content, scanInfo, contentProvider, null, 0,
						ParserUtil.getParserLogService());
				for (int i = 0; i < regions.length; i++) {
					IRegion region = regions[i];
					CodeFormatterVisitor codeFormatter =
							new CodeFormatterVisitor(preferences, region.getOffset(), region.getLength());
					edits[i]= codeFormatter.format(source, ast);
					IStatus status= codeFormatter.getStatus();
					if (!status.isOK()) {
						CCorePlugin.log(status);
					}
				}
			} catch (CoreException e) {
				throw new AbortFormatting(e);
			}
		}
		return edits;
	}
}
