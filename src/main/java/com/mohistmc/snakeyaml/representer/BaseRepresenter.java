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
package com.mohistmc.snakeyaml.representer;

import com.mohistmc.snakeyaml.DumperOptions;
import com.mohistmc.snakeyaml.DumperOptions.FlowStyle;
import com.mohistmc.snakeyaml.DumperOptions.ScalarStyle;
import com.mohistmc.snakeyaml.introspector.PropertyUtils;
import com.mohistmc.snakeyaml.nodes.AnchorNode;
import com.mohistmc.snakeyaml.nodes.MappingNode;
import com.mohistmc.snakeyaml.nodes.Node;
import com.mohistmc.snakeyaml.nodes.NodeTuple;
import com.mohistmc.snakeyaml.nodes.ScalarNode;
import com.mohistmc.snakeyaml.nodes.SequenceNode;
import com.mohistmc.snakeyaml.nodes.Tag;
import java.io.Serial;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

/**
 * Represent basic YAML structures: scalar, sequence, mapping
 */
public abstract class BaseRepresenter {

    /**
     * represent the class without its subclasses
     */
    protected final Map<Class<?>, com.mohistmc.snakeyaml.representer.Represent> representers = new HashMap<>();
    /**
     * represent class and its children with common code
     */
    protected final Map<Class<?>, com.mohistmc.snakeyaml.representer.Represent> multiRepresenters =
            new LinkedHashMap<>();
    // the order is important (map can be also a sequence of key-values)
    /**
     * Keep references of already represented instances
     */
    protected final Map<Object, Node> representedObjects = new IdentityHashMap<>() {
        @Serial
        private static final long serialVersionUID = -5576159264232131854L;

        public Node put(Object key, Node value) {
            return super.put(key, new AnchorNode(value));
        }
    };
    /**
     * in Java 'null' is not a type. So we have to keep the null representer separately otherwise it
     * will coincide with the default representer which is stored with the key null.
     */
    protected com.mohistmc.snakeyaml.representer.Represent nullRepresenter;
    /**
     * default scalar style is PLAIN
     */
    @Setter
    protected DumperOptions.ScalarStyle defaultScalarStyle = ScalarStyle.PLAIN;
    /**
     * flow style to use if not redefined.
     * -- GETTER --
     *  getter
     *
     * @return current flow style

     */
    @Setter
    @Getter
    protected FlowStyle defaultFlowStyle = FlowStyle.AUTO;
    /**
     * object to create the Node for
     */
    protected Object objectToRepresent;
    private PropertyUtils propertyUtils;
    private boolean explicitPropertyUtils = false;

    public Node represent(Object data) {
        Node node = representData(data);
        representedObjects.clear();
        objectToRepresent = null;
        return node;
    }

    protected final Node representData(Object data) {
        objectToRepresent = data;
        // check for identity
        if (representedObjects.containsKey(objectToRepresent)) {
            return representedObjects.get(objectToRepresent);
        }

        // check for null first
        if (data == null) {
            return nullRepresenter.representData(null);
        }
        // check the same class
        Node node;
        Class<?> clazz = data.getClass();
        if (representers.containsKey(clazz)) {
            com.mohistmc.snakeyaml.representer.Represent representer = representers.get(clazz);
            node = representer.representData(data);
        } else {
            // check the parents
            for (Class<?> repr : multiRepresenters.keySet()) {
                if (repr != null && repr.isInstance(data)) {
                    com.mohistmc.snakeyaml.representer.Represent representer = multiRepresenters.get(repr);
                    node = representer.representData(data);
                    return node;
                }
            }

            // check defaults
            if (multiRepresenters.containsKey(null)) {
                com.mohistmc.snakeyaml.representer.Represent representer = multiRepresenters.get(null);
                node = representer.representData(data);
            } else {
                Represent representer = representers.get(null);
                node = representer.representData(data);
            }
        }
        return node;
    }

    protected Node representScalar(Tag tag, String value, DumperOptions.ScalarStyle style) {
        if (style == null) {
            style = this.defaultScalarStyle;
        }
        return new ScalarNode(tag, value, null, null, style);
    }

    protected Node representScalar(Tag tag, String value) {
        return representScalar(tag, value, this.defaultScalarStyle);
    }

    protected Node representSequence(Tag tag, Iterable<?> sequence,
                                     DumperOptions.FlowStyle flowStyle) {
        int size = 10;// default for ArrayList
        if (sequence instanceof List<?>) {
            size = ((List<?>) sequence).size();
        }
        List<Node> value = new ArrayList<>(size);
        SequenceNode node = new SequenceNode(tag, value, flowStyle);
        representedObjects.put(objectToRepresent, node);
        DumperOptions.FlowStyle bestStyle = FlowStyle.FLOW;
        for (Object item : sequence) {
            Node nodeItem = representData(item);
            if (!(nodeItem instanceof ScalarNode && ((ScalarNode) nodeItem).isPlain())) {
                bestStyle = FlowStyle.BLOCK;
            }
            value.add(nodeItem);
        }
        if (flowStyle == FlowStyle.AUTO) {
            if (defaultFlowStyle != FlowStyle.AUTO) {
                node.setFlowStyle(defaultFlowStyle);
            } else {
                node.setFlowStyle(bestStyle);
            }
        }
        return node;
    }

    protected Node representMapping(Tag tag, Map<?, ?> mapping, DumperOptions.FlowStyle flowStyle) {
        List<NodeTuple> value = new ArrayList<>(mapping.size());
        MappingNode node = new MappingNode(tag, value, flowStyle);
        representedObjects.put(objectToRepresent, node);
        DumperOptions.FlowStyle bestStyle = FlowStyle.FLOW;
        for (Map.Entry<?, ?> entry : mapping.entrySet()) {
            Node nodeKey = representData(entry.getKey());
            Node nodeValue = representData(entry.getValue());
            if (!(nodeKey instanceof ScalarNode && ((ScalarNode) nodeKey).isPlain())) {
                bestStyle = FlowStyle.BLOCK;
            }
            if (!(nodeValue instanceof ScalarNode && ((ScalarNode) nodeValue).isPlain())) {
                bestStyle = FlowStyle.BLOCK;
            }
            value.add(new NodeTuple(nodeKey, nodeValue));
        }
        if (flowStyle == FlowStyle.AUTO) {
            if (defaultFlowStyle != FlowStyle.AUTO) {
                node.setFlowStyle(defaultFlowStyle);
            } else {
                node.setFlowStyle(bestStyle);
            }
        }
        return node;
    }

    /**
     * getter
     *
     * @return scala style
     */
    public ScalarStyle getDefaultScalarStyle() {
        if (defaultScalarStyle == null) {
            return ScalarStyle.PLAIN;
        }
        return defaultScalarStyle;
    }

    /**
     * getter
     *
     * @return utils or create if null
     */
    public final PropertyUtils getPropertyUtils() {
        if (propertyUtils == null) {
            propertyUtils = new PropertyUtils();
        }
        return propertyUtils;
    }

    public void setPropertyUtils(PropertyUtils propertyUtils) {
        this.propertyUtils = propertyUtils;
        this.explicitPropertyUtils = true;
    }

    public final boolean isExplicitPropertyUtils() {
        return explicitPropertyUtils;
    }
}
