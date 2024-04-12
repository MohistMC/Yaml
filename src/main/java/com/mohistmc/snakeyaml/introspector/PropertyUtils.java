/**
 * Copyright (c) 2008, SnakeYAML
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.mohistmc.snakeyaml.introspector;

import com.mohistmc.snakeyaml.error.YAMLException;
import com.mohistmc.snakeyaml.util.PlatformFeatureDetector;
import java.beans.FeatureDescriptor;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import lombok.Getter;

public class PropertyUtils {

    private static final String TRANSIENT = "transient";
    private final Map<Class<?>, Map<String, com.mohistmc.snakeyaml.introspector.Property>> propertiesCache =
            new HashMap<>();
    private final Map<Class<?>, Set<com.mohistmc.snakeyaml.introspector.Property>> readableProperties =
            new HashMap<>();
    private final PlatformFeatureDetector platformFeatureDetector;
    private com.mohistmc.snakeyaml.introspector.BeanAccess beanAccess = com.mohistmc.snakeyaml.introspector.BeanAccess.DEFAULT;
    @Getter
    private boolean allowReadOnlyProperties = false;
    @Getter
    private boolean skipMissingProperties = false;

    public PropertyUtils() {
        this(new PlatformFeatureDetector());
    }

    PropertyUtils(PlatformFeatureDetector platformFeatureDetector) {
        this.platformFeatureDetector = platformFeatureDetector;

        /*
         * Android lacks much of java.beans (including the Introspector class, used here), because
         * java.beans classes tend to rely on java.awt, which isn't supported in the Android SDK. That
         * means we have to fall back on FIELD access only when SnakeYAML is running on the Android
         * Runtime.
         */
        if (platformFeatureDetector.isRunningOnAndroid()) {
            beanAccess = com.mohistmc.snakeyaml.introspector.BeanAccess.FIELD;
        }
    }

    protected Map<String, com.mohistmc.snakeyaml.introspector.Property> getPropertiesMap(Class<?> type, com.mohistmc.snakeyaml.introspector.BeanAccess bAccess) {
        if (propertiesCache.containsKey(type)) {
            return propertiesCache.get(type);
        }

        Map<String, com.mohistmc.snakeyaml.introspector.Property> properties = new LinkedHashMap<>();
        boolean inaccessableFieldsExist = false;
        if (bAccess == com.mohistmc.snakeyaml.introspector.BeanAccess.FIELD) {
            for (Class<?> c = type; c != null; c = c.getSuperclass()) {
                for (Field field : c.getDeclaredFields()) {
                    int modifiers = field.getModifiers();
                    if (!Modifier.isStatic(modifiers) && !Modifier.isTransient(modifiers)
                            && !properties.containsKey(field.getName())) {
                        properties.put(field.getName(), new com.mohistmc.snakeyaml.introspector.FieldProperty(field));
                    }
                }
            }
        } else {// add JavaBean properties
            try {
                for (PropertyDescriptor property : Introspector.getBeanInfo(type)
                        .getPropertyDescriptors()) {
                    Method readMethod = property.getReadMethod();
                    if ((readMethod == null || !readMethod.getName().equals("getClass"))
                            && !isTransient(property)) {
                        properties.put(property.getName(), new MethodProperty(property));
                    }
                }
            } catch (IntrospectionException e) {
                throw new YAMLException(e);
            }

            // add public fields
            for (Class<?> c = type; c != null; c = c.getSuperclass()) {
                for (Field field : c.getDeclaredFields()) {
                    int modifiers = field.getModifiers();
                    if (!Modifier.isStatic(modifiers) && !Modifier.isTransient(modifiers)) {
                        if (Modifier.isPublic(modifiers)) {
                            properties.put(field.getName(), new FieldProperty(field));
                        } else {
                            inaccessableFieldsExist = true;
                        }
                    }
                }
            }
        }
        if (properties.isEmpty() && inaccessableFieldsExist) {
            throw new YAMLException("No JavaBean properties found in " + type.getName());
        }
        propertiesCache.put(type, properties);
        return properties;
    }

    private boolean isTransient(FeatureDescriptor fd) {
        return Boolean.TRUE.equals(fd.getValue(TRANSIENT));
    }

    public Set<com.mohistmc.snakeyaml.introspector.Property> getProperties(Class<? extends Object> type) {
        return getProperties(type, beanAccess);
    }

    public Set<com.mohistmc.snakeyaml.introspector.Property> getProperties(Class<? extends Object> type, com.mohistmc.snakeyaml.introspector.BeanAccess bAccess) {
        if (readableProperties.containsKey(type)) {
            return readableProperties.get(type);
        }
        Set<com.mohistmc.snakeyaml.introspector.Property> properties = createPropertySet(type, bAccess);
        readableProperties.put(type, properties);
        return properties;
    }

    protected Set<com.mohistmc.snakeyaml.introspector.Property> createPropertySet(Class<? extends Object> type, com.mohistmc.snakeyaml.introspector.BeanAccess bAccess) {
        Set<com.mohistmc.snakeyaml.introspector.Property> properties = new TreeSet<>();
        Collection<com.mohistmc.snakeyaml.introspector.Property> props = getPropertiesMap(type, bAccess).values();
        for (com.mohistmc.snakeyaml.introspector.Property property : props) {
            if (property.isReadable() && (allowReadOnlyProperties || property.isWritable())) {
                properties.add(property);
            }
        }
        return properties;
    }

    public com.mohistmc.snakeyaml.introspector.Property getProperty(Class<? extends Object> type, String name) {
        return getProperty(type, name, beanAccess);
    }

    public com.mohistmc.snakeyaml.introspector.Property getProperty(Class<? extends Object> type, String name, com.mohistmc.snakeyaml.introspector.BeanAccess bAccess) {
        Map<String, com.mohistmc.snakeyaml.introspector.Property> properties = getPropertiesMap(type, bAccess);
        Property property = properties.get(name);
        if (property == null && skipMissingProperties) {
            property = new MissingProperty(name);
        }
        if (property == null) {
            throw new YAMLException("Unable to find property '" + name + "' on class: " + type.getName());
        }
        return property;
    }

    public void setBeanAccess(com.mohistmc.snakeyaml.introspector.BeanAccess beanAccess) {
        if (platformFeatureDetector.isRunningOnAndroid() && beanAccess != BeanAccess.FIELD) {
            throw new IllegalArgumentException("JVM is Android - only BeanAccess.FIELD is available");
        }

        if (this.beanAccess != beanAccess) {
            this.beanAccess = beanAccess;
            propertiesCache.clear();
            readableProperties.clear();
        }
    }

    public void setAllowReadOnlyProperties(boolean allowReadOnlyProperties) {
        if (this.allowReadOnlyProperties != allowReadOnlyProperties) {
            this.allowReadOnlyProperties = allowReadOnlyProperties;
            readableProperties.clear();
        }
    }

    /**
     * Skip properties that are missing during deserialization of YAML to a Java object. The default
     * is false.
     *
     * @param skipMissingProperties true if missing properties should be skipped, false otherwise.
     */
    public void setSkipMissingProperties(boolean skipMissingProperties) {
        if (this.skipMissingProperties != skipMissingProperties) {
            this.skipMissingProperties = skipMissingProperties;
            readableProperties.clear();
        }
    }

}
