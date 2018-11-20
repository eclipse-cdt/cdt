/*
 * Copyright (c) 2013, 2014 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.cdt.internal.qt.core.pdom;

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNameOwner;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.IIndexFragmentName;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMIterator;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMName;
import org.eclipse.core.runtime.CoreException;

/**
 * Some Qt elements are introduced with empty macro expansions.  The Qt linkage handles this
 * by creating a new name and then adding it as a reference to the C++ language element.
 * This utility helps by containing that C++ name and the location of the Qt name.
 */
@SuppressWarnings("restriction")
public class ASTNameReference extends ASTDelegatedName {

	private final IASTFileLocation location;

	/**
	 * Create and return a name that will reference the given name.
	 */
	public ASTNameReference(IASTName name) {
		super(name);
		this.location = name.getFileLocation();
	}

	/**
	 * Create and return a name that will reference the given name from the given location.
	 */
	public ASTNameReference(IASTName name, IASTFileLocation location) {
		super(name);
		this.location = location;
	}

	/**
	 * Find and return the Qt binding that annotates the given PDOMBinding.  E.g., if the input binding
	 * is an instance of PDOMCPPClassType, then this method will return the QtPDOMQObject that was created
	 * from that class (or null if there is no such Qt element).
	 * <p>
	 * This is implemented by creating an ASTNameReference within the Qt element binding's definition.  That
	 * name is added as reference from the C++ PDOM binding.
	 */
	public static <T extends QtPDOMBinding> T findFromBinding(Class<T> cls, PDOMBinding binding) throws CoreException {
		if (binding == null)
			return null;

		// Look for external references to the binding.
		IPDOMIterator<PDOMName> pdomIterator = binding.getExternalReferences();
		while (pdomIterator.hasNext()) {
			PDOMName extRef = pdomIterator.next();
			IIndexName caller = extRef.getEnclosingDefinition();
			if (caller instanceof IIndexFragmentName) {
				IIndexFragmentBinding extRefBinding = ((IIndexFragmentName) caller).getBinding();
				if (cls.isAssignableFrom(extRefBinding.getClass()))
					return cls.cast(extRefBinding);
			}
		}

		return null;
	}

	@Override
	public IBinding resolveBinding() {
		if (binding == null)
			binding = delegate.resolveBinding();
		return binding;
	}

	@Override
	public IASTFileLocation getFileLocation() {
		return location;
	}

	@Override
	public boolean isReference() {
		return true;
	}

	@Override
	public boolean isDefinition() {
		return false;
	}

	@Override
	public boolean isDeclaration() {
		return false;
	}

	@Override
	public int getRoleOfName(boolean allowResolution) {
		return IASTNameOwner.r_reference;
	}
}
