/*******************************************************************************
 * Copyright (c) 2013 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.core.index;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMLinkageFactory;
import org.eclipse.core.runtime.CoreException;

/**
 * The PDOMASTProcessor extension point allows contributors to the org.eclipse.cdt.core.PDOMASTProcessor
 * extension-point to store their own information in the persisted index.  The intent is for
 * contributors to define their own ILinkage to avoid managing conflicts with the storage format
 * for existing linkages.
 * <p>
 * NOTE: The existing org.eclipse.cdt.core.language extension-point, allows new pdomLinkageFactories
 * to be added.  However, the {@link IPDOMLinkageFactory} interface which must be implemented is
 * in an internal package.
 *
 * @since 5.6
 * @noimplement Clients should extend {@link IPDOMASTProcessor.Abstract}.
 */
public interface IPDOMASTProcessor {
	/**
	 * Processes the input AST by adding significant symbols to the given output map.  Returns the linkage id
	 * that should be used to store the result, or {@link ILinkage#NO_LINKAGE_ID} if the AST contained nothing
	 * of significance to this processor.
	 *
	 * @param ast     The input AST to be processed.
	 * @param symbols The output map of significant symbols.
	 * @return        The linkage-id in which to store the symbols or {@link ILinkage#NO_LINKAGE_ID}
	 *                if the AST contained nothing of significance.
	 */
	public int process(IASTTranslationUnit ast, IIndexSymbols symbols) throws CoreException;

	/**
	 * An abstract class that should be extended by contributors of the extension-point.  Extending
	 * this class means that contributors will get default implementations for future additions.
	 */
	public static abstract class Abstract implements IPDOMASTProcessor {
		@Override
		public int process(IASTTranslationUnit ast, IIndexSymbols symbols) throws CoreException {
			return ILinkage.NO_LINKAGE_ID;
		}
	}
}
