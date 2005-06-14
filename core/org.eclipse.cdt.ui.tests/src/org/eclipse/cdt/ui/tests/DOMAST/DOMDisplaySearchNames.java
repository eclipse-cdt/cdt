/**********************************************************************
 * Copyright (c) 2005 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation 
 **********************************************************************/
package org.eclipse.cdt.ui.tests.DOMAST;

import org.eclipse.cdt.core.browser.PathUtil;
import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IMacroBinding;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMember;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.parser.ParserUtil;
import org.eclipse.cdt.core.search.BasicSearchMatch;
import org.eclipse.cdt.core.search.IMatch;
import org.eclipse.cdt.core.search.IOffsetLocatable;
import org.eclipse.cdt.core.search.OffsetLocatable;
import org.eclipse.cdt.internal.ui.search.CSearchQuery;
import org.eclipse.cdt.internal.ui.search.CSearchResult;
import org.eclipse.cdt.internal.ui.search.NewSearchResultCollector;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;

/**
 * This is used for the DOM AST Viewer only... it requires that the names to be displayed
 * in the search view were already found elsewhere.
 * @author dsteffle
 */
public class DOMDisplaySearchNames extends CSearchQuery implements ISearchQuery {

	private static final String BLANK_STRING = ""; //$NON-NLS-1$
	private CSearchResult _result;
	private IASTNode[] nodes = null;
	private String queryLabel = null;
	
	/**
	 * 
	 */
	public DOMDisplaySearchNames(IASTNode[] nodes, String queryLabel, String pattern) {
		super(CUIPlugin.getWorkspace(), pattern, false, null, null, null, queryLabel);
		this.nodes = nodes;
		this.queryLabel = queryLabel;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.search.ui.ISearchQuery#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStatus run(IProgressMonitor monitor)
			throws OperationCanceledException {
		
		final CSearchResult textResult= (CSearchResult) getSearchResult();
		
		IProgressMonitor mainSearchPM= new SubProgressMonitor(monitor, 1000);
     	NewSearchResultCollector collector = new NewSearchResultCollector(textResult, mainSearchPM);
     	
     	collector.aboutToStart();
     	
     	for (int i=0; i<nodes.length; i++) {
     		try {
     			String fileName = null;
     			IPath path = null;
     			int start = 0;
     			int end = 0;
     			if ( nodes[i] != null ) {
	     		  IASTFileLocation location = nodes[i].getFileLocation();
                  if (location == null) {
                      return new Status(IStatus.ERROR, CUIPlugin.getPluginId(), 0, "Null Location associated with IASTFileLocation.", null); //$NON-NLS-1$
                  }
                  fileName = location.getFileName();
				  start = location.getNodeOffset();
                  end = location.getNodeOffset() + location.getNodeLength();
				  path = new Path(fileName);
				  Object fileResource=null;
				  IResource res = ParserUtil.getResourceForFilename(fileName);
				  if (res != null)
					  fileResource = res;
				  else {
					  fileResource = PathUtil.getWorkspaceRelativePath(fileName);
				  }
	              
                  collector.acceptMatch( createMatch(fileResource, start, end, nodes[i], path ) );
     			}
     		} catch (CoreException ce) {}
     	}
     	     	
     	mainSearchPM.done();
     	collector.done();
     	
     	return new Status(IStatus.OK, CUIPlugin.getPluginId(), 0, BLANK_STRING, null); //$NON-NLS-1$	
	}
	
	 public IMatch createMatch( Object fileResource, int start, int end, IASTNode node, IPath referringElement ) {
	 	BasicSearchMatch result = new BasicSearchMatch();
		
		if( fileResource instanceof IResource )
			result.setResource((IResource) fileResource);
		else if( fileResource instanceof IPath )
			result.setPath((IPath) fileResource);
			
		IOffsetLocatable locatable = new OffsetLocatable(start,end);
		result.setLocatable(locatable);
		
		result.setParentName(BLANK_STRING); //$NON-NLS-1$
		result.setReferringElement(referringElement);
		
		if (node instanceof IASTName)
			result.setName( node.toString());
		else if (node instanceof IASTProblem)
			result.setName(((IASTProblem)node).getMessage());
		else
			result.setName(node.toString());
	
		// set the type and visibility of the match
		if (node instanceof IASTName) {
			IBinding binding = ((IASTName)node).resolveBinding();
			if (binding instanceof ICPPClassType) {
				result.setType(ICElement.C_CLASS);
			} else if (binding instanceof ICompositeType) {
				int key=ICompositeType.k_struct;
				try {
					key = ((ICompositeType)binding).getKey();
				} catch (DOMException e) {}
				switch (key) {
				case ICompositeType.k_struct:
					result.setType(ICElement.C_STRUCT);
					break;
				case ICompositeType.k_union:
					result.setType(ICElement.C_UNION);
					break;
				}
			} else if (binding instanceof ICPPNamespace) {
				result.setType(ICElement.C_NAMESPACE);
			} else if (binding instanceof IEnumeration) {
				result.setType(ICElement.C_ENUMERATION);
			} else if (binding instanceof IMacroBinding) {
				result.setType(ICElement.C_MACRO);
			} else if (binding instanceof IField) {
				result.setType(ICElement.C_FIELD);
				try {
					result.setStatic(((IField)binding).isStatic());
				} catch (DOMException e) {}
				if (binding instanceof ICPPMember) {
					try {
						switch (((ICPPMember)binding).getVisibility()) {
						case ICPPMember.v_private:
							result.setVisibility(ICElement.CPP_PRIVATE); 
							break;
						case ICPPMember.v_public:
							result.setVisibility(ICElement.CPP_PUBLIC);
							break;
						// no ICElement.CPP_PROTECTED
						}
					} catch (DOMException e) {}
				}
				try {
					result.setConst(ASTTypeUtil.isConst(((IField)binding).getType()));
				} catch (DOMException e) {}
			} else if (binding instanceof IVariable) {
				result.setType(ICElement.C_VARIABLE);
				try {
					result.setConst(ASTTypeUtil.isConst(((IVariable)binding).getType()));
				} catch (DOMException e) {}
			} else if (binding instanceof IEnumerator) {
				result.setType(ICElement.C_ENUMERATOR);
			} else if (binding instanceof ICPPMethod) {
				result.setType(ICElement.C_METHOD);
				if (binding instanceof ICPPMember) {
					try {
						switch (((ICPPMember)binding).getVisibility()) {
						case ICPPMember.v_private:
							result.setVisibility(ICElement.CPP_PRIVATE);
							break;
						case ICPPMember.v_public:
							result.setVisibility(ICElement.CPP_PUBLIC);
							break;
							// there is no ICElement.CPP_PROTECTED
						}
					} catch (DOMException e) {}
				}
				try {
					result.setConst(ASTTypeUtil.isConst(((ICPPMethod)binding).getType()));
				} catch (DOMException e) {}
			} else if (binding instanceof IFunction) {
				result.setType(ICElement.C_FUNCTION);
				try {
					result.setStatic(((IFunction)binding).isStatic());
				} catch (DOMException e) {}
			} else if (binding instanceof ITypedef) {
				result.setType(ICElement.C_TYPEDEF);
			}
		} else {
			result.setType(ICElement.C_UNKNOWN_DECLARATION);
		}
		
		result.setReturnType(BLANK_STRING);
		
		return result;
	}	 


	/* (non-Javadoc)
	 * @see org.eclipse.search.ui.ISearchQuery#getLabel()
	 */
	public String getLabel() {
		return queryLabel;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.search.ui.ISearchQuery#canRerun()
	 */
	public boolean canRerun() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.search.ui.ISearchQuery#canRunInBackground()
	 */
	public boolean canRunInBackground() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.search.ui.ISearchQuery#getSearchResult()
	 */
	public ISearchResult getSearchResult() {
		if (_result == null)
			_result= new CSearchResult(this);
		return _result;
	}

}
