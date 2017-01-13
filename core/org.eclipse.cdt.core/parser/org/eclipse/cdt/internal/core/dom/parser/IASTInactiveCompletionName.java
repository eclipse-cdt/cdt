/*******************************************************************************
 * Copyright (c) 2017 Nathan Ridge.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.IASTCompletionContext;
import org.eclipse.cdt.core.dom.ast.IASTName;

/**
 * Interface for a name representing a prefix being completed inside inactive code.
 */
public interface IASTInactiveCompletionName extends IASTName, IASTCompletionContext {
}