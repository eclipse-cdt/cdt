/*******************************************************************************
 * Copyright (c) 2017 Ericsson.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.cdt.internal.ui;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdapterFactory;

import org.eclipse.cdt.core.model.CoreModelUtil;
import org.eclipse.cdt.core.model.ITranslationUnit;


/**
 * An adapter factory that adapts resources to CElements. This was introduced in
 * the context of non-extensible content providers (Package Explorer) which
 * contain plain resources and not CElements. This allows some contributions to
 * work without explicitly depending the content provider in order to extend it.
 */
public class ResourceToCElementAdapterFactory implements IAdapterFactory {

	private static final Class<?>[] ADAPTER_LIST = new Class<?>[] { ITranslationUnit.class };

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
		if (adaptableObject instanceof IFile && adapterType.equals(ITranslationUnit.class)) {
			return (T) CoreModelUtil.findTranslationUnit((IFile) adaptableObject);
		}
		return null;
	}

	@Override
	public Class<?>[] getAdapterList() {
		return ADAPTER_LIST;
	}
}
