/*******************************************************************************
 * Copyright (c) 2016, 2017 Kichwa Coders Ltd (https://kichwacoders.com/) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.linkerscript;

public class WildIDValueConverter extends IDValueConverter {
	@Override
	protected boolean idNeedsQuoting(String id) {
		if ("*".equals(id)) {
			return false;
		}

		return super.idNeedsQuoting(id);
	}
}