/*******************************************************************************
 * Copyright (c) 2018 Manish Khurana , Nathan Ridge and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.lsp4e.cpp.language;

import java.net.URI;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import com.google.gson.JsonObject;

public class CqueryLanguageServer implements ICPPLanguageServer {

	@Override
	public Object getLSSpecificInitializationOptions(Object defaultInitOptions, URI rootPath) {
//		TODO: Allow user to specify cache directory path
		IPath cacheDirectory = Path.fromOSString(rootPath.getPath()).append(".lsp4e-cpp/cquery_index"); //$NON-NLS-1$
		JsonObject result = (defaultInitOptions instanceof JsonObject) ? (JsonObject) defaultInitOptions : new JsonObject();
		result.addProperty("cacheDirectory", cacheDirectory.toString()); //$NON-NLS-1$
		return result;
	}

}
