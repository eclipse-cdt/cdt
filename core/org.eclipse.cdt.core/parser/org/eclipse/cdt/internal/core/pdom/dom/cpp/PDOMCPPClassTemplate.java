/*******************************************************************************
 * Copyright (c) 2007, 2011 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Bryan Wilkinson (QNX) - Initial API and implementation
 *    Andrew Ferguson (Symbian)
 *    Markus Schorn (Wind River Systems)
 *    Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import java.util.ArrayList;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateNonTypeParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTypeParameter;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPDeferredClassInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInstanceCache;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * Implementation for class templates in the index, base class for partial specializations.
 */
public class PDOMCPPClassTemplate extends PDOMCPPClassType
		implements ICPPClassTemplate, ICPPInstanceCache, IPDOMCPPTemplateParameterOwner {
	private static final int PARAMETERS = PDOMCPPClassType.RECORD_SIZE + 0;
	private static final short RELEVANT_PARAMETERS= PDOMCPPClassType.RECORD_SIZE + 4;
	private static final int FIRST_PARTIAL = PDOMCPPClassType.RECORD_SIZE + 6;
	
	/**
	 * The size in bytes of a PDOMCPPClassTemplate record in the database.
	 */
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = PDOMCPPClassType.RECORD_SIZE + 10;
	
	private volatile ICPPTemplateParameter[] params;  // Cached template parameters.
	
	public PDOMCPPClassTemplate(PDOMCPPLinkage linkage, PDOMNode parent, ICPPClassTemplate template)	throws CoreException, DOMException {
		super(linkage, parent, template);
		
		final Database db = getDB();
		final ICPPTemplateParameter[] origParams= template.getTemplateParameters();
		final IPDOMCPPTemplateParameter[] params = PDOMTemplateParameterArray.createPDOMTemplateParameters(linkage, this, origParams);
		long rec= PDOMTemplateParameterArray.putArray(db, params);
		db.putRecPtr(record + PARAMETERS, rec);
		db.putShort(record + RELEVANT_PARAMETERS, (short) params.length);
		linkage.new ConfigureTemplateParameters(origParams, params);
	}

	public PDOMCPPClassTemplate(PDOMLinkage linkage, long bindingRecord) {
		super(linkage, bindingRecord);
	}

	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPP_CLASS_TEMPLATE;
	}
	
	@Override
	public ICPPTemplateParameter[] getTemplateParameters() {
		if (params == null) {
			try {
				final Database db = getDB();
				long rec= db.getRecPtr(record + PARAMETERS);
				int count= Math.max(0, db.getShort(record + RELEVANT_PARAMETERS));
				if (rec == 0 || count == 0) {
					params= ICPPTemplateParameter.EMPTY_TEMPLATE_PARAMETER_ARRAY;
				} else {
					IPDOMCPPTemplateParameter[] allParams = PDOMTemplateParameterArray.getArray(this, rec);
					count= Math.min(count, allParams.length);
					if (count == allParams.length) {
						params= allParams;
					} else {
						params= new ICPPTemplateParameter[count];
						System.arraycopy(allParams, 0, params, 0, count);
					}
				} 
			} catch (CoreException e) {
				CCorePlugin.log(e);
				params = ICPPTemplateParameter.EMPTY_TEMPLATE_PARAMETER_ARRAY;
			}
		}
		return params;
	}

	@Override
	public void update(PDOMLinkage linkage, IBinding newBinding) throws CoreException {
		super.update(linkage, newBinding);
		if (newBinding instanceof ICPPClassTemplate) {
			ICPPClassTemplate ct= (ICPPClassTemplate) newBinding;
			try {
				updateTemplateParameters(linkage, ct.getTemplateParameters());
			} catch (DOMException e) {
				CCorePlugin.log(e);
			}
		}
	}

	private void updateTemplateParameters(PDOMLinkage linkage, ICPPTemplateParameter[] newParams) throws CoreException, DOMException {
		final Database db = getDB();
		long rec= db.getRecPtr(record + PARAMETERS);
		IPDOMCPPTemplateParameter[] allParams;
		if (rec == 0) {
			allParams= IPDOMCPPTemplateParameter.EMPTY_ARRAY;
		} else {
			allParams = PDOMTemplateParameterArray.getArray(this, rec);
		}
		
		final int newParamLength = newParams.length;
		int[] props= new int[allParams.length];
		int[] result= new int[newParamLength];
		int additionalPars= 0;
		boolean reorder= false;
		for (int i = 0; i < props.length; i++) {
			final IPDOMCPPTemplateParameter par = allParams[i];
			props[i]= getProperty(par);
		}

		outer: for (int i = 0; i < newParamLength; i++) {
			ICPPTemplateParameter newPar = newParams[i];
			int prop= getProperty(newPar);
			for (int j = 0; j < props.length; j++) {
				if (props[j] == prop) {
					// reuse param
					result[i]= j;
					props[j]= -1;
					allParams[j].update(linkage, newPar);
					if (j != i)
						reorder= true;
					continue outer;
				}
			}
			result[i]= -1;
			additionalPars++;
		}
		
		if (additionalPars > 0 || reorder) {
			params= null;
			IPDOMCPPTemplateParameter[] newAllParams= new IPDOMCPPTemplateParameter[allParams.length+additionalPars];
			for (int j = 0; j < newParamLength; j++) {
				int idx= result[j];
				if (idx >= 0) {
					newAllParams[j]= allParams[idx];
					allParams[idx]= null;
				} else {
					newAllParams[j]= PDOMTemplateParameterArray.createPDOMTemplateParameter(getLinkage(), this, newParams[j]);
				}
			}
			int pos= newParamLength;
			for (IPDOMCPPTemplateParameter unused : allParams) {
				if (unused != null)
					newAllParams[pos++]= unused;
			}
			if (rec != 0)
				db.free(rec);
			rec= PDOMTemplateParameterArray.putArray(db, newAllParams);
			db.putRecPtr(record + PARAMETERS, rec);
		}
		db.putShort(record + RELEVANT_PARAMETERS, (short) newParamLength);
	}

	private int getProperty(ICPPTemplateParameter par) {
		int result= par.getParameterPosition() & 0xffff;
		if (par instanceof ICPPTemplateTypeParameter)
			return result;
		if (par instanceof ICPPTemplateNonTypeParameter)
			return result | 0x10000;
		return result | 0x20000;
	}

	private PDOMCPPClassTemplatePartialSpecialization getFirstPartial() throws CoreException {
		long value = getDB().getRecPtr(record + FIRST_PARTIAL);
		return value != 0 ? new PDOMCPPClassTemplatePartialSpecialization(getLinkage(), value) : null;
	}
	
	public void addPartial(PDOMCPPClassTemplatePartialSpecialization partial) throws CoreException {
		PDOMCPPClassTemplatePartialSpecialization first = getFirstPartial();
		partial.setNextPartial(first);
		getDB().putRecPtr(record + FIRST_PARTIAL, partial.getRecord());
	}
		
	@Override
	public ICPPClassTemplatePartialSpecialization[] getPartialSpecializations() {
		try {
			ArrayList<PDOMCPPClassTemplatePartialSpecialization> partials =
					new ArrayList<PDOMCPPClassTemplatePartialSpecialization>();
			for (PDOMCPPClassTemplatePartialSpecialization partial = getFirstPartial();
					partial != null;
					partial = partial.getNextPartial()) {
				partials.add(partial);
			}
			
			return partials.toArray(new ICPPClassTemplatePartialSpecialization[partials.size()]);
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return new ICPPClassTemplatePartialSpecialization[0];
		}
	}

	@Override
	public boolean isSameType(IType type) {
		if (type instanceof ITypedef) {
			return type.isSameType(this);
		}
		
		if (type instanceof PDOMNode) {
			PDOMNode node= (PDOMNode) type;
			if (node.getPDOM() == getPDOM()) {
				return node.getRecord() == getRecord();
			}
		}

		// need a class template
		if (type instanceof ICPPClassTemplate == false || type instanceof ProblemBinding) 
			return false;
		
		// exclude other kinds of class templates
		if (type instanceof ICPPClassTemplatePartialSpecialization ||
				type instanceof ICPPTemplateTemplateParameter ||
				type instanceof ICPPClassSpecialization)
			return false;
				
		ICPPClassType ctype= (ICPPClassType) type;
		if (ctype.getKey() != getKey())
			return false;
		char[] nchars = ctype.getNameCharArray();
		if (nchars.length == 0) {
			nchars= ASTTypeUtil.createNameForAnonymous(ctype);
		}
		if (nchars == null || !CharArrayUtils.equals(nchars, getNameCharArray()))
			return false;

		return SemanticUtil.isSameOwner(getOwner(), ctype.getOwner());
	}

	@Override
	public ICPPTemplateInstance getInstance(ICPPTemplateArgument[] arguments) {
		return PDOMInstanceCache.getCache(this).getInstance(arguments);	
	}

	@Override
	public void addInstance(ICPPTemplateArgument[] arguments, ICPPTemplateInstance instance) {
		PDOMInstanceCache.getCache(this).addInstance(arguments, instance);	
	}

	@Override
	public ICPPTemplateInstance[] getAllInstances() {
		return PDOMInstanceCache.getCache(this).getAllInstances();	
	}

	@Override
	public ICPPTemplateParameter adaptTemplateParameter(ICPPTemplateParameter param) {
		// Template parameters are identified by their position in the parameter list.
		int pos = param.getParameterPosition();
		ICPPTemplateParameter[] pars = getTemplateParameters();
		
		if (pars == null || pos >= pars.length)
			return null;
		
		ICPPTemplateParameter result= pars[pos];
		if (param instanceof ICPPTemplateTypeParameter) {
			if (result instanceof ICPPTemplateTypeParameter)
				return result;
		} else if (param instanceof ICPPTemplateNonTypeParameter) {
			if (result instanceof ICPPTemplateNonTypeParameter)
				return result;
		} else if (param instanceof ICPPTemplateTemplateParameter) {
			if (result instanceof ICPPTemplateTemplateParameter)
				return result;
		}
		return null;
	}
	
	@Override
	public final ICPPDeferredClassInstance asDeferredInstance() {
		PDOMInstanceCache cache= PDOMInstanceCache.getCache(this);
		synchronized (cache) {
			ICPPDeferredClassInstance dci= cache.getDeferredInstance();
			if (dci == null) {
				dci= CPPTemplates.createDeferredInstance(this);
				cache.putDeferredInstance(dci);
			}
			return dci;
		}
	}
}
