/*******************************************************************************
 * Copyright (c) 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 * Andrew Ferguson (Symbian)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMNode;
import org.eclipse.cdt.core.dom.IPDOMVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateNonTypeParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;
import org.eclipse.cdt.internal.core.index.IndexCPPSignatureUtil;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.PDOMNodeLinkedList;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMOverloader;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Bryan Wilkinson
 */
abstract class PDOMCPPSpecialization extends PDOMCPPBinding implements ICPPSpecialization, IPDOMOverloader {

	private static final int ARGMAP_PARAMS = PDOMCPPBinding.RECORD_SIZE + 0;
	private static final int ARGMAP_ARGS = PDOMCPPBinding.RECORD_SIZE + 4;
	private static final int SIGNATURE_HASH = PDOMCPPBinding.RECORD_SIZE + 8;
	private static final int SPECIALIZED = PDOMCPPBinding.RECORD_SIZE + 12;
	/**
	 * The size in bytes of a PDOMCPPSpecialization record in the database.
	 */
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = PDOMCPPBinding.RECORD_SIZE + 16;
	
	private IBinding fSpecializedCache= null;
	private ObjectMap fArgMap;
	
	public PDOMCPPSpecialization(PDOM pdom, PDOMNode parent, ICPPSpecialization spec, IPDOMBinding specialized)
	throws CoreException {
		super(pdom, parent, spec.getNameCharArray());
		pdom.getDB().putInt(record + SPECIALIZED, specialized.getRecord());

		// specializations that are no instances have the same argmap as their owner.
		if (this instanceof ICPPTemplateInstance) {
			PDOMNodeLinkedList paramList = new PDOMNodeLinkedList(pdom, record + ARGMAP_PARAMS, getLinkageImpl());
			PDOMNodeLinkedList argList = new PDOMNodeLinkedList(pdom, record + ARGMAP_ARGS, getLinkageImpl());
			ObjectMap argMap = spec.getArgumentMap();
			if (argMap != null) {
				for (int i = 0; i < argMap.size(); i++) {
					Object param = argMap.keyAt(i);
					Object arg = argMap.getAt(i);

					// TODO - non-type template arguments still needs attention
					if (param instanceof ICPPTemplateNonTypeParameter && arg instanceof IType) {
						try {
							ICPPTemplateNonTypeParameter nontype= (ICPPTemplateNonTypeParameter) param;
							PDOMNode paramNode= ((PDOMCPPLinkage)getLinkageImpl()).createBinding(this, nontype);
							PDOMNode argNode= getLinkageImpl().addType(this, (IType) arg);
							if (paramNode != null && argNode != null) {
								paramList.addMember(paramNode);
								argList.addMember(argNode);
							}
						} catch(DOMException de) {
							CCorePlugin.log(de);
						}
					}

					if (param instanceof IType && arg instanceof IType) {
						PDOMNode paramNode = getLinkageImpl().addType(this, (IType) param);
						PDOMNode argNode = getLinkageImpl().addType(this, (IType) arg);
						if (paramNode != null && argNode != null) {
							paramList.addMember(paramNode);
							argList.addMember(argNode);
						}
					}
				}
			}
		}
		try {
			Integer sigHash = IndexCPPSignatureUtil.getSignatureHash(spec);
			pdom.getDB().putInt(record + SIGNATURE_HASH, sigHash != null ? sigHash.intValue() : 0);
		} catch (DOMException e) {
		}
	}

	public PDOMCPPSpecialization(PDOM pdom, int bindingRecord) {
		super(pdom, bindingRecord);
	}

	public IBinding getSpecializedBinding() {
		if (fSpecializedCache == null) {
			try {
				int specializedRec = pdom.getDB().getInt(record + SPECIALIZED);
				fSpecializedCache= (IPDOMBinding) getLinkageImpl().getNode(specializedRec);
			} catch (CoreException e) {
				CCorePlugin.log(e);
			}
		}
		return fSpecializedCache;
	}
	
	private static class NodeCollector implements IPDOMVisitor {
		private List<IPDOMNode> nodes = new ArrayList<IPDOMNode>();
		public boolean visit(IPDOMNode node) throws CoreException {
			nodes.add(node);
			return false;
		}
		public void leave(IPDOMNode node) throws CoreException {
		}
		public IPDOMNode[] getNodes() {
			return nodes.toArray(new IPDOMNode[nodes.size()]);
		}
	}
	
	public ObjectMap getArgumentMap() {
		if (fArgMap == null) {
			try {
				if (this instanceof ICPPTemplateInstance) {
					PDOMNodeLinkedList paramList = new PDOMNodeLinkedList(pdom, record + ARGMAP_PARAMS, getLinkageImpl());
					PDOMNodeLinkedList argList = new PDOMNodeLinkedList(pdom, record + ARGMAP_ARGS, getLinkageImpl());
					NodeCollector paramVisitor = new NodeCollector();
					paramList.accept(paramVisitor);
					IPDOMNode[] paramNodes = paramVisitor.getNodes();
					NodeCollector argVisitor = new NodeCollector();
					argList.accept(argVisitor);
					IPDOMNode[] argNodes = argVisitor.getNodes();

					ObjectMap map = new ObjectMap(paramNodes.length);
					for (int i = 0; i < paramNodes.length; i++) {
						map.put(paramNodes[i], argNodes[i]);
					}
					fArgMap= map;
				} else {
					// specializations that are no instances have the same argmap as their owner.
					IBinding owner= getOwner();
					if (owner instanceof ICPPSpecialization) {
						fArgMap= ((ICPPSpecialization) owner).getArgumentMap();
					}
				}
			} catch (CoreException e) {
				CCorePlugin.log(e);
			}
		}
		return fArgMap;
	}
	
	public int getSignatureHash() throws CoreException {
		return pdom.getDB().getInt(record + SIGNATURE_HASH);
	}
		
	private IType[] getArguments() {
		if (!(this instanceof ICPPTemplateDefinition)
				&& getSpecializedBinding() instanceof ICPPTemplateDefinition) {
			ICPPTemplateDefinition template = (ICPPTemplateDefinition) getSpecializedBinding();
			try {
				ICPPTemplateParameter[] params = template.getTemplateParameters();
				ObjectMap argMap = getArgumentMap();
				IType[] args = new IType[params.length];
				for (int i = 0; i < params.length; i++) {
					args[i] = (IType) argMap.get(params[i]);
				}
				return args;
			} catch (DOMException e) {
			}
		}

		return IType.EMPTY_TYPE_ARRAY;
	}
	
	public boolean matchesArguments(IType[] arguments) {
		IType[] args = getArguments();
		if (args.length == arguments.length) {
			int i = 0;
			for (; i < args.length; i++) {
				if (!CPPTemplates.isSameTemplateArgument(args[i], arguments[i]))
					break;
			}
			return i == args.length;
		}
		return false;
	}
	
	/*
	 * For debug purposes only
	 */
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(getName());
		result.append(' '); 
		result.append(getArgumentMap().toString());
		result.append(' '); 
		try {
			result.append(getConstantNameForValue(getLinkageImpl(), getNodeType()));
		} catch (CoreException ce) {
			result.append(getNodeType());
		}
		return result.toString();
	}
}
