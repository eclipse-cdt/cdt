/*******************************************************************************
 * Copyright (c) 2018 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Hansruedi Patzen (IFS) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.ms;

import org.eclipse.cdt.core.dom.ast.IASTAttributeList;
import org.eclipse.cdt.core.dom.ast.IASTAttributeSpecifier;
import org.eclipse.cdt.core.parser.util.InstanceOfPredicate;

/**
 * Represents a Microsoft attribute specifier, introduced by __declspec.
 *
 * @since 6.5
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IMSASTDeclspecList extends IASTAttributeList {
	public static InstanceOfPredicate<IASTAttributeSpecifier> TYPE_FILTER =
			new InstanceOfPredicate<>(IMSASTDeclspecList.class);
}
