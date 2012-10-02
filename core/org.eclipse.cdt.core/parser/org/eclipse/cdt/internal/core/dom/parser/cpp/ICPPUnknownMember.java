/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IType;

/**
 * Represents the binding for a dependent name within a template declaration.
 */
public interface ICPPUnknownMember extends ICPPUnknownBinding {
	/**
	 * For unknown bindings the owner may just be an unknown type that is not yet resolved to a binding.
	 */
	public IType getOwnerType();
}
