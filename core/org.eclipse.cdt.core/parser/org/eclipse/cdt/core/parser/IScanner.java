/*******************************************************************************
 * Copyright (c) 2003, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.parser;

import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IMacroBinding;
import org.eclipse.cdt.internal.core.parser.scanner.ILocationResolver;
import org.eclipse.cdt.internal.core.parser.scanner.Lexer;

/**
 * Interface between the parser and the preprocessor. 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will
 * work or that it will remain the same. Please do not use this API without
 * consulting with the CDT team.
 * </p>
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IScanner {
	/**
	 * Returns a map from {@link String} to {@link IMacroBinding} containing
	 * all the definitions that are defined at the current point in the 
	 * process of scanning.
	 */
	public Map<String, IMacroBinding> getMacroDefinitions();

	/**
     * Returns next token for the parser. String literals are concatenated.
     * @throws EndOfFileException when the end of the translation unit has been reached.
     * @throws OffsetLimitReachedException see {@link Lexer}.
     */
	public IToken nextToken() throws EndOfFileException;
			
	/**
	 * Returns <code>true</code>, whenever we are processing the outermost file of the translation unit.
	 */
	public boolean isOnTopContext();
	
	/**
	 * Attempts to cancel the scanner.
	 */
	public void cancel();
	
	/**
	 * Returns the location resolver associated with this scanner.
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public ILocationResolver getLocationResolver();
	
	/**
	 * Puts the scanner into content assist mode.
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public void setContentAssistMode(int offset);

	/**
	 * Instructs the scanner to split tokens of kind {@link IToken#tSHIFTR} into two tokens of
	 * kind {@link IToken#tGT_in_SHIFTR}.
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public void setSplitShiftROperator(boolean val);
	
	/**
	 * Turns on/off creation of image locations. 
	 * @see IASTName#getImageLocation()
	 * @noreference This method is not intended to be referenced by clients.
	 * @since 5.0
	 */
	public void setComputeImageLocations(boolean val);
	
	/**
	 * Toggles generation of tokens for inactive code branches. When turned on,
	 * each inactive code branch is preceded by a token of kind {@link IToken#tINACTIVE_CODE_START} and
	 * succeeded by one of kind {@link IToken#tINACTIVE_CODE_END}.
	 * 
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public void setProcessInactiveCode(boolean val);

	/**
	 * When in inactive code, skips all tokens up to the end of the inactive code section. 
	 * <p> Note, token after calling this method may be another token of type {@link IToken#tINACTIVE_CODE_START}.
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public void skipInactiveCode() throws OffsetLimitReachedException;

	/**
	 * Returns the current nesting in code branches.
	 * @see IInactiveCodeToken#getOldNesting()
	 * @see IInactiveCodeToken#getNewNesting()
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public int getCodeBranchNesting();
	
	/**
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Deprecated
	public void setScanComments(boolean val);
}
