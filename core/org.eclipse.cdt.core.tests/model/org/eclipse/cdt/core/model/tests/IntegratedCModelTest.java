/*
 * Created on Jun 3, 2003
 * by bnicolle
 */
package org.eclipse.cdt.core.model.tests;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;

import junit.framework.TestCase;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.model.TranslationUnit;
import org.eclipse.cdt.testplugin.CProjectHelper;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;

/**
 * @author bnicolle
 *
 */
public abstract class IntegratedCModelTest extends TestCase {

	private ICProject fCProject;
	private IFile sourceFile;
	private NullProgressMonitor monitor;

	/**
	 * 
	 */
	public IntegratedCModelTest() {
		super();
	}

	/**
	 * @param name
	 */
	public IntegratedCModelTest(String name) {
		super(name);
	}

	/**
	 * @return the subdirectory (from the plugin root) containing the required
	 *         test sourcefile (plus a trailing slash)
	 */
	abstract public String getSourcefileSubdir();

	/**
	 * @return the name of the test source-file
	 */
	abstract public String getSourcefileResource();

	public void setUp() throws Exception {
		monitor = new NullProgressMonitor();
		String pluginRoot = Platform.asLocalURL(Platform.getPlugin("org.eclipse.cdt.core.tests").getDescriptor().getInstallURL()).getFile();

		fCProject= CProjectHelper.createCCProject("TestProject1", "bin");
	
		sourceFile = fCProject.getProject().getFile( getSourcefileResource() );
		if (!sourceFile.exists()) {
			try{
				FileInputStream fileIn = new FileInputStream(pluginRoot+ getSourcefileSubdir() + getSourcefileResource() ); 
				sourceFile.create(fileIn,false, monitor);        
			} catch (CoreException e) {
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	protected void tearDown() {
		CProjectHelper.delete(fCProject);
	}	

	protected ITranslationUnit getTU() {
		TranslationUnit tu = new TranslationUnit(fCProject, sourceFile);
		// parse the translation unit to get the elements tree		
		Map newElement = tu.parse(); 
		return tu;
	}
}
