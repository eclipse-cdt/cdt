/*******************************************************************************
 * Copyright (c) 2008, 2012 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;

/**
 * Represents a binding for a type found in a template definition, that can be 
 * determined only after the template is instantiated. 
 * 
 * This interface should be made public.
 * @since 5.0
 */
public interface ICPPUnknownMemberClass extends ICPPUnknownMember, ICPPUnknownType, ICPPClassType {
}
