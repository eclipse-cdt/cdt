package org.eclipse.cdt.thirdParty.tests.suite;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

public class ThirdPartyLegacyScannerInfoProviderTest extends AbstractThirdPartyTest {

	private List<IProject> projList = new LinkedList<IProject>();

	public static Test suite() {
		return new TestSuite(ThirdPartyLegacyScannerInfoProviderTest.class);
	}

	public void testLegacyScannerInfoProvider(){
		extractProjects();
		assertTrue("Can't import Projects", importProjects()); //$NON-NLS-1$

		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		List<IProject> rootProjects = new ArrayList<IProject>();
		for(int i = 0 ; i < projects.length; i++) {
			rootProjects.add(projects[i]);
		}
		for (IProject project : rootProjects) {
			try {
				waitForIndexer(CoreModel.getDefault().create(project));
				project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			IFile resource = project.getFile("test.c");
			IScannerInfoProvider provider = CCorePlugin.getDefault().getScannerInfoProvider(project);
			IScannerInfo scannerInfo = provider.getScannerInformation(resource);
			assertNotNull(scannerInfo);
			Map<String, String> macros = scannerInfo.getDefinedSymbols();
			assertTrue(macros.size() > 0);
			assertTrue(macros.containsKey("X"));
			assertTrue(macros.containsKey("Y"));
			assertTrue(macros.containsKey("Z"));
		}
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		for(Iterator<IProject> iter = projList.iterator(); iter.hasNext();){
			IProject proj = iter.next();
			try {
				proj.delete(true, null);
			} catch (Exception e){
			}
			iter.remove();
		}
		super.tearDown();
	}

	@Override
	protected String getZipFileName() {
		return 	"Bug398056.zip"; //$NON-NLS-1$
	}

	@Override
	protected String getResourceDir() {
		return "scannerInfoProvider"; //$NON-NLS-1$
	}

	@Override
	protected List<String> getExpectedProjectFiles(){
		List<String> toBeReturned = new ArrayList<String>();
		toBeReturned.add("testproject_Bug398056/.project"); //$NON-NLS-1$
		return toBeReturned;
	}
}
