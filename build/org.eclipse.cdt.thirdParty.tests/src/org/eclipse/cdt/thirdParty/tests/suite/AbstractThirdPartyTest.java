package org.eclipse.cdt.thirdParty.tests.suite;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import junit.framework.TestCase;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.CCoreInternals;
import org.eclipse.cdt.internal.core.pdom.PDOMManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.Job;

public class AbstractThirdPartyTest extends TestCase {
	
	private static final String DEFAULT_INDEXER_TIMEOUT_SEC = "10";
	private static final String INDEXER_TIMEOUT_PROPERTY = "indexer.timeout";
	protected static final int INDEXER_TIMEOUT_SEC =
			Integer.parseInt(System.getProperty(INDEXER_TIMEOUT_PROPERTY, DEFAULT_INDEXER_TIMEOUT_SEC));

	protected boolean extractProjects() {
		String wsRoot = getWorkspace().getRoot().getLocation().toString();
		FileOutputStream output = null;
		try {
			ZipFile prjZip = new ZipFile(new File(FileLocator.toFileURL(Platform.getBundle("org.eclipse.cdt.thirdParty.tests").getEntry("/resources/" + getResourceDir() + "/" + getZipFileName())).getFile()).getAbsolutePath()); //$NON-NLS-1$ //$NON-NLS-2$
			Enumeration<? extends ZipEntry> entries = prjZip.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				InputStream input = prjZip.getInputStream( entry );
				if(input.available() > 0) {
					File outputFile = new File(wsRoot, entry.getName());
					if(!outputFile.getParentFile().exists()) {
						assertTrue("Failure creating directory in extractProjects", outputFile.getParentFile().mkdirs()); //$NON-NLS-1$
					}
					output = new FileOutputStream(outputFile);
					try {
						int count;
						byte[] buf = new byte[4096];
						while ((count = input.read(buf)) != -1) {
							output.write( buf, 0, count );
						}
						output.close();
					} catch(IOException e) {
						if(output != null) {
							assertTrue("IOException thrown in inner try: " + e, false); //$NON-NLS-1$
							output.close();
							return false;
						}
					}
				}
			}
			prjZip.close();
		} catch (IOException e) {
			assertTrue("IOException thrown in outer try: " + e, false); //$NON-NLS-1$
			return false;
		}
		return true;
	}
	
	protected boolean importProjects() {
		IWorkspace ws = getWorkspace();
		File wsRoot = ws.getRoot().getLocation().toFile();
		if(!wsRoot.isDirectory()) {
			return false;
		}
		Collection<File> files = new ArrayList<File>();
		Iterator<String> listIter = getExpectedProjectFiles().iterator();
		while (listIter.hasNext())
			files.add(new File(wsRoot, listIter.next()));

		if(files.size() == 0) {
			return false;
		}
		Iterator<File> itFiles = files.iterator();
		while (itFiles.hasNext()) {
			File projFile = itFiles.next();
			try {
				IPath locationPath = new Path(projFile.toString());
				IProjectDescription desc = ws.loadProjectDescription(locationPath);
				if (Platform.getLocation().isPrefixOf(locationPath)) {
					desc.setLocation(null);
				} else {
					desc.setLocation(locationPath);
				}
				IProject project = ws.getRoot().getProject(desc.getName());
				project.create(desc, null);
				project.open(null);
				try {
					getWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE, null);
				} catch (CoreException e) {
				}
			} catch (CoreException e) {
				return false;
			}
		}
		try {
			getWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (CoreException e) {
			fail("Import Projects refresh: " + e.getMessage()); //$NON-NLS-1$
			return false;
		}
		return true;
	}
	
    public static void waitForIndexer(ICProject project) throws InterruptedException {
		Job.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_REFRESH, null);

		final PDOMManager indexManager = CCoreInternals.getPDOMManager();
		assertTrue(indexManager.joinIndexer(INDEXER_TIMEOUT_SEC * 1000, new NullProgressMonitor()));
		long waitms= 1;
		while (waitms < 2000 && !indexManager.isProjectRegistered(project)) {
			Thread.sleep(waitms);
			waitms *= 2;
		}
		assertTrue(indexManager.isProjectRegistered(project));
		assertTrue(indexManager.joinIndexer(INDEXER_TIMEOUT_SEC * 1000, new NullProgressMonitor()));
	}
	
	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}
	
	protected String getResourceDir() {
		return 	""; //$NON-NLS-1$
	}
	
	protected String getZipFileName() {
		return 	""; //$NON-NLS-1$
	}
	
	protected List<String> getExpectedProjectFiles(){
		return new ArrayList<String>();
	}
}
