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


import org.eclipse.osgi.util.NLS;



public class UniversalSecurityProperties extends NLS
{
	private static String BUNDLE_NAME = "org.eclipse.rse.dstore.security.UniversalSecurityProperties";//$NON-NLS-1$

	
	public static String RESID_SECURITY_CERTIFICATE_PROP_TITLE;
	public static String RESID_SECURITY_VALIDITY_PERIOD;
	public static String RESID_SECURITY_SERIAL_NUMBER_LBL                 ;
	public static String RESID_SECURITY_CERTIF_VERSION_LBL;
	public static String RESID_SECURITY_PROP_ALIAS_LBL;
	public static String RESID_SECURITY_ISSUED_TO_LBL;
	public static String RESID_SECURITY_ISSUED_BY_LBL;
	public static String RESID_SECURITY_VALIDITY_LBL;
	public static String RESID_SECURITY_SIGALG_LBL;
	public static String RESID_SECURITY_ALGORITHM_LBL;
	public static String RESID_SECURITY_SIGNATURE_LBL;
	public static String RESID_SECURITY_SUBJECT_LBL;
	public static String RESID_SECURITY_KEY_LBL;
	public static String RESID_SECURITY_PUBLIC_KEY_LBL;
	public static String RESID_SECURITY_KEY_ENTRY;
	public static String RESID_SECURITY_ADD_CERT_DLG_TITLE;
	public static String RESID_SECURITY_RENAME_CERT_DLG_TITLE;
	public static String RESID_SECURITY_CERTIFICATE_ALIAS;
	public static String RESID_SECURITY_SEC_MSG;
	public static String RESID_SECURITY_TRUSTED_CERTIFICATE;
	public static String RESID_SECURITY_CERTIFICATE_FILE;
	public static String RESID_SECURITY_BROWSE;
	public static String RESID_SECURITY_ADD_LBL;
	public static String RESID_SECURITY_REMOVE_LBL;
	public static String RESID_SECURITY_RENAME_LBL;
	public static String RESID_SECURITY_PREF_ALIAS_NAME;;
	public static String RESID_SECURITY_PREF_ISSUED_TO;;
	public static String RESID_SECURITY_PREF_ISSUED_FROM;;
	public static String RESID_SECURITY_PREF_EXPIRES;;

	public static String RESID_SECURITY_KEY_IO_ERROR_;
	public static String RESID_SECURITY_KEY_LOAD_ERROR_;
	public static String RESID_SECURITY_ALGORITHM_ERROR_;
	public static String RESID_SECURITY_KEY_MANAG_ERROR_;
	public static String RESID_SECURITY_KEY_STORE_ERROR_;
	public static String RESID_SECURITY_UNREC_KEY_ERROR_;
	public static String RESID_SECURITY_PWD_REQ_INFO_;
	public static String RESID_SECURITY_LOGIN_FAILED_INFO_;
	public static String RESID_SECURITY_KEYSTORE_SAVE_ERROR_;
	public static String RESID_SECURITY_IO_SAVE_ERROR_;
	public static String RESID_SECURITY_CERTIFICATE_STORE_ERROR_;
	public static String RESID_SECURITY_UNINIT_KEYSTORE_ERROR_;
	public static String RESID_SECURITY_INITIALIZE_ERROR_;
	public static String RESID_SECURITY_SECURITY_PROVIDER_ERROR_;
	public static String RESID_SECURITY_LOAD_KEYSTORE_ERROR_;
	public static String RESID_SECURITY_CERTIFICATE_EXC_;
	public static String RESID_SECURITY_LOAD_IO_EXC_;
	public static String RESID_SECURITY_CERTIFICATE_LOAD_EXC_;
	public static String RESID_SECURITY_PREF_SEC_DESCRIPTION;
	public static String RESID_SECURITY_PROPERTIES_LBL;
	
	public static String RESID_SECURITY_TRUST_WIZ_ALIAS_TITLE;
	public static String RESID_SECURITY_TRUST_WIZ_ALIAS_DESC;
	
	public static String RESID_SECURITY_TRUST_WIZ_CERTIFICATE_TITLE;
	public static String RESID_SECURITY_TRUST_WIZ_CERTIFICATE_DESC; 

	public static String RESID_SECURITY_TRUST_IMPORT_CERTIFICATE_WIZARD;
	public static String RESID_SECURITY_CERTIFICATE_INFORMATION;
	
	static 
	{
		// load message values from bundle file
		initializeMessages(BUNDLE_NAME, UniversalSecurityProperties.class);
	}
	
};