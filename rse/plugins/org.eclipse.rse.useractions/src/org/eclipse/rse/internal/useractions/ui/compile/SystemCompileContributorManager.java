package org.eclipse.rse.internal.useractions.ui.compile;

/*******************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
import java.util.Vector;

/**
 * This singleton class manages all compile contributions added through extension points.
 */
public class SystemCompileContributorManager {
	private static SystemCompileContributorManager inst;
	private Vector contributors = new Vector();

	private SystemCompileContributorManager() {
		loadContributors();
	}

	/**
	 * Returns the singleton instance of the manager.
	 * @return The singleton instance of this class 
	 */
	public static SystemCompileContributorManager getInstance() {
		if (inst == null) {
			inst = new SystemCompileContributorManager();
		}
		return inst;
	}

	/**
	 * Loads the compile contributors from the workbench's registry.
	 */
	private void loadContributors() {
		SystemCompileContributorReader reader = new SystemCompileContributorReader();
		reader.readCompileContributors(this);
	}

	/**
	 * Register a contributor with the manager.
	 * @param contributor a contributor.
	 */
	public void registerContributor(SystemCompileContributor contributor) {
		contributors.add(contributor);
	}

	/**
	 * Adds all compile command contributions through extension point for the remote object.
	 * Returns true if there are any contributions, false otherwise.
	 */
	public boolean contributeCompileCommands(SystemCompileProfile prf, Object element) {
		boolean isContributions = false;
		// go through list of all contributors, find out which ones apply for the selected element
		// and add/change a compile type to the given compile profile.
		for (int idx = 0; idx < contributors.size(); idx++) {
			SystemCompileContributor contributor = (SystemCompileContributor) contributors.elementAt(idx);
			if (contributor.isApplicableTo(element)) {
				contributor.contributeCompileCommand(prf, element);
				isContributions = true;
			}
		}
		return isContributions;
	}
}
