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
 *    John Camelon (IBM) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.gnu.cpp;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExplicitTemplateInstantiation;

/**
 * @deprecated Replaced by {@link ICPPASTExplicitTemplateInstantiation}
 * @noreference This interface is not intended to be referenced by clients.
 */
@Deprecated
public interface IGPPASTExplicitTemplateInstantiation extends ICPPASTExplicitTemplateInstantiation {
	/**
	 * <code>ti_static</code> implies 'static' keyword is used.
	 */
	public static final int ti_static = ICPPASTExplicitTemplateInstantiation.STATIC;

	/**
	 * <code>ti_inline</code> implies 'inline' keyword is used.
	 */
	public static final int ti_inline = ICPPASTExplicitTemplateInstantiation.INLINE;

	/**
	 * <code>ti_extern</code> implies 'extern' keyword is used.
	 */
	public static final int ti_extern = ICPPASTExplicitTemplateInstantiation.EXTERN;

	/**
	 * @since 5.1
	 */
	@Override
	public IGPPASTExplicitTemplateInstantiation copy();
}
