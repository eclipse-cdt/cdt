/*******************************************************************************
 * Copyright (c) 2004, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser.scanner;

import org.eclipse.cdt.core.dom.ast.IASTComment;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTImageLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroExpansion;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit.IDependencyTree;
import org.eclipse.cdt.core.dom.ast.IMacroBinding;
import org.eclipse.cdt.internal.core.dom.parser.ASTNodeSpecification;
import org.eclipse.cdt.internal.core.parser.scanner.Lexer.LexerOptions;

/**
 * Interface between the AST and the location-resolver for resolving offsets.
 * @since 5.0
 */
public interface ILocationResolver {
	/**
	 * Introduces the AST translation unit to the location resolver. Must be called before any
	 * tokens from the scanner are obtained.
	 */
	void setRootNode(IASTTranslationUnit tu);

	/**
	 * @see IASTTranslationUnit#getAllPreprocessorStatements()
	 */
	IASTPreprocessorStatement[] getAllPreprocessorStatements();

	/**
	 * @see IASTTranslationUnit#getMacroDefinitions()
	 */
	IASTPreprocessorMacroDefinition[] getMacroDefinitions();

	/**
	 * @see IASTTranslationUnit#getBuiltinMacroDefinitions()
	 */
	IASTPreprocessorMacroDefinition[] getBuiltinMacroDefinitions();

	/**
	 * @see IASTTranslationUnit#getIncludeDirectives()
	 */
	IASTPreprocessorIncludeStatement[] getIncludeDirectives();

	/**
	 * @see IASTTranslationUnit#getPreprocessorProblems()
	 */
	IASTProblem[] getScannerProblems();

	/**
	 * @see IASTTranslationUnit#getPreprocessorProblemsCount()
	 */
	int getScannerProblemsCount();

	/**
	 * Returns the comments encountered.
	 */
	IASTComment[] getComments();

	/**
	 * @see IASTTranslationUnit#getFilePath()
	 */
	String getTranslationUnitPath();

	/**
	 * @see IASTTranslationUnit#getContainingFilename()
	 */
	String getContainingFilePath(int sequenceNumber);

	/**
	 * @see IASTTranslationUnit#getDependencyTree()
	 */
	IDependencyTree getDependencyTree();

	/**
	 * Returns explicit and implicit references for a macro.
	 */
	IASTName[] getReferences(IMacroBinding binding);

	/**
	 * Returns the definition for a macro.
	 */
	IASTName[] getDeclarations(IMacroBinding binding);

	/**
	 * Returns the smallest file location, that encloses the given global range. In case the range
	 * spans over multiple files, the files are mapped to include statements until all of them are
	 * found in the same file. So the resulting location contains the include directives that
	 * actually cause the range to be part of the AST.
	 * @param offset sequence number as stored in the ASTNodes.
	 * @param length
	 */
	IASTFileLocation getMappedFileLocation(int offset, int length);

	/**
	  * Returns an array of locations. This is a sequence of file locations and macro-expansion
	  * locations.
	  *
	  * @param offset sequence number as stored in the ast nodes.
	  * @param length
	  * @return and array of locations.
	  */
	IASTNodeLocation[] getLocations(int sequenceNumber, int length);

	/**
	 * @see IASTName#getImageLocation()
	 */
	IASTImageLocation getImageLocation(int offset, int length);

	/**
	 * Returns the sequence-number for the given file-path and offset, or <code>-1</code> if this
	 * file is not part of the translation-unit.
	 * @param filePath a file path or <code>null</code> to specify the root of the translation unit.
	 * @param fileOffset an offset into the source of the file, or <code>-1</code>.
	 */
	int getSequenceNumberForFileOffset(String filePath, int fileOffset);

	/**
	 * @see IASTNode#getRawSignature()
	 */
	char[] getUnpreprocessedSignature(IASTFileLocation loc);

	/**
	 * Searches for a preprocessor node matching the given specification. Candidates are passed to
	 * nodeSpec, which selects and stores the best result.
	 *
	 * @param nodeSpec specification of node to search for.
	 */
	void findPreprocessorNode(ASTNodeSpecification<?> nodeSpec);

	/**
	 * Returns whether the specified sequence number points into the root file of the
	 * translation unit, or not.
	 * @param offset
	 */
	boolean isPartOfTranslationUnitFile(int sequenceNumber);

	/**
	 * Returns whether the specified sequence number points into a file that is considered a
	 * source file (even if it is included by some other file).
	 */
	boolean isPartOfSourceFile(int sequenceNumber);

	/**
	 * Same as {@link #getMappedFileLocation(int, int)} for the given array of consecutive node
	 * locations.
	 */
	IASTFileLocation flattenLocations(IASTNodeLocation[] nodeLocations);

	/**
	 * Returns all explicit macro expansions that intersect with the given file location.
	 * Include files that may be included within the given location are not examined.
	 * @param loc the file-location to search for macro references
	 * @return an array of macro expansions.
	 * @since 5.0
	 */
	IASTPreprocessorMacroExpansion[] getMacroExpansions(IASTFileLocation loc);

	/**
	 * If you want to use the sequence number of an ast-node as the end of a previous node,
	 * it needs to be adjusted, because gaps are used for the encoding of directives.
	 * @return the adjusted sequence number, to be used as end-number
	 */
	int convertToSequenceEndNumber(int sequenceNumber);

	/**
	 * Returns the lexer options that have been used by the preprocessor.
	 */
	LexerOptions getLexerOptions();
}
