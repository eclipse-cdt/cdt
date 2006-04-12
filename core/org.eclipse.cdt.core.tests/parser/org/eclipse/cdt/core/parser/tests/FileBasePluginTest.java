/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

/*
 * Created on Sept 28, 2004
 */
package org.eclipse.cdt.core.parser.tests;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IParser;
import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.NullLogService;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ParserUtil;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.core.parser.ast.IASTASMDefinition;
import org.eclipse.cdt.core.parser.ast.IASTAbstractTypeSpecifierDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTClassReference;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTCodeScope;
import org.eclipse.cdt.core.parser.ast.IASTCompilationUnit;
import org.eclipse.cdt.core.parser.ast.IASTDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTEnumerationReference;
import org.eclipse.cdt.core.parser.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTEnumeratorReference;
import org.eclipse.cdt.core.parser.ast.IASTField;
import org.eclipse.cdt.core.parser.ast.IASTFieldReference;
import org.eclipse.cdt.core.parser.ast.IASTFunction;
import org.eclipse.cdt.core.parser.ast.IASTFunctionReference;
import org.eclipse.cdt.core.parser.ast.IASTInclusion;
import org.eclipse.cdt.core.parser.ast.IASTLinkageSpecification;
import org.eclipse.cdt.core.parser.ast.IASTMacro;
import org.eclipse.cdt.core.parser.ast.IASTMethod;
import org.eclipse.cdt.core.parser.ast.IASTMethodReference;
import org.eclipse.cdt.core.parser.ast.IASTNamespaceDefinition;
import org.eclipse.cdt.core.parser.ast.IASTNamespaceReference;
import org.eclipse.cdt.core.parser.ast.IASTNode;
import org.eclipse.cdt.core.parser.ast.IASTParameterReference;
import org.eclipse.cdt.core.parser.ast.IASTScope;
import org.eclipse.cdt.core.parser.ast.IASTTemplateDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTTemplateInstantiation;
import org.eclipse.cdt.core.parser.ast.IASTTemplateParameterReference;
import org.eclipse.cdt.core.parser.ast.IASTTemplateSpecialization;
import org.eclipse.cdt.core.parser.ast.IASTTypedefDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTTypedefReference;
import org.eclipse.cdt.core.parser.ast.IASTUsingDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTUsingDirective;
import org.eclipse.cdt.core.parser.ast.IASTVariable;
import org.eclipse.cdt.core.parser.ast.IASTVariableReference;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.FileManager;
import org.eclipse.cdt.internal.core.parser.Parser;
import org.eclipse.cdt.internal.core.parser.ParserException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * @author dsteffle
 */
public class FileBasePluginTest extends TestCase {
    static NullProgressMonitor		monitor;
    static IWorkspace 				workspace;
    static IProject 				project;
    static FileManager 				fileManager;
    static int						numProjects = 0;
    static Class					className;
	static ICProject cPrj; 

    private void initialize(Class aClassName){
        if( CCorePlugin.getDefault() != null && CCorePlugin.getDefault().getCoreModel() != null){
			//(CCorePlugin.getDefault().getCoreModel().getIndexManager()).reset();
			monitor = new NullProgressMonitor();
			
			workspace = ResourcesPlugin.getWorkspace();
			
	        try {
	        	cPrj = CProjectHelper.createCCProject("ParserTestProject", "bin"); //$NON-NLS-1$ //$NON-NLS-2$
	        	
	            project = cPrj.getProject();
	            
	            // ugly
	            if (className == null || !className.equals(aClassName)) {
	            	className = aClassName;
	            	numProjects++;
	            }
	        } catch ( CoreException e ) {
	            /*boo*/
	        }
			if (project == null)
				throw new NullPointerException("Unable to create project"); //$NON-NLS-1$
	
			//Create file manager
			fileManager = new FileManager();
        }
    }

    public FileBasePluginTest(String name, Class className)
    {
    	super(name);
    	initialize(className);
    }
    
    public void cleanupProject() throws Exception {
    	numProjects--;
    	
    	try{
    		if (numProjects == 0) {
    			project.delete( true, false, monitor );
    			project = null;
    		}
	    } catch( Throwable e ){
	        /*boo*/
	    }
    }

    protected void tearDown() throws Exception {
        if( project == null || !project.exists() ) 
            return;
        
        IResource [] members = project.members();
        for( int i = 0; i < members.length; i++ ){
            if( members[i].getName().equals( ".project" ) || members[i].getName().equals( ".cdtproject" ) ) //$NON-NLS-1$ //$NON-NLS-2$
                continue;
            try{
                members[i].delete( false, monitor );
            } catch( Throwable e ){
                /*boo*/
            }
        }
	}

    // below can be used to work with large files (too large for memory)
//    protected IFile importFile(String fileName) throws Exception {
//		IFile file = cPrj.getProject().getFile(fileName);
//		if (!file.exists()) {
//			try{
//				FileInputStream fileIn = new FileInputStream(
//						CTestPlugin.getDefault().getFileInPlugin(new Path("resources/parser/" + fileName))); 
//				file.create(fileIn,false, monitor);        
//			} catch (CoreException e) {
//				e.printStackTrace();
//			} catch (FileNotFoundException e) {
//				e.printStackTrace();
//			}
//		}
//		
//		return file;
//    }
    
    protected IFolder importFolder(String folderName) throws Exception {
    	IFolder folder = project.getProject().getFolder(folderName);
		
		//Create file input stream
		if( !folder.exists() )
			folder.create( false, false, monitor );
		
		return folder;
    }
    public IFile importFile(String fileName, String contents ) throws Exception{
		//Obtain file handle
		IFile file = project.getProject().getFile(fileName);
		
		InputStream stream = new ByteArrayInputStream( contents.getBytes() ); 
		//Create file input stream
		if( file.exists() )
		    file.setContents( stream, false, false, monitor );
		else
			file.create( stream, false, monitor );
		
		fileManager.addFile(file);
		
		return file;
	}
    
    public static class CallbackTracker implements ISourceElementRequestor{
        private List callbacks;
        private IASTScope compUnit;
        public CallbackTracker( List callbacks ){
            this.callbacks = callbacks;
        }
         
        public IASTScope getCompilationUnit()
        {
        	return compUnit;
        }
        public static final String ACCEPT_PROBLEM = "ACCEPT_PROBLEM"; //$NON-NLS-1$
        public static final String ACCEPT_MACRO = "ACCEPT_MACRO"; //$NON-NLS-1$
        public static final String ACCEPT_VARIABLE = "ACCEPT_VARIABLE"; //$NON-NLS-1$
        public static final String ACCEPT_FUNCTION_DECL = "ACCEPT_FUNCTION_DECL"; //$NON-NLS-1$
        public static final String ACCEPT_USING_DIRECTIVE = "ACCEPT_USING_DIRECTIVE"; //$NON-NLS-1$
        public static final String ACCEPT_USING_DECL = "ACCEPT_USING_DECL"; //$NON-NLS-1$
        public static final String ACCEPT_ASM_DEF = "ACCEPT_ASM_DEF"; //$NON-NLS-1$
        public static final String ACCEPT_TYPEDEF = "ACCEPT_TYPEDEF"; //$NON-NLS-1$
        public static final String ACCEPT_ENUMERATION = "ACCEPT_ENUMERATION"; //$NON-NLS-1$
        public static final String ACCEPT_ELABORATED = "ACCEPT_ELABORATED"; //$NON-NLS-1$
        public static final String ACCEPT_ABSTRACT_TYPESPEC = "ACCEPT_ABSTRACT_TYPESPEC"; //$NON-NLS-1$
        public static final String ACCEPT_METHOD = "ACCEPT_METHOD"; //$NON-NLS-1$
        public static final String ACCEPT_FIELD = "ACCEPT_FIELD"; //$NON-NLS-1$
        public static final String ACCEPT_REFERENCE = "ACCEPT_REFERENCE"; //$NON-NLS-1$
        public static final String ACCEPT_FRIEND = "ACCEPT_FRIEND"; //$NON-NLS-1$
        public static final String ENTER_FUNCTION = "ENTER_FUNCTION"; //$NON-NLS-1$
        public static final String ENTER_CODE_BLOCK = "ENTER_CODE_BLOCK"; //$NON-NLS-1$
        public static final String ENTER_COMPILATION_UNIT = "ENTER_COMPILATION_UNIT"; //$NON-NLS-1$
        public static final String ENTER_INCLUSION = "ENTER_INCLUSION"; //$NON-NLS-1$
        public static final String ENTER_NAMESPACE = "ENTER_NAMESPACE"; //$NON-NLS-1$
        public static final String ENTER_CLASS_SPEC = "ENTER_CLASS_SPEC"; //$NON-NLS-1$
        public static final String ENTER_LINKAGE = "ENTER_LINKAGE"; //$NON-NLS-1$
        public static final String ENTER_TEMPLATE_DECL = "ENTER_TEMPLATE_DECL"; //$NON-NLS-1$
        public static final String ENTER_TEMPLATE_SPEC = "ENTER_TEMPLATE_SPEC"; //$NON-NLS-1$
        public static final String ENTER_TEMPLATE_INSTANCE = "ENTER_TEMPLATE_INSTANCE"; //$NON-NLS-1$
        public static final String ENTER_METHOD = "ENTER_METHOD"; //$NON-NLS-1$
        public static final String EXIT_FUNCTION = "EXIT_FUNCTION"; //$NON-NLS-1$
        public static final String EXIT_CODE_BLOCK = "EXIT_CODE_BLOCK"; //$NON-NLS-1$
        public static final String EXIT_METHOD = "EXIT_METHOD"; //$NON-NLS-1$
        public static final String EXIT_TEMPLATE_DECL = "EXIT_TEMPLATE_DECL"; //$NON-NLS-1$
        public static final String EXIT_TEMPLATE_SPEC = "EXIT_TEMPLATE_SPEC"; //$NON-NLS-1$
        public static final String EXIT_TEMPLATE_INSTANCE = "EXIT_TEMPLATE_INSTANCE"; //$NON-NLS-1$
        public static final String EXIT_LINKAGE = "EXIT_LINKAGE"; //$NON-NLS-1$
        public static final String EXIT_CLASS = "EXIT_CLASS"; //$NON-NLS-1$
        public static final String EXIT_NAMESPACE = "EXIT_NAMESPACE"; //$NON-NLS-1$
        public static final String EXIT_INCLUSION = "EXIT_INCLUSION"; //$NON-NLS-1$
        public static final String EXIT_COMPILATION_UNIT = "EXIT_COMPILATION_UNIT"; //$NON-NLS-1$
        
        
        public boolean acceptProblem( IProblem problem ) {
            callbacks.add( ACCEPT_PROBLEM );
            return false;
        }
        public void acceptMacro( IASTMacro macro ) 										{ callbacks.add( ACCEPT_MACRO ); }
        public void acceptVariable( IASTVariable variable ) 							{ callbacks.add( ACCEPT_VARIABLE ); }
        public void acceptFunctionDeclaration( IASTFunction function )					{ callbacks.add( ACCEPT_FUNCTION_DECL); }
        public void acceptUsingDirective( IASTUsingDirective usageDirective ) 			{ callbacks.add( ACCEPT_USING_DIRECTIVE ); }
        public void acceptUsingDeclaration( IASTUsingDeclaration usageDeclaration ) 	{ callbacks.add( ACCEPT_USING_DECL ); }
        public void acceptASMDefinition( IASTASMDefinition asmDefinition ) 				{ callbacks.add( ACCEPT_ASM_DEF ); }
        public void acceptTypedefDeclaration( IASTTypedefDeclaration typedef ) 			{ callbacks.add( ACCEPT_TYPEDEF ); }
        public void acceptEnumerationSpecifier( IASTEnumerationSpecifier enumeration ) 	{ callbacks.add( ACCEPT_ENUMERATION); }
        public void acceptElaboratedForewardDeclaration( IASTElaboratedTypeSpecifier elaboratedType ) { callbacks.add( ACCEPT_ELABORATED ); }
        public void acceptAbstractTypeSpecDeclaration( IASTAbstractTypeSpecifierDeclaration abstractDeclaration ) { callbacks.add( ACCEPT_ABSTRACT_TYPESPEC); }
        public void enterFunctionBody( IASTFunction function )							{ callbacks.add( ENTER_FUNCTION ); }
        public void exitFunctionBody( IASTFunction function ) 							{ callbacks.add( EXIT_FUNCTION ); }
        public void enterCodeBlock( IASTCodeScope scope ) 								{ callbacks.add( ENTER_CODE_BLOCK ); }
        public void exitCodeBlock( IASTCodeScope scope ) 								{ callbacks.add( EXIT_CODE_BLOCK ); }
        public void enterInclusion( IASTInclusion inclusion )							{ callbacks.add( ENTER_INCLUSION ); }
        public void enterNamespaceDefinition( IASTNamespaceDefinition namespaceDefinition )			{ callbacks.add( ENTER_NAMESPACE ); }
        public void enterClassSpecifier( IASTClassSpecifier classSpecification ) 		{ callbacks.add( ENTER_CLASS_SPEC ); }
        public void enterLinkageSpecification( IASTLinkageSpecification linkageSpec ) 	{ callbacks.add( ENTER_LINKAGE ); }
        public void enterTemplateDeclaration( IASTTemplateDeclaration declaration ) 	{ callbacks.add( ENTER_TEMPLATE_DECL ); }
        public void enterTemplateSpecialization( IASTTemplateSpecialization specialization )		{ callbacks.add( ENTER_TEMPLATE_SPEC ); }
        public void enterTemplateInstantiation( IASTTemplateInstantiation instantiation ) 			{ callbacks.add( ENTER_TEMPLATE_INSTANCE ); }
        public void acceptMethodDeclaration( IASTMethod method ) 						{ callbacks.add( ACCEPT_METHOD ); }
        public void enterMethodBody( IASTMethod method ) 								{ callbacks.add( ENTER_METHOD ); }
        public void exitMethodBody( IASTMethod method ) 								{ callbacks.add( EXIT_METHOD ); }
        public void acceptField( IASTField field ) 										{ callbacks.add( ACCEPT_FIELD ); }
        public void acceptClassReference( IASTClassReference reference ) 				{ callbacks.add( ACCEPT_REFERENCE ); }
        public void acceptTypedefReference( IASTTypedefReference reference )			{ callbacks.add( ACCEPT_REFERENCE ); }
        public void acceptNamespaceReference( IASTNamespaceReference reference ) 		{ callbacks.add( ACCEPT_REFERENCE ); }
        public void acceptEnumerationReference( IASTEnumerationReference reference ) 	{ callbacks.add( ACCEPT_REFERENCE ); }
        public void acceptVariableReference( IASTVariableReference reference ) 			{ callbacks.add( ACCEPT_REFERENCE ); }
        public void acceptFunctionReference( IASTFunctionReference reference ) 			{ callbacks.add( ACCEPT_REFERENCE ); }
        public void acceptFieldReference( IASTFieldReference reference )	 			{ callbacks.add( ACCEPT_REFERENCE ); }
        public void acceptMethodReference( IASTMethodReference reference ) 				{ callbacks.add( ACCEPT_REFERENCE ); }
        public void acceptEnumeratorReference( IASTEnumeratorReference reference ) 		{ callbacks.add( ACCEPT_REFERENCE ); }
        public void acceptParameterReference( IASTParameterReference reference ) 		{ callbacks.add( ACCEPT_REFERENCE ); }
        public void acceptTemplateParameterReference( IASTTemplateParameterReference reference ) 	{ callbacks.add( ACCEPT_REFERENCE ); }
        public void acceptFriendDeclaration( IASTDeclaration declaration ) 				{ callbacks.add( ACCEPT_FRIEND ); }
        public void exitTemplateDeclaration( IASTTemplateDeclaration declaration ) 		{ callbacks.add( EXIT_TEMPLATE_DECL); }
        public void exitTemplateSpecialization( IASTTemplateSpecialization specialization ) 		{ callbacks.add( EXIT_TEMPLATE_SPEC ); }
        public void exitTemplateExplicitInstantiation( IASTTemplateInstantiation instantiation ) 	{ callbacks.add( EXIT_TEMPLATE_INSTANCE ); }
        public void exitLinkageSpecification( IASTLinkageSpecification linkageSpec ) 	{ callbacks.add( ACCEPT_MACRO ); }
        public void exitClassSpecifier( IASTClassSpecifier classSpecification )			{ callbacks.add( EXIT_CLASS ); }
        public void exitNamespaceDefinition( IASTNamespaceDefinition namespaceDefinition ) { callbacks.add( EXIT_NAMESPACE); }
        public void exitInclusion( IASTInclusion inclusion ) 							{ callbacks.add( EXIT_INCLUSION ); }
        public void exitCompilationUnit( IASTCompilationUnit compilationUnit ) 			{ callbacks.add( EXIT_COMPILATION_UNIT ); }
        public void enterCompilationUnit( IASTCompilationUnit compilationUnit ) 		
        { 
        	callbacks.add( ENTER_COMPILATION_UNIT );
        	compUnit = compilationUnit;
        }
        public CodeReader createReader( String finalPath, Iterator workingCopies ) { 
            return ParserUtil.createReader(finalPath,workingCopies);
        }
        
    }
	public CallbackTracker callback;
	protected IASTScope parse( IFile code, List callbacks ) throws Exception
    {
    	return parse( code, callbacks, ParserLanguage.CPP );
    }
        
    protected IASTScope parse(IFile code, List callbackList, ParserLanguage language) throws Exception
    {
    	callback = new CallbackTracker( callbackList ); 
    	InputStream stream = code.getContents();
    	IParser parser = ParserFactory.createParser( 
    		ParserFactory.createScanner( new CodeReader( code.getLocation().toOSString(), stream ), new ScannerInfo(), //$NON-NLS-1$
    			ParserMode.COMPLETE_PARSE, language, callback, new NullLogService(), null ), callback, ParserMode.COMPLETE_PARSE, language, null 	
    		);
    	stream.close();
    	boolean parseResult = parser.parse();
    	// throw exception if there are generated IProblems
		if( !parseResult ) throw new ParserException( "FAILURE"); //$NON-NLS-1$
		if( parseResult  )
		{
			assertTrue( ((Parser)parser).validateCaches());
		}
        return callback.getCompilationUnit();
    }

    protected IASTNode parse(IFile code, List callbacks, int start, int end) throws Exception
    {
    	return parse(code, callbacks, start, end, true, ParserLanguage.CPP);
    	
    }
    
    protected IASTNode parse(IFile code, List callbacks, int offset1, int offset2, boolean expectedToPass, ParserLanguage language) throws Exception {
    	callback = new CallbackTracker( callbacks ); 
   	
		IParser parser = ParserFactory.createParser( 
	    		ParserFactory.createScanner( new CodeReader( code.getLocation().toOSString(), code.getCharset() ), new ScannerInfo(), //$NON-NLS-1$
	    			ParserMode.SELECTION_PARSE, language, callback, new NullLogService(), null ), callback, ParserMode.SELECTION_PARSE, language, null 	
	    		);
		
		IParser.ISelectionParseResult result =parser.parse( offset1, offset2 );
		if( expectedToPass )
		{
			assertNotNull( result );
			String filename = result.getFilename();
			assertNotNull( filename );
			assertTrue( !filename.equals( "")); //$NON-NLS-1$
			return (IASTNode) result.getOffsetableNamedElement();
		}
		return null;
	}
}
