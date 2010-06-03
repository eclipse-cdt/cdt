/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.internal.core.browser;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.browser.TypeReference;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.index.IIndexMacro;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.model.ext.CElementHandleFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

/**
 * A {@link IndexTypeReference} tailored for index bindings.
 * 
 * @since 4.0
 */
public class IndexTypeReference extends TypeReference {

	private ICElement fCElement;

	public IndexTypeReference(IBinding binding, IPath path, IProject project, int offset, int length) {
		super(path, project, offset, length);
		fCElement= createCElement(binding);
	}
	
	public IndexTypeReference(IBinding binding, IResource resource, IProject project, int offset, int length) {
		super(resource, project, offset, length);
		fCElement= createCElement(binding);
	}

	public IndexTypeReference(IIndexMacro macro, IPath path, IProject project, int offset, int length) {
		super(path, project, offset, length);
		fCElement= createCElement(macro);
	}
	
	public IndexTypeReference(IIndexMacro macro, IResource resource, IProject project, int offset, int length) {
		super(resource, project, offset, length);
		fCElement= createCElement(macro);
	}

	/**
	 * Compute the C element handle for the given binding.
	 * @param binding
	 */
	private ICElement createCElement(IBinding binding) {
		ITranslationUnit tu= getTranslationUnit();
		if (tu != null) {
			long timestamp= tu.getResource() != null ? tu.getResource().getLocalTimeStamp() : 0;
			IRegion region= new Region(getOffset(), getLength());
			try {
				return CElementHandleFactory.create(tu, binding, true, region, timestamp);
			} catch (CoreException e) {
				CCorePlugin.log(e);
			}
		}
		return null;
	}

	/**
	 * Compute the C element handle for the given macro.
	 */
	private ICElement createCElement(IIndexMacro macro) {
		ITranslationUnit tu= getTranslationUnit();
		if (tu != null) {
			long timestamp= tu.getResource() != null ? tu.getResource().getLocalTimeStamp() : 0;
			IRegion region= new Region(getOffset(), getLength());
			try {
				return CElementHandleFactory.create(tu, macro, region, timestamp);
			} catch (CoreException e) {
				CCorePlugin.log(e);
			}
		}
		return null;
	}

	/*
	 * @see org.eclipse.cdt.core.browser.ITypeReference#getCElements()
	 */
	@Override
	public ICElement[] getCElements() {
		if (fCElement != null) {
			return new ICElement[] { fCElement };
		}
		return super.getCElements();
	}

}
