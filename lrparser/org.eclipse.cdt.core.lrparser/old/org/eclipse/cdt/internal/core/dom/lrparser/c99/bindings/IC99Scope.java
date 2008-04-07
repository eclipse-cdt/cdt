/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.lrparser.c99.bindings;

import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.c.ICScope;


/**
 * TODO maybe rename to IC99InternalScope or something else.
 * 
 * @author Mike Kucera
 */
public interface IC99Scope extends ICScope {

	void setParent(IScope parent);
	
	void setPhysicalNode(IASTNode node);
	
	void setScopeName(IName name);
}
