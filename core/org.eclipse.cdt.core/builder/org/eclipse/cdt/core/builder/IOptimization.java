package org.eclipse.cdt.core.builder;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

/**
 *  
 * Note: This class/interface is part of an interim API that is still under development and
 * expected to change significantly before reaching stability. It is being made available at
 * this early stage to solicit feedback from pioneering adopters on the understanding that any
 * code that uses this API will almost certainly be broken (repeatedly) as the API evolves.
 */
public interface IOptimization {
	String getDescription(int level);
	int getCurrentLevel();
	void setCurrentLevel(int level);
	int[] getLevels();
}
