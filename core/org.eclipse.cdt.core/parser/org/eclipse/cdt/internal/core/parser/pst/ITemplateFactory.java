/**********************************************************************
 * Copyright (c) 2004 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.core.parser.pst;

import java.util.List;

/**
 * @author aniefer
 **/

public interface ITemplateFactory extends IDerivableContainerSymbol {
	public void pushTemplate( ITemplateSymbol template );
	public void pushSymbol( ISymbol symbol );
	public void pushTemplateId( ISymbol symbol, List args );
}
