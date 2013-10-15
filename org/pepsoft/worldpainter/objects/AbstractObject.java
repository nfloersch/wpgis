/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.objects;

import java.io.Serializable;
import java.util.Map;

/**
 *
 * @author pepijn
 */
public abstract class AbstractObject implements WPObject {
    @Override
    @SuppressWarnings("unchecked") // Responsibility of caller
    public <T extends Serializable> T getAttribute(String key, T _default) {
        Map<String, Serializable> attributes = getAttributes();
        if ((attributes != null) && attributes.containsKey(key)) {
            return (T) attributes.get(key);
        } else {
            return _default;
        }
    }
}