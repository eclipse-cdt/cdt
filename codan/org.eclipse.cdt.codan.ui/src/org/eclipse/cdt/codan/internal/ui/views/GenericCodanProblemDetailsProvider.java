/*******************************************************************************
 * Copyright (c) 2010 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.ui.views;

import org.eclipse.cdt.codan.ui.AbstractCodanProblemDetailsProvider;

/**
 * This provides details for errors that do not have own details provider
 */
public class GenericCodanProblemDetailsProvider extends AbstractCodanProblemDetailsProvider {
	@Override
	public boolean isApplicable(String id) {
		return true;
	}
}
