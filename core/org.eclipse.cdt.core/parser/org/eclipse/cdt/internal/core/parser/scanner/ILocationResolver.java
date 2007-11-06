/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
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
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IMacroBinding;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit.IDependencyTree;


/**
 * Interface between the ast and the location-resolver for resolving offsets.
 * @since 5.0
 */
public interface ILocationResolver extends org.eclipse.cdt.internal.core.parser.scanner2.ILocationResolver {
    
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
     * Returns the comments encountered.
     */
	IASTComment[] getComments();

	/**
     * @see IASTTranslationUnit#getFilePath()
     */
    String getTranslationUnitPath();
    
    /**
     * @see IASTTranslationUnit#getContainingFilename()
     * mstodo- scanner removal should be renamed
     */
	String getContainingFilename(int sequenceNumber);
	
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
     * @see IASTTranslationUnit#getLocationInfo(int, int).
     */
	IASTNodeLocation[] getLocations(int sequenceNumber, int length);

	/**
	 * Returns the sequence-number for the given file-path and offset, or <code>-1</code> if this file
	 * is not part of the translation-unit.
	 * @param filePath a file path or <code>null</code> to specify the root of the translation unit.
	 * @param fileOffset an offset into the source of the file.
	 */
	int getSequenceNumberForFileOffset(String filePath, int fileOffset);

	/**
	 * @see IASTTranslationUnit#getUnpreprocessedSignature(IASTFileLocation).
	 */
	char[] getUnpreprocessedSignature(IASTFileLocation loc);
	
	/**
	 * Returns a preprocessor node surrounding the given range, or <code>null</code>.
	 */
	IASTNode findSurroundingPreprocessorNode(int sequenceNumber, int length);
}
