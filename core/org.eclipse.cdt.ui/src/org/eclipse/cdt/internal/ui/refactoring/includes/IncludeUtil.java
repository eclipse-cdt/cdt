/*******************************************************************************
 * Copyright (c) 2012, 2014 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.includes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTComment;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorPragmaStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IndexLocationFactory;
import org.eclipse.cdt.core.parser.Keywords;
import org.eclipse.cdt.core.parser.util.CharArrayIntMap;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.NodeCommentMap;
import org.eclipse.cdt.internal.core.dom.rewrite.util.ASTNodes;
import org.eclipse.cdt.internal.core.parser.scanner.CharArray;
import org.eclipse.cdt.internal.core.parser.scanner.IncludeGuardDetection;
import org.eclipse.cdt.internal.core.parser.scanner.Lexer.LexerOptions;
import org.eclipse.cdt.internal.core.util.TextUtil;
import org.eclipse.cdt.internal.corext.codemanipulation.IncludeInfo;
import org.eclipse.cdt.internal.corext.codemanipulation.InclusionContext;
import org.eclipse.cdt.internal.corext.codemanipulation.StyledInclude;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

public class IncludeUtil {
	/** Not instantiatable. All methods are static. */
	private IncludeUtil() {
	}

	/**
	 * Checks if a file is a source file (.c, .cpp, .cc, etc). Header files are not considered
	 * source files.
	 * @return {@code true} if the the file is a source file.
	 */
	public static boolean isSource(IIndexFile file, IProject project) throws CoreException {
		return isSource(getPath(file), project);
	}

	/**
	 * Checks if a file is a source file (.c, .cpp, .cc, etc). Header files are not considered source files.
	 * @return {@code true} if the the file is a source file.
	 */
	public static boolean isSource(String filename, IProject project) {
		IContentType ct = CCorePlugin.getContentType(project, filename);
		if (ct != null) {
			String id = ct.getId();
			if (CCorePlugin.CONTENT_TYPE_CSOURCE.equals(id) || CCorePlugin.CONTENT_TYPE_CXXSOURCE.equals(id)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks if a file is a header file (.h, .hpp, C++ header without extension, etc).
	 *
	 * @return {@code true} if the the file is a header file.
	 */
	public static boolean isHeader(String filename, IProject project) {
		IContentType ct = CCorePlugin.getContentType(project, filename);
		if (ct != null) {
			String id = ct.getId();
			if (CCorePlugin.CONTENT_TYPE_CHEADER.equals(id) || CCorePlugin.CONTENT_TYPE_CXXHEADER.equals(id)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the path of the given index file.
	 * @param file The index file.
	 * @return The path.
	 */
	public static String getPath(IIndexFile file) throws CoreException {
		return getPath(file.getLocation());
	}

	/**
	 * Returns the path of the given index file.
	 * @param fileLocation The index file location.
	 * @return The path.
	 */
	public static String getPath(IIndexFileLocation fileLocation) {
		return IndexLocationFactory.getAbsolutePath(fileLocation).toOSString();
	}

	public static boolean isContainedInRegion(IASTNode node, IRegion region) {
		return ASTNodes.offset(node) >= region.getOffset()
				&& ASTNodes.endOffset(node) <= region.getOffset() + region.getLength();
	}

	/**
	 * Returns the region containing nothing but include statements located before the first
	 * statement that may depend on includes.
	 *
	 * @param contents the contents of the translation unit
	 * @param ast the AST
	 * @param commentMap comments of the translation unit
	 * @return the include region, possibly empty
	 */
	public static IRegion getSafeIncludeReplacementRegion(String contents, IASTTranslationUnit ast,
			NodeCommentMap commentMap) {
		int maxSafeOffset = ast.getFileLocation().getNodeLength();
		IASTDeclaration[] declarations = ast.getDeclarations(true);
		if (declarations.length != 0)
			maxSafeOffset = declarations[0].getFileLocation().getNodeOffset();

		boolean topCommentSkipped = false;
		int includeOffset = -1;
		int includeEndOffset = -1;
		int includeGuardStatementsToSkip = getNumberOfIncludeGuardStatementsToSkip(contents, ast);
		int includeGuardEndOffset = -1;
		for (IASTPreprocessorStatement statement : ast.getAllPreprocessorStatements()) {
			if (statement.isPartOfTranslationUnitFile()) {
				IASTFileLocation fileLocation = statement.getFileLocation();
				int offset = fileLocation.getNodeOffset();
				if (offset >= maxSafeOffset)
					break;
				int endOffset = offset + fileLocation.getNodeLength();

				if (includeGuardStatementsToSkip > 0) {
					--includeGuardStatementsToSkip;
					includeGuardEndOffset = endOffset;
					if (!commentMap.getLeadingCommentsForNode(statement).isEmpty()) {
						topCommentSkipped = true;
					}
				} else if (statement instanceof IASTPreprocessorIncludeStatement) {
					if (includeOffset < 0)
						includeOffset = offset;
					includeEndOffset = endOffset;
					includeGuardStatementsToSkip = 0; // Just in case
				} else {
					break;
				}
			}
		}
		if (includeOffset < 0) {
			if (includeGuardEndOffset >= 0) {
				includeOffset = TextUtil.skipToNextLine(contents, includeGuardEndOffset);
			} else {
				includeOffset = 0;
			}
			if (!topCommentSkipped) {
				// Skip the first comment block near the top of the file.
				includeOffset = skipStandaloneCommentBlock(contents, includeOffset, maxSafeOffset, ast.getComments(),
						commentMap);
			}
			includeEndOffset = includeOffset;
		} else {
			includeEndOffset = TextUtil.skipToNextLine(contents, includeEndOffset);
		}
		return new Region(includeOffset, includeEndOffset - includeOffset);
	}

	/**
	 * Returns the include statements within the given region.
	 *
	 * @param existingIncludes the include statements to choose from
	 * @param region the region to select includes within
	 * @param inclusionContext the inclusion context
	 * @return a list of {@link StyledInclude} objects representing the includes
	 */
	public static List<StyledInclude> getIncludesInRegion(IASTPreprocessorIncludeStatement[] existingIncludes,
			IRegion region, InclusionContext inclusionContext) {
		// Populate a list of existing includes in the include insertion region.
		List<StyledInclude> includes = new ArrayList<>();
		for (IASTPreprocessorIncludeStatement include : existingIncludes) {
			if (include.isPartOfTranslationUnitFile() && isContainedInRegion(include, region)) {
				String name = new String(include.getName().getSimpleID());
				IncludeInfo includeInfo = new IncludeInfo(name, include.isSystemInclude());
				String path = include.getPath();
				// An empty path means that the include was not resolved.
				IPath header = path.isEmpty() ? null : Path.fromOSString(path);
				IncludeGroupStyle style = header != null ? inclusionContext.getIncludeStyle(header)
						: inclusionContext.getIncludeStyle(includeInfo);
				StyledInclude prototype = new StyledInclude(header, includeInfo, style, include);
				includes.add(prototype);
			}
		}
		return includes;
	}

	/**
	 * Searches for the include guard in the file and, if found, returns its value and occurrences
	 * in the file.
	 *
	 * @param contents the contents of the translation unit
	 * @param ast the AST
	 * @param includeGuardPositions the list of include guard occurrences that is populated by
	 *     the method
	 * @return the include guard, or {@code null} if not found
	 */
	public static String findIncludeGuard(String contents, IASTTranslationUnit ast,
			List<IRegion> includeGuardPositions) {
		includeGuardPositions.clear();
		IASTPreprocessorStatement[] preprocessorStatements = ast.getAllPreprocessorStatements();
		int i = 0;
		while (true) {
			if (i >= preprocessorStatements.length)
				return null;
			if (preprocessorStatements[i].isPartOfTranslationUnitFile())
				break;
			i++;
		}
		IASTPreprocessorStatement statement = preprocessorStatements[i];

		int offset = 0;
		if (isPragmaOnce(statement)) {
			offset = ASTNodes.endOffset(statement);
			i++;
		}
		char[] guardChars = detectIncludeGuard(contents, offset);
		if (guardChars == null)
			return null;
		String guard = new String(guardChars);
		int count = 0;
		IASTPreprocessorStatement lastStatement = null;
		for (; i < preprocessorStatements.length; i++) {
			statement = preprocessorStatements[i];
			if (statement.isPartOfTranslationUnitFile()) {
				if (count < 2) {
					findGuardInRange(contents, guard, ASTNodes.offset(statement), ASTNodes.endOffset(statement),
							includeGuardPositions);
					count++;
				} else {
					lastStatement = statement;
				}
			}
		}
		if (lastStatement != null) {
			findGuardInRange(contents, guard, ASTNodes.offset(lastStatement), contents.length(), includeGuardPositions);
		}
		return guard;
	}

	private static int getNumberOfIncludeGuardStatementsToSkip(String contents, IASTTranslationUnit ast) {
		IASTPreprocessorStatement statement = findFirstPreprocessorStatement(ast);
		if (statement == null)
			return 0;

		int num = 0;
		int offset = 0;
		if (isPragmaOnce(statement)) {
			num++;
			offset = ASTNodes.endOffset(statement);
		}
		char[] guard = detectIncludeGuard(contents, offset);
		if (guard != null) {
			num += 2;
		}
		return num;
	}

	private static char[] detectIncludeGuard(String contents, int offset) {
		char[] contentsChars = contents.toCharArray();
		if (offset != 0)
			contentsChars = Arrays.copyOfRange(contentsChars, offset, contentsChars.length);
		CharArrayIntMap ppKeywords = new CharArrayIntMap(40, -1);
		Keywords.addKeywordsPreprocessor(ppKeywords);
		char[] guardChars = IncludeGuardDetection.detectIncludeGuard(new CharArray(contentsChars), new LexerOptions(),
				ppKeywords);
		return guardChars;
	}

	private static void findGuardInRange(String contents, String guard, int offset, int endOffset,
			List<IRegion> includeGuardPositions) {
		int pos = contents.indexOf(guard, offset);
		if (pos >= 0 && pos + guard.length() <= endOffset) {
			includeGuardPositions.add(new Region(pos, guard.length()));
		}
	}

	private static int skipStandaloneCommentBlock(String contents, int offset, int endOffset, IASTComment[] comments,
			NodeCommentMap commentMap) {
		Map<IASTComment, IASTNode> inverseLeadingMap = new HashMap<>();
		for (Map.Entry<IASTNode, List<IASTComment>> entry : commentMap.getLeadingMap().entrySet()) {
			IASTNode node = entry.getKey();
			if (ASTNodes.offset(node) <= endOffset) {
				for (IASTComment comment : entry.getValue()) {
					inverseLeadingMap.put(comment, node);
				}
			}
		}
		Map<IASTComment, IASTNode> inverseFreestandingMap = new HashMap<>();
		for (Map.Entry<IASTNode, List<IASTComment>> entry : commentMap.getFreestandingMap().entrySet()) {
			IASTNode node = entry.getKey();
			if (ASTNodes.endOffset(node) < endOffset) {
				for (IASTComment comment : entry.getValue()) {
					inverseFreestandingMap.put(comment, node);
				}
			}
		}

		for (int i = 0; i < comments.length; i++) {
			IASTComment comment = comments[i];
			int commentOffset = ASTNodes.offset(comment);
			if (commentOffset >= offset) {
				if (commentOffset >= endOffset)
					break;
				IASTNode node = inverseLeadingMap.get(comment);
				if (node != null) {
					List<IASTComment> leadingComments = commentMap.getLeadingMap().get(node);
					IASTComment previous = leadingComments.get(0);
					for (int j = 1; j < leadingComments.size(); j++) {
						comment = leadingComments.get(j);
						if (ASTNodes.getStartingLineNumber(comment) > ASTNodes.getEndingLineNumber(previous) + 1)
							return ASTNodes.skipToNextLineAfterNode(contents, previous);
						previous = comment;
					}
					if (ASTNodes.getStartingLineNumber(node) > ASTNodes.getEndingLineNumber(previous) + 1)
						return ASTNodes.skipToNextLineAfterNode(contents, previous);
				}
				node = inverseFreestandingMap.get(comment);
				if (node != null) {
					List<IASTComment> freestandingComments = commentMap.getFreestandingMap().get(node);
					IASTComment previous = freestandingComments.get(0);
					for (int j = 1; j < freestandingComments.size(); j++) {
						comment = freestandingComments.get(j);
						if (ASTNodes.getStartingLineNumber(comment) > ASTNodes.getEndingLineNumber(previous) + 1)
							return ASTNodes.skipToNextLineAfterNode(contents, previous);
						previous = comment;
					}
				}
			}
		}
		return offset;
	}

	private static IASTPreprocessorStatement findFirstPreprocessorStatement(IASTTranslationUnit ast) {
		for (IASTPreprocessorStatement statement : ast.getAllPreprocessorStatements()) {
			if (statement.isPartOfTranslationUnitFile())
				return statement;
		}
		return null;
	}

	private static boolean isPragmaOnce(IASTPreprocessorStatement statement) {
		if (!(statement instanceof IASTPreprocessorPragmaStatement))
			return false;
		return CharArrayUtils.equals(((IASTPreprocessorPragmaStatement) statement).getMessage(), "once"); //$NON-NLS-1$
	}
}
