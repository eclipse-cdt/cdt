/*******************************************************************************
 * Copyright (c) 2013 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Eidsness - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.tag;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;

/**
 * Implementations are contributed with the org.eclipse.cdt.core.tagger extension-point.
 * The implementation is responsible for populating the tag's data using a given input binding.
 *
 * @see #process(ITagWriter, IBinding, IASTName)
 * @since 5.5
 */
public interface IBindingTagger {
	/**
	 * Examines the given input binding to decide if a tag should be created. Use the given
	 * tagWriter to create data if needed. Return the tag if one was created and null otherwise.
	 * A tagger (as identified by it's unique id string) is allowed to create only one tag for each
	 * binding.
	 *
	 * @param tagWriter
	 *            the writer to use for creating new tags
	 * @param binding
	 *            the binding to examine when populating the tag (if needed)
	 * @param ast
	 *            the AST name from which the binding was created
	 * @return the tag if one was created and null otherwise
	 */
	public ITag process(ITagWriter tagWriter, IBinding binding, IASTName ast);
}
