/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 * Created on Dec 16, 2004
 */
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;

/**
 * This binding is a container for other bindings.  It is used in instances
 * where an IASTName refers to more than one binding, for example a using declaration
 * refering to a set of overloaded functions. 
 * @author aniefer
 */
public interface ICPPCompositeBinding extends IBinding {
    /**
     * get the bindings 
     * @return
     * @throws DOMException
     */
	IBinding [] getBindings() throws DOMException;
}
