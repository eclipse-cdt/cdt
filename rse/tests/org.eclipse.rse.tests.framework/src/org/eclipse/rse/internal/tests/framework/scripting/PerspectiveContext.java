/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * David Dykstal (IBM) - initial contribution.
 *******************************************************************************/
package org.eclipse.rse.internal.tests.framework.scripting;

import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.rse.internal.tests.framework.ui.TestSuiteConsoleView;
import org.eclipse.rse.internal.tests.framework.ui.TestSuiteHolderView;
import org.eclipse.rse.internal.tests.framework.ui.TestSuiteImageView;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;

/**
 * A perspective context is a kind of script context that coordinates among
 * the several different views in the testing perspective.
 */
public class PerspectiveContext extends ScriptContext {

	private TestSuiteHolderView holderView;

	/**
	 * Constructs a new PerspectiveContext for running the suites with a user interface. The suites 
	 * are run from the holder view named here, the image and console views used for show and tell
	 * are located on the same workbench page as the holder view.
	 * @param holderView the test suite holder view that will drive the scripts.
	 * @param home the URL that names the location that contains the script's resources
	 */
	public PerspectiveContext(TestSuiteHolderView holderView, URL home) {
		super(home);
		this.holderView = holderView;
	}

	/**
	 * A show operation will resolve a name to an image and show that image
	 * in the current environment.
	 * @param imageName the name of the image to resolve and show.
	 */
	public void show(String imageName) {
		TestSuiteImageView view = findImageView();
		URL imageURL = getResourceURL(imageName);
		if (imageURL != null) {
			ImageDescriptor descriptor = ImageDescriptor.createFromURL(imageURL);
			view.setImage(descriptor);
		}
	}
	
	/**
	 * A tell operation will show a string in the environment.
	 * @param text the String to show.
	 */
	public void tell(String text) {
		TestSuiteConsoleView view = findConsoleView();
		view.add(text);
	}

	/**
	 * A pause operation will stop and wait for a "continue" or "fail" indication
	 * from the environment.
	 * @param text the message to display during the pause
	 */
	public void pause(String text) {
		tell("pausing"); //$NON-NLS-1$
		// TODO this doesn't really pause yet. still need a way to continue.
	}

	private TestSuiteImageView findImageView() {
		return (TestSuiteImageView) findView("org.eclipse.rse.tests.framework.ImageView"); //$NON-NLS-1$
	}

	private TestSuiteConsoleView findConsoleView() {
		return (TestSuiteConsoleView) findView("org.eclipse.rse.tests.framework.ConsoleView"); //$NON-NLS-1$
	}

	private IViewPart findView(String viewId) {
		IViewPart result = null;
		IViewSite site = holderView.getViewSite();
		IWorkbenchPage page = site.getPage();
		IViewReference[] references = page.getViewReferences();
		for (int i = 0; i < references.length; i++) {
			IViewReference reference = references[i];
			String referenceId = reference.getId();
			if (referenceId.equals(viewId)) {
				result = reference.getView(true);
			}
		}
		return result;
	}

}
