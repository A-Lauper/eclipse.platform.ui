package org.eclipse.search.internal.ui;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 1999, 2000
 */
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.util.Assert;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IPluginRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.eclipse.search.internal.ui.util.ExceptionHandler;
import org.eclipse.search.ui.ISearchResultView;
import org.eclipse.search.ui.SearchUI;

/**
 * The plug-in runtime class for Search plug-in
 */
public class SearchPlugin extends AbstractUIPlugin {
	
	private static final String RESOURCE_BUNDLE= "org.eclipse.search.internal.ui.SearchPluginResources";
	
	public static final String SEARCH_PAGE_EXTENSION_POINT= "searchPages";
	public static final String SORTER_EXTENSION_POINT= "searchResultSorters";
	
	private static SearchPlugin fgSearchPlugin;
	private static ResourceBundle fgResourceBundle= ResourceBundle.getBundle(RESOURCE_BUNDLE);
		
	private List fPageDescriptors;
	private List fSorterDescriptors;

	public SearchPlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		Assert.isTrue(fgSearchPlugin == null);
		fgSearchPlugin= this;
	}
	/**
	 * Returns the search plugin instance.
	 */
	public static SearchPlugin getDefault() {
		return fgSearchPlugin;
	}
	/**
	 * Returns the active workbench window.
	 * <code>null</code> if the active window is not a workbench window
	 */
	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		IWorkbenchWindow window= fgSearchPlugin.getWorkbench().getActiveWorkbenchWindow();
		if (window == null) {
			final WindowRef windowRef= new WindowRef();
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					setActiveWorkbenchWindow(windowRef);
				}
			});
			return windowRef.window;
		}
		else
			return window;
	}

	private static class WindowRef {
		public IWorkbenchWindow window;
	}

	private static void setActiveWorkbenchWindow(WindowRef windowRef) {
		windowRef.window= null;
		Display display= Display.getCurrent();
		if (display == null)
			return;
		Control shell= display.getActiveShell();
		while (shell != null) {
			Object data= shell.getData();
			if (data instanceof IWorkbenchWindow) {
				windowRef.window= (IWorkbenchWindow)data;
				return;
			}
			shell= shell.getParent();
		}
		Shell shells[]= display.getShells();
		for (int i= 0; i < shells.length; i++) {
			Object data= shells[i].getData();
			if (data instanceof IWorkbenchWindow) {
				windowRef.window= (IWorkbenchWindow)data;
				return;
			}
		}
	}
	/**
	 * Returns the shell of the active workbench window.
	 */
	public static Shell getActiveWorkbenchShell() {
		IWorkbenchWindow window= getActiveWorkbenchWindow();
		if (window != null)
			return window.getShell();
		return null;
	}
	/**
	 * Beeps using the display of the active workbench window.
	 */
	public static void beep() {
		getActiveWorkbenchShell().getDisplay().beep();
	}
	/**
	 * Returns the active workbench window's currrent page.
	 */
	public static IWorkbenchPage getActivePage() {
		return getActiveWorkbenchWindow().getActivePage();
	} 
	/**
	 * Returns the workbench from which this plugin has been loaded.
	 */	
	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}
	/**
	 * Returns the search plugin's resource bundle.
	 */
	public static String getResourceString(String key) {
		return fgResourceBundle.getString(key);
	}
	/**
	 * Gets the Java UI resource bundle
	 */
	public static ResourceBundle getResourceBundle() {
		return fgResourceBundle;
	}	
	/**
	 * Activates the search result view in the active page.
	 * This call has no effect, if the search result view is
	 * already activated.
	 *
	 * @return <code>true</code> if the search result view could be activated
	 */
	public static boolean activateSearchResultView() {
		try {
			return (getActivePage().showView(SearchUI.SEARCH_RESULT_VIEW_ID) != null);
		} catch (PartInitException ex) {
			ExceptionHandler.handle(ex, SearchPlugin.getResourceBundle(), "Search.Error.openResultView.");
			return false;
		}	
	}
	/**
	 * Returns the search result view of the active workbench window. Returns <code>
	 * null</code> if the active workbench window doesn't have any search result
	 * view.
	 */
	public static ISearchResultView getSearchResultView() {
		IViewPart part= getActivePage().findView(SearchUI.SEARCH_RESULT_VIEW_ID);
		if (part instanceof ISearchResultView)
			return (ISearchResultView) part;
		return null;	
	}

	static void setAutoBuilding(boolean state) {
		IWorkspaceDescription workspaceDesc= getDefault().getWorkspace().getDescription();
		workspaceDesc.setAutoBuilding(state);
		try {
			getDefault().getWorkspace().setDescription(workspaceDesc);
		}
		catch (CoreException ex) {
			ExceptionHandler.handle(ex, fgResourceBundle, "Search.Error.setDescription.");
		}
	}
	/**
	 * Shuts down this plug-in.
	 */
	public void shutdown() throws CoreException {
		super.shutdown();
		getWorkspace().removeResourceChangeListener(SearchManager.getDefault());
		fgSearchPlugin = null;
	}
	/**
	 * Returns all search pages contributed to the workbench.
	 */
	public List getSearchPageDescriptors() {
		if (fPageDescriptors == null) {
			IPluginRegistry registry= Platform.getPluginRegistry();
			IConfigurationElement[] elements= registry.getConfigurationElementsFor(SearchUI.PLUGIN_ID, SEARCH_PAGE_EXTENSION_POINT);
			fPageDescriptors= createSearchPageDescriptors(elements);
		}	
		return fPageDescriptors;
	} 

	/**
	 * Creates all necessary search page nodes.
	 */
	private List createSearchPageDescriptors(IConfigurationElement[] elements) {
		List result= new ArrayList(5);
		for (int i= 0; i < elements.length; i++) {
			IConfigurationElement element= elements[i];
			if (SearchPageDescriptor.PAGE_TAG.equals(element.getName())) {
				SearchPageDescriptor desc= new SearchPageDescriptor(element);
				result.add(desc);
			}
		}
		Collections.sort(result);
		return result;
	}
	/**
	 * Returns all sorters contributed to the workbench.
	 */
	public List getSorterDescriptors() {
		if (fSorterDescriptors == null) {
			IPluginRegistry registry= Platform.getPluginRegistry();
			IConfigurationElement[] elements= registry.getConfigurationElementsFor(SearchUI.PLUGIN_ID, SORTER_EXTENSION_POINT);
			fSorterDescriptors= createSorterDescriptors(elements);
		}	
		return fSorterDescriptors;
	} 
	/**
	 * Creates all necessary sorter description nodes.
	 */
	private List createSorterDescriptors(IConfigurationElement[] elements) {
		List result= new ArrayList(5);
		for (int i= 0; i < elements.length; i++) {
			IConfigurationElement element= elements[i];
			if (SorterDescriptor.SORTER_TAG.equals(element.getName()))
				result.add(new SorterDescriptor(element));
		}
		return result;
	}
	/**
	 * Log status to platform log
	 */	
	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}
}
