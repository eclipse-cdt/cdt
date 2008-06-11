/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTNode;

/**
 * Manages lists of template parameter nodes during the parsing
 * of names. The purpose of this class is performance, as these
 * lists are managed using lazy initialization and object pooling.
 * This class is basically as substitute for List<List<IASTNode>>.
 * 
 * When using the object pool code must be wrapped in a try-finally
 * block to ensure the object is returned to the pool.
 * 
 * TODO: How much of a performance improvement are we talking about here?
 * It might make sense just to get rid of this class. The extra complexity
 * might not be worth it.
 */
public final class TemplateParameterManager {
	
	protected void reset() {
		list = Collections.emptyList();
		emptySegmentCount = 0;
	}
	
	private TemplateParameterManager(int i) {
		reset();
		counterId = i;
	}
	
	private final int counterId;
	private List<List<IASTNode>> list;
	private int emptySegmentCount;

	public List<List<IASTNode>> getTemplateArgumentsList() {
		return list;
	}
	
	public void addSegment(List<IASTNode> inputSegment) {
		// avoid creating an actual ArrayList instance for as long as possible
		if(inputSegment == null) {
			if(list.isEmpty())
				++emptySegmentCount;
			else
				list.add( null );
		}
		else {
			if(list.isEmpty()) {
				list = new ArrayList<List<IASTNode>>();
				for( int i = 0; i < emptySegmentCount; ++i )
					list.add( null );
			}
			list.add( inputSegment );
		}
	}
	
	
	// An object pool

	private static final int NUMBER_OF_INSTANCES = 8;
	private static final boolean [] instancesUsed = new boolean[ NUMBER_OF_INSTANCES ];
	private static final TemplateParameterManager [] counters = new TemplateParameterManager[ NUMBER_OF_INSTANCES ];
	private static int counter = 8;
	static {
		for( int i = 0; i < NUMBER_OF_INSTANCES; ++i ) {
			counters[ i ] = new TemplateParameterManager( i );
		}
	}

	public synchronized static TemplateParameterManager getInstance() {
		int index = findFreeCounter();
		if( index == -1 )
			return new TemplateParameterManager(++counter);
		instancesUsed[ index ] = true;
		return counters[ index ];
	}

	public synchronized static void returnInstance( TemplateParameterManager c ) {
		if( c.counterId > 0 && c.counterId < NUMBER_OF_INSTANCES )
			instancesUsed[ c.counterId ] = false;
		c.reset();
	}
	
	
	private static int findFreeCounter() {
		for( int i = 0; i < NUMBER_OF_INSTANCES; ++i )
			if( instancesUsed[i] == false )
				return i;
		return -1;
	}
	
}
