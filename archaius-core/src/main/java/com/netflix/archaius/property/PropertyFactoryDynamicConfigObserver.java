package com.netflix.archaius.property;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.netflix.archaius.Config;
import com.netflix.archaius.DynamicConfigObserver;
import com.netflix.archaius.PropertyContainer;
import com.netflix.archaius.PropertyFactory;

public class PropertyFactoryDynamicConfigObserver implements DynamicConfigObserver {

    private final ConcurrentMap<String, PropertyContainer> registry = new ConcurrentHashMap<String, PropertyContainer>();
    private final PropertyFactory factory;
    
    public PropertyFactoryDynamicConfigObserver(PropertyFactory factory) {
        this.factory = factory;
    }
    
    @Override
    public void onUpdate(String key, Config config) {
        PropertyContainer property = registry.get(key);
        if (property != null) {
            property.update();
        }
    }
    
    @Override
    public void onError(Throwable error, Config config) {
        // TODO
    }

    /**
     * Get the ObservableProperty for a specific property name.  The ObservableProperty
     * is cached internally.
     * 
     * @param key
     * @return
     */
    public PropertyContainer create(String key) {
        PropertyContainer container = registry.get(key);
        if (container == null) {
            container = factory.connectProperty(key);
            PropertyContainer existing = registry.putIfAbsent(key, container);
            if (existing != null) {
                return existing;
            }
        }
        
        return container;
    }

    public PropertyContainer get(String key) {
        return registry.get(key);
    }
    
    @Override
    public void onUpdate(Config config) {
        invalidate();
    }

    public void invalidate() {
        for (PropertyContainer prop : registry.values()) {
            prop.update();
        }
    }
}
