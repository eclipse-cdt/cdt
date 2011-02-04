/********************************************************************************
 * Copyright (c) 2011 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight.
 *
 * Contributors:
 * David McKnight   (IBM)        - [334295] SystemViewForm dialogs don't display cancellable progress in the dialog
 ********************************************************************************/

package org.eclipse.rse.internal.ui.view;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.osgi.util.NLS;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.rse.ui.view.IContextObject;
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;
import org.eclipse.ui.internal.progress.ProgressMessages;

public class SystemViewFormLabelAndContentProvider extends
		SystemViewLabelAndContentProvider {

	// override to deal with progress monitor	
	public Object[] getChildren(Object object)
    {
    	Object element = object;
    	if (object instanceof IContextObject)
    	{
    		element = ((IContextObject)object).getModelObject();
    	}
    	ISystemViewElementAdapter adapter = getViewAdapter(element);
    	if (supportsDeferredQueries() && !adapter.isPromptable(element))
    	{ 
	      	  IRunnableContext irc = RSEUIPlugin.getTheSystemRegistryUI().getRunnableContext();
	    	  if (irc == null){
	    		  irc = SystemBasePlugin.getActiveWorkbenchWindow();
	    	  }
	    	  if (irc == null){ // no window - defer to the base behaviour
	    		  return super.getChildren(object);
	    	  }
	    	  final Object fparent = object;
	    	  final Object felement = element;
	    	  final ISystemViewElementAdapter fadapter = adapter;
	    	  class MyRunnable implements IRunnableWithProgress
	    	  {
	    		  private Object[] _children = null;
				public void run(IProgressMonitor monitor)
						throws InvocationTargetException, InterruptedException {
					
					String taskName = NLS.bind(ProgressMessages.DeferredTreeContentManager_FetchingName, fadapter.getAbsoluteName(felement));
					monitor.beginTask(taskName, IProgressMonitor.UNKNOWN);
				  	  if (fparent instanceof IContextObject){
			    		  _children = fadapter.getChildren((IContextObject)fparent, monitor);
			    	  }
			    	  else {
			    		  _children = fadapter.getChildren((IAdaptable)fparent, monitor);
			    	  }
				  	  monitor.done();
				}
				
				public Object[] getChildren(){
					return _children;
				}    		  
	    	  };
	    	  
	    	  MyRunnable runnable = new MyRunnable();
	    	  try {
				irc.run(true, true, runnable);
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return runnable.getChildren();
    	}
    	else {
    		return super.getChildren(object);
    	}
    }
}
