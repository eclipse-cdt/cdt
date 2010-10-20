/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.tcmodification;

import org.eclipse.cdt.managedbuilder.internal.core.IRealBuildObjectAssociation;

/**
 * The class implements the storage organized by types extending
 * {@link IRealBuildObjectAssociation}. 
 *
 * @param <T> - the type of object being stored.
 */
public final class ObjectTypeBasedStorage<T> implements Cloneable {
	private static final int TOOL_INDEX = 0;
	private static final int TOOLCHAIN_INDEX = 1;
	private static final int BUILDER_INDEX = 2;
	private static final int CFG_INDEX = 3;
	private static final int SIZE = 4;

	private static final int[] OBJECT_TYPES = new int[]{
		IRealBuildObjectAssociation.OBJECT_TOOL,
		IRealBuildObjectAssociation.OBJECT_TOOLCHAIN,
		IRealBuildObjectAssociation.OBJECT_BUILDER,
		IRealBuildObjectAssociation.OBJECT_CONFIGURATION,
	};
	
	@SuppressWarnings("unchecked")
	private T fStorage[] = (T[]) new Object[SIZE];
	
	public static int[] getSupportedObjectTypes(){
		return OBJECT_TYPES.clone();
	}
	
	private int getIndex(int type){
		switch (type) {
		case IRealBuildObjectAssociation.OBJECT_TOOL:
			return TOOL_INDEX;
		case IRealBuildObjectAssociation.OBJECT_TOOLCHAIN:
			return TOOLCHAIN_INDEX;
		case IRealBuildObjectAssociation.OBJECT_BUILDER:
			return BUILDER_INDEX;
		case IRealBuildObjectAssociation.OBJECT_CONFIGURATION:
			return CFG_INDEX;
		default:
			throw new IllegalArgumentException();
		}
	}
	
//	private int getType(int index){
//		switch (index) {
//		case TOOL_INDEX:
//			return IRealBuildObjectAssociation.OBJECT_TOOL;
//		case TOOLCHAIN_INDEX:
//			return IRealBuildObjectAssociation.OBJECT_TOOLCHAIN;
//		case BUILDER_INDEX:
//			return IRealBuildObjectAssociation.OBJECT_BUILDER;
//		case CFG_INDEX:
//			return IRealBuildObjectAssociation.OBJECT_CONFIGURATION;
//		default:
//			throw new IllegalArgumentException();
//		}
//	}
	
	public T get(int type){
		return fStorage[getIndex(type)];
	}
	
	public T set(int type, T value){
		int index = getIndex(type);
		T oldValue = fStorage[index];
		fStorage[index] = value;
		return oldValue;
	}

	@Override
	public Object clone(){
		try {
			@SuppressWarnings("unchecked")
			ObjectTypeBasedStorage<T> clone = (ObjectTypeBasedStorage<T>)super.clone();
			clone.fStorage = fStorage.clone();
			return clone;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public boolean isEmpty(){
		for(int i = 0; i < fStorage.length; i++){
			if(fStorage[i] != null)
				return false;
		}
		return true;
	}
}
