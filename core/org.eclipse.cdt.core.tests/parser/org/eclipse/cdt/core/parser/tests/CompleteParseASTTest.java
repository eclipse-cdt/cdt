/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: -
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.core.parser.tests;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import junit.framework.TestCase;

import org.eclipse.cdt.core.parser.IParser;
import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.cdt.core.parser.ast.IASTASMDefinition;
import org.eclipse.cdt.core.parser.ast.IASTAbstractTypeSpecifierDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTBaseSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTClassReference;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTCompilationUnit;
import org.eclipse.cdt.core.parser.ast.IASTDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTEnumerationReference;
import org.eclipse.cdt.core.parser.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTEnumerator;
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
import org.eclipse.cdt.core.parser.ast.IASTPointerToFunction;
import org.eclipse.cdt.core.parser.ast.IASTPointerToMethod;
import org.eclipse.cdt.core.parser.ast.IASTReference;
import org.eclipse.cdt.core.parser.ast.IASTScope;
import org.eclipse.cdt.core.parser.ast.IASTSimpleTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTTemplateDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTTemplateInstantiation;
import org.eclipse.cdt.core.parser.ast.IASTTemplateSpecialization;
import org.eclipse.cdt.core.parser.ast.IASTTypedefDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTTypedefReference;
import org.eclipse.cdt.core.parser.ast.IASTUsingDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTUsingDirective;
import org.eclipse.cdt.core.parser.ast.IASTVariable;
import org.eclipse.cdt.core.parser.ast.IASTVariableReference;
import org.eclipse.cdt.internal.core.parser.ParserException;
import org.eclipse.cdt.internal.core.parser.ScannerInfo;


/**
 * @author jcamelon
 *
 */
public class CompleteParseASTTest extends TestCase
{
	public class Scope implements IASTScope
	{
		private List decls = new ArrayList(); 
		private final IASTScope scope; 
		public Scope( IASTScope scope )
		{
			this.scope = scope;
		}
		
		public void addDeclaration( IASTDeclaration  d )
		{
			decls.add(d);
		}
		
		public Iterator getDeclarations()
		{
			return decls.iterator();
		}

        /**
         * @return
         */
        public IASTScope getScope()
        {
         
            return scope;
        }
	}

	public class FullParseCallback implements ISourceElementRequestor 
	{
		private List references = new ArrayList(); 
        private Stack inclusions = new Stack();
        private Scope compilationUnit;
        
        public IASTScope getCompilationUnit()
        {
        	return compilationUnit;
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptVariable(org.eclipse.cdt.core.parser.ast.IASTVariable)
         */
        public void acceptVariable(IASTVariable variable)
        {
        	getCurrentScope().addDeclaration( variable );
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptFunctionDeclaration(org.eclipse.cdt.core.parser.ast.IASTFunction)
         */
        public void acceptFunctionDeclaration(IASTFunction function)
        {
            getCurrentScope().addDeclaration(function);
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptUsingDirective(org.eclipse.cdt.core.parser.ast.IASTUsingDirective)
         */
        public void acceptUsingDirective(IASTUsingDirective usageDirective)
        {
			getCurrentScope().addDeclaration(usageDirective);
            
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptUsingDeclaration(org.eclipse.cdt.core.parser.ast.IASTUsingDeclaration)
         */
        public void acceptUsingDeclaration(IASTUsingDeclaration usageDeclaration)
        {
			getCurrentScope().addDeclaration(usageDeclaration);
            
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptASMDefinition(org.eclipse.cdt.core.parser.ast.IASTASMDefinition)
         */
        public void acceptASMDefinition(IASTASMDefinition asmDefinition)
        {
			getCurrentScope().addDeclaration(asmDefinition);
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptTypedefDeclaration(org.eclipse.cdt.core.parser.ast.IASTTypedefDeclaration)
         */
        public void acceptTypedefDeclaration(IASTTypedefDeclaration typedef)
        {
            getCurrentScope().addDeclaration(typedef);
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptEnumerationSpecifier(org.eclipse.cdt.core.parser.ast.IASTEnumerationSpecifier)
         */
        public void acceptEnumerationSpecifier(IASTEnumerationSpecifier enumeration)
        {           
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptAbstractTypeSpecDeclaration(org.eclipse.cdt.core.parser.ast.IASTAbstractTypeSpecifierDeclaration)
         */
        public void acceptAbstractTypeSpecDeclaration(IASTAbstractTypeSpecifierDeclaration abstractDeclaration)
        {
            getCurrentScope().addDeclaration( abstractDeclaration );
            
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterFunctionBody(org.eclipse.cdt.core.parser.ast.IASTFunction)
         */
        public void enterFunctionBody(IASTFunction function)
        {
            pushScope( function );
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitFunctionBody(org.eclipse.cdt.core.parser.ast.IASTFunction)
         */
        public void exitFunctionBody(IASTFunction function)
        {
            popScope();
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterCompilationUnit(org.eclipse.cdt.core.parser.ast.IASTCompilationUnit)
         */
        public void enterCompilationUnit(IASTCompilationUnit compilationUnit)
        {
            pushScope( compilationUnit );            
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterInclusion(org.eclipse.cdt.core.parser.ast.IASTInclusion)
         */
        public void enterInclusion(IASTInclusion inclusion)
        {
            pushInclusion( inclusion );
        }

        /**
         * @param inclusion
         */
        private void pushInclusion(IASTInclusion inclusion)
        {
            inclusions.push( inclusion );
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterNamespaceDefinition(org.eclipse.cdt.core.parser.ast.IASTNamespaceDefinition)
         */
        public void enterNamespaceDefinition(IASTNamespaceDefinition namespaceDefinition)
        {
            pushScope( namespaceDefinition );
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#entesrClassSpecifier(org.eclipse.cdt.core.parser.ast.IASTClassSpecifier)
         */
        public void enterClassSpecifier(IASTClassSpecifier classSpecification)
        {
            pushScope( classSpecification );
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterLinkageSpecification(org.eclipse.cdt.core.parser.ast.IASTLinkageSpecification)
         */
        public void enterLinkageSpecification(IASTLinkageSpecification linkageSpec)
        {
        	pushScope( linkageSpec );
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterTemplateDeclaration(org.eclipse.cdt.core.parser.ast.IASTTemplateDeclaration)
         */
        public void enterTemplateDeclaration(IASTTemplateDeclaration declaration)
        {
            // TODO Auto-generated method stub
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterTemplateSpecialization(org.eclipse.cdt.core.parser.ast.IASTTemplateSpecialization)
         */
        public void enterTemplateSpecialization(IASTTemplateSpecialization specialization)
        {
            // TODO Auto-generated method stub
            
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterTemplateInstantiation(org.eclipse.cdt.core.parser.ast.IASTTemplateInstantiation)
         */
        public void enterTemplateInstantiation(IASTTemplateInstantiation instantiation)
        {
            // TODO Auto-generated method stub
            
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptMethodDeclaration(org.eclipse.cdt.core.parser.ast.IASTMethod)
         */
        public void acceptMethodDeclaration(IASTMethod method)
        {
            getCurrentScope().addDeclaration( method );
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterMethodBody(org.eclipse.cdt.core.parser.ast.IASTMethod)
         */
        public void enterMethodBody(IASTMethod method)
        {
            pushScope(method);
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitMethodBody(org.eclipse.cdt.core.parser.ast.IASTMethod)
         */
        public void exitMethodBody(IASTMethod method)
        {
            popScope();
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptField(org.eclipse.cdt.core.parser.ast.IASTField)
         */
        public void acceptField(IASTField field)
        {
            getCurrentScope().addDeclaration(field);
            
        }


        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitTemplateDeclaration(org.eclipse.cdt.core.parser.ast.IASTTemplateDeclaration)
         */
        public void exitTemplateDeclaration(IASTTemplateDeclaration declaration)
        {
            // TODO Auto-generated method stub
            
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitTemplateSpecialization(org.eclipse.cdt.core.parser.ast.IASTTemplateSpecialization)
         */
        public void exitTemplateSpecialization(IASTTemplateSpecialization specialization)
        {
            // TODO Auto-generated method stub
            
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitTemplateExplicitInstantiation(org.eclipse.cdt.core.parser.ast.IASTTemplateInstantiation)
         */
        public void exitTemplateExplicitInstantiation(IASTTemplateInstantiation instantiation)
        {
            // TODO Auto-generated method stub
            
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitLinkageSpecification(org.eclipse.cdt.core.parser.ast.IASTLinkageSpecification)
         */
        public void exitLinkageSpecification(IASTLinkageSpecification linkageSpec)
        {
            popScope();
            getCurrentScope().addDeclaration(linkageSpec);
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitClassSpecifier(org.eclipse.cdt.core.parser.ast.IASTClassSpecifier)
         */
        public void exitClassSpecifier(IASTClassSpecifier classSpecification)
        {
            popScope();
            
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitNamespaceDefinition(org.eclipse.cdt.core.parser.ast.IASTNamespaceDefinition)
         */
        public void exitNamespaceDefinition(IASTNamespaceDefinition namespaceDefinition)
        {
            popScope();
            getCurrentScope().addDeclaration(namespaceDefinition);
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitInclusion(org.eclipse.cdt.core.parser.ast.IASTInclusion)
         */
        public void exitInclusion(IASTInclusion inclusion)
        {
            popInclusion(); 
        }

        /**
         * 
         */
        private void popInclusion()
        {
            inclusions.pop();
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitCompilationUnit(org.eclipse.cdt.core.parser.ast.IASTCompilationUnit)
         */
        public void exitCompilationUnit(IASTCompilationUnit compilationUnit)
        {
            this.compilationUnit = popScope();
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptPointerToFunction(org.eclipse.cdt.core.parser.ast.IASTPointerToFunction)
         */
        public void acceptPointerToFunction(IASTPointerToFunction function)
        {
			getCurrentScope().addDeclaration(function);
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptPointerToMethod(org.eclipse.cdt.core.parser.ast.IASTPointerToMethod)
         */
        public void acceptPointerToMethod(IASTPointerToMethod method)
        {
			getCurrentScope().addDeclaration(method);        }
        
        
        private Stack scopes = new Stack();
        protected Scope getCurrentScope()
        {
        	return (Scope)scopes.peek();
        }
        
        protected Scope popScope()
        {
        	Scope s = (Scope)scopes.pop();
        	h.put( s.getScope(), s );
        	return s; 
        }
        
        protected void pushScope( IASTScope scope )
        {
        	scopes.push( new Scope( scope ));
        }
        
        Hashtable h = new Hashtable();
        
        public Scope lookup( IASTScope s)
        {
        	return (Scope)h.get(s);
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptProblem(org.eclipse.cdt.core.parser.IProblem)
         */
        public void acceptProblem(IProblem problem)
        {
            // TODO Auto-generated method stub
            
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptMacro(org.eclipse.cdt.core.parser.ast.IASTMacro)
         */
        public void acceptMacro(IASTMacro macro)
        {
            // TODO Auto-generated method stub
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptClassReference(org.eclipse.cdt.core.parser.ast.IASTClassReference)
         */
        public void acceptClassReference(IASTClassReference reference)
        {
            references.add( reference );
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptTypedefReference(org.eclipse.cdt.core.parser.ast.IASTTypedefReference)
         */
        public void acceptTypedefReference(IASTTypedefReference reference)
        {
			references.add( reference );
            
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptNamespaceReference(org.eclipse.cdt.core.parser.ast.IASTNamespaceReference)
         */
        public void acceptNamespaceReference(IASTNamespaceReference reference)
        {
			references.add( reference );
            
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptEnumerationReference(org.eclipse.cdt.core.parser.ast.IASTEnumerationReference)
         */
        public void acceptEnumerationReference(IASTEnumerationReference reference)
        {
			references.add( reference );
            
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptVariableReference(org.eclipse.cdt.core.parser.ast.IASTVariableReference)
         */
        public void acceptVariableReference(IASTVariableReference reference)
        {
			references.add( reference );
            
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptFunctionReference(org.eclipse.cdt.core.parser.ast.IASTFunctionReference)
         */
        public void acceptFunctionReference(IASTFunctionReference reference)
        {
			references.add( reference );
            
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptFieldReference(org.eclipse.cdt.core.parser.ast.IASTFieldReference)
         */
        public void acceptFieldReference(IASTFieldReference reference)
        {
			references.add( reference );
            
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptMethodReference(org.eclipse.cdt.core.parser.ast.IASTMethodReference)
         */
        public void acceptMethodReference(IASTMethodReference reference)
        {
			references.add( reference );
            
        }
        
        public List getReferences()
        {
        	return references;
        }
	}
	
	protected Iterator getDeclarations( IASTScope scope )
	{
		Scope s = callback.lookup( scope ); 
		if( s != null )
			return s.getDeclarations();
		return null;
	}
	
	
	protected FullParseCallback callback;
	
	protected IASTScope parse( String code )throws ParserException
	{
		callback = new FullParseCallback(); 
		IParser parser = ParserFactory.createParser( 
			ParserFactory.createScanner( new StringReader( code ), "test-code", new ScannerInfo(),
				ParserMode.COMPLETE_PARSE, callback ), callback, ParserMode.COMPLETE_PARSE	
			);
		parser.parse();
        return callback.getCompilationUnit();
    }

    /**
     * @param a
     */
    public CompleteParseASTTest(String a)
    {
        super(a);
    }
    
    public void testEmptyCompilationUnit() throws Exception
    {
    	IASTScope compilationUnit = parse( "// no real code ");
    	assertNotNull( compilationUnit );
    	assertFalse( compilationUnit.getDeclarations().hasNext() );
    }
    
    public void testSimpleNamespace() throws Exception
    {
    	Iterator declarations = parse( "namespace A { }").getDeclarations();
    	IASTNamespaceDefinition namespaceDefinition = (IASTNamespaceDefinition)declarations.next(); 
    	assertEquals( namespaceDefinition.getName(), "A" ); 
    	assertFalse( getDeclarations( namespaceDefinition ).hasNext() );
    }

	public void testMultipleNamespaceDefinitions() throws Exception
	{
		Iterator declarations = parse( "namespace A { } namespace A { }").getDeclarations();
		IASTNamespaceDefinition namespaceDefinition = (IASTNamespaceDefinition)declarations.next(); 
		assertEquals( namespaceDefinition.getName(), "A" );
		namespaceDefinition = (IASTNamespaceDefinition)declarations.next(); 
		assertEquals( namespaceDefinition.getName(), "A" ); 
		assertFalse( getDeclarations( namespaceDefinition ).hasNext() );
	}

    public void testNestedNamespaceDefinitions() throws Exception
    {
		Iterator declarations = parse( "namespace A { namespace B { } }").getDeclarations();
		IASTNamespaceDefinition namespaceDefinition = (IASTNamespaceDefinition)declarations.next(); 
		assertEquals( namespaceDefinition.getName(), "A" );
		assertFalse( declarations.hasNext() );
		Iterator subDeclarations = getDeclarations( namespaceDefinition );
		IASTNamespaceDefinition subDeclaration = (IASTNamespaceDefinition)subDeclarations.next();
		assertEquals( subDeclaration.getName(), "B" );
		assertFalse( subDeclarations.hasNext() );
    }
    
    public void testEmptyClassDeclaration() throws Exception
    {
    	Iterator declarations = parse( "class A { };").getDeclarations();
    	IASTAbstractTypeSpecifierDeclaration abs = (IASTAbstractTypeSpecifierDeclaration)declarations.next();
    	IASTClassSpecifier classSpec = (IASTClassSpecifier)abs.getTypeSpecifier();
    	assertEquals( classSpec.getName(), "A");
    	assertFalse( getDeclarations( classSpec ).hasNext() ); 
    	assertFalse( declarations.hasNext() );
    }
    
    public void testSimpleSubclass() throws Exception
    {
    	Iterator declarations = parse( "class A { };  class B : public A { };").getDeclarations();
    	IASTClassSpecifier classA = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)declarations.next()).getTypeSpecifier();
		IASTClassSpecifier classB = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)declarations.next()).getTypeSpecifier();
		Iterator parentClasses = classB.getBaseClauses();
		IASTBaseSpecifier baseClass = (IASTBaseSpecifier)parentClasses.next();
		assertEquals( classA, baseClass.getParentClassSpecifier() );
		assertEquals( baseClass.getParentClassName(), "A");
		assertEquals( baseClass.getAccess(), ASTAccessVisibility.PUBLIC);
		assertFalse( baseClass.isVirtual() );
    }
    
    public void testNestedSubclass() throws Exception
    {
    	Iterator declarations = parse( "namespace N { class A { }; } class B : protected virtual N::A { };").getDeclarations();
    	IASTNamespaceDefinition namespaceDefinition = (IASTNamespaceDefinition)declarations.next();
    	IASTClassSpecifier classA = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)getDeclarations( namespaceDefinition).next() ).getTypeSpecifier(); 
		IASTClassSpecifier classB = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)declarations.next()).getTypeSpecifier();
		Iterator baseClauses = classB.getBaseClauses(); 
		IASTBaseSpecifier baseClass = (IASTBaseSpecifier)baseClauses.next();
		assertEquals( classA, baseClass.getParentClassSpecifier() );
		assertEquals( callback.getReferences().size(), 2 );
    }
    
    public void testSimpleVariable() throws Exception
    {
    	Iterator declarations = parse( "int x;").getDeclarations();
    	IASTVariable v = (IASTVariable)declarations.next();
    	assertEquals( v.getName(), "x");
    	assertEquals( ((IASTSimpleTypeSpecifier)v.getAbstractDeclaration().getTypeSpecifier()).getType(), IASTSimpleTypeSpecifier.Type.INT ); 
    }
    
	public void testSimpleClassReferenceVariable() throws Exception
	{
		Iterator declarations = parse( "class A { }; A x;").getDeclarations();
		IASTClassSpecifier classA = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)declarations.next()).getTypeSpecifier();
		IASTVariable v = (IASTVariable)declarations.next();
		assertEquals( v.getName(), "x");
		assertEquals( ((IASTSimpleTypeSpecifier)v.getAbstractDeclaration().getTypeSpecifier()).getTypeSpecifier(), classA ); 
	}
    
	public void testNestedClassReferenceVariable() throws Exception
	{
		Iterator declarations = parse( "namespace N { class A { }; } N::A x;").getDeclarations();
		IASTNamespaceDefinition namespace = (IASTNamespaceDefinition)declarations.next();
		Iterator iter = getDeclarations( namespace );
		IASTClassSpecifier classA = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)iter.next()).getTypeSpecifier();
		IASTVariable v = (IASTVariable)declarations.next();
		assertEquals( v.getName(), "x");
		assertEquals( ((IASTSimpleTypeSpecifier)v.getAbstractDeclaration().getTypeSpecifier()).getTypeSpecifier(), classA );
		assertEquals( callback.getReferences().size(), 2 ); 
	}
	
	public void testMultipleDeclaratorsVariable() throws Exception
	{
		Iterator declarations = parse( "class A { }; A x, y, z;").getDeclarations();
		IASTClassSpecifier classA = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)declarations.next()).getTypeSpecifier();
		IASTVariable v = (IASTVariable)declarations.next();
		assertEquals( v.getName(), "x");
		assertEquals( ((IASTSimpleTypeSpecifier)v.getAbstractDeclaration().getTypeSpecifier()).getTypeSpecifier(), classA );
		assertEquals( callback.getReferences().size(), 3 );  
	}
	
	public void testSimpleField() throws Exception
	{
		Iterator declarations = parse( "class A { double x; };").getDeclarations();
		IASTClassSpecifier classA = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)declarations.next()).getTypeSpecifier();
		Iterator fields =getDeclarations(classA);
		IASTField f = (IASTField)fields.next(); 
		assertEquals( f.getName(), "x" );
		assertEquals( ((IASTSimpleTypeSpecifier)f.getAbstractDeclaration().getTypeSpecifier()).getType(), IASTSimpleTypeSpecifier.Type.DOUBLE ); 
	}
	
	public void testUsingClauses() throws Exception
	{
		Iterator declarations = parse( "namespace A { namespace B { int x;  class C { static int y = 5; }; } } \n using namespace A::B;\n using A::B::x;using A::B::C;using A::B::C::y;").getDeclarations();
		IASTNamespaceDefinition namespaceA = (IASTNamespaceDefinition)declarations.next();
		IASTNamespaceDefinition  namespaceB = (IASTNamespaceDefinition)getDeclarations( namespaceA ).next();
		Iterator i = getDeclarations( namespaceB );
		IASTVariable variableX = (IASTVariable)i.next();
		IASTClassSpecifier classC = ((IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier());
		IASTField fieldY = (IASTField)getDeclarations( classC ).next(); 
		assertQualifiedName( fieldY.getFullyQualifiedName(), new String [] { "A", "B", "C", "y" } );		
		IASTUsingDirective directive = (IASTUsingDirective)declarations.next();
		assertEquals( directive.getNamespaceDefinition(), namespaceB );
		IASTUsingDeclaration declaration = (IASTUsingDeclaration)declarations.next();
		assertEquals( declaration.getUsingType(), variableX );
		declaration = (IASTUsingDeclaration)declarations.next();
		assertEquals( declaration.getUsingType(), classC );
		declaration = (IASTUsingDeclaration)declarations.next();
		assertEquals( declaration.getUsingType(), fieldY );
		assertEquals( callback.getReferences().size(), 12 );
		
	}
	
	public void testEnumerations() throws Exception
	{
		Iterator declarations = parse( "namespace A { enum E { e1, e2, e3 }; E varE;}").getDeclarations();
		IASTNamespaceDefinition namespaceA = (IASTNamespaceDefinition)declarations.next(); 
		Iterator namespaceMembers = getDeclarations( namespaceA ); 
		IASTEnumerationSpecifier enumE = (IASTEnumerationSpecifier)((IASTAbstractTypeSpecifierDeclaration)namespaceMembers.next()).getTypeSpecifier();
		assertEquals( enumE.getName(), "E");
		assertQualifiedName( enumE.getFullyQualifiedName(), new String [] { "A", "E" } );		
		Iterator enumerators = enumE.getEnumerators();
		IASTEnumerator enumerator_e1 = (IASTEnumerator)enumerators.next();
		IASTEnumerator enumerator_e2 = (IASTEnumerator)enumerators.next();
		IASTEnumerator enumerator_e3 = (IASTEnumerator)enumerators.next();
		assertFalse( enumerators.hasNext() );
		assertEquals( enumerator_e1.getName(), "e1");
		assertEquals( enumerator_e2.getName(), "e2");
		assertEquals( enumerator_e3.getName(), "e3");
		IASTVariable varE = (IASTVariable)namespaceMembers.next();
		assertEquals( ((IASTSimpleTypeSpecifier)varE.getAbstractDeclaration().getTypeSpecifier()).getTypeSpecifier(), enumE );
	}
	
	public void testSimpleFunction() throws Exception
	{
		Iterator declarations = parse( "void foo( void );").getDeclarations();
		IASTFunction function = (IASTFunction)declarations.next();
		assertEquals( function.getName(), "foo" );
		assertEquals( callback.getReferences().size(), 0 );
	}
	
	public void testSimpleFunctionWithTypes() throws Exception
	{
		Iterator declarations = parse( "class A { public: \n class B { }; }; const A::B &  foo( A * myParam );").getDeclarations();
		IASTClassSpecifier classA = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)declarations.next()).getTypeSpecifier();
		IASTFunction function = (IASTFunction)declarations.next(); 
		assertEquals( callback.getReferences().size(), 3 ); 
	}
	
	public void testSimpleMethod() throws Exception
	{
		Iterator declarations = parse( "class A { void foo(); };").getDeclarations();
		IASTClassSpecifier classA = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)declarations.next()).getTypeSpecifier();
		IASTMethod method = (IASTMethod)getDeclarations( classA ).next();
		assertEquals( method.getName(), "foo" );
	}
	
	public void testSimpleMethodWithTypes() throws Exception
	{
		Iterator declarations = parse( "class U { }; class A { U foo( U areDumb ); };").getDeclarations();
		IASTClassSpecifier classU = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)declarations.next()).getTypeSpecifier();
		IASTClassSpecifier classA = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)declarations.next()).getTypeSpecifier();
		IASTMethod method = (IASTMethod)getDeclarations( classA ).next();
		assertEquals( method.getName(), "foo" );
		assertEquals( callback.getReferences().size(), 2 );
	}
	
	public void testUsingDeclarationWithFunctionsAndMethods() throws Exception
	{
		Iterator declarations = parse( "namespace N { int foo(void); } class A { static int bar(void); }; using N::foo; using ::A::bar;" ).getDeclarations();
		IASTNamespaceDefinition namespaceN = (IASTNamespaceDefinition)declarations.next();
		IASTFunction fooFunction = (IASTFunction)(getDeclarations(namespaceN).next()); 
		IASTClassSpecifier classA = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)declarations.next()).getTypeSpecifier();
		IASTMethod methodM = (IASTMethod)(getDeclarations(classA).next());
		IASTUsingDeclaration using1 = (IASTUsingDeclaration)declarations.next(); 
		IASTUsingDeclaration using2 = (IASTUsingDeclaration)declarations.next();
		assertEquals( callback.getReferences().size(), 4 );
		Iterator references = callback.getReferences().iterator();
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), namespaceN );
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), fooFunction );
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), classA );
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), methodM ); 
	}
	
	public void testLinkageSpec() throws Exception
	{
		IASTLinkageSpecification linkage = (IASTLinkageSpecification)parse( "extern \"C\" { int foo(); }").getDeclarations().next();
		Iterator i = getDeclarations( linkage );
		IASTFunction f = (IASTFunction)i.next();
		assertEquals( f.getName(),"foo");
	}
	

	public void testBogdansExample() throws Exception
	{
		IASTNamespaceDefinition namespaceA = (IASTNamespaceDefinition)parse( "namespace A { namespace B {	enum e1{e_1,e_2};	int x;	class C	{	static int y = 5;	}; }} ").getDeclarations().next();
		IASTNamespaceDefinition namespaceB = (IASTNamespaceDefinition)(getDeclarations(namespaceA).next());
		Iterator subB = getDeclarations( namespaceB );
		IASTEnumerationSpecifier enumE1 = (IASTEnumerationSpecifier)((IASTAbstractTypeSpecifierDeclaration)subB.next()).getTypeSpecifier();
		Iterator enumerators = enumE1.getEnumerators();
		IASTEnumerator enumeratorE_1 = (IASTEnumerator)enumerators.next();
		assertEquals( enumeratorE_1.getOwnerEnumerationSpecifier(), enumE1 );
		IASTVariable variableX = (IASTVariable)subB.next(); 
		IASTClassSpecifier classC = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)subB.next()).getTypeSpecifier();
				
		
	}
	
//	public void testSimpleTypedef() throws Exception
//	{
//		IASTTypedefDeclaration typedef = (IASTTypedefDeclaration)parse( "typedef int myInt;").getDeclarations().next();
//		assertEquals( typedef.getName(), "myInt");
//	}
	
	protected void assertQualifiedName(String [] fromAST, String [] theTruth)
	 {
		 assertNotNull( fromAST );
		 assertNotNull( theTruth );
		 assertEquals( fromAST.length, theTruth.length );
		 for( int i = 0; i < fromAST.length; ++i )
		 {
			 assertEquals( fromAST[i], theTruth[i]);
		 }
	 }
	 
	 
}
