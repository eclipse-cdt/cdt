/*******************************************************************************
 * Copyright (c) 2007, 2012 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.pdom.tests;

import java.io.File;
import java.net.URI;

import junit.framework.Test;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexLocationConverter;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.index.ResourceContainerRelativeLocationConverter;
import org.eclipse.cdt.core.index.URIRelativeLocationConverter;
import org.eclipse.cdt.core.index.provider.IPDOMDescriptor;
import org.eclipse.cdt.core.index.provider.IReadOnlyPDOMProvider;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.LanguageManager;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
import org.eclipse.cdt.internal.core.CCoreInternals;
import org.eclipse.cdt.internal.core.index.provider.IndexProviderManager;
import org.eclipse.cdt.internal.core.index.provider.ReadOnlyPDOMProviderBridge;
import org.eclipse.cdt.internal.core.pdom.WritablePDOM;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * Tests addition of external pdom's into the logical index
 */
public class PDOMProviderTests extends PDOMTestBase {
	private static final int A_FRAGMENT_OPTIONS = IIndexManager.ADD_EXTENSION_FRAGMENTS_ADD_IMPORT;

	public static Test suite() {
		return suite(PDOMProviderTests.class);
	}

	public void testLifeCycle() throws Exception {
		final File tempPDOM= File.createTempFile("foo", "bar");

		{
			ICProject cproject= CProjectHelper.createCCProject("foo" + System.currentTimeMillis(), null, IPDOMManager.ID_FAST_INDEXER);
			TestSourceReader.createFile(cproject.getProject(), new Path("/this.h"), "class A {};\n\n");
			waitForIndexer(cproject);

			IIndex index= CCorePlugin.getIndexManager().getIndex(cproject, A_FRAGMENT_OPTIONS);
			index.acquireReadLock();
			try {
				IBinding[] bindings= index.findBindings("A".toCharArray(), IndexFilter.ALL, npm());
				assertEquals(1, bindings.length);
			} finally {
				index.releaseReadLock();
			}

			ResourceContainerRelativeLocationConverter cvr= new ResourceContainerRelativeLocationConverter(cproject.getProject());
			CCoreInternals.getPDOMManager().exportProjectPDOM(cproject, tempPDOM, cvr);
			assertTrue(tempPDOM.exists());

			CProjectHelper.delete(cproject);
		}

		final URI baseURI= new File("c:/ExternalSDK/").toURI();
		final ICProject cproject2= CProjectHelper.createCCProject("bar" + System.currentTimeMillis(), null, IPDOMManager.ID_FAST_INDEXER);
		TestSourceReader.createFile(cproject2.getProject(), new Path("/source.cpp"), "namespace X { class A {}; }\n\n");
		waitForIndexer(cproject2);

		IndexProviderManager ipm= CCoreInternals.getPDOMManager().getIndexProviderManager();
		ipm.addIndexProvider(new ReadOnlyPDOMProviderBridge(
				new IReadOnlyPDOMProvider() {
					@Override
					public IPDOMDescriptor[] getDescriptors(ICConfigurationDescription config) {
						return new IPDOMDescriptor[] {
								new IPDOMDescriptor() {
									@Override
									public IIndexLocationConverter getIndexLocationConverter() {
										return new URIRelativeLocationConverter(baseURI);
									}

									@Override
									public IPath getLocation() {
										return new Path(tempPDOM.getAbsolutePath());
									}
								}
						};
					}

					@Override
					public boolean providesFor(ICProject project) throws CoreException {
						return cproject2.equals(project);
					}
				}
		));
		IIndex index= CCorePlugin.getIndexManager().getIndex(cproject2, A_FRAGMENT_OPTIONS);
		index.acquireReadLock();
		try {
			IBinding[] bindings= index.findBindings("A".toCharArray(), IndexFilter.ALL, npm());
			assertEquals(1, bindings.length);
			bindings= index.findBindingsForPrefix("A".toCharArray(), false, new IndexFilter() {
				@Override
				public boolean acceptBinding(IBinding binding) {
					return binding instanceof ICPPClassType;
				}
			}, null);
			assertEquals(2, bindings.length);
		} finally {
			index.releaseReadLock();
		}
	}

	public void testCommonSDK() throws Exception {
		final File tempPDOM= File.createTempFile("foo", "bar");

		{
			ICProject cproject= CProjectHelper.createCCProject("foo" + System.currentTimeMillis(), null, IPDOMManager.ID_FAST_INDEXER);
			TestSourceReader.createFile(cproject.getProject(), new Path("/this.h"), "class A {};\n\n");
			waitForIndexer(cproject);

			IIndex index= CCorePlugin.getIndexManager().getIndex(cproject, A_FRAGMENT_OPTIONS);
			index.acquireReadLock();
			try {
				IBinding[] bindings= index.findBindings("A".toCharArray(), IndexFilter.ALL, npm());
				assertEquals(1, bindings.length);
			} finally {
				index.releaseReadLock();
			}

			ResourceContainerRelativeLocationConverter cvr= new ResourceContainerRelativeLocationConverter(cproject.getProject());
			CCoreInternals.getPDOMManager().exportProjectPDOM(cproject, tempPDOM, cvr);
			assertTrue(tempPDOM.exists());

			CProjectHelper.delete(cproject);
		}

		final ICProject cproject3= CProjectHelper.createCCProject("bar" + System.currentTimeMillis(), null, IPDOMManager.ID_FAST_INDEXER);
		TestSourceReader.createFile(cproject3.getProject(), new Path("/source.cpp"), "namespace Y { class A {}; }\n\n");
		waitForIndexer(cproject3);

		final URI baseURI= new File("c:/ExternalSDK/").toURI();
		final ICProject cproject2= CProjectHelper.createCCProject("baz" + System.currentTimeMillis(), null, IPDOMManager.ID_FAST_INDEXER);
		TestSourceReader.createFile(cproject2.getProject(), new Path("/source.cpp"), "namespace X { class A {}; }\n\n");
		waitForIndexer(cproject2);

		IndexProviderManager ipm= CCoreInternals.getPDOMManager().getIndexProviderManager();
		ipm.addIndexProvider(new ReadOnlyPDOMProviderBridge(
				new IReadOnlyPDOMProvider() {
					@Override
					public IPDOMDescriptor[] getDescriptors(ICConfigurationDescription config) {
						return new IPDOMDescriptor[] {
								new IPDOMDescriptor() {
									@Override
									public IIndexLocationConverter getIndexLocationConverter() {
										return new URIRelativeLocationConverter(baseURI);
									}

									@Override
									public IPath getLocation() {
										return new Path(tempPDOM.getAbsolutePath());
									}
								}
						};
					}

					@Override
					public boolean providesFor(ICProject project) throws CoreException {
						return cproject2.equals(project) || cproject3.equals(project);
					}
				}
		));

		{
			IIndex index= CCorePlugin.getIndexManager().getIndex(cproject2, A_FRAGMENT_OPTIONS);
			index.acquireReadLock();
			try {
				IBinding[] bindings= index.findBindings("A".toCharArray(), IndexFilter.ALL, npm());
				assertEquals(1, bindings.length);
				assertEquals(1, index.findDefinitions(bindings[0]).length);
				bindings= index.findBindingsForPrefix("A".toCharArray(), false, new IndexFilter() {
					@Override
					public boolean acceptBinding(IBinding binding) {
						return binding instanceof ICPPClassType;
					}
				}, null);
				assertEquals(2, bindings.length);
			} finally {
				index.releaseReadLock();
			}
		}

		{
			IIndex index= CCorePlugin.getIndexManager().getIndex(cproject3, A_FRAGMENT_OPTIONS);
			index.acquireReadLock();
			try {
				IBinding[] bindings= index.findBindings("A".toCharArray(), IndexFilter.ALL, npm());
				assertEquals(1, bindings.length);
				assertEquals(1, index.findDefinitions(bindings[0]).length);
				bindings= index.findBindingsForPrefix("A".toCharArray(), false, new IndexFilter() {
					@Override
					public boolean acceptBinding(IBinding binding) {
						return binding instanceof ICPPClassType;
					}
				}, null);
				assertEquals(2, bindings.length);
			} finally {
				index.releaseReadLock();
			}
		}
		
		{
			IIndex index= CCorePlugin.getIndexManager().getIndex(new ICProject[]{cproject2, cproject3}, A_FRAGMENT_OPTIONS);
			index.acquireReadLock();
			try {
				IBinding[] bindings= index.findBindings("A".toCharArray(), IndexFilter.ALL, npm());
				assertEquals(1, bindings.length);
				assertEquals(1, index.findDefinitions(bindings[0]).length);
				bindings= index.findBindingsForPrefix("A".toCharArray(), false, new IndexFilter() {
					@Override
					public boolean acceptBinding(IBinding binding) {
						return binding instanceof ICPPClassType;
					}
				}, null);
				assertEquals(3, bindings.length);
			} finally {
				index.releaseReadLock();
			}		
		}
	}
	
	public void testVersionMismatchOfExternalPDOM_178998() throws Exception {
		final File tempPDOM= File.createTempFile("foo", "bar");

		{
			ICProject cproject= CProjectHelper.createCCProject("foo" + System.currentTimeMillis(), null, IPDOMManager.ID_FAST_INDEXER);
			TestSourceReader.createFile(cproject.getProject(), new Path("/this.h"), "class A {};\n\n");
			waitForIndexer(cproject);
			ResourceContainerRelativeLocationConverter cvr= new ResourceContainerRelativeLocationConverter(cproject.getProject());
			CCoreInternals.getPDOMManager().exportProjectPDOM(cproject, tempPDOM, cvr);
			CProjectHelper.delete(cproject);
			
			// Mimic a PDOM with superseded version.
			WritablePDOM wpdom= new WritablePDOM(tempPDOM, cvr, LanguageManager.getInstance().getPDOMLinkageFactoryMappings());
			wpdom.acquireWriteLock();
			try {
				wpdom.getDB().setVersion(1);	
				wpdom.close();
			} finally {
				wpdom.releaseWriteLock();
			}
		}

		final URI baseURI= new File("c:/ExternalSDK/").toURI();
		final ICProject cproject2= CProjectHelper.createCCProject("baz" + System.currentTimeMillis(), null, IPDOMManager.ID_FAST_INDEXER);
		TestSourceReader.createFile(cproject2.getProject(), new Path("/source.cpp"), "namespace X { class A {}; }\n\n");
		waitForIndexer(cproject2);

		IndexProviderManager ipm= CCoreInternals.getPDOMManager().getIndexProviderManager();
		ipm.addIndexProvider(new ReadOnlyPDOMProviderBridge(
				new IReadOnlyPDOMProvider() {
					@Override
					public IPDOMDescriptor[] getDescriptors(ICConfigurationDescription config) {
						return new IPDOMDescriptor[] {
								new IPDOMDescriptor() {
									@Override
									public IIndexLocationConverter getIndexLocationConverter() {
										return new URIRelativeLocationConverter(baseURI);
									}

									@Override
									public IPath getLocation() {
										return new Path(tempPDOM.getAbsolutePath());
									}

								}
						};
					}

					@Override
					public boolean providesFor(ICProject project) throws CoreException {
						return cproject2.equals(project);
					}
				}
		));
		
		setExpectedNumberOfLoggedNonOKStatusObjects(1); // (this applies to the entire test duration)
		
		for (int i= 0; i < 3; i++) {
			// Try several times in order to test the status is logged only once.
			ICProjectDescription pd= CCorePlugin.getDefault().getProjectDescription(cproject2.getProject(), false);
			assertEquals(0, ipm.getProvidedIndexFragments(pd.getActiveConfiguration(), -1).length);
		}
	}
}
