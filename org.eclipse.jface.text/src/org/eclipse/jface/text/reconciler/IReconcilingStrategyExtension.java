/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/

package org.eclipse.jface.text.reconciler;


import org.eclipse.core.runtime.IProgressMonitor;

 
/**
 * Extension interface for <code>IReconcilingStrategy</code>.
 * The new functions are:
 * <ul>
 * <li> usage of a progress monitor
 * <li> initial reconciling step: If a reconciler runs as periodic activity in the background, this
 * 		methods offers the reconciler a chance for initializing its startegies and achieving a 
 * 		reconciled state before the periodic activity starts.
 * </ul>
 * 
 * @since 2.0
 */
public interface IReconcilingStrategyExtension {

	/**
	 * Tells this reconciling strategy with which progress monitor
	 * it will work. This method will be called before any other 
	 * method and can be called multiple times.
	 *
	 * @param monitor the progress monitor with which this strategy will work
	 */
	void setProgressMonitor(IProgressMonitor monitor);
	
	/**
	 * Called only once in the life time of this reconciling strategy.
	 */
	void initialReconcile();
}
