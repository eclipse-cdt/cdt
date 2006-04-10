/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.ui;
//import org.eclipse.jface.dialogs.*;
//import org.eclipse.jface.viewers.*;

/**
 * This interface is used to identify objects whose job is to massage user-entered
 * text before saving it to a model. Eg, the text, while valid, may need to be folded
 * to uppercase or trimmed of blanks, or resolved if it has a substitution variable. 
 * <p>
 * This interface, like IInputValidator, allows this work to be abstracted such that one
 * object that does it can be used in various dialogs or wizards or property sheets.
 */
public interface ISystemMassager 
{


    /**
     * Given the user-entered input, return the massaged version of it.
     * If no massaging required, return the input as is.
     */
    public String massage(String text);
}