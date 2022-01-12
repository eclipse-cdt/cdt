/*******************************************************************************
 * Copyright (c) 2004, 2012 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.chelp;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.internal.ui.CHelpProviderManager;
import org.eclipse.cdt.internal.ui.text.CHelpBookDescriptor;
import org.eclipse.cdt.internal.ui.text.CHelpSettings;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.ICHelpBook;
import org.eclipse.cdt.ui.ICHelpResourceDescriptor;
import org.eclipse.cdt.ui.IFunctionSummary;
import org.eclipse.cdt.ui.text.ICHelpInvocationContext;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.junit.Assert;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *
 * CHelpProvider tests
 */
public class CHelpTest extends TestCase {
	public final static String TEST_EXTENSION_ID_PREFIX = "org.eclipse.cdt.ui.tests.chelp.extension"; //$NON-NLS-1$
	private final static String C_PROJECT_NAME = "cHelpTestProject"; //$NON-NLS-1$
	private final static String CC_PROJECT_NAME = "ccHelpTestProject"; //$NON-NLS-1$
	private final static String BIN_DIR_NAME = "bin"; //$NON-NLS-1$

	private ICProject fCProject = null;
	private ICProject fCCProject = null;
	private ICHelpInvocationContext fDefaultCCHelpContext = null;
	private ICHelpInvocationContext fDefaultCHelpContext = null;

	private ICHelpInvocationContext getDefaultCCHelpContext() throws CoreException {
		if (fDefaultCCHelpContext == null) {
			final IProject project = getCCProject().getProject();
			fDefaultCCHelpContext = new ICHelpInvocationContext() {
				@Override
				public IProject getProject() {
					return project;
				}

				@Override
				public ITranslationUnit getTranslationUnit() {
					return null;
				}
			};
		}
		return fDefaultCCHelpContext;
	}

	private ICHelpInvocationContext getDefaultCHelpContext() throws CoreException {
		if (fDefaultCHelpContext == null) {
			final IProject project = getCProject().getProject();
			fDefaultCHelpContext = new ICHelpInvocationContext() {
				@Override
				public IProject getProject() {
					return project;
				}

				@Override
				public ITranslationUnit getTranslationUnit() {
					return null;
				}
			};
		}
		return fDefaultCHelpContext;
	}

	private ICProject getCProject() throws CoreException {
		if (fCProject == null)
			fCProject = CProjectHelper.createCProject(C_PROJECT_NAME, BIN_DIR_NAME, IPDOMManager.ID_NO_INDEXER);
		return fCProject;
	}

	private ICProject getCCProject() throws CoreException {
		if (fCCProject == null)
			fCCProject = CProjectHelper.createCCProject(CC_PROJECT_NAME, BIN_DIR_NAME, IPDOMManager.ID_NO_INDEXER);
		return fCCProject;
	}

	public static Test suite() {
		return new TestSuite(CHelpTest.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		CHelpTestInfoProvider.fgEnabled = true;
	}

	@Override
	protected void tearDown() throws Exception {
		CHelpTestInfoProvider.fgEnabled = false;
		super.tearDown();
	}

	public void testCHelpProviderManagerGeneral() {
		CHelpProviderManager mngr = CHelpProviderManager.getDefault();
		if (mngr == null)
			fail("manager not created"); //$NON-NLS-1$
		if (mngr != CHelpProviderManager.getDefault())
			fail("getDefault returned an other instance of manager"); //$NON-NLS-1$

		try {
			ICHelpInvocationContext cContext = getDefaultCHelpContext();
			ICHelpInvocationContext ccContext = getDefaultCCHelpContext();

			String requestedName = "dummyName"; //$NON-NLS-1$
			CHelpProviderManager.getDefault().getMatchingFunctions(cContext, requestedName);
			CHelpProviderManager.getDefault().getMatchingFunctions(ccContext, requestedName);

			CHelpProviderManager.getDefault().getFunctionInfo(cContext, requestedName);
			CHelpProviderManager.getDefault().getFunctionInfo(ccContext, requestedName);

			CHelpProviderManager.getDefault().getHelpResources(cContext, requestedName);
			CHelpProviderManager.getDefault().getHelpResources(ccContext, requestedName);

			IConfigurationElement configElements[] = Platform.getExtensionRegistry()
					.getConfigurationElementsFor(CUIPlugin.PLUGIN_ID, CHelpSettings.CONTRIBUTION_EXTENSION);
			int numExts = 0;
			for (int i = 0; i < configElements.length; i++) {
				String id = configElements[i].getAttribute("id"); //$NON-NLS-1$
				if (id.startsWith(TEST_EXTENSION_ID_PREFIX))
					numExts++;
			}

			assertTrue("number of provider instances created (" + CHelpTestInfoProvider.getNumProviders() //$NON-NLS-1$
					+ ") is not equal to number of extensions (" + numExts + ")", //$NON-NLS-1$//$NON-NLS-2$
					numExts == CHelpTestInfoProvider.getNumProviders());
		} catch (CoreException e) {
			fail(e);
		}
	}

	public void testGetMatchingFunctions() {
		if (!CHelpProviderTester.getDefault().onlyTestInfoProvidersAvailable()) {
			//this test assumes that only CHelpTestInfoProviders are available
			return;
		}
		try {
			ICHelpInvocationContext cContext = getDefaultCHelpContext();
			ICHelpInvocationContext ccContext = getDefaultCCHelpContext();

			String requestedName = "dummyName"; //$NON-NLS-1$
			IFunctionSummary summaries[] = CHelpProviderManager.getDefault().getMatchingFunctions(cContext,
					requestedName);
			CHelpProviderTester.getDefault().checkMatchingFunctions(summaries, cContext, requestedName);

			summaries = CHelpProviderManager.getDefault().getMatchingFunctions(ccContext, requestedName);
			CHelpProviderTester.getDefault().checkMatchingFunctions(summaries, ccContext, requestedName);
		} catch (CoreException e) {
			fail(e);
		}
	}

	public void testGetFunctionInfo() {
		if (!CHelpProviderTester.getDefault().onlyTestInfoProvidersAvailable()) {
			// This test assumes that only CHelpTestInfoProviders are available.
			return;
		}
		try {
			ICHelpInvocationContext cContext = getDefaultCHelpContext();
			ICHelpInvocationContext ccContext = getDefaultCCHelpContext();

			String requestedName = "dummyName"; //$NON-NLS-1$
			IFunctionSummary summary = CHelpProviderManager.getDefault().getFunctionInfo(cContext, requestedName);
			CHelpProviderTester.getDefault().checkFunctionInfo(summary, cContext, requestedName);

			summary = CHelpProviderManager.getDefault().getFunctionInfo(ccContext, requestedName);
			CHelpProviderTester.getDefault().checkFunctionInfo(summary, ccContext, requestedName);
		} catch (CoreException e) {
			fail("CoreException occured: " + e.getMessage()); //$NON-NLS-1$
		}
	}

	public void testGetHelpResources() {
		if (!CHelpProviderTester.getDefault().onlyTestInfoProvidersAvailable()) {
			//this test assumes that only CHelpTestInfoProviders are available
			return;
		}
		try {
			ICHelpInvocationContext cContext = getDefaultCHelpContext();
			ICHelpInvocationContext ccContext = getDefaultCCHelpContext();

			String requestedName = "dummyName"; //$NON-NLS-1$
			ICHelpResourceDescriptor resourceDes[] = CHelpProviderManager.getDefault().getHelpResources(cContext,
					requestedName);
			CHelpProviderTester.getDefault().checkHelpResources(resourceDes, cContext, requestedName);

			resourceDes = CHelpProviderManager.getDefault().getHelpResources(ccContext, requestedName);
			CHelpProviderTester.getDefault().checkHelpResources(resourceDes, ccContext, requestedName);
		} catch (CoreException e) {
			fail("CoreException occured: " + e.getMessage()); //$NON-NLS-1$
		}
	}

	public void testCHelpBookDescriptors() {
		CHelpProviderManager mngr = CHelpProviderManager.getDefault();

		try {
			CHelpBookDescriptor ccBookDescriptors[] = mngr.getCHelpBookDescriptors(getDefaultCCHelpContext());
			CHelpBookDescriptor cBookDescriptors[] = mngr.getCHelpBookDescriptors(getDefaultCHelpContext());

			assertTrue("CC book descriptors length (" + ccBookDescriptors.length //$NON-NLS-1$
					+ ") is less than C book descriptors length (" + cBookDescriptors.length + ")", //$NON-NLS-1$ //$NON-NLS-2$
					ccBookDescriptors.length >= cBookDescriptors.length);

			for (int i = 0; i < cBookDescriptors.length; i++) {
				CHelpBookDescriptor curBookDes = cBookDescriptors[i];
				assertTrue("book \"" + curBookDes.getCHelpBook().getTitle() //$NON-NLS-1$
						+ "\" of type HELP_TYPE_CPP in book descriptors for C project \"" //$NON-NLS-1$
						+ getDefaultCHelpContext().getProject().getName() + "\"", //$NON-NLS-1$
						curBookDes.getCHelpBook().getCHelpType() != ICHelpBook.HELP_TYPE_CPP);
				int j = 0;
				for (; j < ccBookDescriptors.length; j++) {
					if (ccBookDescriptors[j].getCHelpBook().getTitle().equals(curBookDes.getCHelpBook().getTitle()))
						break;
				}
				assertTrue("book \"" + curBookDes.getCHelpBook().getTitle() + "\" was not found in CC books", //$NON-NLS-1$//$NON-NLS-2$
						j < ccBookDescriptors.length);
			}

			for (int i = 0; i < ccBookDescriptors.length; i++) {
				CHelpBookDescriptor curBookDes = ccBookDescriptors[i];
				int j = 0;
				for (; j < cBookDescriptors.length; j++) {
					if (cBookDescriptors[j].getCHelpBook().getTitle().equals(curBookDes.getCHelpBook().getTitle()))
						break;
				}
				assertTrue("book \"" + curBookDes.getCHelpBook().getTitle() //$NON-NLS-1$
						+ "\" of type HELP_TYPE_C was not found in C books", //$NON-NLS-1$
						j < cBookDescriptors.length
								|| curBookDes.getCHelpBook().getCHelpType() == ICHelpBook.HELP_TYPE_CPP);
			}
		} catch (CoreException e) {
			fail("CoreException occured: " + e.getMessage()); //$NON-NLS-1$
		}
	}

	static void fail(Throwable t) {
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		printWriter.println(t);
		t.printStackTrace(printWriter);
		Assert.fail(stringWriter.toString());
	}
}
