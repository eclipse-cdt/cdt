/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
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
