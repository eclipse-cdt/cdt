/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
/*
 * Created on Mar 28, 2005
 */
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.parser.util.ObjectMap;

/**
 * @author aniefer
 */
public interface ICPPInstance extends IBinding {
	
	/**
	 * Get the original binding of which this is an instance of
	 * @return
	 */
	public IBinding getOriginalBinding();
	
	/**
	 * return a map which maps from template parameter to the corresponding
	 * template argument 
	 * @return
	 */
	public ObjectMap getArgumentMap();
	
	public ICPPTemplateDefinition getTemplate();

}
