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
package org.eclipse.search.internal.ui.text;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

public class FileTableContentProvider extends FileContentProvider implements IStructuredContentProvider {
	private TableViewer fTableViewer;

	/**
	 * @param viewer	
	 */
	public FileTableContentProvider(TableViewer viewer) {
		fTableViewer= viewer;
	}
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof FileSearchResult)
			return ((FileSearchResult)inputElement).getElements();
		return EMPTY_ARR;
	}
	
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (newInput instanceof FileSearchResult) {
			fResult= (FileSearchResult) newInput;
		}
	}
	
	public void elementsChanged(Object[] updatedElements) {
		for (int i= 0; i < updatedElements.length; i++) {
			if (fResult.getMatchCount(updatedElements[i]) > 0) {
				if (fTableViewer.testFindItem(updatedElements[i]) != null)
					fTableViewer.update(updatedElements[i], null);
				else
					fTableViewer.add(updatedElements[i]);
			} else
				fTableViewer.remove(updatedElements[i]);
		}
	}

	public void clear() {
		fTableViewer.refresh();
	}
}
