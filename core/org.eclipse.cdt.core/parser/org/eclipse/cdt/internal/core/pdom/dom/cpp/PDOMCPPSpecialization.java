/*******************************************************************************
 * Copyright (c) 2008, 2014 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Bryan Wilkinson (QNX) - Initial API and implementation
 *    Andrew Ferguson (Symbian)
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTemplateParameterMap;
import org.eclipse.cdt.internal.core.index.IndexCPPSignatureUtil;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMOverloader;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * Base class for specializations and instances of other bindings.
 */
abstract class PDOMCPPSpecialization extends PDOMCPPBinding implements ICPPSpecialization, IPDOMOverloader {
	private static final int ARGMAP = PDOMCPPBinding.RECORD_SIZE + 0;
	private static final int SIGNATURE_HASH = PDOMCPPBinding.RECORD_SIZE + 4;
	private static final int SPECIALIZED = PDOMCPPBinding.RECORD_SIZE + 8;
	/**
	 * The size in bytes of a PDOMCPPSpecialization record in the database.
	 */
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = PDOMCPPBinding.RECORD_SIZE + 12;

	private volatile IBinding fSpecializedCache;
	private volatile ICPPTemplateParameterMap fArgMap;

	public PDOMCPPSpecialization(PDOMCPPLinkage linkage, PDOMNode parent, ICPPSpecialization spec,
			IPDOMBinding specialized) throws CoreException {
		super(linkage, parent, spec.getNameCharArray());
		getDB().putRecPtr(record + SPECIALIZED, specialized.getRecord());

		// Specializations that are not instances have the same map as their owner except for friend functions
		// declared inside a class template that are not owned by the template, but have their own template
		// parameter maps.
		if (this instanceof ICPPTemplateInstance || !(parent instanceof ICPPSpecialization)) {
			// Defer storing of template parameter map to the post-process to avoid infinite recursion
			// when the evaluation of a non-type template argument tries to store its template definition.
			// Until the post-process runs, temporarily store the input (possibly non-PDOM) map.
			fArgMap = spec.getTemplateParameterMap();
			linkage.new ConfigureInstance(this);
		}
		try {
			Integer sigHash = IndexCPPSignatureUtil.getSignatureHash(spec);
			getDB().putInt(record + SIGNATURE_HASH, sigHash != null ? sigHash.intValue() : 0);
		} catch (DOMException e) {
		}
	}

	public PDOMCPPSpecialization(PDOMLinkage linkage, long bindingRecord) {
		super(linkage, bindingRecord);
	}

	@Override
	public IBinding getSpecializedBinding() {
		if (fSpecializedCache == null) {
			try {
				long specializedRec = getDB().getRecPtr(record + SPECIALIZED);
				fSpecializedCache = loadSpecializedBinding(specializedRec);
			} catch (CoreException e) {
				CCorePlugin.log(e);
			}
		}
		return fSpecializedCache;
	}

	protected IPDOMBinding loadSpecializedBinding(long specializedRec) throws CoreException {
		return (IPDOMBinding) PDOMNode.load(getPDOM(), specializedRec);
	}

	@Override
	public ICPPTemplateParameterMap getTemplateParameterMap() {
		if (fArgMap == null) {
			try {
				if (this instanceof ICPPTemplateInstance) {
					fArgMap = PDOMCPPTemplateParameterMap.getMap(this, getDB().getRecPtr(record + ARGMAP));
				} else {
					// Specializations that are not instances have the same template parameter map as their
					// owner.
					IBinding owner = getOwner();
					if (owner instanceof ICPPSpecialization) {
						fArgMap = ((ICPPSpecialization) owner).getTemplateParameterMap();
					} else {
						// Friend functions declared inside a class template are not owned by the template,
						// but have their own template parameter maps.
						fArgMap = PDOMCPPTemplateParameterMap.getMap(this, getDB().getRecPtr(record + ARGMAP));
					}
				}
			} catch (CoreException e) {
				CCorePlugin.log(e);
				return CPPTemplateParameterMap.EMPTY;
			}
		}
		return fArgMap;
	}

	public void storeTemplateParameterMap() {
		try {
			// fArgMap here is the temporarily stored, possibly non-PDOM map stored by the constructor.
			// Construct the PDOM map and store it.
			long rec = PDOMCPPTemplateParameterMap.putMap(this, fArgMap);
			getDB().putRecPtr(record + ARGMAP, rec);

			// Read the stored map next time getTemplateParameterMap() is called.
			fArgMap = null;
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
	}

	@Override
	public int getSignatureHash() throws CoreException {
		return getDB().getInt(record + SIGNATURE_HASH);
	}
}
