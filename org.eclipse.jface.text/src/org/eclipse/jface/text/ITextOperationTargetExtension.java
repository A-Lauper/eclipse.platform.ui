/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/

package org.eclipse.jface.text;

 
/**
 * Extension interface to <code>ITextOperationTarget</code>. Allows a client to control
 * the enable state of operations provided by this target.
 * 
 * @since 2.0
 */
public interface ITextOperationTargetExtension {
	
	/**
	 * Enables/disabled the given text operation.
	 * 
	 * @param operation the operation to enable/disable
	 * @param enable <code>true</code> to enable the operation otherwise <code>false</code>
	 */
	void enableOperation(int operation, boolean enable);
}

