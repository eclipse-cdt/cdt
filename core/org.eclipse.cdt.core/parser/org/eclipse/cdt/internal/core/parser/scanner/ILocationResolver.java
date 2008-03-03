/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser.scanner;

import org.eclipse.cdt.core.dom.ast.IASTComment;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTImageLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroExpansion;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IMacroBinding;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit.IDependencyTree;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.ASTNodeMatchKind;


/**
 * Interface between the ast and the location-resolver for resolving offsets.
 * @since 5.0
 */
public interface ILocationResolver {    
	/**
	 * Introduces the ast translation unit to the location resolver. Must be called before any tokens from the
	 * scanner are obtained.
	 */
	void setRootNode(IASTTranslationUnit tu);

	/**
	 * @see IASTTranslationUnit#getAllPreprocessorStatements()
	 */
	IASTPreprocessorStatement [] getAllPreprocessorStatements();

	/**
	 * @see IASTTranslationUnit#getMacroDefinitions()
	 */
	IASTPreprocessorMacroDefinition [] getMacroDefinitions();

	/**
	 * @see IASTTranslationUnit#getBuiltinMacroDefinitions()
	 */
	IASTPreprocessorMacroDefinition [] getBuiltinMacroDefinitions();

	/**
	 * @see IASTTranslationUnit#getIncludeDirectives()
	 */
	IASTPreprocessorIncludeStatement [] getIncludeDirectives();

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
	 * found in the same file. So the resulting location contains the include directives that actually 
	 * cause the range to be part of the AST.
	 * @param offset sequence number as stored in the ASTNodes.
	 * @param length 
	 */
	IASTFileLocation getMappedFileLocation(int offset, int length);

	/**
	  * Returns an array of locations. This is a sequence of file locations and macro-expansion locations.
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
	 * Returns the sequence-number for the given file-path and offset, or <code>-1</code> if this file
	 * is not part of the translation-unit.
	 * @param filePath a file path or <code>null</code> to specify the root of the translation unit.
	 * @param fileOffset an offset into the source of the file, or <code>-1</code>.
	 */
	int getSequenceNumberForFileOffset(String filePath, int fileOffset);

	/**
	 * @see IASTNode#getRawSignature().
	 */
	char[] getUnpreprocessedSignature(IASTFileLocation loc);
	
	/**
	 * Returns a preprocessor node surrounding the given range, or <code>null</code>. The result is either a
	 * preprocessing directive ({@link IASTPreprocessorStatement}) or a name contained therein {@link IASTName} or 
	 * a macro expansion ({@link IASTName}).
	 * 
	 * @param sequenceNumber the sequence number of the start of the interesting region.
	 * @param length the sequence length of the interesting region.
	 * @param matchOption the kind of the desired match.
	 */
	ASTNode findPreprocessorNode(int sequenceNumber, int length, ASTNodeMatchKind matchOption);

	/**
	 * Returns whether the specified sequence number points into the root file of the
	 * translation unit, or not.
	 * @param offset
	 */
	boolean isPartOfTranslationUnitFile(int sequenceNumber);

	/**
	 * Same as {@link #getMappedFileLocation(int, int)} for the given array of consecutive node locations.
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
}
