/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.elhan.cphunter;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

/**
 *
 * @author alperen
 */
public class FileList extends Observable{
    
    private ArrayList<String> _files;
    public FileList() {
        _files = new ArrayList<String>();
    }
    
    public void add(String element) {
        _files.add(element);
        setChanged();
        notifyObservers(element);
    }   
    
}
