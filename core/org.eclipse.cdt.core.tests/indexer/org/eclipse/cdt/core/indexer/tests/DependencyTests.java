/*
 * Created on Sep 25, 2003
 */
package org.eclipse.cdt.core.indexer.tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.search.BasicSearchResultCollector;
import org.eclipse.cdt.core.search.ICSearchConstants;
import org.eclipse.cdt.core.search.ICSearchPattern;
import org.eclipse.cdt.core.search.ICSearchResultCollector;
import org.eclipse.cdt.core.search.ICSearchScope;
import org.eclipse.cdt.core.search.IMatch;
import org.eclipse.cdt.core.search.SearchEngine;
import org.eclipse.cdt.internal.core.index.impl.IFileDocument;
import org.eclipse.cdt.internal.core.search.PathCollector;
import org.eclipse.cdt.internal.core.search.PatternSearchJob;
import org.eclipse.cdt.internal.core.search.indexing.IndexManager;
import org.eclipse.cdt.internal.core.search.matching.CSearchPattern;
import org.eclipse.cdt.internal.core.search.processing.IJob;
import org.eclipse.cdt.internal.core.sourcedependency.DependencyQueryJob;
import org.eclipse.cdt.internal.core.sourcedependency.UpdateDependency;
import org.eclipse.cdt.testplugin.CProjectHelper;
import org.eclipse.cdt.testplugin.CTestPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

	/**
 	* @author bgheorgh
 	*/
	public class DependencyTests extends TestCase {
	IFile 					file;
	IFileDocument 			fileDoc;
	IProject 				testProject;
	NullProgressMonitor		monitor;
	IndexManager 			indexManager;
	IWorkspace 				workspace;
	BasicSearchResultCollector	resultCollector;
	SearchEngine			searchEngine;
	ICSearchScope 			scope;

	public static Test suite() {
		TestSuite suite = new TestSuite(DependencyTests.class.getName());

		suite.addTest(new DependencyTests("testDependencyTree"));
		suite.addTest(new DependencyTests("testDepTable"));
		suite.addTest(new DependencyTests("testDepSourceChangeTree"));
		suite.addTest(new DependencyTests("testDepHeaderChangeTree"));
		suite.addTest(new DependencyTests("testDepHeaderChangeReindex"));
		suite.addTest(new DependencyTests("testDepSourceChangeTable"));
		suite.addTest(new DependencyTests("testDepHeaderChangeTable"));
		suite.addTest(new DependencyTests("testUpdateDependancyNPE"));
		return suite;
	}
	/**
	 * @param name
	 */
	public DependencyTests(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}
	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		//Create temp project
		testProject = createProject("DepTestProject");
		IPath pathLoc = CCorePlugin.getDefault().getStateLocation();
		
		File indexFile = new File(pathLoc.append("281274758.index").toOSString());
		if (indexFile.exists())
			indexFile.delete();
		
		testProject.setSessionProperty(IndexManager.activationKey,new Boolean(false));
		
		if (testProject==null)
			fail("Unable to create project");	
		
		indexManager = CCorePlugin.getDefault().getCoreModel().getIndexManager();
		indexManager.reset();
		
		workspace = ResourcesPlugin.getWorkspace();
		
		scope = SearchEngine.createWorkspaceScope();
		monitor = new NullProgressMonitor();
		resultCollector = new BasicSearchResultCollector();
	
		searchEngine = new SearchEngine();
	}
	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() {
		try {
			super.tearDown();
		} catch (Exception e1) {
		}
		//Delete project
		if (testProject.exists()){
			try {
				System.gc();
				System.runFinalization();
				testProject.delete(true,monitor);
			} catch (CoreException e) {
				fail(getMessage(e.getStatus()));
			}
		}
	}
	
	private String getMessage(IStatus status) {
		StringBuffer message = new StringBuffer("[");
		message.append(status.getMessage());
		if (status.isMultiStatus()) {
			IStatus children[] = status.getChildren();
			for (int i = 0; i < children.length; i++) {
				message.append(getMessage(children[i]));
			}
		}
		message.append("]");
		return message.toString();
	}
	
	public void testDependencyTree() throws Exception{
   //Add a file to the project
   importFile("c.h","resources/dependency/c.h");
   importFile("a.h","resources/dependency/a.h");
   importFile("Inc1.h","resources/dependency/Inc1.h");
   importFile("DepTest.h","resources/dependency/DepTest.h");
   importFile("d.h","resources/dependency/d.h");
   importFile("DepTest2.h","resources/dependency/DepTest2.h");
   IFile depTest = importFile("DepTest.cpp","resources/dependency/DepTest.cpp");
   IFile depTest2 = importFile("DepTest2.cpp","resources/dependency/DepTest2.cpp");
   //Enable indexing on the created project
   //By doing this, we force the Dependency Manager to do a g()

   IndexManager indexManager = CCorePlugin.getDefault().getCoreModel().getIndexManager();
   //indexManager.setEnabled(testProject,true);
   testProject.setSessionProperty(IndexManager.activationKey,new Boolean(true));
	

   String[] depTestModel = {File.separator + "DepTestProject" + File.separator + "d.h", File.separator + "DepTestProject" + File.separator + "Inc1.h", File.separator + "DepTestProject" + File.separator + "c.h", File.separator + "DepTestProject" + File.separator + "a.h", File.separator + "DepTestProject" + File.separator + "DepTest.h"};
   String[] depTest2Model = {File.separator + "DepTestProject" + File.separator + "d.h", File.separator + "DepTestProject" + File.separator + "DepTest2.h"};
	
   ArrayList includes = new ArrayList();
   indexManager.performConcurrentJob(new DependencyQueryJob(testProject,depTest,indexManager,includes),ICSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,null,null);

   String[] depTestModelLocal = convertToLocalPath(depTestModel);
   String[] depTestIncludes = new String[includes.size()];
   Iterator includesIterator = includes.iterator();
   int i=0;
   while(includesIterator.hasNext()){
	   depTestIncludes[i] = (String) includesIterator.next();
	   i++;
   }
	
   if (depTestModelLocal.length != depTestIncludes.length)
		   fail("Number of included files differsfrom model");
	
   Arrays.sort(depTestModelLocal);
   Arrays.sort(depTestIncludes);
		
   for (i=0;i<depTestIncludes.length; i++)
   {
	   assertEquals(depTestModelLocal[i],depTestIncludes[i]);
   }
	
   ArrayList includes2 = new ArrayList();
   indexManager.performConcurrentJob(new DependencyQueryJob(testProject,depTest2,indexManager,includes2),ICSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,null,null);
   
   String[] depTest2ModelLocal = convertToLocalPath(depTest2Model);
   String[] depTest2Includes = new String[includes2.size()];
   Iterator includes2Iterator = includes2.iterator();
   i=0;
   while(includes2Iterator.hasNext()){
	   depTest2Includes[i] = (String) includes2Iterator.next();
	   i++;
   }
	
   if (depTest2ModelLocal.length != depTest2Includes.length)
		   fail("Number of included files differsfrom model");
	
   Arrays.sort(depTest2ModelLocal);
   Arrays.sort(depTest2Includes);
	
   for (i=0;i<depTest2Includes.length; i++)
   {
	   assertEquals(depTest2ModelLocal[i],depTest2Includes[i]);
   }
 }

 public void testDepTable() throws Exception{
   //Add a file to the project
 	
 	IFile depTest2C = importFile("DepTest2.cpp","resources/dependency/DepTest2.cpp");
    IFile depTestC = importFile("DepTest.cpp","resources/dependency/DepTest.cpp");
   IFile cH = importFile("c.h","resources/dependency/c.h");
   IFile aH = importFile("a.h","resources/dependency/a.h");
   IFile Inc1H = importFile("Inc1.h","resources/dependency/Inc1.h");
   IFile dH = importFile("d.h","resources/dependency/d.h");
   IFile depTestH = importFile("DepTest.h","resources/dependency/DepTest.h");
   IFile depTest2H = importFile("DepTest2.h","resources/dependency/DepTest2.h");
   
   testProject.setSessionProperty(IndexManager.activationKey,new Boolean(true));
	
   PathCollector pathCollector = new PathCollector();
   getTableRefs(dH, pathCollector);

   String[] dHModel = {IPath.SEPARATOR + "DepTestProject" + IPath.SEPARATOR + "DepTest2.cpp", IPath.SEPARATOR + "DepTestProject" + IPath.SEPARATOR + "DepTest.cpp"};
   String[] iPath = pathCollector.getPaths();
	
   if (dHModel.length != iPath.length)
	   fail("Number of included files differsfrom model");
	
   Arrays.sort(dHModel);
   Arrays.sort(iPath);
	
   for (int i=0;i<iPath.length; i++)
   {
	   assertEquals(iPath[i],dHModel[i]);
   }
	
   pathCollector = new PathCollector();
   getTableRefs(Inc1H, pathCollector);
				
   String[] Inc1HModel = {IPath.SEPARATOR + "DepTestProject" + IPath.SEPARATOR + "DepTest.cpp"};
   iPath = pathCollector.getPaths();
	
   if (Inc1HModel.length != iPath.length)
	   fail("Number of included files differsfrom model");
	
   Arrays.sort(Inc1HModel);
   Arrays.sort(iPath);
	
   for (int i=0;i<iPath.length; i++)
   {
	   assertEquals(iPath[i],Inc1HModel[i]);
   }
 }
  
 public void testDepSourceChangeTable() throws Exception{

   //Add a file to the project
   IFile cH = importFile("c.h","resources/dependency/c.h"); 
   IFile aH = importFile("a.h","resources/dependency/a.h");
   IFile Inc1H = importFile("Inc1.h","resources/dependency/Inc1.h");
   IFile dH = importFile("d.h","resources/dependency/d.h");
   IFile depTestH = importFile("DepTest.h","resources/dependency/DepTest.h");
   IFile depTestC = importFile("DepTest.cpp","resources/dependency/DepTest.cpp");

   testProject.setSessionProperty(IndexManager.activationKey,new Boolean(true));
	
   String[] beforeModel = {Path.SEPARATOR + "DepTestProject" + IPath.SEPARATOR + "DepTest.cpp"};
	
   PathCollector pathCollector = new PathCollector();
   getTableRefs(depTestH, pathCollector);
				
   String[] iPath = pathCollector.getPaths();
	
   compareArrays(iPath,beforeModel);
	
   pathCollector = new PathCollector();
   getTableRefs(dH, pathCollector);
				
   iPath = pathCollector.getPaths();
		
   compareArrays(iPath,beforeModel);
	
   pathCollector = new PathCollector();
   getTableRefs(Inc1H, pathCollector);
				
   iPath = pathCollector.getPaths();
   String[] inc1Model = {Path.SEPARATOR + "DepTestProject" + IPath.SEPARATOR + "DepTest.cpp"};
   compareArrays(iPath,inc1Model);
	
   pathCollector = new PathCollector();
   getTableRefs(aH, pathCollector);
			
   iPath = pathCollector.getPaths();
	
   compareArrays(iPath,inc1Model);
	
   pathCollector = new PathCollector();
   getTableRefs(cH, pathCollector);
			
   iPath = pathCollector.getPaths();

   String[] cHModel = {Path.SEPARATOR + "DepTestProject" + Path.SEPARATOR + "DepTest.cpp"};
   compareArrays(iPath,cHModel);
	
   editCode(depTestC,"#include \"DepTest.h\"","//#include \"DepTest.h\"");
	

   pathCollector = new PathCollector();
   getTableRefs(depTestH, pathCollector);
				
   iPath = pathCollector.getPaths();
	
   if (iPath.length != 0)
	   fail("Number of included files differs from model");
	
	
   pathCollector = new PathCollector();
   getTableRefs(dH, pathCollector);
				
   iPath = pathCollector.getPaths();
		
   compareArrays(iPath,beforeModel);
	
   pathCollector = new PathCollector();
   getTableRefs(Inc1H, pathCollector);
				
   iPath = pathCollector.getPaths();
		
   if (iPath.length != 0)
	   fail("Number of included files differs from model");
	
   pathCollector = new PathCollector();
   getTableRefs(aH, pathCollector);
			
   iPath = pathCollector.getPaths();
	
   if (iPath.length != 0)
	   fail("Number of included files differs from model");
	
	
   pathCollector = new PathCollector();
   getTableRefs(cH, pathCollector);
			
   iPath = pathCollector.getPaths();
	
   if (iPath.length != 0)
	   fail("Number of included files differs from model");

 }
  
 public void testDepSourceChangeTree() throws Exception{
// Add a file to the project
	 IFile cH = importFile("c.h","resources/dependency/c.h");
	 IFile aH = importFile("a.h","resources/dependency/a.h");
	 IFile Inc1H = importFile("Inc1.h","resources/dependency/Inc1.h");
	 IFile depTestH = importFile("DepTest.h","resources/dependency/DepTest.h");
	 IFile dH = importFile("d.h","resources/dependency/d.h");
	 IFile depTest2H = importFile("DepTest2.h","resources/dependency/DepTest2.h");
	 IFile depTestC = importFile("DepTest.cpp","resources/dependency/DepTest.cpp");
	 IFile depTest2C = importFile("DepTest2.cpp","resources/dependency/DepTest2.cpp");

	 testProject.setSessionProperty(IndexManager.activationKey,new Boolean(true));
		
	 IndexManager indexManager = CCorePlugin.getDefault().getCoreModel().getIndexManager();
	  
	 String[] preDepTestModel = {File.separator + "DepTestProject" + File.separator + "DepTest.h", File.separator + "DepTestProject" + File.separator + "Inc1.h", File.separator + "DepTestProject" + File.separator + "a.h", File.separator + "DepTestProject" + File.separator + "c.h", File.separator + "DepTestProject" + File.separator + "d.h"};
	
	 ArrayList includes = new ArrayList();
	 indexManager.performConcurrentJob(new DependencyQueryJob(testProject,depTestC,indexManager,includes),ICSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,null, null);

	 String[] preDepTestModelLocal = convertToLocalPath(preDepTestModel);
	 String[] preDepTestIncludes = new String[includes.size()];
	
	 Iterator includesIterator = includes.iterator();
	 int i=0;
	 while(includesIterator.hasNext()){
		 preDepTestIncludes[i] = (String) includesIterator.next();
		 i++;
	 }

	 if (preDepTestModelLocal.length != preDepTestIncludes.length)
			 fail("Number of included files differs from model");

	 Arrays.sort(preDepTestModelLocal);
	 Arrays.sort(preDepTestIncludes);
	
	 for (i=0;i<preDepTestIncludes.length; i++){
		 assertEquals(preDepTestModelLocal[i],preDepTestIncludes[i]);
	 }
	
	 editCode(depTestC,"#include \"DepTest.h\"","//#include \"DepTest.h\"");
	 String[] postDepTestModel = {File.separator + "DepTestProject" + File.separator + "d.h"};
	
	 ArrayList includes2 = new ArrayList();

	 testProject.refreshLocal(IResource.DEPTH_INFINITE,null);
	
	 indexManager.performConcurrentJob(new DependencyQueryJob(testProject,depTestC,indexManager,includes2),ICSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,null, null);
	
	
	 String[] postDepTestModelLocal = convertToLocalPath(postDepTestModel);
	 String[] postDepTestIncludes = new String[includes2.size()];
	
	 Iterator includesIterator2 = includes2.iterator();
	
	 int j=0;
	 while(includesIterator2.hasNext()){
		 postDepTestIncludes[j] = (String) includesIterator2.next();
		 j++;
	 }

	 if (postDepTestModelLocal.length != postDepTestIncludes.length)
			 fail("Number of included files differs from model");

	 Arrays.sort(postDepTestModelLocal);
	 Arrays.sort(postDepTestIncludes);
	
	 for (i=0;i<postDepTestIncludes.length; i++){
		 assertEquals(postDepTestModelLocal[i],postDepTestIncludes[i]);
	 }
	
 }
  
 public void testDepHeaderChangeTree() throws Exception{
	 //	Add a file to the project
	 IFile cH = importFile("c.h","resources/dependency/c.h");
	 IFile aH = importFile("a.h","resources/dependency/a.h");
	 IFile depTest3H = importFile("DepTest3.h","resources/dependency/DepTest3.h");
	 IFile depTest3C = importFile("DepTest3.cpp","resources/dependency/DepTest3.cpp");

	 testProject.setSessionProperty(IndexManager.activationKey,new Boolean(true));
		
	 IndexManager indexManager = CCorePlugin.getDefault().getCoreModel().getIndexManager();
	  
	 String[] preDepTestModel = {File.separator + "DepTestProject" + File.separator + "DepTest3.h", File.separator + "DepTestProject" + File.separator + "a.h", File.separator + "DepTestProject" + File.separator + "c.h"};
	
	 ArrayList includes = new ArrayList();
	 indexManager.performConcurrentJob(new DependencyQueryJob(testProject,depTest3C,indexManager,includes),ICSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,null, null);

	 String[] preDepTestModelLocal = convertToLocalPath(preDepTestModel);
	 String[] preDepTestIncludes = new String[includes.size()];
	
	 Iterator includesIterator = includes.iterator();
	 int i=0;
	 while(includesIterator.hasNext()){
		 preDepTestIncludes[i] = (String) includesIterator.next();
		 i++;
	 }

	 if (preDepTestModelLocal.length != preDepTestIncludes.length)
			 fail("Number of included files differs from model");

	 Arrays.sort(preDepTestModelLocal);
	 Arrays.sort(preDepTestIncludes);
	
	 for (i=0;i<preDepTestIncludes.length; i++){
		 assertEquals(preDepTestModelLocal[i],preDepTestIncludes[i]);
	 }
	
	 editCode(aH,"#include \"c.h\"","//#include \"c.h\"");
	 String[] postDepTestModel = {File.separator + "DepTestProject" + File.separator + "DepTest3.h", File.separator + "DepTestProject" + File.separator + "a.h"};
	
	 ArrayList includes2 = new ArrayList();

	 testProject.refreshLocal(IResource.DEPTH_INFINITE,null);
	
	 indexManager.performConcurrentJob(new DependencyQueryJob(testProject,depTest3C,indexManager,includes2),ICSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,null, null);
	
	
	 String[] postDepTestModelLocal = convertToLocalPath(postDepTestModel);
	 String[] postDepTestIncludes = new String[includes2.size()];
	
	 Iterator includesIterator2 = includes2.iterator();
	
	 int j=0;
	 while(includesIterator2.hasNext()){
		 postDepTestIncludes[j] = (String) includesIterator2.next();
		 j++;
	 }

	 assertEquals(postDepTestModelLocal.length, postDepTestIncludes.length);

	 Arrays.sort(postDepTestModelLocal);
	 Arrays.sort(postDepTestIncludes);
	
	 for (i=0;i<postDepTestIncludes.length; i++){
		 assertEquals(postDepTestModelLocal[i],postDepTestIncludes[i]);
	 }
	
 }
  
 public void testDepHeaderChangeTable() throws Exception{
   
	//	Add a file to the project
	IFile cH = importFile("c.h","resources/dependency/c.h");
	IFile aH = importFile("a.h","resources/dependency/a.h");
	IFile depTest3H = importFile("DepTest3.h","resources/dependency/DepTest3.h");
	IFile depTest3C = importFile("DepTest3.cpp","resources/dependency/DepTest3.cpp");
	
	testProject.setSessionProperty(IndexManager.activationKey,new Boolean(true));
		
	 
	String[] beforeModel = {Path.SEPARATOR + "DepTestProject" + IPath.SEPARATOR + "DepTest3.cpp"}; 
	String[] cHModel = {Path.SEPARATOR + "DepTestProject" + IPath.SEPARATOR + "DepTest3.cpp"};
	String[] aHModel = {Path.SEPARATOR + "DepTestProject" + Path.SEPARATOR + "DepTest3.cpp"};
	PathCollector pathCollector = new PathCollector();
	getTableRefs(depTest3H, pathCollector);
				
	String[] iPath = pathCollector.getPaths();
	
	compareArrays(iPath,beforeModel);
	 
	pathCollector = new PathCollector();
	getTableRefs(cH, pathCollector);
				
	iPath = pathCollector.getPaths();
	
	compareArrays(iPath,cHModel);
	
	pathCollector = new PathCollector();
	getTableRefs(aH, pathCollector);
				
	iPath = pathCollector.getPaths();
	
	compareArrays(iPath,aHModel);
	

   editCode(aH,"#include \"c.h\"","//#include \"c.h\"");
	
   pathCollector = new PathCollector();
   getTableRefs(depTest3H, pathCollector);
			
   iPath = pathCollector.getPaths();

   compareArrays(iPath,beforeModel);
	
   pathCollector = new PathCollector();
   getTableRefs(cH, pathCollector);
			
   iPath = pathCollector.getPaths();

   if (iPath.length != 0)
	   fail("Number of included files differs from model");
	
   pathCollector = new PathCollector();
   getTableRefs(aH, pathCollector);
			
   iPath = pathCollector.getPaths();

   compareArrays(iPath,aHModel);
   
 }
 
 public void testUpdateDependancyNPE() {
 	IResource nonExistantResource = ResourcesPlugin.getWorkspace().getRoot().getProject("non-existant-project-aha");
 	
 	assertFalse(nonExistantResource.exists());
 	assertNull(nonExistantResource.getLocation());
 	
 	IJob job = new UpdateDependency(nonExistantResource);
 	assertFalse(job.execute(new NullProgressMonitor()));
 }
  

 public void testDepHeaderChangeReindex() throws Exception{
   //	Add a file to the project
   IFile cH = importFile("c.h","resources/dependency/c.h");
   IFile aH = importFile("a.h","resources/dependency/a.h");
   IFile depTest3H = importFile("DepTest3.h","resources/dependency/DepTest3.h");
   IFile depTest3C = importFile("DepTest3.cpp","resources/dependency/DepTest3.cpp");
 	
   testProject.setSessionProperty(IndexManager.activationKey,new Boolean(true));
	
   ICSearchPattern pattern = SearchEngine.createSearchPattern( "Z", ICSearchConstants.TYPE, ICSearchConstants.DECLARATIONS, true );
		
   search(workspace,pattern,scope,resultCollector);
 	
   Set resultSet = resultCollector.getSearchResults();
   if (resultSet.size() != 1)
	   fail("Expected 1 match");
 	
   Iterator iter = resultSet.iterator();
   IMatch match = (IMatch) iter.next();
   if (!(match.getName().equals("Z")) && 
	   (match.getElementType() != 64 ))
	   fail("Wrong search result");
   
   editCode(depTest3H,"#include \"a.h\"","//#include \"a.h\"");
    
   search(workspace,pattern,scope,resultCollector);
 	
   resultSet = resultCollector.getSearchResults();
	
   if (resultSet.size() != 1)
		   fail("Expected no matches");
 }


   private String[] convertToLocalPath(String[] model) {
	   IPath defaultPath = Platform.getLocation();
	   String pathString = defaultPath.toOSString();
	   char endChar = pathString.charAt(pathString.length() - 1);
       if (endChar == '/' ||
       	   endChar	== '\\')
       {
       	 pathString = pathString.substring(0, pathString.length() - 1);
       }
	   String[] tempLocalArray = new String[model.length];
	   for (int i=0;i<model.length;i++){
		   StringBuffer buffer = new StringBuffer();
		   buffer.append(pathString);
		   buffer.append(model[i]);
		   tempLocalArray[i]=buffer.toString();
	   }
	   return tempLocalArray;
   }
	
   private void getTableRefs(IFile tempFile, PathCollector pathCollector) throws InterruptedException{
	   
	   ICSearchScope scope = SearchEngine.createWorkspaceScope();
	   CSearchPattern pattern = CSearchPattern.createPattern(tempFile.getLocation().toOSString(),ICSearchConstants.INCLUDE, ICSearchConstants.REFERENCES,ICSearchConstants.EXACT_MATCH,true);
	   IndexManager indexManager = CCorePlugin.getDefault().getCoreModel().getIndexManager();
	   indexManager.performConcurrentJob( 
		   new PatternSearchJob(
			   (CSearchPattern) pattern,
			   scope,
			   pathCollector,
			   indexManager 
		   ),
		   ICSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
		   null, null );
   }
	
   private void editCode(IFile tempFile, String beforeString, String afterString) throws IOException, CoreException, InterruptedException{
	   FileReader fileReader = null;
	   try {
		   fileReader = new FileReader(tempFile.getLocation().toOSString());
	   } catch (FileNotFoundException e) {
		   fail(e.getMessage());
	   }
	
	   BufferedReader buff = new BufferedReader(fileReader);
	   String tempString;
	   File tempUtilFile= new File(tempFile.getLocation().toOSString() + "TempFile"); 
	   FileWriter writer = new FileWriter(tempUtilFile);

	   try {
			  while ((tempString = buff.readLine())!= null ) {
			   if (tempString.equals(beforeString)){
				   writer.write(afterString + "\n" );
				   writer.flush();
			   }
			   else{
				   writer.write(tempString + "\n" );
				   writer.flush();
			   }
				
			  }
	   } catch (IOException e1) {
		   fail(e1.getMessage());
	   }		  
	   writer.close();
	   buff.close();
	   
	   FileInputStream buff2 = new FileInputStream(tempUtilFile);
	   tempFile.setContents(buff2,true,false,null);
	   tempFile.refreshLocal(IResource.DEPTH_INFINITE, null);
	   //buff2.close();
		
		
   }
	
   private void compareArrays(String[] first, String[] second){
		
	   if (first.length != second.length)
			   fail("Number of included files differs from model");
	
	   Arrays.sort(first);
	   Arrays.sort(second);

	   for (int i=0;i<first.length; i++)
	   {
		   assertEquals(first[i],second[i]);
	   }
   }
	
	protected void search(IWorkspace workspace, ICSearchPattern pattern, ICSearchScope scope, ICSearchResultCollector collector) {
   		try {
   			searchEngine.search( workspace, pattern, scope, collector, false );
		} catch (InterruptedException e) {
		}
	}
   
   /*
	* Utils
	*/
   private IProject createProject(String projectName) throws CoreException
   {
   		ICProject cproject = CProjectHelper.createCCProject(projectName, "bin");
   		return cproject.getProject();
	   
   }
	
   private IFile importFile(String fileName, String resourceLocation)throws Exception{
   		String testCaseName = this.getName();
		//Obtain file handle
		file = testProject.getProject().getFile(fileName);
		//Create file input stream
		monitor = new NullProgressMonitor();
		if (!file.exists()) {
			file.create(new FileInputStream(
					CTestPlugin.getDefault().getFileInPlugin(new Path(resourceLocation))),
					false, monitor);
		}
		fileDoc = new IFileDocument(file);
		return file;
   }
}
