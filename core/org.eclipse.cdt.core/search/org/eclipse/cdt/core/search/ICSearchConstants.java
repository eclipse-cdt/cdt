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
 * Created on May 31, 2003
 */
package org.eclipse.cdt.core.search;

/**
 * @author bgheorgh
 */
import org.eclipse.cdt.internal.core.search.processing.*;


/**
 * <p>
 * This interface defines the constants used by the search engine.
 * </p>
 * <p>
 * This interface declares constants only; it is not intended to be implemented.
 * </p>
 * @see org.eclipse.cdt.core.search.SearchEngine
 */
public interface ICSearchConstants {
	/**
	 * The nature of searched element or the nature
	 * of match in unknown.
	 */
	public static final SearchFor UNKNOWN_SEARCH_FOR = new SearchFor( -1 );
	public static final LimitTo UNKNOWN_LIMIT_TO = new LimitTo( -1 );
	
	/* Nature of searched element */
	
	/**
	 * The searched element is a type.
	 */
	public static final SearchFor TYPE = new SearchFor( 0 );

	/**
	 * The searched element is a function.
	 */
	public static final SearchFor FUNCTION = new SearchFor( 1 );

	/**
	* The searched element is a namespace.
    */
	public static final SearchFor NAMESPACE = new SearchFor( 2 );
	
	/**
	 * The searched element is a method (member function).
	 */
	public static final SearchFor METHOD = new SearchFor( 3 );

	/**
	 * The searched element is a field (member variable).
     */
	public static final SearchFor FIELD = new SearchFor( 4 );
	
	/**
	 * The searched element is a variable.
	 * More selective than using TYPE
	 */
	public static final SearchFor VAR = new SearchFor( 5 );

	/**
	 * The searched element is a class. 
	 * More selective than using TYPE
	 */
	public static final SearchFor CLASS = new SearchFor( 6 );

	/**
	 * The searched element is a struct.
	 * More selective than using TYPE
	 */
	public static final SearchFor STRUCT = new SearchFor( 7 );

	/**
	 * The searched element is a enum.
	 * More selective than using TYPE
	 */
	public static final SearchFor ENUM = new SearchFor( 8 );
	
	/**
	 * The searched element is a union.
	 * More selective than using TYPE
	 */
	public static final SearchFor UNION = new SearchFor( 9 );
	
	public static final SearchFor MACRO = new SearchFor( 10 );
	
	public static final SearchFor CLASS_STRUCT = new SearchFor( 11 );

	public static final SearchFor TYPEDEF = new SearchFor( 12 );
	
	public static final SearchFor INCLUDE = new SearchFor( 13 );
	
	public static final SearchFor DERIVED = new SearchFor( 14 );
	
	public static final SearchFor ENUMTOR = new SearchFor( 15 );
	
	public static final SearchFor FRIEND = new SearchFor( 16 );
	
	/* Nature of match */
	
	/**
	 * The search result is a declaration.
	 * Can be used in conjunction with any of the nature of searched elements
	 * so as to better narrow down the search.
	 */
	public static final LimitTo DECLARATIONS = new LimitTo( 0 ); 

	/**
	 * The search result is a type that implements an interface. 
	 * Used in conjunction with either TYPE or CLASS or INTERFACE, it will
	 * respectively search for any type implementing/extending an interface, or
	 * rather exclusively search for classes implementing an interface, or interfaces 
	 * extending an interface.
	 */
	public static final LimitTo DEFINITIONS = new LimitTo( 1 );

	/**
	 * The search result is a reference.
	 * Can be used in conjunction with any of the nature of searched elements
	 * so as to better narrow down the search.
	 * References can contain implementers since they are more generic kind
	 * of matches.
	 */
	public static final LimitTo REFERENCES = new LimitTo( 2 );

	/**
	 * The search result is a declaration, a reference, or an implementer 
	 * of an interface.
	 * Can be used in conjunction with any of the nature of searched elements
	 * so as to better narrow down the search.
	 */
	public static final LimitTo ALL_OCCURRENCES = new LimitTo( 3 );

	
	/* Syntactic match modes */
	
	/**
	 * The search pattern matches exactly the search result,
	 * that is, the source of the search result equals the search pattern.
	 */
	int EXACT_MATCH = 0;
	/**
	 * The search pattern is a prefix of the search result.
	 */
	int PREFIX_MATCH = 1;
	/**
	 * The search pattern contains one or more wild cards ('*') where a 
	 * wild-card can replace 0 or more characters in the search result.
	 */
	int PATTERN_MATCH = 2;


	/* Case sensitivity */
	
	/**
	 * The search pattern matches the search result only
	 * if cases are the same.
	 */
	boolean CASE_SENSITIVE = true;
	/**
	 * The search pattern ignores cases in the search result.
	 */
	boolean CASE_INSENSITIVE = false;
	

	/* Waiting policies */
	
	/**
	 * The search operation starts immediately, even if the underlying indexer
	 * has not finished indexing the workspace. Results will more likely
	 * not contain all the matches.
	 */
	int FORCE_IMMEDIATE_SEARCH = IJob.ForceImmediate;
	/**
	 * The search operation throws an <code>org.eclipse.core.runtime.OperationCanceledException</code>
	 * if the underlying indexer has not finished indexing the workspace.
	 */
	int CANCEL_IF_NOT_READY_TO_SEARCH = IJob.CancelIfNotReady;
	/**
	 * The search operation waits for the underlying indexer to finish indexing 
	 * the workspace before starting the search.
	 */
	int WAIT_UNTIL_READY_TO_SEARCH = IJob.WaitUntilReady;
	
	public static final String EXTERNAL_SEARCH_LINK_PREFIX = "cdtlnk"; //$NON-NLS-1$
	
	public class SearchFor{
		private SearchFor( int value )
		{
			this.value = value;
		}
		private final int value;
	}
	
	public class LimitTo {
		private LimitTo( int value )
		{
			this.value = value;
		}
		private final int value;
	}
}
