/*******************************************************************************
 * Copyright (c) 2008, 2011 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;
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
	
	private volatile IBinding fSpecializedCache= null;
	private volatile ICPPTemplateParameterMap fArgMap;
	
	public PDOMCPPSpecialization(PDOMLinkage linkage, PDOMNode parent, ICPPSpecialization spec,
			IPDOMBinding specialized) throws CoreException {
		super(linkage, parent, spec.getNameCharArray());
		getDB().putRecPtr(record + SPECIALIZED, specialized.getRecord());

		// Specializations that are not instances have the same map as their owner.
		if (this instanceof ICPPTemplateInstance) {
			long rec= PDOMCPPTemplateParameterMap.putMap(this, spec.getTemplateParameterMap());
			getDB().putRecPtr(record + ARGMAP, rec);
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
				fSpecializedCache= loadSpecializedBinding(specializedRec);
			} catch (CoreException e) {
				CCorePlugin.log(e);
			}
		}
		return fSpecializedCache;
	}

	protected IPDOMBinding loadSpecializedBinding(long specializedRec) throws CoreException {
		return (IPDOMBinding) getLinkage().getNode(specializedRec);
	}
		
	@Override
	@Deprecated
	public ObjectMap getArgumentMap() {
		return CPPTemplates.getArgumentMap(this, getTemplateParameterMap());
	}
	
	@Override
	public ICPPTemplateParameterMap getTemplateParameterMap() {
		if (fArgMap == null) {
			try {
				if (this instanceof ICPPTemplateInstance) {
					fArgMap= PDOMCPPTemplateParameterMap.getMap(this, getDB().getRecPtr(record + ARGMAP));
				} else {
					// specializations that are no instances have the same argmap as their owner.
					IBinding owner= getOwner();
					if (owner instanceof ICPPSpecialization) {
						fArgMap= ((ICPPSpecialization) owner).getTemplateParameterMap();
					}
				}
			} catch (CoreException e) {
				CCorePlugin.log(e);
			}
		}
		return fArgMap;
	}
	
	@Override
	public int getSignatureHash() throws CoreException {
		return getDB().getInt(record + SIGNATURE_HASH);
	}
}
