/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

/**
 *
 * @author pepijn
 */
public class CustomCLObjectInputStream extends ObjectInputStream {
    public CustomCLObjectInputStream(InputStream in, ClassLoader classLoader) throws IOException {
        super(in);
        this.classLoader = classLoader;
    }

    @Override
    protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
        try {
            return Class.forName(desc.getName(), false, classLoader);
        } catch (ClassNotFoundException e) {
            return super.resolveClass(desc);
        }
    }
    
    private final ClassLoader classLoader;
}