/*******************************************************************************
 * Copyright (c) 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.dom;

import org.eclipse.cdt.core.dom.ast.ASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.pdom.dom.PDOMBinding;

/**
 * @author Doug Schaefer
 *
 */
public interface ILanguage {

	//public static final QualifiedName KEY = new QualifiedName(CCorePlugin.PLUGIN_ID, "language"); //$NON-NLS-1$
	public static final String KEY = "language"; //$NON-NLS-1$

	/**
	 * Style for getTranslationUnit. Use the index for resolving bindings that aren't
	 * found in the AST.
	 */
	public static final int AST_USE_INDEX = 1;

	/**
	 * Style for getTranslationUnit. Don't parse header files. It's a good idea to
	 * turn on AST_USE_INDEX when you do this.
	 */
	public static final int AST_SKIP_ALL_HEADERS = 2;

	/**
	 * Style for getTranslationUnit. Used by the indexer to skip over headers it
	 * already has indexed.
	 */
	public static final int AST_SKIP_INDEXED_HEADERS = 4;

	/**
	 * Create the AST for the given translation unit with the given style.
	 * 
	 * @param file
	 * @param style
	 * @return
	 */
	public IASTTranslationUnit getTranslationUnit(ITranslationUnit file, int style);

	/**
	 * Return the AST Completion Node for the given working copy at the given
	 * offset.
	 * 
	 * @param workingCopy
	 * @param offset
	 * @return
	 */
	public ASTCompletionNode getCompletionNode(IWorkingCopy workingCopy, int offset);

	/**
	 * Return the PDOM Binding for the given AST binding.
	 *  
	 * @param binding
	 * @return
	 */
	public PDOMBinding getPDOMBinding(IBinding binding);

	/**
	 * Return a new PDOM Binding that has the given language specific type.
	 * The type id is extracted from the PDOM Database.
	 * 
	 * @param bindingType
	 * @return
	 */
	public PDOMBinding createPDOMBinding(int bindingType);
	
}
