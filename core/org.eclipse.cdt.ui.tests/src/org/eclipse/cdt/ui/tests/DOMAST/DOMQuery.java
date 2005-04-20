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

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.search.BasicSearchMatch;
import org.eclipse.cdt.core.search.IMatch;
import org.eclipse.cdt.internal.ui.search.CSearchQuery;
import org.eclipse.cdt.internal.ui.search.CSearchResult;
import org.eclipse.cdt.internal.ui.search.NewSearchResultCollector;
import org.eclipse.cdt.ui.testplugin.CTestPlugin;
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
 * @author dsteffle
 */
public class DOMQuery extends CSearchQuery implements ISearchQuery {

	private static final String BLANK_STRING = ""; //$NON-NLS-1$
	private CSearchResult _result;
	private IASTNode[] nodes = null;
	private String queryLabel = null;
	
	/**
	 * 
	 */
	public DOMQuery(IASTNode[] nodes, String queryLabel, String pattern) {
		super(CTestPlugin.getWorkspace(), pattern, false, null, null, null, queryLabel, null);
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
	     		  IASTFileLocation location = nodes[i].getTranslationUnit().flattenLocationsToFile( nodes[i].getNodeLocations() );
                  fileName = location.getFileName();
	     		  path = new Path(fileName);
	              start = location.getNodeOffset();
                  end = location.getNodeOffset() + location.getNodeLength();
                  collector.acceptMatch( createMatch(path, start, end, nodes[i], path ) );
     			}
     		} catch (CoreException ce) {}
     	}
     	     	
     	mainSearchPM.done();
     	collector.done();
     	
     	return new Status(IStatus.OK, CTestPlugin.getPluginId(), 0, BLANK_STRING, null); //$NON-NLS-1$	
	}
	
	 public IMatch createMatch( Object fileResource, int start, int end, IASTNode node, IPath referringElement ) {
	 	BasicSearchMatch result = new BasicSearchMatch();
		if( fileResource instanceof IResource )
			result.resource = (IResource) fileResource;
		else if( fileResource instanceof IPath )
			result.path = (IPath) fileResource;
			
		result.startOffset = start;
		result.endOffset = end;
		result.parentName = BLANK_STRING; //$NON-NLS-1$
		result.referringElement = referringElement;
		
		if (node instanceof IASTName)
			result.name = node.toString();
		else if (node instanceof IASTProblem)
			result.name = ((IASTProblem)node).getMessage();
		else
			result.name = node.toString();
	
		result.type = ICElement.C_FIELD; // TODO Devin static for now, want something like BasicSearchResultCollector#setElementInfo
		result.visibility = ICElement.CPP_PUBLIC; // TODO Devin static for now, want something like BasicSearchResultCollector#setElementInfo
		result.returnType = BLANK_STRING;
		
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
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.search.ui.ISearchQuery#canRunInBackground()
	 */
	public boolean canRunInBackground() {
		// TODO Auto-generated method stub
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
