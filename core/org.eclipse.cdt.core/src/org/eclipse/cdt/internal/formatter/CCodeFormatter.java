/*******************************************************************************
 * Copyright (c) 2000, 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeSelector;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroExpansion;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.formatter.CodeFormatter;
import org.eclipse.cdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.cdt.core.formatter.DefaultCodeFormatterOptions;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.core.parser.FileContent;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IncludeFileContentProvider;
import org.eclipse.cdt.core.parser.ParserUtil;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
import org.eclipse.cdt.internal.core.util.TextUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.text.edits.TextEdit;

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

		int tabs = 0;
		int spaces = 0;
		switch (preferences.tab_char) {
		case DefaultCodeFormatterOptions.SPACE:
			spaces = indentationLevel * preferences.tab_size;
			break;

		case DefaultCodeFormatterOptions.TAB:
			tabs = indentationLevel;
			break;

		case DefaultCodeFormatterOptions.MIXED:
			int tabSize = preferences.tab_size;
			int spaceEquivalents = indentationLevel * preferences.indentation_size;
			tabs = spaceEquivalents / tabSize;
			spaces = spaceEquivalents % tabSize;
			break;

		default:
			return EMPTY_STRING;
		}

		if (tabs == 0 && spaces == 0) {
			return EMPTY_STRING;
		}
		StringBuilder buffer = new StringBuilder(tabs + spaces);
		for (int i = 0; i < tabs; i++) {
			buffer.append('\t');
		}
		for (int i = 0; i < spaces; i++) {
			buffer.append(' ');
		}
		return buffer.toString();
	}

	@Override
	public void setOptions(Map<String, ?> options) {
		if (options != null) {
			this.options = options;
			Map<String, String> formatterPrefs = new HashMap<>(options.size());
			for (String key : options.keySet()) {
				Object value = options.get(key);
				if (value instanceof String) {
					formatterPrefs.put(key, (String) value);
				}
			}
			preferences = new DefaultCodeFormatterOptions(formatterPrefs);
		} else {
			this.options = CCorePlugin.getOptions();
			preferences = DefaultCodeFormatterOptions.getDefaultSettings();
		}
	}

	@Override
	public TextEdit format(int kind, String source, int offset, int length, int indentationLevel,
			String lineSeparator) {
		preferences.initial_indentation_level = indentationLevel;
		return format(kind, source, new IRegion[] { new Region(offset, length) }, lineSeparator)[0];
	}

	@Override
	public TextEdit[] format(int kind, String source, IRegion[] regions, String lineSeparator) {
		TextEdit[] edits = new TextEdit[regions.length];
		if (lineSeparator != null) {
			preferences.line_separator = lineSeparator;
		} else {
			preferences.line_separator = System.getProperty("line.separator"); //$NON-NLS-1$
		}

		ITranslationUnit tu = getTranslationUnit(source);
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
					ast = tu.getAST(index, ITranslationUnit.AST_SKIP_INDEXED_HEADERS);
				} catch (CoreException e) {
					throw new AbortFormatting(e);
				}
				if (ast == null) {
					throw new AbortFormatting("AST is null"); //$NON-NLS-1$
				}
				formatRegions(source, regions, edits, ast);
			} finally {
				index.releaseReadLock();
			}
		} else {
			IncludeFileContentProvider contentProvider = IncludeFileContentProvider.getSavedFilesProvider();
			IScannerInfo scanInfo = new ScannerInfo();
			FileContent content = FileContent.create("<text>", source.toCharArray()); //$NON-NLS-1$

			ILanguage language = (ILanguage) options.get(DefaultCodeFormatterConstants.FORMATTER_LANGUAGE);
			if (language == null) {
				language = GPPLanguage.getDefault();
			}
			IASTTranslationUnit ast;
			try {
				ast = language.getASTTranslationUnit(content, scanInfo, contentProvider, null, 0,
						ParserUtil.getParserLogService());
				formatRegions(source, regions, edits, ast);
			} catch (CoreException e) {
				throw new AbortFormatting(e);
			}
		}
		return edits;
	}

	private void formatRegions(String source, IRegion[] regions, TextEdit[] edits, IASTTranslationUnit ast) {
		for (int i = 0; i < regions.length; i++) {
			IRegion region = regions[i];
			if (shouldFormatWholeStatements()) {
				// An empty region is replaced by the region containing the line corresponding to
				// the offset and all statements overlapping with that line.
				region = getLineOrStatementRegion(source, region, ast);
			}
			CodeFormatterVisitor codeFormatter = new CodeFormatterVisitor(preferences, region.getOffset(),
					region.getLength());
			edits[i] = codeFormatter.format(source, ast);
			IStatus status = codeFormatter.getStatus();
			if (!status.isOK()) {
				CCorePlugin.log(status);
			}
		}
	}

	private boolean shouldFormatWholeStatements() {
		Object obj = options.get(DefaultCodeFormatterConstants.FORMATTER_STATEMENT_SCOPE);
		return obj instanceof Boolean && ((Boolean) obj).booleanValue();
	}

	/**
	 * Returns the smallest region containing the lines overlapping with the given region and all
	 * statements overlapping with those lines.
	 */
	private IRegion getLineOrStatementRegion(String source, IRegion region, IASTTranslationUnit ast) {
		int start = TextUtil.getLineStart(source, region.getOffset());
		int end = TextUtil.skipToNextLine(source, region.getOffset() + region.getLength());
		IASTNode node = findOverlappingPreprocessorStatement(start, end, ast);
		if (node != null) {
			IASTFileLocation location = node.getFileLocation();
			int nodeOffset = location.getNodeOffset();
			if (nodeOffset < start)
				start = nodeOffset;
			int nodeEnd = nodeOffset + location.getNodeLength();
			if (nodeEnd > end)
				end = nodeEnd;
			return new Region(start, end - start);
		}
		IASTNodeSelector nodeSelector = ast.getNodeSelector(null);
		for (int pos = start; pos < end;) {
			node = nodeSelector.findFirstContainedNode(pos, end - pos);
			if (node != null) {
				IASTNode containedNode = node;
				node = ASTQueries.findAncestorWithType(containedNode, IASTStatement.class);
				if (node == null)
					node = ASTQueries.findAncestorWithType(containedNode, IASTDeclaration.class);
				if (node == null)
					node = ASTQueries.findAncestorWithType(containedNode, IASTPreprocessorMacroExpansion.class);
			}
			if (node == null)
				break;
			IASTFileLocation location = node.getFileLocation();
			int nodeOffset = location.getNodeOffset();
			if (nodeOffset < start)
				start = nodeOffset;
			int nodeEnd = nodeOffset + location.getNodeLength();
			if (nodeEnd > end)
				end = nodeEnd;
			pos = nodeEnd;
		}

		return new Region(start, end - start);
	}

	private IASTNode findOverlappingPreprocessorStatement(int start, int end, IASTTranslationUnit ast) {
		IASTPreprocessorStatement[] statements = ast.getAllPreprocessorStatements();
		int low = 0;
		int high = statements.length;
		while (low < high) {
			int mid = (low + high) >>> 1;
			IASTPreprocessorStatement statement = statements[mid];
			IASTFileLocation location = statement.getFileLocation();
			if (location == null) {
				low = mid + 1;
			} else {
				int statementOffset = location.getNodeOffset();
				if (statementOffset >= end) {
					high = mid;
				} else if (statementOffset + location.getNodeLength() <= start) {
					low = mid + 1;
				} else {
					return statement;
				}
			}
		}
		return null;
	}

	private ITranslationUnit getTranslationUnit(String source) {
		ITranslationUnit tu = (ITranslationUnit) options.get(DefaultCodeFormatterConstants.FORMATTER_TRANSLATION_UNIT);
		if (tu == null) {
			IFile file = (IFile) options.get(DefaultCodeFormatterConstants.FORMATTER_CURRENT_FILE);
			if (file != null) {
				tu = (ITranslationUnit) CoreModel.getDefault().create(file);
			}
		}
		if (tu != null && source != null) {
			try {
				// Create a private working copy and set it contents to source.
				if (tu.isWorkingCopy())
					tu = ((IWorkingCopy) tu).getOriginalElement();
				tu = tu.getWorkingCopy();
				tu.getBuffer().setContents(source);
			} catch (CModelException e) {
				throw new AbortFormatting(e);
			}
		}
		return tu;
	}
}
