/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 ******************************************************************************/
/*
 * Created on Jun 11, 2003
 */
package org.eclipse.cdt.internal.ui.search;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate;
import org.eclipse.cdt.core.parser.ast.*;
import org.eclipse.cdt.core.search.ICSearchResultCollector;
import org.eclipse.cdt.core.search.IMatch;
import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.ui.CElementImageDescriptor;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.search.ui.ISearchResultView;
import org.eclipse.search.ui.SearchUI;
import org.eclipse.swt.graphics.Point;

/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class CSearchResultCollector implements ICSearchResultCollector {
	
	public static final String IMATCH = "IMatchObject";
	private static final Point SMALL_SIZE= new Point(16, 16);
	
	/**
	 * 
	 */
	public CSearchResultCollector() {
		super();
	}
	
	public CSearchResultCollector( boolean maintain ){
		this();
		_maintainOwnCollection = maintain;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.search.ICSearchResultCollector#aboutToStart()
	 */
	public void aboutToStart() {
		_matchCount = 0;
		
		_view = SearchUI.getSearchResultView();
		
		if( _view != null ){
			_view.searchStarted(
				null,//new ActionGroupFactory(),
				_operation.getSingularLabel(),
				_operation.getPluralLabelPattern(),
				_operation.getImageDescriptor(),
				CSearchPage.EXTENSION_POINT_ID,
				new CSearchResultLabelProvider(),
				new GotoMarkerAction(),
				new GroupByKeyComputer(),
				_operation
			);
		}
		
		if( getProgressMonitor() != null && !getProgressMonitor().isCanceled() ){
			getProgressMonitor().subTask( SEARCHING );
		}
		
		if( _maintainOwnCollection ){
			_matches = new HashSet();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.search.ICSearchResultCollector#accept(org.eclipse.core.resources.IResource, int, int, java.lang.Object, int)
	 */
	public void accept(
		IResource resource,
		int start,
		int end,
		IMatch enclosingObject,
		int accuracy)
		throws CoreException 
	{
		IMarker marker = resource.createMarker( SearchUI.SEARCH_MARKER );
		
		Match match = (Match) enclosingObject;
		
		Object groupKey = match;
		
		HashMap markerAttributes = new HashMap( 2 );
		
		//we can hang any other info we want off the marker
		markerAttributes.put( IMarker.CHAR_START, new Integer( Math.max( start, 0 ) ) );		
		markerAttributes.put( IMarker.CHAR_END,   new Integer( Math.max( end, 0 ) ) );
		markerAttributes.put( IMATCH, enclosingObject );
		
		marker.setAttributes( markerAttributes );
		
		if( _view != null )
			_view.addMatch( match.name, groupKey, resource, marker );
		
		if( _maintainOwnCollection ){
			_matches.add( enclosingObject );
		}
		_matchCount++;
	}

	public void accept(
		IPath path,
		int start,
		int end,
		IMatch match,
		int accuracy)
		throws CoreException 
	{
		if( _maintainOwnCollection )
			_matches.add( match );
	}


	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.search.ICSearchResultCollector#createMatch(org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement)
	 */
	public IMatch createMatch(ISourceElementCallbackDelegate node, IASTScope parent ) {
		String name = null;
		String parentName = "";
		IASTOffsetableNamedElement offsetable = null;
		
		if( parent instanceof IASTQualifiedNameElement ){
			String [] names = ((IASTQualifiedNameElement)parent).getFullyQualifiedName();
			for( int i = 0; i < names.length; i++ ){
				if( i > 0 )
					parentName += "::";
					
				parentName += names[ i ];
			}
		}
		
		if( node instanceof IASTReference ){
			offsetable = (IASTOffsetableNamedElement) ((IASTReference)node).getReferencedElement();
			name = ((IASTReference)node).getName();
		} else if( node instanceof IASTOffsetableNamedElement ){
			offsetable = (IASTOffsetableNamedElement)node;
			name = offsetable.getName(); 
		} else {
			return null;
		}
		
		ImageDescriptor image = getImageDescriptor( offsetable );
		int adornmentFlags = computeAdornmentFlags( offsetable );
		IMatch match = new Match(name, parentName, image );//, node.getNameOffset(), name.length() );
		
		return match;
	}
	
	private ImageDescriptor getImageDescriptor( IASTOffsetableElement node ){
		ImageDescriptor imageDescriptor = null;
		if( node instanceof IASTClassSpecifier ){
			ASTClassKind kind = ((IASTClassSpecifier)node).getClassKind();
			if( kind == ASTClassKind.CLASS ){
				imageDescriptor = CPluginImages.DESC_OBJS_CLASS;
			} else if ( kind == ASTClassKind.STRUCT ){
				imageDescriptor = CPluginImages.DESC_OBJS_STRUCT;
			} else if ( kind == ASTClassKind.UNION ){
				imageDescriptor = CPluginImages.DESC_OBJS_UNION;
			}
		} else if ( node instanceof IASTNamespaceDefinition ){
			imageDescriptor = CPluginImages.DESC_OBJS_CONTAINER;
		} else if ( node instanceof IASTEnumerationSpecifier ){
			imageDescriptor = CPluginImages.DESC_OBJS_ENUMERATION;
		} else if ( node instanceof IASTField ){
			IASTField  field = (IASTField)node;
			ASTAccessVisibility visibility = field.getVisiblity();
			if( visibility == ASTAccessVisibility.PUBLIC ){
				imageDescriptor = CPluginImages.DESC_OBJS_PUBLIC_FIELD;
			} else if ( visibility == ASTAccessVisibility.PROTECTED) {
				imageDescriptor = CPluginImages.DESC_OBJS_PROTECTED_FIELD;
			} else if ( visibility == ASTAccessVisibility.PRIVATE ) {
				imageDescriptor = CPluginImages.DESC_OBJS_PRIVATE_FIELD;
			}
		} else if ( node instanceof IASTVariable ){
			imageDescriptor = CPluginImages.DESC_OBJS_FIELD;
		} else if ( node instanceof IASTEnumerator ){
			imageDescriptor = CPluginImages.DESC_OBJS_ENUMERATOR;
		} else if ( node instanceof IASTMethod ){
			IASTMethod method = (IASTMethod) node;
			ASTAccessVisibility visibility = method.getVisiblity();
			if( visibility == ASTAccessVisibility.PUBLIC ){
				imageDescriptor = CPluginImages.DESC_OBJS_PUBLIC_METHOD;
			} else if ( visibility == ASTAccessVisibility.PROTECTED) {
				imageDescriptor = CPluginImages.DESC_OBJS_PROTECTED_METHOD;
			} else if ( visibility == ASTAccessVisibility.PRIVATE ) {
				imageDescriptor = CPluginImages.DESC_OBJS_PRIVATE_METHOD;
			}
		} else if ( node instanceof IASTFunction ){
			imageDescriptor = CPluginImages.DESC_OBJS_FUNCTION;
		}
		
		if( imageDescriptor != null) {
			int adornmentFlags = computeAdornmentFlags( node );
			return new CElementImageDescriptor( imageDescriptor, adornmentFlags, SMALL_SIZE );
		}
		return imageDescriptor;		
	}
	
	private int computeAdornmentFlags(IASTOffsetableElement element ) {
		int flags = 0;
			
		if( element instanceof IASTVariable ){
			flags |= ((IASTVariable) element).isStatic() ? 0 : CElementImageDescriptor.STATIC;
		} else if ( element instanceof IASTMethod ){
			flags |= ((IASTMethod) element).isStatic()   ? 0 : CElementImageDescriptor.STATIC;
			flags |= ((IASTMethod) element).isConst()    ? 0 : CElementImageDescriptor.CONSTANT;
			flags |= ((IASTMethod) element).isVolatile() ? 0 : CElementImageDescriptor.VOLATILE;
		} else if( element instanceof IASTFunction ){
			flags |= ((IASTFunction) element).isStatic() ? 0 : CElementImageDescriptor.STATIC;
		}
		
		return flags;
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.search.ICSearchResultCollector#done()
	 */
	public void done() {
		if( !getProgressMonitor().isCanceled() ){
			String matchesString;
			if( _matchCount == 1 ){
				matchesString = MATCH;
			} else {
				matchesString = MessageFormat.format( MATCHES, new Integer[]{ new Integer(_matchCount) } );
			}
			
			getProgressMonitor().setTaskName( MessageFormat.format( DONE, new String[]{ matchesString } ) );
		}

		if( _view != null ){
			_view.searchFinished();
		}
		
		_view    = null;
		_monitor = null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.search.ICSearchResultCollector#getProgressMonitor()
	 */
	public IProgressMonitor getProgressMonitor() {
		return _monitor;
	}

	public void setProgressMonitor(IProgressMonitor monitor) {
		this._monitor = monitor;
	}

	public void setOperation( CSearchOperation operation ) {
		_operation = operation;
	}
	
	public Set getMatches(){
		return _matches;
	}
	
	private static final String SEARCHING = CSearchMessages.getString("CSearchResultCollector.searching"); //$NON-NLS-1$
	private static final String MATCH     = CSearchMessages.getString("CSearchResultCollector.match"); //$NON-NLS-1$
	private static final String MATCHES   = CSearchMessages.getString("CSearchResultCollector.matches"); //$NON-NLS-1$
	private static final String DONE      = CSearchMessages.getString("CSearchResultCollector.done"); //$NON-NLS-1$
	
	private IProgressMonitor 	_monitor;
	private CSearchOperation 	_operation;
	private ISearchResultView 	_view;
	private int					_matchCount;
	private Set					_matches;
	private boolean 			_maintainOwnCollection = false;
}