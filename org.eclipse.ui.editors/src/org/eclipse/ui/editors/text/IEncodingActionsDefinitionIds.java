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
 * Defines the definition ids for the encoding actions. <p>
 * This interface contains constants only; it is not intended to be implemented.
 * @since 2.0
 */
public interface IEncodingActionsDefinitionIds  {
	/**
	 * Action definition id of the action to changes the encoding into US ASCII.
	 * Value is <code>"org.eclipse.ui.edit.text.encoding.us-ascii"</code>.
	 */
	public static final String US_ASCII= "org.eclipse.ui.edit.text.encoding.us-ascii"; //$NON-NLS-1$
	
	/**
	 *  Action definition id of the action to changes the encoding into ISO-8859-1.
	 * Value is <code>"org.eclipse.ui.edit.text.encoding.iso-8859-1"</code>.
	 */
	public static final String ISO_8859_1= "org.eclipse.ui.edit.text.encoding.iso-8859-1"; //$NON-NLS-1$
	
	/**
	 *  Action definition id of the action to changes the encoding into UTF-8.
	 * Value is <code>"org.eclipse.ui.edit.text.encoding.utf-8"</code>.
	 */
	public static final String UTF_8= "org.eclipse.ui.edit.text.encoding.utf-8"; //$NON-NLS-1$
	
	/**
	 *  Action definition id of the action to changes the encoding into UTF-16BE.
	 * Value is <code>"org.eclipse.ui.edit.text.encoding.utf-16be"</code>.
	 */
	public static final String UTF_16BE= "org.eclipse.ui.edit.text.encoding.utf-16be"; //$NON-NLS-1$
	
	/**
	 *  Action definition id of the action to changes the encoding into UTF-16LE.
	 * Value is <code>"org.eclipse.ui.edit.text.encoding.utf-16le"</code>.
	 */
	public static final String UTF_16LE= "org.eclipse.ui.edit.text.encoding.utf-16le"; //$NON-NLS-1$
	
	/**
	 *  Action definition id of the action to changes the encoding into UTF-16.
	 * Value is <code>"org.eclipse.ui.edit.text.encoding.utf-16"</code>.
	 */
	public static final String UTF_16= "org.eclipse.ui.edit.text.encoding.utf-16"; //$NON-NLS-1$
	
	/**
	 *  Action definition id of the action to changes the encoding into the system encoding.
	 * Value is <code>"org.eclipse.ui.edit.text.encoding.system"</code>.
	 */
	public static final String SYSTEM= "org.eclipse.ui.edit.text.encoding.system"; //$NON-NLS-1$
	
	/**
	 *  Action definition id of the action to changes the encoding into a custom encoding.
	 * Value is <code>"org.eclipse.ui.edit.text.encoding.custom"</code>.
	 */
	public static final String CUSTOM= "org.eclipse.ui.edit.text.encoding.custom"; //$NON-NLS-1$
}