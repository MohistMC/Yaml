/**
 * Copyright (c) 2008, SnakeYAML
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.mohistmc.snakeyaml.constructor;

import com.mohistmc.snakeyaml.LoaderOptions;
import com.mohistmc.snakeyaml.TypeDescription;
import com.mohistmc.snakeyaml.composer.Composer;
import com.mohistmc.snakeyaml.composer.ComposerException;
import com.mohistmc.snakeyaml.error.YAMLException;
import com.mohistmc.snakeyaml.introspector.PropertyUtils;
import com.mohistmc.snakeyaml.nodes.CollectionNode;
import com.mohistmc.snakeyaml.nodes.MappingNode;
import com.mohistmc.snakeyaml.nodes.Node;
import com.mohistmc.snakeyaml.nodes.NodeId;
import com.mohistmc.snakeyaml.nodes.NodeTuple;
import com.mohistmc.snakeyaml.nodes.ScalarNode;
import com.mohistmc.snakeyaml.nodes.SequenceNode;
import com.mohistmc.snakeyaml.nodes.Tag;
import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Base code
 */
public abstract class BaseConstructor {

  /**
   * An instance returned by newInstance methods when instantiation has not been performed.
   */
  protected static final Object NOT_INSTANTIATED_OBJECT = new Object();

  /**
   * It maps the node kind to the Construct implementation. When the runtime class is known then the
   * implicit tag is ignored.
   */
  protected final Map<NodeId, com.mohistmc.snakeyaml.constructor.Construct> yamlClassConstructors =
          new EnumMap<>(NodeId.class);
  /**
   * It maps the (explicit or implicit) tag to the Construct implementation. It is used: 1) explicit
   * tag - if present. 2) implicit tag - when the runtime class of the instance is unknown (the node
   * has the Object.class) 3) when nothing else is found the Construct for the key 'null' is chosen
   * (which is ConstructYamlObject)
   */
  protected final Map<Tag, com.mohistmc.snakeyaml.constructor.Construct> yamlConstructors = new HashMap<>();
  /**
   * It maps the (explicit or implicit) tag to the Construct implementation. It is used when no
   * exact match found. The key in the Map is checked if it starts the class name
   */
  protected final Map<String, com.mohistmc.snakeyaml.constructor.Construct> yamlMultiConstructors = new HashMap<>();

  /**
   * No graph creator
   */
  protected Composer composer;
  final Map<Node, Object> constructedObjects;
  private final Set<Node> recursiveObjects;
  private final ArrayList<RecursiveTuple<Map<Object, Object>, RecursiveTuple<Object, Object>>> maps2fill;
  private final ArrayList<RecursiveTuple<Set<Object>, Object>> sets2fill;

  /**
   * the tag for the root node
   */
  protected Tag rootTag;
  private PropertyUtils propertyUtils;
  private boolean explicitPropertyUtils;
  private boolean allowDuplicateKeys = true;
  private boolean wrappedToRootException = false;

  private boolean enumCaseSensitive = false;

  /**
   * Mapping from a class to its manager
   */
  protected final Map<Class<? extends Object>, TypeDescription> typeDefinitions;
  /**
   * register classes for tags
   */
  protected final Map<Tag, Class<? extends Object>> typeTags;

  /**
   * options
   */
  protected LoaderOptions loadingConfig;

  /**
   * Create
   *
   * @param loadingConfig - options
   */
  public BaseConstructor(LoaderOptions loadingConfig) {
    if (loadingConfig == null) {
      throw new NullPointerException("LoaderOptions must be provided.");
    }
    constructedObjects = new HashMap<>();
    recursiveObjects = new HashSet<>();
    maps2fill =
            new ArrayList<>();
    sets2fill = new ArrayList<>();
    typeDefinitions = new HashMap<>();
    typeTags = new HashMap<>();

    rootTag = null;
    explicitPropertyUtils = false;

    typeDefinitions.put(SortedMap.class,
        new TypeDescription(SortedMap.class, Tag.OMAP, TreeMap.class));
    typeDefinitions.put(SortedSet.class,
        new TypeDescription(SortedSet.class, Tag.SET, TreeSet.class));
    this.loadingConfig = loadingConfig;
  }

  public void setComposer(Composer composer) {
    this.composer = composer;
  }

  /**
   * Check if more documents available
   *
   * @return true when there are more YAML documents in the stream
   */
  public boolean checkData() {
    // If there are more documents available?
    return composer.checkNode();
  }

  /**
   * Construct and return the next document
   *
   * @return constructed instance
   */
  public Object getData() throws NoSuchElementException {
    // Construct and return the next document.
    if (!composer.checkNode()) {
      throw new NoSuchElementException("No document is available.");
    }
    Node node = composer.getNode();
    if (rootTag != null) {
      node.setTag(rootTag);
    }
    return constructDocument(node);
  }

  /**
   * Ensure that the stream contains a single document and construct it
   *
   * @param type the class of the instance being created
   * @return constructed instance
   * @throws ComposerException in case there are more documents in the stream
   */
  public Object getSingleData(Class<?> type) {
    // Ensure that the stream contains a single document and construct it
    final Node node = composer.getSingleNode();
    if (node != null && !Tag.NULL.equals(node.getTag())) {
      if (Object.class != type) {
        node.setTag(new Tag(type));
      } else if (rootTag != null) {
        node.setTag(rootTag);
      }
      return constructDocument(node);
    } else {
      com.mohistmc.snakeyaml.constructor.Construct construct = yamlConstructors.get(Tag.NULL);
      return construct.construct(node);
    }
  }

  /**
   * Construct complete YAML document. Call the second step in case of recursive structures. At the
   * end cleans all the state.
   *
   * @param node root Node
   * @return Java instance
   */
  protected final Object constructDocument(Node node) {
    try {
      Object data = constructObject(node);
      fillRecursive();
      return data;
    } catch (RuntimeException e) {
      if (wrappedToRootException && !(e instanceof YAMLException)) {
        throw new YAMLException(e);
      } else {
        throw e;
      }
    } finally {
      // clean up resources
      constructedObjects.clear();
      recursiveObjects.clear();
    }
  }

  /**
   * Fill the recursive structures and clean the internal collections
   */
  private void fillRecursive() {
    if (!maps2fill.isEmpty()) {
      for (RecursiveTuple<Map<Object, Object>, RecursiveTuple<Object, Object>> entry : maps2fill) {
        RecursiveTuple<Object, Object> key_value = entry._2();
        entry._1().put(key_value._1(), key_value._2());
      }
      maps2fill.clear();
    }
    if (!sets2fill.isEmpty()) {
      for (RecursiveTuple<Set<Object>, Object> value : sets2fill) {
        value._1().add(value._2());
      }
      sets2fill.clear();
    }
  }

  /**
   * Construct object from the specified Node. Return existing instance if the node is already
   * constructed.
   *
   * @param node Node to be constructed
   * @return Java instance
   */
  protected Object constructObject(Node node) {
    if (constructedObjects.containsKey(node)) {
      return constructedObjects.get(node);
    }
    return constructObjectNoCheck(node);
  }

  /**
   * Construct object from the specified Node without the check if it was already created.
   *
   * @param node - the source
   * @return constructed instance
   */
  protected Object constructObjectNoCheck(Node node) {
    if (recursiveObjects.contains(node)) {
      throw new ConstructorException(null, null, "found unconstructable recursive node",
          node.getStartMark());
    }
    recursiveObjects.add(node);
    com.mohistmc.snakeyaml.constructor.Construct constructor = getConstructor(node);
    Object data = (constructedObjects.containsKey(node)) ? constructedObjects.get(node)
        : constructor.construct(node);

    data = finalizeConstruction(node, data);
    constructedObjects.put(node, data);
    recursiveObjects.remove(node);
    if (node.isTwoStepsConstruction()) {
      constructor.construct2ndStep(node, data);
    }
    return data;
  }

  /**
   * Get the constructor to construct the Node. For implicit tags if the runtime class is known a
   * dedicated Construct implementation is used. Otherwise, the constructor is chosen by the tag.
   *
   * @param node {@link Node} to construct an instance from
   * @return {@link com.mohistmc.snakeyaml.constructor.Construct} implementation for the specified node
   */
  protected com.mohistmc.snakeyaml.constructor.Construct getConstructor(Node node) {
    if (node.useClassConstructor()) {
      return yamlClassConstructors.get(node.getNodeId());
    } else {
      Tag tag = node.getTag();
      Construct constructor = yamlConstructors.get(tag);
      if (constructor == null) {
        for (String prefix : yamlMultiConstructors.keySet()) {
          if (tag.startsWith(prefix)) {
            return yamlMultiConstructors.get(prefix);
          }
        }
        return yamlConstructors.get(null);
      }
      return constructor;
    }
  }

  /**
   * Create string from scalar
   *
   * @param node - the source
   * @return the data
   */
  protected String constructScalar(ScalarNode node) {
    return node.getValue();
  }

  // >>>> DEFAULTS >>>>
  protected List<Object> createDefaultList(int initSize) {
    return new ArrayList<>(initSize);
  }

  protected Set<Object> createDefaultSet(int initSize) {
    return new LinkedHashSet<>(initSize);
  }

  protected Map<Object, Object> createDefaultMap(int initSize) {
    // respect order from YAML document
    return new LinkedHashMap<>(initSize);
  }

  protected Object createArray(Class<?> type, int size) {
    return Array.newInstance(type.getComponentType(), size);
  }

  // <<<< DEFAULTS <<<<

  protected Object finalizeConstruction(Node node, Object data) {
    final Class<? extends Object> type = node.getType();
    if (typeDefinitions.containsKey(type)) {
      return typeDefinitions.get(type).finalizeConstruction(data);
    }
    return data;
  }

  // >>>> NEW instance
  protected Object newInstance(Node node) {
    return newInstance(Object.class, node);
  }

  protected final Object newInstance(Class<?> ancestor, Node node) {
    return newInstance(ancestor, node, true);
  }

  /**
   * Tries to create a new object for the node.
   *
   * @param ancestor expected ancestor of the {@code node.getType()}
   * @param node for which to create a corresponding java object
   * @param tryDefault should default constructor to be tried when there is no corresponding
   *        {@code TypeDescription} or {@code TypeDescription.newInstance(node)} returns
   *        {@code null}.
   *
   * @return - a new object created for {@code node.getType()} by using corresponding
   *         TypeDescription.newInstance or default constructor. - {@code NOT_INSTANTIATED_OBJECT}
   *         in case no object has been created
   */
  protected Object newInstance(Class<?> ancestor, Node node, boolean tryDefault) {
    try {
      final Class<? extends Object> type = node.getType();
      if (typeDefinitions.containsKey(type)) {
        TypeDescription td = typeDefinitions.get(type);
        final Object instance = td.newInstance(node);
        if (instance != null) {
          return instance;
        }
      }

      if (tryDefault) {
        /*
         * Removed <code> have InstantiationException in case of abstract type
         */
        if (ancestor.isAssignableFrom(type) && !Modifier.isAbstract(type.getModifiers())) {
          java.lang.reflect.Constructor<?> c = type.getDeclaredConstructor();
          c.setAccessible(true);
          return c.newInstance();
        }
      }
    } catch (Exception e) {
      throw new YAMLException(e);
    }

    return NOT_INSTANTIATED_OBJECT;
  }

  @SuppressWarnings("unchecked")
  protected Set<Object> newSet(CollectionNode<?> node) {
    Object instance = newInstance(Set.class, node);
    if (instance != NOT_INSTANTIATED_OBJECT) {
      return (Set<Object>) instance;
    } else {
      return createDefaultSet(node.getValue().size());
    }
  }

  @SuppressWarnings("unchecked")
  protected List<Object> newList(SequenceNode node) {
    Object instance = newInstance(List.class, node);
    if (instance != NOT_INSTANTIATED_OBJECT) {
      return (List<Object>) instance;
    } else {
      return createDefaultList(node.getValue().size());
    }
  }

  @SuppressWarnings("unchecked")
  protected Map<Object, Object> newMap(MappingNode node) {
    Object instance = newInstance(Map.class, node);
    if (instance != NOT_INSTANTIATED_OBJECT) {
      return (Map<Object, Object>) instance;
    } else {
      return createDefaultMap(node.getValue().size());
    }
  }

  // <<<< NEW instance

  // >>>> Construct => NEW, 2ndStep(filling)

  /**
   * Create List and fill it with data
   *
   * @param node - the source
   * @return filled List
   */
  protected List<? extends Object> constructSequence(SequenceNode node) {
    List<Object> result = newList(node);
    constructSequenceStep2(node, result);
    return result;
  }

  /**
   * create Set from sequence
   *
   * @param node - sequence
   * @return constructed Set
   */
  protected Set<? extends Object> constructSet(SequenceNode node) {
    Set<Object> result = newSet(node);
    constructSequenceStep2(node, result);
    return result;
  }

  /**
   * Create array from sequence
   *
   * @param node - sequence
   * @return constructed array
   */
  protected Object constructArray(SequenceNode node) {
    return constructArrayStep2(node, createArray(node.getType(), node.getValue().size()));
  }

  /**
   * Fill the provided collection with the data from the Node
   *
   * @param node - the source
   * @param collection - data to fill
   */
  protected void constructSequenceStep2(SequenceNode node, Collection<Object> collection) {
    for (Node child : node.getValue()) {
      collection.add(constructObject(child));
    }
  }

  /**
   * Fill array from node
   *
   * @param node - the source
   * @param array - the destination
   * @return filled array
   */
  protected Object constructArrayStep2(SequenceNode node, Object array) {
    final Class<?> componentType = node.getType().getComponentType();

    int index = 0;
    for (Node child : node.getValue()) {
      // Handle multi-dimensional arrays...
      if (child.getType() == Object.class) {
        child.setType(componentType);
      }

      final Object value = constructObject(child);

      if (componentType.isPrimitive()) {
        // Null values are disallowed for primitives
        if (value == null) {
          throw new NullPointerException("Unable to construct element value for " + child);
        }

        // Primitive arrays require quite a lot of work.
        if (byte.class.equals(componentType)) {
          Array.setByte(array, index, ((Number) value).byteValue());

        } else if (short.class.equals(componentType)) {
          Array.setShort(array, index, ((Number) value).shortValue());

        } else if (int.class.equals(componentType)) {
          Array.setInt(array, index, ((Number) value).intValue());

        } else if (long.class.equals(componentType)) {
          Array.setLong(array, index, ((Number) value).longValue());

        } else if (float.class.equals(componentType)) {
          Array.setFloat(array, index, ((Number) value).floatValue());

        } else if (double.class.equals(componentType)) {
          Array.setDouble(array, index, ((Number) value).doubleValue());

        } else if (char.class.equals(componentType)) {
          Array.setChar(array, index, (Character) value);

        } else if (boolean.class.equals(componentType)) {
          Array.setBoolean(array, index, (Boolean) value);

        } else {
          throw new YAMLException("unexpected primitive type");
        }

      } else {
        // Non-primitive arrays can simply be assigned:
        Array.set(array, index, value);
      }

      ++index;
    }
    return array;
  }

  /**
   * Create Set from mapping
   *
   * @param node - mapping
   * @return constructed Set
   */
  protected Set<Object> constructSet(MappingNode node) {
    final Set<Object> set = newSet(node);
    constructSet2ndStep(node, set);
    return set;
  }

  /**
   * Create Map from mapping
   *
   * @param node - mapping
   * @return constructed Map
   */
  protected Map<Object, Object> constructMapping(MappingNode node) {
    final Map<Object, Object> mapping = newMap(node);
    constructMapping2ndStep(node, mapping);
    return mapping;
  }

  /**
   * Fill provided Map with constructed data
   *
   * @param node - source
   * @param mapping - map to fill
   */
  protected void constructMapping2ndStep(MappingNode node, Map<Object, Object> mapping) {
    List<NodeTuple> nodeValue = node.getValue();
    for (NodeTuple tuple : nodeValue) {
      Node keyNode = tuple.keyNode();
      Node valueNode = tuple.valueNode();
      Object key = constructObject(keyNode);
      if (key != null) {
        try {
          key.hashCode();// check circular dependencies
        } catch (Exception e) {
          throw new ConstructorException("while constructing a mapping", node.getStartMark(),
              "found unacceptable key " + key, tuple.keyNode().getStartMark(), e);
        }
      }
      Object value = constructObject(valueNode);
      if (keyNode.isTwoStepsConstruction()) {
        if (loadingConfig.getAllowRecursiveKeys()) {
          postponeMapFilling(mapping, key, value);
        } else {
          throw new YAMLException(
              "Recursive key for mapping is detected but it is not configured to be allowed.");
        }
      } else {
        mapping.put(key, value);
      }
    }
  }

  /*
   * if keyObject is created it 2 steps we should postpone putting it in map because it may have
   * different hash after initialization compared to clean just created one. And map of course does
   * not observe key hashCode changes.
   */
  protected void postponeMapFilling(Map<Object, Object> mapping, Object key, Object value) {
    maps2fill.add(0, new RecursiveTuple(mapping, new RecursiveTuple(key, value)));
  }

  protected void constructSet2ndStep(MappingNode node, Set<Object> set) {
    List<NodeTuple> nodeValue = node.getValue();
    for (NodeTuple tuple : nodeValue) {
      Node keyNode = tuple.keyNode();
      Object key = constructObject(keyNode);
      if (key != null) {
        try {
          key.hashCode();// check circular dependencies
        } catch (Exception e) {
          throw new ConstructorException("while constructing a Set", node.getStartMark(),
              "found unacceptable key " + key, tuple.keyNode().getStartMark(), e);
        }
      }
      if (keyNode.isTwoStepsConstruction()) {
        postponeSetFilling(set, key);
      } else {
        set.add(key);
      }
    }
  }

  /*
   * if keyObject is created it 2 steps we should postpone putting it into the set because it may
   * have different hash after initialization compared to clean just created one. And set of course
   * does not observe value hashCode changes.
   */
  protected void postponeSetFilling(Set<Object> set, Object key) {
    sets2fill.add(0, new RecursiveTuple<>(set, key));
  }

  public void setPropertyUtils(PropertyUtils propertyUtils) {
    this.propertyUtils = propertyUtils;
    explicitPropertyUtils = true;
    Collection<TypeDescription> tds = typeDefinitions.values();
    for (TypeDescription typeDescription : tds) {
      typeDescription.setPropertyUtils(propertyUtils);
    }
  }

  public final PropertyUtils getPropertyUtils() {
    if (propertyUtils == null) {
      propertyUtils = new PropertyUtils();
    }
    return propertyUtils;
  }

  /**
   * Make YAML aware how to parse a custom Class. If there is no root Class assigned in constructor
   * then the 'root' property of this definition is respected.
   *
   * @param definition to be added to the Constructor
   * @return the previous value associated with <code>definition</code>, or <code>null</code> if
   *         there was no mapping for <code>definition</code>.
   */
  public TypeDescription addTypeDescription(TypeDescription definition) {
    if (definition == null) {
      throw new NullPointerException("TypeDescription is required.");
    }
    Tag tag = definition.getTag();
    typeTags.put(tag, definition.getType());
    definition.setPropertyUtils(getPropertyUtils());
    return typeDefinitions.put(definition.getType(), definition);
  }

  private record RecursiveTuple<T, K>(T _1, K _2) {
  }

  public final boolean isExplicitPropertyUtils() {
    return explicitPropertyUtils;
  }

  public boolean isAllowDuplicateKeys() {
    return allowDuplicateKeys;
  }

  public void setAllowDuplicateKeys(boolean allowDuplicateKeys) {
    this.allowDuplicateKeys = allowDuplicateKeys;
  }

  public boolean isWrappedToRootException() {
    return wrappedToRootException;
  }

  public void setWrappedToRootException(boolean wrappedToRootException) {
    this.wrappedToRootException = wrappedToRootException;
  }

  public boolean isEnumCaseSensitive() {
    return enumCaseSensitive;
  }

  public void setEnumCaseSensitive(boolean enumCaseSensitive) {
    this.enumCaseSensitive = enumCaseSensitive;
  }

  public LoaderOptions getLoadingConfig() {
    return loadingConfig;
  }
}
