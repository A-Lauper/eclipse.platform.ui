/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.search.internal.ui;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.viewers.ViewerSorter;

import org.eclipse.ui.IMemento;

/**
 * Drop down action that holds the currently registered sort actions.
 */
class SortDropDownAction extends Action implements IMenuCreator {

	// Persistance tags.
	private static final String TAG_SORTERS= "sorters"; //$NON-NLS-1$
	private static final String TAG_DEFAULT_SORTERS= "defaultSorters"; //$NON-NLS-1$	
	private static final String TAG_ELEMENT= "element"; //$NON-NLS-1$	
	private static final String TAG_PAGE_ID= "pageId"; //$NON-NLS-1$
	private static final String TAG_SORTER_ID= "sorterId"; //$NON-NLS-1$

	private static Map fgLastCheckedForType= new HashMap(5);;

	private SearchResultViewer fViewer;
	private String fPageId;
	private Menu fMenu;
	private String fCheckedId;
	private Map fLastCheckedForType;

	public SortDropDownAction(SearchResultViewer viewer) {
		super(SearchMessages.getString("SortDropDownAction.label")); //$NON-NLS-1$
		SearchPluginImages.setImageDescriptors(this, SearchPluginImages.T_LCL, SearchPluginImages.IMG_LCL_SEARCH_SORT);
		fViewer= viewer;
		setToolTipText(SearchMessages.getString("SortDropDownAction.tooltip")); //$NON-NLS-1$
		setMenuCreator(this);
		fLastCheckedForType= new HashMap(5);		
	}

	public void dispose() {
		fViewer= null;
		fPageId= null;
		fLastCheckedForType= null;
	}

	public Menu getMenu(Control parent) {
		return null;
	}

	void setPageId(String pageId) {
		fPageId= pageId;
		SorterDescriptor sorterDesc= (SorterDescriptor)fLastCheckedForType.get(pageId);
		if (sorterDesc == null)
			sorterDesc= (SorterDescriptor)fgLastCheckedForType.get(pageId);
		if (sorterDesc == null)
			sorterDesc= findSorter(fPageId);
		if (sorterDesc != null) {
			setChecked(sorterDesc);
			fViewer.setSorter(sorterDesc.createObject());
		}
	}

	public Menu getMenu(final Menu parent) {
		boolean hasEntries= false;
		Menu menu= new Menu(parent);
		Iterator iter= SearchPlugin.getDefault().getSorterDescriptors().iterator();
		while (iter.hasNext()) {
			Object value= fLastCheckedForType.get(fPageId);
			final String checkedId;
			if (value instanceof SorterDescriptor)
				checkedId= ((SorterDescriptor)value).getId();
			else
				checkedId= ""; //$NON-NLS-1$
			
			final SorterDescriptor sorterDesc= (SorterDescriptor) iter.next();
			if (!sorterDesc.getPageId().equals(fPageId) && !sorterDesc.getPageId().equals("*")) //$NON-NLS-1$
				continue;
			final ViewerSorter sorter= sorterDesc.createObject();
			if (sorter != null) {
				final Action action= new Action() {
					public void run() {
						if (!checkedId.equals(sorterDesc.getId())) {
							setChecked(sorterDesc);
							BusyIndicator.showWhile(parent.getDisplay(), new Runnable() {
								public void run() {
									fViewer.setSorter(sorter);
								}
							});
						}
					}
				};
				action.setText(sorterDesc.getLabel());
				action.setImageDescriptor(sorterDesc.getImage());
				action.setToolTipText(sorterDesc.getToolTipText());
				action.setChecked(checkedId.equals(sorterDesc.getId()));
				addActionToMenu(menu, action);
				hasEntries= true;
			}
		}
		setEnabled(hasEntries);
		return menu;
	}

	protected void addActionToMenu(Menu parent, Action action) {
		ActionContributionItem item= new ActionContributionItem(action);
		item.fill(parent, -1);
	}

    public void run() {
		// nothing to do
	    }

	private SorterDescriptor findSorter(String pageId) {
		Iterator iter= SearchPlugin.getDefault().getSorterDescriptors().iterator();
		while (iter.hasNext()) {
			SorterDescriptor sorterDesc= (SorterDescriptor)iter.next();
			if (sorterDesc.getPageId().equals(pageId) || sorterDesc.getPageId().equals("*")) //$NON-NLS-1$
				return sorterDesc;
		}
		return null;
	}

	private SorterDescriptor getSorter(String sorterId) {
		Iterator iter= SearchPlugin.getDefault().getSorterDescriptors().iterator();
		while (iter.hasNext()) {
			SorterDescriptor sorterDesc= (SorterDescriptor)iter.next();
			if (sorterDesc.getId().equals(sorterId))
				return sorterDesc;
		}
		return null;
	}

	private void setChecked(SorterDescriptor sorterDesc) {
		fLastCheckedForType.put(fPageId, sorterDesc);
		fgLastCheckedForType.put(fPageId, sorterDesc);
	}

	//--- Persistency -------------------------------------------------
	
	void restoreState(IMemento memento) {
		if (fLastCheckedForType.isEmpty())
			restoreState(memento, fLastCheckedForType, TAG_SORTERS);
		if (fgLastCheckedForType.isEmpty())
			restoreState(memento, fgLastCheckedForType, TAG_DEFAULT_SORTERS);
	}

	private void restoreState(IMemento memento, Map map, String mapName) {
		memento= memento.getChild(mapName);
		if (memento == null)
			return;
		IMemento[] mementoElements= memento.getChildren(TAG_ELEMENT);
		for (int i= 0; i < mementoElements.length; i++) {
			String pageId= mementoElements[i].getString(TAG_PAGE_ID);
			String sorterId= mementoElements[i].getString(TAG_SORTER_ID);
			SorterDescriptor sorterDesc= getSorter(sorterId);
			if (sorterDesc != null)
				map.put(pageId, sorterDesc);
		}
	}
	
	void saveState(IMemento memento) {
		saveState(memento, fgLastCheckedForType, TAG_DEFAULT_SORTERS);
		saveState(memento, fLastCheckedForType, TAG_SORTERS);
	}
	
	private void saveState(IMemento memento, Map map, String mapName) {
		Iterator iter= map.entrySet().iterator();
		memento= memento.createChild(mapName);
		while (iter.hasNext()) {
			IMemento mementoElement= memento.createChild(TAG_ELEMENT);
			Map.Entry entry= (Map.Entry)iter.next();
			mementoElement.putString(TAG_PAGE_ID, (String)entry.getKey());
			mementoElement.putString(TAG_SORTER_ID, ((SorterDescriptor)entry.getValue()).getId());
		}
	}
}
