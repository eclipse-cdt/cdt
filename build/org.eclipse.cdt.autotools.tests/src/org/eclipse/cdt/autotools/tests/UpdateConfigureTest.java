/*******************************************************************************
 * Copyright (c) 2008, 2012 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *     Marc-Andre Laperle - Fix failing test on Windows
 *******************************************************************************/
package org.eclipse.cdt.autotools.tests;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import junit.framework.TestCase;

import org.eclipse.cdt.autotools.core.AutotoolsOptionConstants;
import org.eclipse.cdt.autotools.core.AutotoolsPlugin;
import org.eclipse.cdt.autotools.core.IAutotoolsOption;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.ManagedCProjectNature;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

// This test verifies an autogen.sh project that builds configure, but
// does not run it.
public class UpdateConfigureTest extends TestCase {
    
	private IProject testProject;
	
    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        if (!ProjectTools.setup())
        	fail("could not perform basic project workspace setup");
		testProject = ProjectTools.createProject("testProject2");
		if (testProject == null) {
            fail("Unable to create test project");
        }
		testProject.open(new NullProgressMonitor());
    }
	
    /**
     * Test getting and updating configuration options for an Autotools Project. The top-level 
     * contains autogen.sh which will build configure, but not run it.
     * @throws Exception
     */
	public void testGetAndUpdateConfigureOptions() throws Exception {
		Path p = new Path("zip/project2.zip");
		ProjectTools.addSourceContainerWithImport(testProject, "src", p, null);
		assertTrue(testProject.hasNature(ManagedCProjectNature.MNG_NATURE_ID));
		ProjectTools.setConfigDir(testProject, "src");
		ProjectTools.markExecutable(testProject, "src/autogen.sh");
		assertTrue(ProjectTools.build());
		ICConfigurationDescription cfgDes = CoreModel.getDefault().getProjectDescription(testProject).getActiveConfiguration();
		IConfiguration cfg = ManagedBuildManager.getConfigurationForDescription(cfgDes);
		assertTrue(cfg.getName().equals("Build (GNU)"));
		Map<String, IAutotoolsOption> opts = AutotoolsPlugin.getDefault().getAutotoolCfgOptions(testProject, cfg.getId());
		IAutotoolsOption configdir = opts.get(AutotoolsOptionConstants.OPT_CONFIGDIR);
		assertEquals(configdir.getType(), IAutotoolsOption.INTERNAL);
		assertTrue(configdir.getValue().equals("src"));
		assertTrue(configdir.canUpdate());
		// Verify we cannot update any of the categories or flags
		IAutotoolsOption k = opts.get(AutotoolsOptionConstants.CATEGORY_DIRECTORIES);
		assertFalse(k.canUpdate());
		assertEquals(k.getType(), IAutotoolsOption.CATEGORY);

		k = opts.get(AutotoolsOptionConstants.CATEGORY_FEATURES);
		assertFalse(k.canUpdate());
		assertEquals(k.getType(), IAutotoolsOption.CATEGORY);

		k = opts.get(AutotoolsOptionConstants.CATEGORY_FILENAMES);
		assertFalse(k.canUpdate());

		k = opts.get(AutotoolsOptionConstants.CATEGORY_GENERAL);
		assertEquals(k.getType(), IAutotoolsOption.CATEGORY);
		assertFalse(k.canUpdate());

		k = opts.get(AutotoolsOptionConstants.CATEGORY_OPTIONS);
		assertEquals(k.getType(), IAutotoolsOption.CATEGORY);
		assertFalse(k.canUpdate());

		k = opts.get(AutotoolsOptionConstants.CATEGORY_PLATFORM);
		assertFalse(k.canUpdate());
		assertEquals(k.getType(), IAutotoolsOption.CATEGORY);

		k = opts.get(AutotoolsOptionConstants.FLAG_CFLAGS);
		assertFalse(k.canUpdate());
		assertEquals(k.getType(), IAutotoolsOption.FLAG);
		
		// Tools are ok to update
		k = opts.get(AutotoolsOptionConstants.TOOL_AUTOGEN);
		assertTrue(k.canUpdate());
		assertEquals(k.getType(), IAutotoolsOption.TOOL);
		assertEquals(k.getValue(), "autogen.sh"); //$NON-NLS-1$
		k.setValue("autogen2.sh"); //$NON-NLS-1$

		k = opts.get(AutotoolsOptionConstants.TOOL_CONFIGURE);
		assertTrue(k.canUpdate());
		assertEquals(k.getType(), IAutotoolsOption.TOOL);
		assertEquals(k.getValue(), "configure"); //$NON-NLS-1$
		k.setValue("config"); //$NON-NLS-1$

		// Flag values are ok to update
		k = opts.get(AutotoolsOptionConstants.OPT_CFLAGS_DEBUG);
		assertTrue(k.canUpdate());
		assertEquals(k.getType(), IAutotoolsOption.FLAGVALUE);
		assertEquals(k.getValue(), "false"); //$NON-NLS-1$
		k.setValue("true");
		
		k = opts.get(AutotoolsOptionConstants.OPT_CFLAGS_GCOV);
		assertTrue(k.canUpdate());
		assertEquals(k.getType(), IAutotoolsOption.FLAGVALUE);
		assertEquals(k.getValue(), "false"); //$NON-NLS-1$
		k.setValue("true");
		
		k = opts.get(AutotoolsOptionConstants.OPT_CFLAGS_GPROF);
		assertTrue(k.canUpdate());
		assertEquals(k.getType(), IAutotoolsOption.FLAGVALUE);
		assertEquals(k.getValue(), "false"); //$NON-NLS-1$
		k.setValue("true");

		// Check other options
		k = opts.get(AutotoolsOptionConstants.OPT_AUTOGENOPTS);
		assertTrue(k.canUpdate());
		assertEquals(k.getType(), IAutotoolsOption.MULTIARG);
		assertEquals(k.getValue(), ""); //$NON-NLS-1$
		k.setValue("some opts");

		k = opts.get(AutotoolsOptionConstants.OPT_BINDIR);
		assertTrue(k.canUpdate());
		assertEquals(k.getType(), IAutotoolsOption.STRING);
		assertEquals(k.getValue(), ""); //$NON-NLS-1$
		k.setValue("/usr/bin");
		
		k = opts.get(AutotoolsOptionConstants.OPT_BUILD);
		assertTrue(k.canUpdate());
		assertEquals(k.getType(), IAutotoolsOption.STRING);
		assertEquals(k.getValue(), ""); //$NON-NLS-1$
		k.setValue("linux");
		
		k = opts.get(AutotoolsOptionConstants.OPT_CACHE_FILE);
		assertTrue(k.canUpdate());
		assertEquals(k.getType(), IAutotoolsOption.STRING);
		assertEquals(k.getValue(), ""); //$NON-NLS-1$
		k.setValue("config.status");
		
		k = opts.get(AutotoolsOptionConstants.OPT_DATADIR);
		assertTrue(k.canUpdate());
		assertEquals(k.getType(), IAutotoolsOption.STRING);
		assertEquals(k.getValue(), ""); //$NON-NLS-1$
		k.setValue("/usr/data");
		
		k = opts.get(AutotoolsOptionConstants.OPT_ENABLE_MAINTAINER_MODE);
		assertTrue(k.canUpdate());
		assertEquals(k.getType(), IAutotoolsOption.BIN);
		assertEquals(k.getValue(), "false"); //$NON-NLS-1$
		k.setValue("true");
		
		k = opts.get(AutotoolsOptionConstants.OPT_EXEC_PREFIX);
		assertTrue(k.canUpdate());
		assertEquals(k.getType(), IAutotoolsOption.STRING);
		assertEquals(k.getValue(), ""); //$NON-NLS-1$
		k.setValue("/usr/exec");
		
		k = opts.get(AutotoolsOptionConstants.OPT_HELP);
		assertTrue(k.canUpdate());
		assertEquals(k.getType(), IAutotoolsOption.BIN);
		assertEquals(k.getValue(), "false"); //$NON-NLS-1$
		k.setValue("true");
		
		k = opts.get(AutotoolsOptionConstants.OPT_HOST);
		assertTrue(k.canUpdate());
		assertEquals(k.getType(), IAutotoolsOption.STRING);
		assertEquals(k.getValue(), ""); //$NON-NLS-1$
		k.setValue("linux");
		
		k = opts.get(AutotoolsOptionConstants.OPT_INCLUDEDIR);
		assertTrue(k.canUpdate());
		assertEquals(k.getType(), IAutotoolsOption.STRING);
		assertEquals(k.getValue(), ""); //$NON-NLS-1$
		k.setValue("/usr/include");
		
		k = opts.get(AutotoolsOptionConstants.OPT_INFODIR);
		assertTrue(k.canUpdate());
		assertEquals(k.getType(), IAutotoolsOption.STRING);
		assertEquals(k.getValue(), ""); //$NON-NLS-1$
		k.setValue("/usr/info");
		
		k = opts.get(AutotoolsOptionConstants.OPT_LIBDIR);
		assertTrue(k.canUpdate());
		assertEquals(k.getType(), IAutotoolsOption.STRING);
		assertEquals(k.getValue(), ""); //$NON-NLS-1$
		k.setValue("/usr/lib");
		
		k = opts.get(AutotoolsOptionConstants.OPT_LIBEXECDIR);
		assertTrue(k.canUpdate());
		assertEquals(k.getType(), IAutotoolsOption.STRING);
		assertEquals(k.getValue(), ""); //$NON-NLS-1$
		k.setValue("/usr/libexec");
		
		k = opts.get(AutotoolsOptionConstants.OPT_LOCALSTATEDIR);
		assertTrue(k.canUpdate());
		assertEquals(k.getType(), IAutotoolsOption.STRING);
		assertEquals(k.getValue(), ""); //$NON-NLS-1$
		k.setValue("/usr/localstate");
		
		k = opts.get(AutotoolsOptionConstants.OPT_MANDIR);
		assertTrue(k.canUpdate());
		assertEquals(k.getType(), IAutotoolsOption.STRING);
		assertEquals(k.getValue(), ""); //$NON-NLS-1$
		k.setValue("/usr/man");
		
		k = opts.get(AutotoolsOptionConstants.OPT_NO_CREATE);
		assertTrue(k.canUpdate());
		assertEquals(k.getType(), IAutotoolsOption.BIN);
		assertEquals(k.getValue(), "false"); //$NON-NLS-1$
		k.setValue("true");
		
		k = opts.get(AutotoolsOptionConstants.OPT_OLDINCLUDEDIR);
		assertTrue(k.canUpdate());
		assertEquals(k.getType(), IAutotoolsOption.STRING);
		assertEquals(k.getValue(), ""); //$NON-NLS-1$
		k.setValue("/usr/oldinclude");
		
		k = opts.get(AutotoolsOptionConstants.OPT_PREFIX);
		assertTrue(k.canUpdate());
		assertEquals(k.getType(), IAutotoolsOption.STRING);
		assertEquals(k.getValue(), ""); //$NON-NLS-1$
		k.setValue("prefix");
		
		k = opts.get(AutotoolsOptionConstants.OPT_PROGRAM_PREFIX);
		assertTrue(k.canUpdate());
		assertEquals(k.getType(), IAutotoolsOption.STRING);
		assertEquals(k.getValue(), ""); //$NON-NLS-1$
		k.setValue("program_prefix");
		
		k = opts.get(AutotoolsOptionConstants.OPT_PROGRAM_SUFFIX);
		assertTrue(k.canUpdate());
		assertEquals(k.getType(), IAutotoolsOption.STRING);
		assertEquals(k.getValue(), ""); //$NON-NLS-1$
		k.setValue("program_suffix");
		
		k = opts.get(AutotoolsOptionConstants.OPT_PROGRAM_TRANSFORM_NAME);
		assertTrue(k.canUpdate());
		assertEquals(k.getType(), IAutotoolsOption.STRING);
		assertEquals(k.getValue(), ""); //$NON-NLS-1$
		k.setValue("program_transform_name");
		
		k = opts.get(AutotoolsOptionConstants.OPT_QUIET);
		assertTrue(k.canUpdate());
		assertEquals(k.getType(), IAutotoolsOption.BIN);
		assertEquals(k.getValue(), "false"); //$NON-NLS-1$
		k.setValue("true");
		
		k = opts.get(AutotoolsOptionConstants.OPT_SBINDIR);
		assertTrue(k.canUpdate());
		assertEquals(k.getType(), IAutotoolsOption.STRING);
		assertEquals(k.getValue(), ""); //$NON-NLS-1$
		k.setValue("/usr/sbin");
		
		k = opts.get(AutotoolsOptionConstants.OPT_SHAREDSTATEDIR);
		assertTrue(k.canUpdate());
		assertEquals(k.getType(), IAutotoolsOption.STRING);
		assertEquals(k.getValue(), ""); //$NON-NLS-1$
		k.setValue("/usr/sharedstate");
		
		k = opts.get(AutotoolsOptionConstants.OPT_SRCDIR);
		assertTrue(k.canUpdate());
		assertEquals(k.getType(), IAutotoolsOption.STRING);
		assertEquals(k.getValue(), ""); //$NON-NLS-1$
		k.setValue("src");
		
		k = opts.get(AutotoolsOptionConstants.OPT_SYSCONFDIR);
		assertTrue(k.canUpdate());
		assertEquals(k.getType(), IAutotoolsOption.STRING);
		assertEquals(k.getValue(), ""); //$NON-NLS-1$
		k.setValue("/usr/sysconf");
		
		k = opts.get(AutotoolsOptionConstants.OPT_TARGET);
		assertTrue(k.canUpdate());
		assertEquals(k.getType(), IAutotoolsOption.STRING);
		assertEquals(k.getValue(), ""); //$NON-NLS-1$
		k.setValue("target");
		
		
		k = opts.get(AutotoolsOptionConstants.OPT_USER);
		assertTrue(k.canUpdate());
		assertEquals(k.getType(), IAutotoolsOption.MULTIARG);
		assertEquals(k.getValue(), ""); //$NON-NLS-1$
		k.setValue("user opts");
		
		k = opts.get(AutotoolsOptionConstants.OPT_VERSION);
		assertTrue(k.canUpdate());
		assertEquals(k.getType(), IAutotoolsOption.BIN);
		assertEquals(k.getValue(), "false"); //$NON-NLS-1$
		k.setValue("true");
		
		// Verify last option changed has changed in our copy, but not
		// in the actual options
		assertEquals(k.getValue(), "true");
		Map<String, IAutotoolsOption> opts2 = AutotoolsPlugin.getDefault().getAutotoolCfgOptions(testProject, cfg.getId());
		IAutotoolsOption k2 = opts2.get(AutotoolsOptionConstants.OPT_VERSION);
		assertEquals(k2.getValue(), "false");
		
		// Now update the options we changed
		AutotoolsPlugin.getDefault().updateAutotoolCfgOptions(testProject, cfg.getId(), opts);
		opts2 = AutotoolsPlugin.getDefault().getAutotoolCfgOptions(testProject, cfg.getId());
		
		// Verify new option values
		k = opts2.get(AutotoolsOptionConstants.TOOL_AUTOGEN);
		assertEquals(k.getValue(), "autogen2.sh"); //$NON-NLS-1$

		k = opts2.get(AutotoolsOptionConstants.TOOL_CONFIGURE);
		assertEquals(k.getValue(), "config"); //$NON-NLS-1$

		k = opts2.get(AutotoolsOptionConstants.OPT_CFLAGS_DEBUG);
		assertEquals(k.getValue(), "true"); //$NON-NLS-1$
		
		k = opts2.get(AutotoolsOptionConstants.OPT_CFLAGS_GCOV);
		assertEquals(k.getValue(), "true"); //$NON-NLS-1$
		
		k = opts2.get(AutotoolsOptionConstants.OPT_CFLAGS_GPROF);
		assertEquals(k.getValue(), "true"); //$NON-NLS-1$

		k = opts2.get(AutotoolsOptionConstants.OPT_AUTOGENOPTS);
		assertEquals(k.getValue(), "some opts"); //$NON-NLS-1$

		k = opts2.get(AutotoolsOptionConstants.OPT_BINDIR);
		assertEquals(k.getValue(), "/usr/bin"); //$NON-NLS-1$
		
		k = opts2.get(AutotoolsOptionConstants.OPT_BUILD);
		assertEquals(k.getValue(), "linux"); //$NON-NLS-1$
		
		k = opts2.get(AutotoolsOptionConstants.OPT_CACHE_FILE);
		assertEquals(k.getValue(), "config.status"); //$NON-NLS-1$
		
		k = opts2.get(AutotoolsOptionConstants.OPT_DATADIR);
		assertEquals(k.getValue(), "/usr/data"); //$NON-NLS-1$
		
		k = opts2.get(AutotoolsOptionConstants.OPT_ENABLE_MAINTAINER_MODE);
		assertEquals(k.getValue(), "true"); //$NON-NLS-1$
		
		k = opts2.get(AutotoolsOptionConstants.OPT_EXEC_PREFIX);
		assertEquals(k.getValue(), "/usr/exec"); //$NON-NLS-1$
		
		k = opts2.get(AutotoolsOptionConstants.OPT_HELP);
		assertEquals(k.getValue(), "true"); //$NON-NLS-1$
		
		k = opts2.get(AutotoolsOptionConstants.OPT_HOST);
		assertEquals(k.getValue(), "linux"); //$NON-NLS-1$
		
		k = opts2.get(AutotoolsOptionConstants.OPT_INCLUDEDIR);
		assertEquals(k.getValue(), "/usr/include"); //$NON-NLS-1$
		
		k = opts2.get(AutotoolsOptionConstants.OPT_INFODIR);
		assertEquals(k.getValue(), "/usr/info"); //$NON-NLS-1$
		
		k = opts2.get(AutotoolsOptionConstants.OPT_LIBDIR);
		assertEquals(k.getValue(), "/usr/lib"); //$NON-NLS-1$
		
		k = opts2.get(AutotoolsOptionConstants.OPT_LIBEXECDIR);
		assertEquals(k.getValue(), "/usr/libexec"); //$NON-NLS-1$
		
		k = opts2.get(AutotoolsOptionConstants.OPT_LOCALSTATEDIR);
		assertEquals(k.getValue(), "/usr/localstate"); //$NON-NLS-1$
		
		k = opts2.get(AutotoolsOptionConstants.OPT_MANDIR);
		assertEquals(k.getValue(), "/usr/man"); //$NON-NLS-1$
		
		k = opts2.get(AutotoolsOptionConstants.OPT_NO_CREATE);
		assertEquals(k.getValue(), "true"); //$NON-NLS-1$
		
		k = opts2.get(AutotoolsOptionConstants.OPT_OLDINCLUDEDIR);
		assertEquals(k.getValue(), "/usr/oldinclude"); //$NON-NLS-1$
		
		k = opts2.get(AutotoolsOptionConstants.OPT_PREFIX);
		assertEquals(k.getValue(), "prefix"); //$NON-NLS-1$
		
		k = opts2.get(AutotoolsOptionConstants.OPT_PROGRAM_PREFIX);
		assertEquals(k.getValue(), "program_prefix"); //$NON-NLS-1$
		
		k = opts2.get(AutotoolsOptionConstants.OPT_PROGRAM_SUFFIX);
		assertEquals(k.getValue(), "program_suffix"); //$NON-NLS-1$
		
		k = opts2.get(AutotoolsOptionConstants.OPT_PROGRAM_TRANSFORM_NAME);
		assertEquals(k.getValue(), "program_transform_name"); //$NON-NLS-1$
		
		k = opts2.get(AutotoolsOptionConstants.OPT_QUIET);
		assertEquals(k.getValue(), "true"); //$NON-NLS-1$
		
		k = opts2.get(AutotoolsOptionConstants.OPT_SBINDIR);
		assertEquals(k.getValue(), "/usr/sbin"); //$NON-NLS-1$
		
		k = opts2.get(AutotoolsOptionConstants.OPT_SHAREDSTATEDIR);
		assertEquals(k.getValue(), "/usr/sharedstate"); //$NON-NLS-1$
		
		k = opts2.get(AutotoolsOptionConstants.OPT_SRCDIR);
		assertEquals(k.getValue(), "src"); //$NON-NLS-1$
		
		k = opts2.get(AutotoolsOptionConstants.OPT_SYSCONFDIR);
		assertEquals(k.getValue(), "/usr/sysconf"); //$NON-NLS-1$
		
		k = opts2.get(AutotoolsOptionConstants.OPT_TARGET);
		assertEquals(k.getValue(), "target"); //$NON-NLS-1$
		
		k = opts2.get(AutotoolsOptionConstants.OPT_USER);
		assertEquals(k.getValue(), "user opts"); //$NON-NLS-1$
		
		k = opts2.get(AutotoolsOptionConstants.OPT_VERSION);
		assertEquals(k.getValue(), "true"); //$NON-NLS-1$

		// Verify one updated option in the .autotools file for the project
		try {
			IPath fileLocation = testProject.getLocation().append(".autotools"); //$NON-NLS-1$
			File dirFile = fileLocation.toFile();
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			assertTrue(dirFile.exists());
			Document d = db.parse(dirFile);
			Element e = d.getDocumentElement();
			// Get the stored configuration data
			NodeList cfgs = e.getElementsByTagName("configuration"); // $NON-NLS-1$
			for (int x = 0; x < cfgs.getLength(); ++x) {
				Node n = cfgs.item(x);
				NodeList l = n.getChildNodes();
				for (int y = 0; y < l.getLength(); ++y) {
					Node child = l.item(y);
					if (child.getNodeName().equals("option")) { // $NON-NLS-1$
						NamedNodeMap optionAttrs = child.getAttributes();
						Node id = optionAttrs.getNamedItem("id"); // $NON-NLS-1$
						Node value = optionAttrs.getNamedItem("value"); // $NON-NLS-1$
						// Verify the bindir option is updated
						if (id.equals(AutotoolsOptionConstants.OPT_BINDIR))
							assertEquals(value, "/usr/bin"); //$NON-NLS-1$
					}
				}
			}
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	protected void tearDown() throws Exception {
		testProject.refreshLocal(IResource.DEPTH_INFINITE, null);
		try {
			testProject.delete(true, true, null);
		} catch (Exception e) {
			//FIXME: Why does a ResourceException occur when deleting the project??
		}
		super.tearDown();
	}

}
