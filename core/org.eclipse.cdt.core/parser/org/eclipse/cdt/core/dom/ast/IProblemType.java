/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.core.dom.ast;

/**
 * Represents an type that cannot be determined or is illegal. Reasons include
 * <ul>
 * <li> A type depends on a name that cannot be resolved (resolves to a {@link IProblemBinding}).
 * <li> The construction of a type is illegal.
 * </ul>
 * 
 * @since 5.3
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IProblemType extends IType, ISemanticProblem {

}
