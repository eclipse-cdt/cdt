/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/


package org.eclipse.rse.dstore.security;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

public class ImageRegistry {

  private static URL fgIconBaseURL= null;

	static {
		try {
			fgIconBaseURL= new URL(UniversalSecurityPlugin.getDefault().getBundle().getEntry("/"), "icons/full/" ); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (MalformedURLException e) {
			UniversalSecurityPlugin.getDefault().log(e);
		}
	}	
  	
	/*
	 * Set of predefined Image Descriptors.
	 * the following String are all $NON-NLS-1$
	 */
	public static final String T_OBJ		= "obj16"; //$NON-NLS-1$
	public static final String T_WIZBAN	= "wizban"; //$NON-NLS-1$

    public static final String IMG_CERTIF_FILE 	= "certif_file.gif";
	public static final String IMG_WZ_IMPORT_CERTIF  = "import_cert_wiz.gif";//"newjprj_wiz.gif";//$NON-NLS-1$
			    	
    public static final ImageDescriptor DESC_IMG_CERTIF_FILE	= createManaged(T_OBJ,IMG_CERTIF_FILE);
    public static final ImageDescriptor DESC_IMG_WZ_IMPORT_CERTIF = createManaged(T_WIZBAN,IMG_WZ_IMPORT_CERTIF);

	protected static HashMap _images;
	protected static HashMap _imageDescriptors;	

	public static Image getImage(String name)
	{
		return (Image)_images.get(name);
	}


	/**
	 * Insert the method's description here.
	 * Creation date: (2/16/2001 4:57:29 PM)
	 * @return ImageDescriptor
	 * @param name java.lang.String
	 */
	public static ImageDescriptor getImageDescriptor(String name) {
		
		return (ImageDescriptor) _imageDescriptors.get(name);
	}

	private static ImageDescriptor createManaged(String prefix, String name) {
		try {
			ImageDescriptor result =
				ImageDescriptor.createFromURL(makeIconFileURL(prefix, name));

			if (_images == null || _imageDescriptors == null) {
				_images = new HashMap();
				_imageDescriptors = new HashMap();
			}

			_imageDescriptors.put(name, result);
			_images.put(name, result.createImage());
			return result;

		} catch (MalformedURLException e) {
			return ImageDescriptor.getMissingImageDescriptor();
		}
	}

	public static void setImageDescriptors(
		IAction action,
		String type,
		String relPath) {
		try {
			ImageDescriptor id =
				ImageDescriptor.createFromURL(makeIconFileURL("d" + type, relPath));
			//$NON-NLS-1$
			if (id != null) {
				action.setDisabledImageDescriptor(id);
			}
		} catch (MalformedURLException e) {
		}

		try {
			ImageDescriptor id =
				ImageDescriptor.createFromURL(makeIconFileURL("c" + type, relPath));//$NON-NLS-1$
			if (id != null) {
				action.setHoverImageDescriptor(id);
			}
		} catch (MalformedURLException e) {
		}

		action.setImageDescriptor(create("e" + type, relPath)); //$NON-NLS-1$
	}

	private static URL makeIconFileURL(String prefix, String name)
		throws MalformedURLException {
		if (fgIconBaseURL == null)
			throw new MalformedURLException();

		StringBuffer buffer;
		if (prefix != null) {
			buffer = new StringBuffer(prefix);
			buffer.append('/');
			buffer.append(name);
		} else {
			buffer = new StringBuffer(name);
		}
		return new URL(fgIconBaseURL, buffer.toString());
	}
	
	private static ImageDescriptor create(String prefix, String name) {
		try {
			return ImageDescriptor.createFromURL(makeIconFileURL(prefix, name));
		} catch (MalformedURLException e) {
			return ImageDescriptor.getMissingImageDescriptor();
		}
	}	
	
	public static void shutdown() {
		if (_images == null)
			return;

		for (Iterator e = _images.values().iterator(); e.hasNext();) {
			Object next = e.next();
			if (next instanceof Image && !((Image) next).isDisposed()) {
				((Image) next).dispose();
			}
		}

		_images.clear();
		_images = null;
		_imageDescriptors.clear();
		_imageDescriptors = null;
	}
}