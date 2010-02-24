/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.source;


/**
 * Extension interface for {@link org.eclipse.jface.text.source.ISourceViewer}.
 * <p>
 * It provides access to the annotation hover.
 * </p>
 * 
 * @since 3.6
 */
public interface ISourceViewerExtension5 {

	/**
	 * Returns the annotation hover if any, <code>null</code> otherwise.
	 * 
	 * @return the annotation hover or <code>null</code>
	 * @since 3.6
	 */
	IAnnotationHover getAnnotationHover();

}
