/*******************************************************************************
 * Copyright (c) 2016 COSEDA Technologies GmbH
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dominic Scharfe (COSEDA Technologies GmbH) - initial implementation 
 *
 *******************************************************************************/
package org.eclipse.cdt.codan.checkers.ui.quickfix;

import static org.junit.Assert.*;

import org.eclipse.cdt.codan.internal.checkers.ui.quickfix.QuickFixCreateNewClass;
import org.eclipse.cdt.codan.ui.AbstractCodanCMarkerResolution;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import static org.mockito.Mockito.*;

import java.util.function.Function;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;

@RunWith(MockitoJUnitRunner.class)
public class QuickFixCreateNewClassTest {

	QuickFixCreateNewClass qut;

	@Mock
	IMarker marker;

	/**
	 * TranslationUnit of the marker's resource which is received from the
	 * workspace.
	 */
	@Mock
	ITranslationUnit translationUnitViaWorkspace;

	/**
	 * TranslationUnit of the marker's resource if there is an open editor.
	 */
	@Mock
	ITranslationUnit translationUnitWorkingCopy;

	/**
	 * Function to get the working copy of translationUnitViaWorkspace
	 */
	@Mock
	Function<ITranslationUnit, ITranslationUnit> toWorkingCopy;

	/**
	 * Language of the TranslationUnit
	 */
	@Mock
	ILanguage translationUnitLanguage;

	@Before
	public void setUp() throws Exception {
		qut = new QuickFixCreateNewClass(toWorkingCopy) {
			@Override
			protected ITranslationUnit getTranslationUnitViaWorkspace(IMarker marker) {
				if (QuickFixCreateNewClassTest.this.marker == marker) {
					return translationUnitViaWorkspace;
				}
				throw new RuntimeException("Invalid marker");
			};
		};

		when(toWorkingCopy.apply(translationUnitViaWorkspace)).thenReturn(translationUnitWorkingCopy);
		when(translationUnitWorkingCopy.getLanguage()).thenReturn(translationUnitLanguage);
	}

	/**
	 * Test if the marker is applicable if
	 * {@link AbstractCodanCMarkerResolution#getTranslationUnitViaWorkspace}
	 * returns null.
	 */
	@Test
	public void isApplicableForUnresolvableMarker() {
		translationUnitViaWorkspace = null;
		assertTrue("Unresolvable marker is not applicable", qut.isApplicable(marker));
	}

	/**
	 * Test if the marker is applicable if
	 * {@link AbstractCodanCMarkerResolution#getTranslationUnitViaWorkspace}
	 * returns a marker valid marker.
	 */
	@Test
	public void isApplicableForResolvableMarker() {
		assertTrue("Resolvable marker is not applicable", qut.isApplicable(marker));
	}

	/**
	 * Test if the marker is applicable if
	 * <code>tu.getLanguage().getLinkageID() == ILinkage.C_LINKAGE_ID</code>
	 */
	@Test
	public void isNotApplicableForResolvableMarkerWithCLinkage() {
		when(translationUnitLanguage.getLinkageID()).thenReturn(ILinkage.C_LINKAGE_ID);
		assertFalse("Resolvable marker with ILinkage.C_LINKAGE_ID must not be applicable", qut.isApplicable(marker));
	}

	/**
	 * Test if the marker is applicable if <code>tu.getLanguage()</code> throws
	 * a {@link CoreException}.
	 * 
	 * @throws CoreException
	 */
	@Test
	public void isApplicableForResolvableMarkerWithCLinkageResolutionCoreException() throws CoreException {
		CoreException coreException = new CoreException(mock(IStatus.class));
		doThrow(coreException).when(translationUnitWorkingCopy).getLanguage();
		assertTrue("Resolvable marker with invalid language must be applicable", qut.isApplicable(marker));
	}
}
