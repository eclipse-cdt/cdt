/*******************************************************************************
 * Copyright (c) 2007, 2008 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.search;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;

/**
 * Finds locations of linked names. Used by Rename in File.
 */
public class LinkedNamesFinder {
	private static final IRegion[] EMPTY_LOCATIONS_ARRAY = new IRegion[0]; 

	public LinkedNamesFinder() {
		super();
	}

	public static IRegion[] findByName(IASTTranslationUnit root, IASTName name) {
		IBinding target = name.resolveBinding();
		if (target == null) {
			return EMPTY_LOCATIONS_ARRAY;
		}
		BindingFinder bindingFinder = new BindingFinder(root);
		bindingFinder.find(target);
		return bindingFinder.getLocations();
	}

	private static class BindingFinder {
		private final IASTTranslationUnit root;
		private final List<IRegion> locations;
		
		public BindingFinder(IASTTranslationUnit root) {
			this.root = root;
			locations = new ArrayList<IRegion>();
		}

		public void find(IBinding target) {
			try {
				if (target instanceof ICPPConstructor ||
						target instanceof ICPPMethod && ((ICPPMethod) target).isDestructor()) {
					target = ((ICPPMethod) target).getClassOwner();
				}
			} catch (DOMException e1) {
			}

			findBinding(target);
			if (target instanceof ICPPClassType) {
				try {
					ICPPConstructor[] constructors = ((ICPPClassType) target).getConstructors();
					for (IBinding ctor : constructors) {
						findBinding(ctor);
					}
					ICPPMethod[] methods = ((ICPPClassType) target).getMethods();
					for (ICPPMethod method : methods) {
						if (method.isDestructor()) {
							findBinding(method);
						}
					}
				} catch (DOMException e) {
				}
			}
		}

		public IRegion[] getLocations() {
			if (locations.isEmpty()) {
				return EMPTY_LOCATIONS_ARRAY;
			}
			return locations.toArray(new IRegion[locations.size()]);
		}

		private void findBinding(IBinding target) {
			IASTName[] names= root.getDeclarationsInAST(target);
			for (int i= 0; i < names.length; i++) {
				IASTName candidate= names[i];
				if (candidate.isPartOfTranslationUnitFile()) {
					addLocation(candidate);
				}
			}
			names= root.getReferences(target);
			for (int i= 0; i < names.length; i++) {
				IASTName candidate= names[i];
				if (candidate.isPartOfTranslationUnitFile()) {
					addLocation(candidate);
				}
			}
		}

		private void addLocation(IASTName name) {
			IBinding binding = name.resolveBinding();
			if (binding != null) {
				if (name instanceof ICPPASTTemplateId) {
					name= ((ICPPASTTemplateId) name).getTemplateName();
				}
				IASTFileLocation fileLocation= name.getImageLocation();
				if (fileLocation == null || !root.getFilePath().equals(fileLocation.getFileName())) {
					fileLocation= name.getFileLocation();
				}
				if (fileLocation != null) {
					int offset= fileLocation.getNodeOffset();
					int length= fileLocation.getNodeLength();
					try {
						if (binding instanceof ICPPMethod && ((ICPPMethod) binding).isDestructor()) {
							// Skip tilde.
							offset++;
							length--;
						}
					} catch (DOMException e) {
					}
					if (offset >= 0 && length > 0) {
						locations.add(new Region(offset, length));
					}
				}
			}
		}
	}
}
