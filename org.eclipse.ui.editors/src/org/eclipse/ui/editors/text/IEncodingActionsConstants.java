/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/

package org.eclipse.ui.editors.text;

/**
 * Defines the names of the encoding actions.<p>
 * This interface contains constants only; it is not intended to be implemented.
 * @since 2.0
 */
public interface IEncodingActionsConstants {
	
	/**
	 * Name of the action to changes the encoding into US ASCII.
	 * Value is <code>"US-ASCII"</code>.
	 */
	static final String US_ASCII= "US-ASCII"; //$NON-NLS-1$
	
	/**
	 * Name of the action to changes the encoding into ISO-8859-1.
	 * Value is <code>"ISO-8859-1"</code>.
	 */
	static final String ISO_8859_1= "ISO-8859-1"; //$NON-NLS-1$
	
	/**
	 * Name of the action to changes the encoding into UTF-8.
	 * Value is <code>"UTF-8"</code>.
	 */
	static final String UTF_8= "UTF-8"; //$NON-NLS-1$
	
	/**
	 * Name of the action to changes the encoding into UTF-16BE.
	 * Value is <code>"UTF-16BE"</code>.
	 */
	static final String UTF_16BE= "UTF-16BE"; //$NON-NLS-1$
	
	/**
	 * Name of the action to changes the encoding into UTF-16LE.
	 * Value is <code>"UTF-16LE"</code>.
	 */
	static final String UTF_16LE= "UTF-16LE"; //$NON-NLS-1$
	
	/**
	 * Name of the action to changes the encoding into UTF-16.
	 * Value is <code>"UTF-16"</code>.
	 */
	static final String UTF_16= "UTF-16"; //$NON-NLS-1$
	
	/**
	 * Name of the action to changes the encoding into the system encoding.
	 * Value is <code>"System"</code>.
	 */
	static final String SYSTEM= "System"; //$NON-NLS-1$
	
	/**
	 * Name of the action to changes the encoding into a custom encoding.
	 * Value is <code>"Custom"</code>.
	 */
	static final String CUSTOM= "Custom"; //$NON-NLS-1$
}