/*
 *  Copyright 2007, Plutext Pty Ltd.
 *   
 *  This file is part of Docx4all.

    Docx4all is free software: you can redistribute it and/or modify
    it under the terms of version 3 of the GNU General Public License 
    as published by the Free Software Foundation.

    Docx4all is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License   
    along with Docx4all.  If not, see <http://www.gnu.org/licenses/>.
    
 */

package org.docx4all.ui.menu;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;

import org.docx4all.ui.main.Constants;
import org.docx4all.ui.main.ToolBarStates;
import org.docx4all.ui.main.WordMLEditor;
import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;

/**
 *	@author Jojada Tirtowidjojo - 27/11/2007
 */
public class FileMenu extends UIMenu {
	private final static FileMenu _instance = new FileMenu();
	
	/**
	 * The binding key used for this FileMenu object 
	 * when passed into scripting environment
	 */
	public final static String SCRIPT_BINDING_KEY = "fileMenu:org.docx4all.ui.menu.FileMenu";
	
	//==========
	//MENU Names
	//==========
	//Used as an argument to JMenu.setName().
	//Therefore it can be used in .properties file 
	//to configure File Menu property in the menu bar
	/**
	 * The name of JMenu object that hosts File menu in the menu bar
	 */
	public final static String FILE_MENU_NAME = "fileMenu";
	
	
	//============
	//ACTION Names
	//============
	//The string value of each action name must be the same as 
	//the method name annotated by @Action tag.
	//Action name is used to configure Menu/Button Action property
	//in .properties file and get an Action object out of 
	//Spring Application Framework
	
	/**
	 * The action name of New file menu
	 */
	public final static String NEW_FILE_ACTION_NAME = "newFile";
	
	/**
	 * The action name of Open file menu
	 */
	public final static String OPEN_FILE_ACTION_NAME = "openFile";
	
	/**
	 * The action name of Save file menu
	 */
	public final static String SAVE_FILE_ACTION_NAME = "saveFile";
	
	/**
	 * The action name of Save As file menu
	 */
	public final static String SAVE_AS_FILE_ACTION_NAME = "saveAsFile";
	
	/**
	 * The action name of Save As file menu
	 */
	public final static String SAVE_ALL_FILES_ACTION_NAME = "saveAllFiles";
	
	/**
	 * The action name of Close menu
	 */
	public final static String CLOSE_FILE_ACTION_NAME = "closeFile";
	
	/**
	 * The action name of Close All menu
	 */
	public final static String CLOSE_ALL_FILES_ACTION_NAME = "closeAllFiles";
	
	/**
	 * The action name of Exit menu
	 */
	public final static String EXIT_ACTION_NAME = "exit";
	
	private static final String[] _menuItemActionNames = {
		NEW_FILE_ACTION_NAME,
		OPEN_FILE_ACTION_NAME,
		SAVE_FILE_ACTION_NAME,
		SAVE_AS_FILE_ACTION_NAME,
		SAVE_ALL_FILES_ACTION_NAME,
		SEPARATOR_CODE,
		CLOSE_FILE_ACTION_NAME,
		CLOSE_ALL_FILES_ACTION_NAME,
		SEPARATOR_CODE,
		EXIT_ACTION_NAME
	};
	
	public static FileMenu getInstance() {
		return _instance;
	}
	
	private short _untitledFileNumber = 0;
	
	private FileMenu() {
		;//singleton
	}
	
	public String[] getMenuItemActionNames() {
		String[] names = new String[_menuItemActionNames.length];
		System.arraycopy(_menuItemActionNames, 0, names, 0, _menuItemActionNames.length);
		return names;
	}
	
	public String getMenuName() {
		return FILE_MENU_NAME;
	}
	
    protected JMenuItem createMenuItem(String actionName) {
    	JMenuItem theItem = super.createMenuItem(actionName);
    	
        WordMLEditor editor = WordMLEditor.getInstance(WordMLEditor.class);
        ToolBarStates toolbarStates = editor.getToolbarStates();
        
    	if (SAVE_FILE_ACTION_NAME.equals(actionName)
    		|| SAVE_AS_FILE_ACTION_NAME.equals(actionName)) {
    		theItem.setEnabled(false);
    		MenuItemStateManager listener = new MenuItemStateManager(theItem);
    		toolbarStates.addPropertyChangeListener(
    				ToolBarStates.DOC_DIRTY_PROPERTY_NAME, 
    				listener);
    	} else if (SAVE_ALL_FILES_ACTION_NAME.equals(actionName)) {
    		theItem.setEnabled(false);
    		MenuItemStateManager listener = new MenuItemStateManager(theItem);
    		toolbarStates.addPropertyChangeListener(
    				ToolBarStates.ALL_DOC_DIRTY_PROPERTY_NAME, 
    				listener);
    	}
    	
    	return theItem;
    }
    
	@Action public void newFile() {
        Preferences prefs = Preferences.userNodeForPackage( getClass() );
        String lastFileName = prefs.get(Constants.LAST_OPENED_FILE, "");
        
        File dir = null;
        if (lastFileName.length() > 0) {
        	dir = new File(lastFileName).getParentFile();
        } else {
        	dir = FileSystemView.getFileSystemView().getDefaultDirectory();
        }

        WordMLEditor editor = WordMLEditor.getInstance(WordMLEditor.class);
        ResourceMap rm = editor.getContext().getResourceMap(WordMLEditor.class);
        String filename = rm.getString(Constants.UNTITLED_FILE_NAME);
        if (filename == null || filename.length() == 0) {
        	filename = "Untitled";
        }
        
        File file = new File(dir, filename + (++_untitledFileNumber) + ".docx");
		editor.createInternalFrame(file);
	}
	
    @Action public void openFile(ActionEvent actionEvent) {
        Preferences prefs = Preferences.userNodeForPackage( getClass() );
        WordMLEditor editor = WordMLEditor.getInstance(WordMLEditor.class);
        
        JFileChooser chooser = new JFileChooser();
        
    	ResourceMap rm = editor.getContext().getResourceMap(WordMLEditor.class);
    	String desc = rm.getString(Constants.DOCX_FILTER_DESC);
    	if (desc == null || desc.length() == 0) {
    		desc = "Docx Files";
    	}

        FileNameExtensionFilter docxFilter = 
        	new FileNameExtensionFilter(desc, "docx");        
        chooser.setFileFilter(docxFilter);
        
        String lastFileName = prefs.get(Constants.LAST_OPENED_FILE, "");
        if (lastFileName.length() > 0) {
        	chooser.setCurrentDirectory(new File(lastFileName).getParentFile());
        }
        
        int returnVal = chooser.showOpenDialog((Component) actionEvent.getSource());
        if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			prefs.put(Constants.LAST_OPENED_FILE, file.getAbsolutePath());
			editor.createInternalFrame(file);
		}
    }

    @Action public void saveFile() {
    	//TODO: Save File Action
        WordMLEditor editor = WordMLEditor.getInstance(WordMLEditor.class);
        editor.getToolbarStates().setDocumentDirty(false);
    }

    @Action public void saveAsFile() {
    	//TODO: Save As File Action
        WordMLEditor editor = WordMLEditor.getInstance(WordMLEditor.class);
        editor.getToolbarStates().setDocumentDirty(false);
    }
    
    @Action public void saveAllFiles() {
    	//TODO: Save All Files Action
    }
    
    @Action public void closeFile() {
    	//TODO: Close File Action
    }
    
    @Action public void closeAllFiles() {
    	//TODO: Close All Files Action
    }
    
    @Action public void exit() {
        WordMLEditor editor = WordMLEditor.getInstance(WordMLEditor.class);
        editor.exit();
    }
}// FileMenu class



















