/**********************************************************************
 * Copyright (c) 2002-2004 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
 ***********************************************************************/
package org.eclipse.cdt.internal.core.parser.ast.complete;

import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate;
import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.ast.IASTClassReference;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTEnumerationReference;
import org.eclipse.cdt.core.parser.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTEnumerator;
import org.eclipse.cdt.core.parser.ast.IASTEnumeratorReference;
import org.eclipse.cdt.core.parser.ast.IASTField;
import org.eclipse.cdt.core.parser.ast.IASTFieldReference;
import org.eclipse.cdt.core.parser.ast.IASTFunction;
import org.eclipse.cdt.core.parser.ast.IASTFunctionReference;
import org.eclipse.cdt.core.parser.ast.IASTMethod;
import org.eclipse.cdt.core.parser.ast.IASTMethodReference;
import org.eclipse.cdt.core.parser.ast.IASTNamespaceDefinition;
import org.eclipse.cdt.core.parser.ast.IASTNamespaceReference;
import org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement;
import org.eclipse.cdt.core.parser.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTParameterReference;
import org.eclipse.cdt.core.parser.ast.IASTReference;
import org.eclipse.cdt.core.parser.ast.IASTTemplateParameter;
import org.eclipse.cdt.core.parser.ast.IASTTemplateParameterReference;
import org.eclipse.cdt.core.parser.ast.IASTTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTTypedefDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTTypedefReference;
import org.eclipse.cdt.core.parser.ast.IASTVariable;
import org.eclipse.cdt.core.parser.ast.IASTVariableReference;
import org.eclipse.cdt.core.parser.ast.IReferenceManager;

/**
 * @author jcamelon
 *  
 */
public class ReferenceCache implements IReferenceManager {

		/**
	 * @author jcamelon
	 *
	 */
	private interface IReferenceFactory {
		ASTReference createReference();
		ASTReference [] createReferenceArray( int size );
	}
	
	private static final int DEFAULT_CACHE_SIZE = 16;

	private boolean[] classReferencesAvailable;
	private ASTReference[] classReferences;
	private boolean[] variableReferencesAvailable;
	private ASTReference[] variableReferences;
	
	private abstract static class BaseReferenceFactory implements IReferenceFactory
	{
		public ASTReference[] createReferenceArray(int size) {
			return new ASTReference[ size ];
		}
	}

	private static final IReferenceFactory CLASSREF_FACTORY = 
		new BaseReferenceFactory() {
		public ASTReference createReference() { return new ASTClassReference(); }


	};
	
	private static final IReferenceFactory VARIABLEREF_FACTORY = 
		new BaseReferenceFactory()	{

			public ASTReference createReference() {
				return new ASTVariableReference();
			}
		
	};
	
	
	{
		classReferences = CLASSREF_FACTORY.createReferenceArray(DEFAULT_CACHE_SIZE);
		variableReferences = VARIABLEREF_FACTORY.createReferenceArray(DEFAULT_CACHE_SIZE);
		classReferencesAvailable = new boolean[DEFAULT_CACHE_SIZE];
		variableReferencesAvailable = new boolean[DEFAULT_CACHE_SIZE];
		for (int i = 0; i < DEFAULT_CACHE_SIZE; ++i) {
			classReferencesAvailable[i] = true;
			variableReferencesAvailable[i] = true;
			classReferences[i] =  CLASSREF_FACTORY.createReference();
			variableReferences[i] = VARIABLEREF_FACTORY.createReference();
		}
	}

	public void returnReference(IASTReference reference) {
		if (reference instanceof IASTClassReference)
		{
			returnReference(classReferencesAvailable, classReferences,
					reference);
			return;
		}
		if( reference instanceof IASTVariableReference )
		{
			returnReference( variableReferencesAvailable, variableReferences, reference );
			return;
		}
		
	}

	/**
	 * @param referencesAvailable
	 * @param references
	 * @param reference
	 */
	private void returnReference(boolean[] referencesAvailable,
			ASTReference[] references, IASTReference reference) {
		for (int i = 0; i < referencesAvailable.length; ++i)
			if (references[i] == reference) {
				referencesAvailable[i] = true;
				references[i].reset();
				break;
			}

	}

	public IASTReference getReference(int offset,
			ISourceElementCallbackDelegate referencedElement) {
		if (referencedElement instanceof IASTTypedefDeclaration)
			return getTypedefReference(offset,
					(IASTTypedefDeclaration) referencedElement);
		if (referencedElement instanceof IASTEnumerationSpecifier)
			return getEnumerationReference(offset,
					(IASTEnumerationSpecifier) referencedElement);
		if (referencedElement instanceof IASTTemplateParameter)
			return getTemplateParameterReference(offset,
					(IASTTemplateParameter) referencedElement);
		if (referencedElement instanceof IASTParameterDeclaration)
			return getParameterReference(offset,
					(IASTParameterDeclaration) referencedElement);
		if (referencedElement instanceof IASTTypeSpecifier)
			return getClassReference(offset, referencedElement);
		if (referencedElement instanceof IASTNamespaceDefinition)
			return getNamespaceReference(offset,
					(IASTNamespaceDefinition) referencedElement);
		if (referencedElement instanceof IASTMethod)
			return getMethodReference(offset, (IASTMethod) referencedElement);
		if (referencedElement instanceof IASTFunction)
			return getFunctionReference(offset,
					(IASTFunction) referencedElement);
		if (referencedElement instanceof IASTField)
			return getFieldReference(offset, (IASTField) referencedElement);
		if (referencedElement instanceof IASTVariable)
			return getVariableReference(offset,
					(IASTVariable) referencedElement);
		if (referencedElement instanceof IASTEnumerator)
			return getEnumeratorReference(offset,
					(IASTEnumerator) referencedElement);
		return null;
	}

	private IASTReference getClassReference(int offset,
			ISourceElementCallbackDelegate referencedElement) {
		for (int i = 0; i < classReferencesAvailable.length; ++i) {
			if (classReferencesAvailable[i]) {
				classReferencesAvailable[i] = false;
				classReferences[i].initialize(offset, referencedElement);
				return classReferences[i];
			}
		}
		int currentSize = classReferences.length;
		GrowResult g = growArrays(classReferences, classReferencesAvailable, CLASSREF_FACTORY );
		classReferences = g.getReferences();
		classReferencesAvailable = g.getAvailables();
		classReferencesAvailable[currentSize] = false;
		classReferences[currentSize].initialize(offset, referencedElement);
		return classReferences[currentSize];
	}

	protected static class GrowResult
	{
		private boolean[] b;
		private ASTReference[] r;

		public void initialize( boolean [] bools, ASTReference [] refs )
		{
			this.b = bools;
			this.r = refs;
		}
		/**
		 * @return Returns the b.
		 */
		public boolean[] getAvailables() {
			return b;
		}
		/**
		 * @return Returns the r.
		 */
		public ASTReference[] getReferences() {
			return r;
		}
	}
	
	protected static final GrowResult growResult = new GrowResult();
	
	/**
	 * @param inReferences, boolean [] inReferencesAvailable, IReferenceFactory factory 
	 * @return
	 */
	protected static GrowResult growArrays(ASTReference[] inReferences, boolean [] inReferencesAvailable, IReferenceFactory factory ) {
		int currentSize = inReferences.length;
		boolean[] availables = new boolean[currentSize * 2];
		System.arraycopy(inReferencesAvailable, 0, availables, 0,	currentSize);
		ASTReference[] refs = factory.createReferenceArray(currentSize * 2);
		System.arraycopy(inReferences, 0, refs, 0, currentSize);
		for (int i = currentSize; i < availables.length; ++i) {
			refs[i] = factory.createReference();
			availables[i] = true;
		}
		growResult.initialize( availables, refs );
		return growResult;
	}

	/**
	 * @param offset
	 * @param declaration
	 * @return
	 */
	private IASTReference getTypedefReference(int offset,
			IASTTypedefDeclaration declaration) {
		return new ASTTypedefReference(offset, declaration);
	}

	/**
	 * @param offset
	 * @param variable
	 * @return
	 */
	private IASTReference getVariableReference(int offset, IASTVariable referencedElement) {
		for (int i = 0; i < variableReferencesAvailable.length; ++i) {
			if (variableReferencesAvailable[i]) {
				variableReferencesAvailable[i] = false;
				variableReferences[i].initialize(offset, referencedElement);
				return variableReferences[i];
			}
		}
		int currentSize = variableReferences.length;
		GrowResult g = growArrays( variableReferences, variableReferencesAvailable, VARIABLEREF_FACTORY );
		variableReferencesAvailable = g.getAvailables();
		variableReferences = g.getReferences();
		variableReferencesAvailable[currentSize] = false;
		variableReferences[currentSize].initialize(offset, referencedElement);
		return variableReferences[currentSize];

	}

	/**
	 * @param offset
	 * @param declaration
	 * @return
	 */
	private IASTReference getParameterReference(int offset,
			IASTParameterDeclaration declaration) {
		return new ASTParameterReference(offset, declaration);
	}

	/**
	 * @param offset
	 * @param parameter
	 * @return
	 */
	private IASTReference getTemplateParameterReference(int offset,
			IASTTemplateParameter parameter) {
		return new ASTTemplateParameterReference(offset, parameter);
	}

	/**
	 * @param offset
	 * @param definition
	 * @return
	 */
	private IASTReference getNamespaceReference(int offset,
			IASTNamespaceDefinition definition) {
		return new ASTNamespaceReference(offset, definition);
	}

	/**
	 * @param offset
	 * @param specifier
	 * @return
	 */
	private IASTReference getEnumerationReference(int offset,
			IASTEnumerationSpecifier specifier) {
		return new ASTEnumerationReference(offset, specifier);
	}

	/**
	 * @param offset
	 * @param enumerator
	 * @return
	 */
	private IASTReference getEnumeratorReference(int offset,
			IASTEnumerator enumerator) {
		return new ASTEnumeratorReference(offset, enumerator);
	}

	/**
	 * @param offset
	 * @param method
	 * @return
	 */
	private IASTReference getMethodReference(int offset, IASTMethod method) {
		return new ASTMethodReference(offset, method);
	}

	/**
	 * @param offset
	 * @param function
	 * @return
	 */
	private IASTReference getFunctionReference(int offset, IASTFunction function) {
		return new ASTFunctionReference(offset, function);
	}

	/**
	 * @param offset
	 * @param field
	 * @return
	 */
	private IASTReference getFieldReference(int offset, IASTField field) {
		return new ASTFieldReference(offset, field);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTReferenceStore#processReferences()
	 */
	public void processReferences(List references, ISourceElementRequestor requestor)
	{
		if( references == null || references == Collections.EMPTY_LIST || references.isEmpty() )
			return;
	    
	    for( int i = 0; i < references.size(); ++i )
	    {
	    	IASTReference reference = ((IASTReference)references.get(i));
			reference.acceptElement(requestor, this );
			returnReference( reference );
	    }
	    	
	    references.clear();
	}

	public abstract static class ASTReference implements IASTReference {
		protected int offset;
		private static final String EMPTY_STRING = ""; //$NON-NLS-1$

		public void reset() {
			offset = 0;
		}

		/**
		 * @param offset2
		 * @param referencedElement
		 */
		public void initialize(int o, ISourceElementCallbackDelegate referencedElement) {
		}

		public void initialize(int o) {
			this.offset = o;
		}

		/**
		 *  
		 */
		public ASTReference(int offset) {
			this.offset = offset;
		}
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.core.parser.ast.IASTReference#getOffset()
		 */
		public int getOffset() {
			return offset;
		}
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.core.parser.ast.IASTReference#getName()
		 */
		public String getName() {
			if (getReferencedElement() instanceof IASTOffsetableNamedElement)
				return ((IASTOffsetableNamedElement) getReferencedElement())
						.getName();
			return EMPTY_STRING;
		}

		public boolean equals(Object obj) {
			if (obj == null)
				return false;
			if (!(obj instanceof IASTReference))
				return false;

			if (((IASTReference) obj).getName().equals(getName())
					&& ((IASTReference) obj).getOffset() == getOffset())
				return true;
			return false;
		}

		public void enterScope(ISourceElementRequestor requestor, IReferenceManager manager) {
		}

		public void exitScope(ISourceElementRequestor requestor, IReferenceManager manager) {
		}
	}
	public static class ASTClassReference extends ASTReference
			implements
				IASTClassReference {
		private IASTTypeSpecifier reference;
		/**
		 * @param i
		 * @param specifier
		 */
		public ASTClassReference(int i, IASTTypeSpecifier specifier) {
			super(i);
			reference = specifier;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.internal.core.parser.ast.complete.ASTReference#initialize(int)
		 */
		public void initialize(int o, ISourceElementCallbackDelegate specifier) {
			super.initialize(o);
			reference = (IASTTypeSpecifier) specifier;
		}
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.internal.core.parser.ast.complete.ASTReference#reset()
		 */
		public void reset() {
			super.reset();
			reference = null;
		}
		/**
		 *  
		 */
		public ASTClassReference() {
			super(0);
			reference = null;
		}
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.core.parser.ast.IASTReference#getReferencedElement()
		 */
		public ISourceElementCallbackDelegate getReferencedElement() {
			return (ISourceElementCallbackDelegate) reference;
		}
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#acceptElement(org.eclipse.cdt.core.parser.ISourceElementRequestor)
		 */
		public void acceptElement(ISourceElementRequestor requestor, IReferenceManager manager) {
			try {
				requestor.acceptClassReference(this);
			} catch (Exception e) {
				/* do nothing */
			}
		}
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.core.parser.ast.IASTClassReference#isResolved()
		 */
		public boolean isResolved() {
			return (reference instanceof IASTClassSpecifier);
		}
	}

	public static class ASTEnumerationReference extends ASTReference
			implements
				IASTEnumerationReference {
		private final IASTEnumerationSpecifier referencedElement;
		/**
		 * @param offset
		 * @param specifier
		 */
		public ASTEnumerationReference(int offset,
				IASTEnumerationSpecifier specifier) {
			super(offset);
			referencedElement = specifier;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.core.parser.ast.IASTReference#getReferencedElement()
		 */
		public ISourceElementCallbackDelegate getReferencedElement() {
			return referencedElement;
		}
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#acceptElement(org.eclipse.cdt.core.parser.ISourceElementRequestor)
		 */
		public void acceptElement(ISourceElementRequestor requestor, IReferenceManager manager) {
			try {
				requestor.acceptEnumerationReference(this);
			} catch (Exception e) {
				/* do nothing */
			}
		}
	}

	public static class ASTEnumeratorReference extends ASTReference
			implements
				IASTEnumeratorReference {

		private final IASTEnumerator enumerator;
		/**
		 * @param offset
		 * @param enumerator
		 */
		public ASTEnumeratorReference(int offset, IASTEnumerator enumerator) {
			super(offset);
			this.enumerator = enumerator;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.core.parser.ast.IASTReference#getReferencedElement()
		 */
		public ISourceElementCallbackDelegate getReferencedElement() {
			return enumerator;
		}
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#acceptElement(org.eclipse.cdt.core.parser.ISourceElementRequestor)
		 */
		public void acceptElement(ISourceElementRequestor requestor, IReferenceManager manager) {
			try {
				requestor.acceptEnumeratorReference(this);
			} catch (Exception e) {
				/* do nothing */
			}
		}
	}
	public static class ASTFieldReference extends ASTReference
			implements
				IASTReference,
				IASTFieldReference {
		private final IASTField referencedElement;
		/**
		 * @param offset
		 * @param field
		 */
		public ASTFieldReference(int offset, IASTField field) {
			super(offset);
			referencedElement = field;
		}
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.core.parser.ast.IASTReference#getReferencedElement()
		 */
		public ISourceElementCallbackDelegate getReferencedElement() {
			return referencedElement;
		}
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#acceptElement(org.eclipse.cdt.core.parser.ISourceElementRequestor)
		 */
		public void acceptElement(ISourceElementRequestor requestor, IReferenceManager manager) {
			try {
				requestor.acceptFieldReference(this);
			} catch (Exception e) {
				/* do nothing */
			}
		}
	}
	
	public static class ASTFunctionReference extends ASTReference
			implements
				IASTReference,
				IASTFunctionReference {
		private final IASTFunction declaration;
		/**
		 * @param offset
		 */
		public ASTFunctionReference(int offset,
				IASTFunction referencedDeclaration) {
			super(offset);
			this.declaration = referencedDeclaration;
		}
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.core.parser.ast.IASTReference#getReferencedElement()
		 */
		public ISourceElementCallbackDelegate getReferencedElement() {
			return declaration;
		}
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#acceptElement(org.eclipse.cdt.core.parser.ISourceElementRequestor)
		 */
		public void acceptElement(ISourceElementRequestor requestor, IReferenceManager manager) {
			try {
				requestor.acceptFunctionReference(this);
			} catch (Exception e) {
				/* do nothing */
			}
		}
	}
	public static class ASTMethodReference extends ASTReference
			implements
				IASTMethodReference {
		private final IASTMethod method;
		/**
		 * @param offset
		 */
		public ASTMethodReference(int offset, IASTMethod method) {
			super(offset);
			this.method = method;
		}
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.core.parser.ast.IASTReference#getReferencedElement()
		 */
		public ISourceElementCallbackDelegate getReferencedElement() {
			return method;
		}
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#acceptElement(org.eclipse.cdt.core.parser.ISourceElementRequestor)
		 */
		public void acceptElement(ISourceElementRequestor requestor, IReferenceManager manager) {
			try {
				requestor.acceptMethodReference(this);
			} catch (Exception e) {
				/* do nothing */
			}
		}
	}
	public static class ASTNamespaceReference extends ASTReference
			implements
				IASTNamespaceReference {
		private final IASTNamespaceDefinition reference;

		/**
		 * @param offset
		 * @param definition
		 */
		public ASTNamespaceReference(int offset,
				IASTNamespaceDefinition definition) {
			super(offset);
			reference = definition;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.core.parser.ast.IASTReference#getReferencedElement()
		 */
		public ISourceElementCallbackDelegate getReferencedElement() {
			return reference;
		}
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#acceptElement(org.eclipse.cdt.core.parser.ISourceElementRequestor)
		 */
		public void acceptElement(ISourceElementRequestor requestor, IReferenceManager manager) {
			try {
				requestor.acceptNamespaceReference(this);
			} catch (Exception e) {
				/* do nothing */
			}
		}
	}

	public static class ASTParameterReference extends ASTReference
			implements
				IASTParameterReference {
		private final IASTParameterDeclaration parm;

		/**
		 * @param offset
		 * @param declaration
		 */
		public ASTParameterReference(int offset,
				IASTParameterDeclaration declaration) {
			super(offset);
			parm = declaration;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.core.parser.ast.IASTReference#getReferencedElement()
		 */
		public ISourceElementCallbackDelegate getReferencedElement() {
			return parm;
		}
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#acceptElement(org.eclipse.cdt.core.parser.ISourceElementRequestor)
		 */
		public void acceptElement(ISourceElementRequestor requestor, IReferenceManager manager) {
			try {
				requestor.acceptParameterReference(this);
			} catch (Exception e) {
				/* do nothing */
			}
		}
	}
	public static class ASTTemplateParameterReference extends ASTReference
			implements
				IASTTemplateParameterReference {
		private final IASTTemplateParameter parameter;
		/**
		 * @param offset
		 */
		public ASTTemplateParameterReference(int offset,
				IASTTemplateParameter param) {
			super(offset);
			parameter = param;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.core.parser.ast.IASTReference#getReferencedElement()
		 */
		public ISourceElementCallbackDelegate getReferencedElement() {
			return parameter;
		}
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#acceptElement(org.eclipse.cdt.core.parser.ISourceElementRequestor)
		 */
		public void acceptElement(ISourceElementRequestor requestor, IReferenceManager manager) {
			try {
				requestor.acceptTemplateParameterReference(this);
			} catch (Exception e) {
				/* do nothing */
			}
		}
	}
	public static class ASTTypedefReference extends ASTReference
			implements
				IASTTypedefReference {
		private final IASTTypedefDeclaration referencedItem;
		/**
		 * @param offset
		 */
		public ASTTypedefReference(int offset,
				IASTTypedefDeclaration referencedItem) {
			super(offset);
			this.referencedItem = referencedItem;
		}
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.core.parser.ast.IASTReference#getReferencedElement()
		 */
		public ISourceElementCallbackDelegate getReferencedElement() {
			return referencedItem;
		}
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#acceptElement(org.eclipse.cdt.core.parser.ISourceElementRequestor)
		 */
		public void acceptElement(ISourceElementRequestor requestor, IReferenceManager manager) {
			try {
				requestor.acceptTypedefReference(this);
			} catch (Exception e) {
				/* do nothing */
			}
		}
	}
	public static class ASTVariableReference extends ASTReference
			implements
				IASTReference,
				IASTVariableReference {

		private IASTVariable referencedElement;
		/**
		 * @param offset
		 * @param variable
		 */
		public ASTVariableReference(int offset, IASTVariable variable) {
			super(offset);
			referencedElement = variable;
		}
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.internal.core.parser.ast.complete.ReferenceCache.ASTReference#initialize(int)
		 */
		public void initialize(int o, ISourceElementCallbackDelegate var ) {
			super.initialize(o);
			referencedElement = (IASTVariable) var;
		}
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.internal.core.parser.ast.complete.ReferenceCache.ASTReference#reset()
		 */
		public void reset() {
			super.reset();
			referencedElement = null;
		}
		/**
		 * 
		 */
		public ASTVariableReference() {
			super(0);
			referencedElement = null;
		}
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.core.parser.ast.IASTReference#getReferencedElement()
		 */
		public ISourceElementCallbackDelegate getReferencedElement() {
			return referencedElement;
		}
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#acceptElement(org.eclipse.cdt.core.parser.ISourceElementRequestor)
		 */
		public void acceptElement(ISourceElementRequestor requestor, IReferenceManager manager) {
			try {
				requestor.acceptVariableReference(this);
			} catch (Exception e) {
				/* do nothing */
			}
		}
	}
	/**
	 * @return
	 */
	public boolean isBalanced() {
		return isBalanced( classReferencesAvailable ) && isBalanced( variableReferencesAvailable );
	}

	/**
	 * @param referencesAvailable 
	 * @return
	 */
	private boolean isBalanced(boolean[] referencesAvailable ) {
		for( int i = 0; i < referencesAvailable.length; ++i )
		{
			if( !referencesAvailable[i] ) 
				return false;
		}
		return true;
	}

}