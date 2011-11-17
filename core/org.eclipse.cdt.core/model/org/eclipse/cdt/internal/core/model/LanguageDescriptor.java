/*******************************************************************************
 * Copyright (c) 2007, 2008 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.model;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ILanguageDescriptor;
import org.eclipse.cdt.internal.core.CExtensionDescriptor;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;

public class LanguageDescriptor extends CExtensionDescriptor implements
		ILanguageDescriptor {
	private static final String ATTRIBUTE_CLASS = "class"; //$NON-NLS-1$
	private static final String ELEMENT_CONTENT_TYPE = "contentType"; //$NON-NLS-1$
	private static final String NAMESPACE_SEPARATOR = "."; //$NON-NLS-1$

	private ILanguage fLanguage;
	private String fContentTypeIds[];
	private String fId;
	private IContentType[] fContentTypes;


	public LanguageDescriptor(IConfigurationElement el) {
		super(el);
	}

	@Override
	public ILanguage getLanguage() {
		if(fLanguage == null){
			SafeRunner.run(new ISafeRunnable(){
				@Override
				public void handleException(Throwable exception) {
					CCorePlugin.log(exception);
				}

				@Override
				public void run() throws Exception {
					fLanguage = (ILanguage)getConfigurationElement().createExecutableExtension(ATTRIBUTE_CLASS);
				}
			});
		}
		return fLanguage;
	}

	@Override
	public String[] getContentTypeIds() {
		if(fContentTypeIds == null){
			fContentTypeIds = calculateCintentTypeIds();
		}
		return fContentTypeIds;
	}

	private String[] calculateCintentTypeIds(){
		IConfigurationElement el = getConfigurationElement();
		IConfigurationElement children[] = el.getChildren();
		String ids[] = new String[children.length];
		int num = 0;
		String tmp;
		if(children.length > 0){
			for(int i = 0; i < children.length; i++){
				if(ELEMENT_CONTENT_TYPE.equals(children[i].getName())){
					tmp = children[i].getAttribute(ATTRIBUTE_ID);
					if(tmp != null)
						ids[num++] = tmp;
				}
			}

			if(num < children.length){
				String t[] = new String[num];
				System.arraycopy(ids, 0, t, 0, num);
				ids = t;
			}
		}

		return ids;
	}

	@Override
	public String getId(){
		if(fId == null)
			fId = getConfigurationElement().getNamespaceIdentifier() + NAMESPACE_SEPARATOR + super.getId();
		return fId;
	}

	@Override
	public IContentType[] getContentTypes() {
		if(fContentTypes == null){
			fContentTypes = calculateContentTypes(getContentTypeIds());
		}
		return fContentTypes;
	}

	private IContentType[] calculateContentTypes(String ids[]){
		IContentType cTypes[] = new IContentType[ids.length];

		if(ids.length > 0){
			int num = 0;
			IContentTypeManager manager = Platform.getContentTypeManager();

			for (int k = 0; k < ids.length; ++k) {
				IContentType langContType = manager.getContentType(ids[k]);
				if(langContType != null)
					cTypes[num++] = langContType;
			}

			if(num < ids.length){
				IContentType tmp[] = new IContentType[num];
				System.arraycopy(cTypes, 0, tmp, 0, num);
				cTypes = tmp;
			}
		}
		return cTypes;
	}

}
