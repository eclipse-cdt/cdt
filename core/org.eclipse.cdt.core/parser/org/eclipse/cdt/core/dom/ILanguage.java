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
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.core.pdom.PDOMDatabase;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.core.runtime.CoreException;

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
	 * Return the language id for this language in the given PDOM.
	 * This is to differentiate languages from eachother.
	 * 
	 * @return language id
	 */
	public String getId();

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
	 * Return the PDOM Binding for the given name. Create a new one if necessary
	 * and store it in the PDOM.
	 *  
	 * @param binding
	 * @return
	 */
	public PDOMBinding getPDOMBinding(PDOMDatabase pdom, int languageId, IASTName name) throws CoreException;

	/**
	 * Creates a language specific binding given the generic binding extracted
	 * from the PDOM database.
	 *  
	 * @param pdom
	 * @param binding
	 * 
	 * @return
	 */
	public PDOMBinding getPDOMBinding(PDOMDatabase pdom, PDOMBinding binding) throws CoreException;

}
