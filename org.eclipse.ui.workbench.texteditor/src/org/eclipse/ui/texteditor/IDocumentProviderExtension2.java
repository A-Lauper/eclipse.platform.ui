/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.texteditor;


import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Extension interface for <code>IDocumentProvider</code>. It adds the following
 * functions:
 * <ul>
 * <li> global temporary progress monitor
 * </ul>
 * @since 2.1
 */
public interface IDocumentProviderExtension2 {
	
	/**
	 * Sets this providers progress monitor.
	 * @param progressMonitor
	 */
	void setProgressMonitor(IProgressMonitor progressMonitor);
	
	/**
	 * Returns this providers progress monitor.
	 * @return IProgressMonitor
	 */
	IProgressMonitor getProgressMonitor();
}
