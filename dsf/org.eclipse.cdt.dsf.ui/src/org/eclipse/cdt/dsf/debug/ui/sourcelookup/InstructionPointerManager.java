/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Wind River Systems - Adapter to use with DSF
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.sourcelookup;


import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.dsf.concurrent.ThreadSafe;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationPresentation;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * This class tracks instruction pointer contexts for a single DSF session.
 */
@ThreadSafe
class InstructionPointerManager {

	/**
	 * Current instruction pointer annotation type.
	 */
	private static final String ID_CURRENT_IP= IDebugUIConstants.ANNOTATION_TYPE_INSTRUCTION_POINTER_CURRENT;
	
    /**
	 * Secondary instruction pointer annotation type.
	 */
	private static final String ID_SECONDARY_IP= IDebugUIConstants.ANNOTATION_TYPE_INSTRUCTION_POINTER_SECONDARY;

	/**
     * Editor annotation object for instruction pointers.
     */
    static class IPAnnotation extends Annotation implements IAnnotationPresentation {
        
        /** The image for this annotation. */
        private Image fImage;
        
        /** Frame DMC that this IP is for **/
        private IStack.IFrameDMContext fFrame;
        
        /**
         * Constructs an instruction pointer image.
         * 
         * @param frame stack frame the instruction pointer is associated with
         * @param annotationType the type of annotation to display (annotation identifier)
         * @param text the message to display with the annotation as hover help
         * @param image the image used to display the annotation
         */
        IPAnnotation(IStack.IFrameDMContext frame, String annotationType, String text, Image image) {
            super(annotationType, false, text);
            fFrame = frame;
            fImage = image;
        }
        
        /**
         * Returns this annotation's image.
         * 
         * @return image
         */
        protected Image getImage() {
            return fImage;
        }
        
        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object other) {
            if (other instanceof IPAnnotation) {
                return fFrame.equals(((IPAnnotation)other).fFrame);            
            }
            return false;
        }
        
        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            return fFrame.hashCode();
        }

        /*
         * @see org.eclipse.jface.text.source.IAnnotationPresentation#paint(org.eclipse.swt.graphics.GC, org.eclipse.swt.widgets.Canvas, org.eclipse.swt.graphics.Rectangle)
         */
        @Override
		public void paint(GC gc, Canvas canvas, Rectangle bounds) {
            Rectangle imageBounds = fImage.getBounds();
            gc.drawImage(fImage, bounds.x + (bounds.width - imageBounds.width) / 2 , bounds.y + (bounds.height - imageBounds.height) / 2);
        }

        /*
         * @see org.eclipse.jface.text.source.IAnnotationPresentation#getLayer()
         */
        @Override
		public int getLayer() {
            return 5;
        }

    }
    
    /**
     * Represents the context for a single instruction pointer.  This is a convenience class
     * used to store the three objects that comprise an instruction pointer 'context' so it
     * can be stored in collections.
     */
    static class AnnotationWrapper {

        /** The text editor for this context. */
        private ITextEditor fTextEditor;
        
        /** Stack frame that this annotation is for */
        private IStack.IFrameDMContext fFrameDmc;
        
        /** The vertical ruler annotation for this context. */
        private Annotation fAnnotation;

        AnnotationWrapper(ITextEditor textEditor, Annotation annotation, IStack.IFrameDMContext frameDmc) {
            fTextEditor = textEditor;
            fAnnotation = annotation;
            fFrameDmc = frameDmc;
        }
        
        /**
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object other) {
            if (other instanceof AnnotationWrapper) {
                AnnotationWrapper otherContext = (AnnotationWrapper) other;
                return getAnnotation().equals(otherContext.getAnnotation());
            }
            return false;
        }
        
        /**
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            return getAnnotation().hashCode();
        }

        ITextEditor getTextEditor() { return fTextEditor; }
        Annotation getAnnotation() { return fAnnotation; }
        IStack.IFrameDMContext getFrameDMC() { return fFrameDmc; }
    }
    
	/**
	 * Mapping of IDebugTarget objects to (mappings of IThread objects to lists of instruction
	 * pointer contexts).
	 */
	private final List<AnnotationWrapper> fAnnotationWrappers;

	/**
	 * For customized instruction pointer presentation.
	 */
	private final IInstructionPointerPresentation fPresentation;

	/**
	 * Clients must not instantiate this class.
	 */
	public InstructionPointerManager() {
		this(null);
	}

	/**
	 * Clients must not instantiate this class.
	 * 
	 * @param presentation
	 *            the custom instruction pointer presentation or
	 *            <code>null</code> to use the default presentation
	 */
	public InstructionPointerManager(IInstructionPointerPresentation presentation) {
		fPresentation = presentation;
        fAnnotationWrappers = new LinkedList<AnnotationWrapper>();
	}
		
	/**
	 * Add an instruction pointer annotation in the specified editor for the 
	 * specified stack frame.
	 */
	public void addAnnotation(ITextEditor textEditor, IStack.IFrameDMContext frame, Position position, boolean isTopFrame) {
		
		IDocumentProvider docProvider = textEditor.getDocumentProvider();
		IEditorInput editorInput = textEditor.getEditorInput();
        // If there is no annotation model, there's nothing more to do
        IAnnotationModel annModel = docProvider.getAnnotationModel(editorInput);
        if (annModel == null) {
            return;
        }        

        if (isTopFrame) {
        	// remove other top-frame IP annotation(s) for this execution-context
        	removeAnnotations(DMContexts.getAncestorOfType(frame.getParents()[0], IExecutionDMContext.class));
        }
        Annotation annotation = createAnnotation(textEditor, frame); 
        
		// Add the annotation at the position to the editor's annotation model.
		annModel.removeAnnotation(annotation);
		annModel.addAnnotation(annotation, position);	
		
		// Add to list of existing wrappers
		synchronized (fAnnotationWrappers) {
			fAnnotationWrappers.add(new AnnotationWrapper(textEditor, annotation, frame));
		}
	}

	private Annotation createAnnotation(ITextEditor editorPart, IStack.IFrameDMContext frame) {
        String id = null;
        String text = null;
        Image image = null;
		if (fPresentation != null) {
			Annotation annotation = fPresentation.getInstructionPointerAnnotation(editorPart, frame);
			if (annotation != null) {
				return annotation;
			}
			id = fPresentation.getInstructionPointerAnnotationType(editorPart, frame);
			if (id == null) {
				image = fPresentation.getInstructionPointerImage(editorPart, frame);
			}
			text = fPresentation.getInstructionPointerText(editorPart, frame);
		}
		if (id == null) {
	        if (frame.getLevel() == 0) {
	        	id = ID_CURRENT_IP;
	        	if (text == null) {
	        		text = Messages.IPAnnotation_primary;
	        	}
	        	if (image == null) {
	        		image = DebugUITools.getImage(IDebugUIConstants.IMG_OBJS_INSTRUCTION_POINTER_TOP);
	        	}
	        } else {
	            id = ID_SECONDARY_IP;
	            if (text == null) {
	            	text = Messages.IPAnnotation_secondary;
	            }
	            if (image == null) {
	            	image = DebugUITools.getImage(IDebugUIConstants.IMG_OBJS_INSTRUCTION_POINTER);
	            }
	        }
	        return new IPAnnotation(frame, id, text, image);
		}
		return new Annotation(id, false, text);
	}
	
	/**
	 * Remove all annotations associated with the specified debug target that this class
	 * is tracking.
	 */
	public void removeAnnotations(IRunControl.IExecutionDMContext execDmc) {
		// Retrieve the mapping of threads to context lists
        synchronized(fAnnotationWrappers) {
            for (Iterator<AnnotationWrapper> wrapperItr = fAnnotationWrappers.iterator(); wrapperItr.hasNext();) {
                AnnotationWrapper wrapper = wrapperItr.next();
                if (DMContexts.isAncestorOf(wrapper.getFrameDMC(), execDmc)) {
                    removeAnnotation(wrapper.getTextEditor(), wrapper.getAnnotation());
                    wrapperItr.remove();
                }
            }
        }
	}

	/**
	 * Remove all top-frame annotations associated with the specified debug target that this class
	 * is tracking.
	 */
	public void removeTopFrameAnnotations(IRunControl.IExecutionDMContext execDmc) {
		// Retrieve the mapping of threads to context lists
        synchronized(fAnnotationWrappers) {
            for (Iterator<AnnotationWrapper> wrapperItr = fAnnotationWrappers.iterator(); wrapperItr.hasNext();) {
                AnnotationWrapper wrapper = wrapperItr.next();
                final IFrameDMContext frameDmc= wrapper.getFrameDMC();
				if (DMContexts.isAncestorOf(frameDmc, execDmc) && frameDmc.getLevel() == 0) {
                    removeAnnotation(wrapper.getTextEditor(), wrapper.getAnnotation());
                    wrapperItr.remove();
                }
            }
        }
	}

    /** Removes all annotations tracked by this manager */
    public void removeAllAnnotations() {
        synchronized(fAnnotationWrappers) {
            for (AnnotationWrapper wrapper : fAnnotationWrappers) {
                removeAnnotation(wrapper.getTextEditor(), wrapper.getAnnotation());
            }   
            fAnnotationWrappers.clear();
        }
    }
    
	/**
	 * Remove the specified annotation from the specified text editor.
	 */
	private void removeAnnotation(ITextEditor textEditor, Annotation annotation) {
		IDocumentProvider docProvider = textEditor.getDocumentProvider();
		if (docProvider != null) {
			IAnnotationModel annotationModel = docProvider.getAnnotationModel(textEditor.getEditorInput());
			if (annotationModel != null) {
				annotationModel.removeAnnotation(annotation);
			}
		}
	}
}
