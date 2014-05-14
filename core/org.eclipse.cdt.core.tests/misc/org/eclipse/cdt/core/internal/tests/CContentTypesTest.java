package org.eclipse.cdt.core.internal.tests;

import org.eclipse.cdt.internal.core.CContentTypes;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.content.IContentType;
import org.junit.Assert;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests the {@link CContentTypes#getContentType(IProject, String)} method for a non existing project.
 * This method gets called with a non existing project if a project has been renamed and an editor
 *  is opened for one of the renamed projects file.
 */
public class CContentTypesTest extends TestCase {
    public static Test suite() {
        return new TestSuite(CContentTypesTest.class);
    }
	private IProject fProject;

    @Override
	protected void setUp() {
		final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		fProject= root.getProject("contentTypes_" + getName());
		// do not create the project because the test does need a non existing project
    }
    	
	public void testGetContentTypeReturnsCHeaderForADotHFile() throws CoreException {
		IContentType contentType = CContentTypes.getContentType(fProject, "myFile.h");
		Assert.assertEquals("org.eclipse.cdt.core.cHeader", contentType.getId());
	}
	
	public void testGetContentTypeReturnsCxxHeaderForADotHppFile() throws CoreException {
		IContentType contentType = CContentTypes.getContentType(fProject, "myFile.hpp");
		Assert.assertEquals("org.eclipse.cdt.core.cxxHeader", contentType.getId());
	}
	
	public void testGetContentTypeReturnsCSourceForADotCFile() throws CoreException {
		IContentType contentType = CContentTypes.getContentType(fProject, "myFile.c");
		Assert.assertEquals("org.eclipse.cdt.core.cSource", contentType.getId());
	}
	
	public void testGetContentTypeReturnsCxxSourceForADotCppFile() throws CoreException {
		IContentType contentType = CContentTypes.getContentType(fProject, "myFile.cpp");
		Assert.assertEquals("org.eclipse.cdt.core.cxxSource", contentType.getId());
	}
}
