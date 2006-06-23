/*******************************************************************************
 * Copyright (c) 2004, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.browser;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.dom.IPDOMNode;
import org.eclipse.cdt.core.dom.IPDOMVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.gnu.c.GCCLanguage;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.c.PDOMCStructure;
import org.eclipse.cdt.internal.core.pdom.dom.cpp.PDOMCPPClassType;
import org.eclipse.cdt.internal.core.pdom.dom.cpp.PDOMCPPNamespace;
import org.eclipse.cdt.internal.core.pdom.dom.cpp.PDOMCPPNamespaceAlias;
import org.eclipse.core.runtime.CoreException;

/**
 * Manages a search cache for types in the workspace. Instead of returning
 * objects of type <code>ICElement</code> the methods of this class returns a
 * list of the lightweight objects <code>TypeInfo</code>.
 * <P>
 * AllTypesCache runs asynchronously using a background job to rebuild the cache
 * as needed. If the cache becomes dirty again while the background job is
 * running, the job is restarted.
 * <P>
 * If <code>getTypes</code> is called in response to a user action, a progress
 * dialog is shown. If called before the background job has finished, getTypes
 * waits for the completion of the background job.
 */
public class AllTypesCache {

	private abstract static class TypesCollector implements IPDOMVisitor {
		private final int[] kinds;
		protected final List types;
		protected final ICProject project;
		
		protected TypesCollector(int[] kinds, List types, ICProject project) {
			this.kinds = kinds;
			this.types = types;
			this.project = project;
		}
		
		protected abstract void visitKind(IPDOMNode node, int kind);
		
		public boolean visit(IPDOMNode node) throws CoreException {
			for (int i = 0; i < kinds.length; ++i)
				visitKind(node, kinds[i]);
			return true;
		}
		
		public List getTypes() {
			return types;
		}
	}
	
	private static class CTypesCollector extends TypesCollector {
		public CTypesCollector(int[] kinds, List types, ICProject project) {
			super(kinds, types, project);
		}
		
		protected void visitKind(IPDOMNode node, int kind) {
			switch (kind) {
			case ICElement.C_NAMESPACE:
				return;
			case ICElement.C_CLASS:
				return;
			case ICElement.C_STRUCT:
				if (node instanceof PDOMCStructure)
					types.add(new PDOMTypeInfo((PDOMBinding)node, kind, project));
				return;
			case ICElement.C_UNION:
				return;
			case ICElement.C_ENUMERATION:
				return;
			case ICElement.C_TYPEDEF:
				return;
			}
		}
	}
	
	private static class CPPTypesCollector extends TypesCollector {
		public CPPTypesCollector(int[] kinds, List types, ICProject project) {
			super(kinds, types, project);
		}
		
		protected void visitKind(IPDOMNode node, int kind) {
			try {
				switch (kind) {
				case ICElement.C_NAMESPACE:
					if (node instanceof PDOMCPPNamespace || node instanceof PDOMCPPNamespaceAlias)
						types.add(new PDOMTypeInfo((PDOMBinding)node, kind, project));
					return;
				case ICElement.C_CLASS:
					if (node instanceof PDOMCPPClassType
							&& ((PDOMCPPClassType)node).getKey() == ICPPClassType.k_class)
						types.add(new PDOMTypeInfo((PDOMBinding)node, kind, project));
					return;
				case ICElement.C_STRUCT:
					if (node instanceof PDOMCPPClassType
							&& ((PDOMCPPClassType)node).getKey() == ICPPClassType.k_struct)
						types.add(new PDOMTypeInfo((PDOMBinding)node, kind, project));
					return;
				case ICElement.C_UNION:
					if (node instanceof PDOMCPPClassType
							&& ((PDOMCPPClassType)node).getKey() == ICPPClassType.k_union)
						types.add(new PDOMTypeInfo((PDOMBinding)node, kind, project));
					return;
				case ICElement.C_ENUMERATION:
					return;
				case ICElement.C_TYPEDEF:
					return;
				}
			} catch (DOMException e) {
				CCorePlugin.log(e);
			}
		}
	}
	
	private static ITypeInfo[] getTypes(ICProject[] projects, int[] kinds) throws CoreException {
		List types = new ArrayList();
		IPDOMManager pdomManager = CCorePlugin.getPDOMManager();
		
		for (int i = 0; i < projects.length; ++i) {
			ICProject project = projects[i];
			CTypesCollector cCollector = new CTypesCollector(kinds, types, project);
			CPPTypesCollector cppCollector = new CPPTypesCollector(kinds, types, project);
				
			PDOM pdom = (PDOM)pdomManager.getPDOM(project);
			PDOMLinkage cLinkage = pdom.getLinkage(GCCLanguage.getDefault());
			cLinkage.accept(cCollector);
			PDOMLinkage cppLinkage = pdom.getLinkage(GPPLanguage.getDefault());
			cppLinkage.accept(cppCollector);
		}
			
		return (ITypeInfo[])types.toArray(new ITypeInfo[types.size()]);
	}
	
	/**
	 * Returns all types in the workspace.
	 */
	public static ITypeInfo[] getAllTypes() {
		try {
			ICProject[] projects = CoreModel.getDefault().getCModel().getCProjects();
			return getTypes(projects, ITypeInfo.KNOWN_TYPES);
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return new ITypeInfo[0];
		}
	}
	
	/**
	 * Returns all types in the given scope.
	 * 
	 * @param scope The search scope
	 * @param kinds Array containing CElement types: C_NAMESPACE, C_CLASS,
	 *              C_UNION, C_ENUMERATION, C_TYPEDEF
	 */
	public static ITypeInfo[] getTypes(ITypeSearchScope scope, int[] kinds) {
		try {
			return getTypes(scope.getEnclosingProjects(), kinds);
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return new ITypeInfo[0];
		}
	}
	
	/**
	 * Returns all namespaces in the given scope.
	 * 
	 * @param scope The search scope
	 * @param includeGlobalNamespace <code>true</code> if the global (default) namespace should be returned
	 */
	public static ITypeInfo[] getNamespaces(ITypeSearchScope scope, boolean includeGlobalNamespace) {
		try {
			return getTypes(scope.getEnclosingProjects(), new int[] {ICElement.C_NAMESPACE});
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return new ITypeInfo[0];
		}
	}
	
	/** Returns first type in the cache which matches the given
	 *  type and name.  If no type is found, <code>null</code>
	 *  is returned.
	 *
	 * @param project the enclosing project
	 * @param type the ICElement type
	 * @param qualifiedName the qualified type name to match
	 * @return the matching type
	 */
	public static ITypeInfo getType(ICProject project, int type, IQualifiedTypeName qualifiedName) {
		// TODO - seems to be only used when a namespace name is changed
		// which would be pretty slow against the PDOM.
		return null;
	}

	/**
	 * Returns all types matching name in the given project.
	 * 
	 * @param project the enclosing project
	 * @param qualifiedName The qualified type name
	 * @param matchEnclosed <code>true</code> if enclosed types count as matches (foo::bar == bar)
	 * @param ignoreCase <code>true</code> if case-insensitive
	 * @return Array of types
	 */
	public static ITypeInfo[] getTypes(ICProject project, IQualifiedTypeName qualifiedName, boolean matchEnclosed, boolean ignoreCase) {
		// TODO - seems to be only used when a class or namespace name is changed
		// which would be pretty slow against the PDOM.
		return new ITypeInfo[0];
	}

}
