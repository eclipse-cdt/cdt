/*******************************************************************************
 * Copyright (c) 2005, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    QNX - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *    Andrew Ferguson (Symbian)
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionList;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFieldReference;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceAlias;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBasicType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceAlias;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.internal.core.Util;
import org.eclipse.cdt.internal.core.dom.bid.IBindingIdentityFactory;
import org.eclipse.cdt.internal.core.dom.bid.ILocalBindingIdentity;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBlockScope;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPImplicitMethod;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPPointerType;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.dom.FindBindingByLinkageConstant;
import org.eclipse.cdt.internal.core.pdom.dom.FindEquivalentBinding;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMMemberOwner;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMFile;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMName;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;

/**
 * @author Doug Schaefer
 *
 */
class PDOMCPPLinkage extends PDOMLinkage {
	public PDOMCPPLinkage(PDOM pdom, int record) {
		super(pdom, record);
	}

	public PDOMCPPLinkage(PDOM pdom) throws CoreException {
		super(pdom, CPP_LINKAGE_ID, CPP_LINKAGE_ID.toCharArray());
	}

	public String getID() {
		return CPP_LINKAGE_ID;
	}

	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	public int getNodeType() {
		return LINKAGE;
	}

	// Binding types
	public static final int CPPVARIABLE = PDOMLinkage.LAST_NODE_TYPE + 1;
	public static final int CPPFUNCTION = PDOMLinkage.LAST_NODE_TYPE + 2;
	public static final int CPPCLASSTYPE = PDOMLinkage.LAST_NODE_TYPE + 3;
	public static final int CPPFIELD = PDOMLinkage.LAST_NODE_TYPE + 4;
	public static final int CPPMETHOD = PDOMLinkage.LAST_NODE_TYPE + 5;
	public static final int CPPNAMESPACE = PDOMLinkage.LAST_NODE_TYPE + 6;
	public static final int CPPNAMESPACEALIAS = PDOMLinkage.LAST_NODE_TYPE + 7;
	public static final int CPPBASICTYPE = PDOMLinkage.LAST_NODE_TYPE + 8;
	public static final int CPPPARAMETER = PDOMLinkage.LAST_NODE_TYPE + 9;
	public static final int CPPENUMERATION = PDOMLinkage.LAST_NODE_TYPE + 10;
	public static final int CPPENUMERATOR = PDOMLinkage.LAST_NODE_TYPE + 11;
	public static final int CPPTYPEDEF = PDOMLinkage.LAST_NODE_TYPE + 12;

	public ILanguage getLanguage() {
		return new GPPLanguage();
	}


	public PDOMBinding addName(IASTName name, PDOMFile file) throws CoreException {
		if (name == null || name instanceof ICPPASTQualifiedName)
			return null;
		
		// Check for null name
		char[] namechars = name.toCharArray();
		if (namechars == null || namechars.length == 0)
			return null;


		IBinding binding = name.resolveBinding();

		if (binding == null || binding instanceof IProblemBinding)
			// Can't tell what it is
			return null;

		if (binding instanceof IParameter)
			// Skip parameters (TODO and others I'm sure)
			return null;

		PDOMBinding pdomBinding = adaptBinding(binding);
		try {
			if (pdomBinding == null) {
				PDOMNode parent = getAdaptedParent(binding);
				if (parent != null) {
					if (binding instanceof ICPPField && parent instanceof PDOMCPPClassType)
						pdomBinding = new PDOMCPPField(pdom, (PDOMCPPClassType)parent, name);
					else if (binding instanceof ICPPVariable) {
						if (!(binding.getScope() instanceof CPPBlockScope))
							pdomBinding = new PDOMCPPVariable(pdom, parent, name);
					} else if (binding instanceof ICPPMethod && parent instanceof PDOMCPPClassType) {
						pdomBinding = new PDOMCPPMethod(pdom, parent, name);
					} else if (binding instanceof CPPImplicitMethod && parent instanceof PDOMCPPClassType) {
						if(!name.isReference()) {
							//because we got the implicit method off of an IASTName that is not a reference,
							//it is no longer completly implicit and it should be treated as a normal method.
							pdomBinding = new PDOMCPPMethod(pdom, parent, name);
						} else if (binding instanceof CPPImplicitMethod && parent instanceof PDOMCPPClassType) {
							if(!name.isReference()) {
								//because we got the implicit method off of an IASTName that is not a reference,
								//it is no longer completly implicit and it should be treated as a normal method.
								pdomBinding = new PDOMCPPMethod(pdom, parent, name);
							}
						} else if (binding instanceof ICPPFunction) {
							pdomBinding = new PDOMCPPFunction(pdom, parent, name);
						} else if (binding instanceof ICPPClassType) {
							pdomBinding = new PDOMCPPClassType(pdom, parent, name);
						} else if (binding instanceof ICPPNamespaceAlias) {
							pdomBinding = new PDOMCPPNamespaceAlias(pdom, parent, name);
						} else if (binding instanceof ICPPNamespace) {
							pdomBinding = new PDOMCPPNamespace(pdom, parent, name);
						} else if (binding instanceof IEnumeration) {
							pdomBinding = new PDOMCPPEnumeration(pdom, parent, name);
						} else if (binding instanceof IEnumerator) {
							IEnumeration enumeration = (IEnumeration)((IEnumerator)binding).getType();
							PDOMBinding pdomEnumeration = adaptBinding(enumeration);
							if (pdomEnumeration instanceof PDOMCPPEnumeration)
								pdomBinding = new PDOMCPPEnumerator(pdom, parent, name,
										(PDOMCPPEnumeration)pdomEnumeration);
						} else if (binding instanceof ITypedef) {
							pdomBinding = new PDOMCPPTypedef(pdom, parent, name, (ITypedef)binding);
						}
					} else if (binding instanceof ICPPFunction) {
						pdomBinding = new PDOMCPPFunction(pdom, parent, name);
					} else if (binding instanceof ICPPClassType) {
						pdomBinding = new PDOMCPPClassType(pdom, parent, name);
					} else if (binding instanceof ICPPNamespaceAlias) {
						pdomBinding = new PDOMCPPNamespaceAlias(pdom, parent, name);
					} else if (binding instanceof ICPPNamespace) {
						pdomBinding = new PDOMCPPNamespace(pdom, parent, name);
					} else if (binding instanceof IEnumeration) {
						pdomBinding = new PDOMCPPEnumeration(pdom, parent, name);
					} else if (binding instanceof IEnumerator) {
						IEnumeration enumeration = (IEnumeration)((IEnumerator)binding).getType();
						PDOMBinding pdomEnumeration = adaptBinding(enumeration);
						if (pdomEnumeration instanceof PDOMCPPEnumeration)
							pdomBinding = new PDOMCPPEnumerator(pdom, parent, name,
									(PDOMCPPEnumeration)pdomEnumeration);
					} else if (binding instanceof ITypedef) {
						pdomBinding = new PDOMCPPTypedef(pdom, parent, name, (ITypedef)binding);
					}
					parent.addChild(pdomBinding);
				}
			}
		} catch(DOMException e) {
			throw new CoreException(Util.createStatus(e));
		}

		// final processing
		if (pdomBinding != null) {
			// Add in the name
			new PDOMName(pdom, name, file, pdomBinding);

			// Check if is a base specifier
			if (pdomBinding instanceof ICPPClassType && name.getParent() instanceof ICPPASTBaseSpecifier) {
				ICPPASTBaseSpecifier baseNode = (ICPPASTBaseSpecifier)name.getParent();
				ICPPASTCompositeTypeSpecifier ownerNode = (ICPPASTCompositeTypeSpecifier)baseNode.getParent();
				IBinding ownerBinding = adaptBinding(ownerNode.getName().resolveBinding());
				if (ownerBinding != null && ownerBinding instanceof PDOMCPPClassType) {
					PDOMCPPClassType ownerClass = (PDOMCPPClassType)ownerBinding;
					PDOMCPPBase pdomBase = new PDOMCPPBase(pdom, (PDOMCPPClassType)pdomBinding,
							baseNode.isVirtual(), baseNode.getVisibility());
					ownerClass.addBase(pdomBase);
				}
			}
		}

		return pdomBinding;
	}

	protected int getBindingType(IBinding binding) {
		if (binding instanceof ICPPTemplateDefinition)
			// this must be before class type
			return 0;
		else if (binding instanceof ICPPField)
			// this must be before variables
			return CPPFIELD;
		else if (binding instanceof ICPPVariable)
			return CPPVARIABLE;
		else if (binding instanceof ICPPMethod)
			// this must be before functions
			return CPPMETHOD;
		else if (binding instanceof ICPPFunction)
			return CPPFUNCTION;
		else if (binding instanceof ICPPClassType)
			return CPPCLASSTYPE;
		else if (binding instanceof ICPPNamespaceAlias)
			return CPPNAMESPACEALIAS;
		else if (binding instanceof ICPPNamespace)
			return CPPNAMESPACE;
		else if (binding instanceof IEnumeration)
			return CPPENUMERATION;
		else if (binding instanceof IEnumerator)
			return CPPENUMERATOR;
		else if (binding instanceof ITypedef)
			return CPPTYPEDEF;
		else
			return 0;
	}

	/**
	 * Find the equivalent binding, or binding placeholder within this PDOM
	 */
	public PDOMBinding adaptBinding(IBinding binding) throws CoreException {
		if (binding == null || binding instanceof IProblemBinding)
			return null;

		if (binding instanceof PDOMBinding) {
			// there is no guarantee, that the binding is from the same PDOM object.
			PDOMBinding pdomBinding = (PDOMBinding) binding;
			if (pdomBinding.getPDOM() == getPDOM()) {
				return pdomBinding;
			}
			// so if the binding is from another pdom it has to be adapted. 
		}

		FindEquivalentBinding visitor = new FindEquivalentBinding(this, binding);
		PDOMNode parent = getAdaptedParent(binding);

		if (parent == this) {
			getIndex().accept(visitor);
			return visitor.getResult();
		} else if (parent instanceof IPDOMMemberOwner) {
			IPDOMMemberOwner owner = (IPDOMMemberOwner)parent;
			try {
				owner.accept(visitor);
			} catch (CoreException e) {
				if (e.getStatus().equals(Status.OK_STATUS))
					return visitor.getResult();
				else
					throw e;
			}
		} else if (parent instanceof PDOMCPPNamespace) {
			((PDOMCPPNamespace)parent).getIndex().accept(visitor);
			return visitor.getResult();
		}

		return null;
	}

	public PDOMBinding resolveBinding(IASTName name) throws CoreException {
		try {
			return _resolveBinding(name);
		} catch(DOMException e) {
			throw new CoreException(Util.createStatus(e));
		}
	}
	
	private PDOMBinding _resolveBinding(IASTName name) throws CoreException, DOMException {
		// mstodo revisit
		IBinding origBinding = name.getBinding();	
		if (origBinding != null)
			return adaptBinding(origBinding);


		if (name instanceof ICPPASTQualifiedName) {
			IASTName[] names = ((ICPPASTQualifiedName)name).getNames();
			if (names.length == 1)
				return resolveBinding(names[0]);
			IASTName lastName = names[names.length - 1];
			PDOMBinding nsBinding = adaptBinding(names[names.length - 2].resolveBinding());
			if (nsBinding instanceof IScope) {
				return (PDOMBinding) ((IScope)nsBinding).getBinding(lastName, true);
			}
		}
		IASTNode parent = name.getParent();
		if (parent instanceof ICPPASTQualifiedName) {
			ICPPASTQualifiedName qualName = (ICPPASTQualifiedName)parent;
			IASTName lastName = qualName.getLastName();
			if (name != lastName) {
				return resolveInQualifiedName(name);
			} else {
				// Drop down to the rest of the resolution procedure
				// with the parent of the qualified name
				parent = parent.getParent();
			}
		}

		if (parent instanceof IASTIdExpression) {
			// reference
			IASTNode eParent = parent.getParent();
			if (eParent instanceof IASTFunctionCallExpression) {
				return resolveFunctionCall((IASTIdExpression)parent, name, (IASTFunctionCallExpression) eParent);
			} else {
				int constant = (name.getParent() instanceof ICPPASTQualifiedName
						&& ((ICPPASTQualifiedName)name.getParent()).getLastName() != name)
						? CPPNAMESPACE : CPPVARIABLE; 
				FindBindingByLinkageConstant finder = new FindBindingByLinkageConstant(this, name.toCharArray(), constant);
				getIndex().accept(finder);
				return finder.getResult();
			}
		} else if (parent instanceof IASTNamedTypeSpecifier) {
			FindBindingByLinkageConstant finder = new FindBindingByLinkageConstant(this, name.toCharArray(), CPPCLASSTYPE);
			getIndex().accept(finder);
			PDOMBinding result = finder.getResult();
			return result;

		} else if (parent instanceof ICPPASTNamespaceAlias) {
			FindBindingByLinkageConstant finder = new FindBindingByLinkageConstant(this, name.toCharArray(), CPPNAMESPACE);
			getIndex().accept(finder);
			return finder.getResult();
		} else if(parent instanceof ICPPASTFieldReference) {
			ICPPASTFieldReference ref = (ICPPASTFieldReference) parent;
			IASTExpression exp = ref.getFieldOwner();
			if(exp instanceof IASTIdExpression) {
				IASTIdExpression id = (IASTIdExpression) exp;

				IASTNode eParent = parent.getParent();
				if (eParent instanceof IASTFunctionCallExpression) {
					IASTFunctionCallExpression exp2 = (IASTFunctionCallExpression) eParent;
					return resolveFunctionCall(id, name, exp2);
				}
			}
		}

		return null;
	}

	public IType[] getTypes(IASTExpression paramExp) {
		IType[] types;
		if(paramExp==null) {
			types = new IType[0];
		} else {
			if(paramExp instanceof IASTExpressionList) {
				IASTExpressionList list = (IASTExpressionList) paramExp;
				IASTExpression[] paramExps = list.getExpressions();
				types = new IType[paramExps.length];
				for(int i=0; i<paramExps.length; i++) {
					types[i] = paramExps[i].getExpressionType();
				}
			} else if(paramExp instanceof IASTLiteralExpression) {
				IASTLiteralExpression litExp = (IASTLiteralExpression) paramExp;
				types = new IType[] {litExp.getExpressionType()};
			} else if(paramExp instanceof ICPPASTNewExpression) {
				ICPPASTNewExpression exp3 = (ICPPASTNewExpression) paramExp;
				IType type = exp3.getExpressionType();
				types = new IType[] {new CPPPointerType(type)};
			} else {
				types = new IType[]{paramExp.getExpressionType()};
			}
		}
		return types;
	}

	/*
	 * aftodo - I'm not confident I'm going through the correct AST routes here
	 * 
	 * (It does work though)
	 */
	public PDOMBinding resolveFunctionCall(IASTIdExpression id, IASTName name, IASTFunctionCallExpression exp2) 
	throws CoreException, DOMException {
		IASTExpression paramExp = exp2.getParameterExpression();
		ILocalBindingIdentity bid = null;

		IType[] types = getTypes(paramExp);

		IBinding parentBinding = id.getName().getBinding();
		if(parentBinding == null) {
			bid = new CPPBindingIdentity.Holder(
					new String(name.toCharArray()),
					CPPFUNCTION,
					types);
			FindEquivalentBinding feb = new FindEquivalentBinding(this, bid);
			getIndex().accept(feb);
			return feb.getResult();
		} else if(parentBinding instanceof ICPPVariable) {
			ICPPVariable v = (ICPPVariable) parentBinding;
			IType type = v.getType();
			if(type instanceof PDOMBinding) {
				bid = new CPPBindingIdentity.Holder(
						new String(name.toCharArray()),
						CPPMETHOD,
						types);
				FindEquivalentBinding feb = new FindEquivalentBinding(this, bid);
				try {
					((PDOMBinding)type).accept(feb);
				} catch (CoreException e) {
					if (e.getStatus().equals(Status.OK_STATUS)) {
						return feb.getResult();
					}
					else {
						throw e;
					}
				}
			}
		}

		return null;
	}

	private PDOMBinding resolveInQualifiedName(IASTName name) throws CoreException {
		ICPPASTQualifiedName qualName = (ICPPASTQualifiedName)name.getParent();

		// Must be a namespace or a class
		IASTName[] names = qualName.getNames();
		IASTName nsName = null;
		for (int i = 0; i < names.length; ++i) {
			if (names[i] == name)
				break;
			else
				nsName = names[i];
		}
		if (nsName == names[names.length - 1])
			// didn't find our name here, weird...
			return null;

		if (nsName == null) {
			// we are at the root

			FindBindingByLinkageConstant finder = new FindBindingByLinkageConstant(this, name.toCharArray(), CPPNAMESPACE);
			getIndex().accept(finder);
			if(finder.getResult()==null) {
				finder = new FindBindingByLinkageConstant(this, name.toCharArray(), CPPCLASSTYPE);
				getIndex().accept(finder);
			}
			return finder.getResult();
		} else {
			// TODO we are in another namespace
			return null;
		}
	}

	public PDOMNode addType(PDOMNode parent, IType type) throws CoreException {
		if (type instanceof ICPPBasicType) {
			return new PDOMCPPBasicType(pdom, parent, (ICPPBasicType)type);
		} else if (type instanceof ICPPClassType) {
			FindEquivalentBinding feb = new FindEquivalentBinding(this,(ICPPClassType)type);
			getIndex().accept(feb);
			if(feb.getResult()!=null) {
				return feb.getResult();
			}
		}
		return super.addType(parent, type); 
	}

	public PDOMNode getNode(int record) throws CoreException {
		if (record == 0)
			return null;

		switch (PDOMNode.getNodeType(pdom, record)) {
		case CPPVARIABLE:
			return new PDOMCPPVariable(pdom, record);
		case CPPFUNCTION:
			return new PDOMCPPFunction(pdom, record);
		case CPPCLASSTYPE:
			return new PDOMCPPClassType(pdom, record);
		case CPPFIELD:
			return new PDOMCPPField(pdom, record);
		case CPPMETHOD:
			return new PDOMCPPMethod(pdom, record);
		case CPPNAMESPACE:
			return new PDOMCPPNamespace(pdom, record);
		case CPPNAMESPACEALIAS:
			return new PDOMCPPNamespaceAlias(pdom, record);
		case CPPBASICTYPE:
			return new PDOMCPPBasicType(pdom, record);
		case CPPENUMERATION:
			return new PDOMCPPEnumeration(pdom, record);
		case CPPENUMERATOR:
			return new PDOMCPPEnumerator(pdom, record);
		case CPPTYPEDEF:
			return new PDOMCPPTypedef(pdom, record);
		default:
			return super.getNode(record);
		}
	}

	public ILocalBindingIdentity getLocalBindingIdentity(IBinding b) throws CoreException {
		return new CPPBindingIdentity(b, this);
	}

	public IBindingIdentityFactory getBindingIdentityFactory() {
		return this;
	}
}
