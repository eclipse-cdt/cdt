/*******************************************************************************
 * Copyright (c) 2018 Manish Khurana , Nathan Ridge and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.lsp4e.cpp.language;

import java.net.URI;

public class ClangdLanguageServer implements ICPPLanguageServer {

	@Override
	public Object getLSSpecificInitializationOptions(Object defaultInitOptions,URI rootPath) {
		return defaultInitOptions;
	}

}
