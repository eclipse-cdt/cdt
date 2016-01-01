/*******************************************************************************
 * Copyright (c) 2006, 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.buildmodel;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.managedbuilder.buildmodel.IBuildStep;

public class BuildGroup {
	private Set<BuildStep> fActions = new HashSet<BuildStep>();
	private boolean fNeedsRebuild;

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.builddescription.IBuildGroup#getSteps()
	 */
	public IBuildStep[] getSteps() {
		return fActions.toArray(new IBuildStep[fActions.size()]);
	}
	
	public void addAction(BuildStep action){
		fActions.add(action);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.builddescription.IBuildGroup#contains(org.eclipse.cdt.managedbuilder.builddescription.IBuildStep)
	 */
	public boolean contains(IBuildStep action) {
		return fActions.contains(action);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.builddescription.IBuildGroup#needsRebuild()
	 */
	public boolean needsRebuild() {
		return fNeedsRebuild;
	}
	
	public void setRebuildState(boolean rebuild){
		fNeedsRebuild = rebuild;
		
		for (BuildStep action : fActions) {
			action.setRebuildState(rebuild);
		}
	}
}
