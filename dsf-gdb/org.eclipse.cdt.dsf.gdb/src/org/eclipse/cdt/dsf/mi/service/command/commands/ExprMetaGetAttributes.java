/*******************************************************************************
 * Copyright (c) 2008, 2009 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson           - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.ExprMetaGetAttributesInfo;

public class ExprMetaGetAttributes extends ExprMetaCommand<ExprMetaGetAttributesInfo> {

	public ExprMetaGetAttributes(IExpressionDMContext ctx) {
		super(ctx);
	}
}