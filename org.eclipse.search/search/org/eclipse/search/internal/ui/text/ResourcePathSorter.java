/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.search.internal.ui.text;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import org.eclipse.search.internal.ui.util.FileLabelProvider;
import org.eclipse.search.ui.ISearchResultView;
import org.eclipse.search.ui.ISearchResultViewEntry;
import org.eclipse.search.ui.SearchUI;

/**
 * Sorts the search result viewer by the resource path.
 */
public class ResourcePathSorter extends ViewerSorter {

	/*
	 * Overrides method from ViewerSorter
	 */
	public int compare(Viewer viewer, Object e1, Object e2) {
		String name1= null;
		String name2= null;

		if (e1 instanceof ISearchResultViewEntry)
			name1= ((ISearchResultViewEntry)e1).getResource().getFullPath().toString();
		if (e2 instanceof ISearchResultViewEntry)
			name2= ((ISearchResultViewEntry)e2).getResource().getFullPath().toString();
		if (name1 == null)
			name1= ""; //$NON-NLS-1$
		if (name2 == null)
			name2= ""; //$NON-NLS-1$
		return getCollator().compare(name1, name2);
	}

	/*
	 * Overrides method from ViewerSorter
	 */
	public boolean isSorterProperty(Object element, String property) {
		return true;
	}

	/*
	 * Overrides method from ViewerSorter
	 */
	public void sort(Viewer viewer, Object[] elements) {
		// Set label provider to show "resource - path"
		ISearchResultView view= SearchUI.getSearchResultView();
		if (view != null) {
			ILabelProvider labelProvider= view.getLabelProvider();
			if (labelProvider instanceof FileLabelProvider)
				((FileLabelProvider)labelProvider).setOrder(FileLabelProvider.SHOW_PATH_LABEL);
		}
		super.sort(viewer, elements);
	}
}
