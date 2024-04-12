package com.mohistmc.yaml.file;

import com.mohistmc.yaml.serialization.ConfigurationSerialization;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.mohistmc.snakeyaml.LoaderOptions;
import com.mohistmc.snakeyaml.constructor.SafeConstructor;
import com.mohistmc.snakeyaml.error.YAMLException;
import com.mohistmc.snakeyaml.nodes.MappingNode;
import com.mohistmc.snakeyaml.nodes.Node;
import com.mohistmc.snakeyaml.nodes.Tag;

import java.util.LinkedHashMap;
import java.util.Map;

public class YamlConstructor extends SafeConstructor {

    public YamlConstructor(@NotNull LoaderOptions loaderOptions) {
        super(loaderOptions);
        this.yamlConstructors.put(Tag.MAP, new ConstructCustomObject());
    }

    @Override
    public void flattenMapping(@NotNull final MappingNode node) {
        super.flattenMapping(node);
    }

    @Nullable
    public Object construct(@NotNull Node node) {
        return constructObject(node);
    }

    private class ConstructCustomObject extends ConstructYamlMap {

        @Nullable
        @Override
        public Object construct(@NotNull Node node) {
            if (node.isTwoStepsConstruction()) {
                throw new YAMLException("Unexpected referential mapping structure. Node: " + node);
            }

            Map<?, ?> raw = (Map<?, ?>) super.construct(node);

            if (raw.containsKey(ConfigurationSerialization.SERIALIZED_TYPE_KEY)) {
                Map<String, Object> typed = new LinkedHashMap<>(raw.size());
                for (Map.Entry<?, ?> entry : raw.entrySet()) {
                    typed.put(entry.getKey().toString(), entry.getValue());
                }

                try {
                    return ConfigurationSerialization.deserializeObject(typed);
                } catch (IllegalArgumentException ex) {
                    throw new YAMLException("Could not deserialize object", ex);
                }
            }

            return raw;
        }

        @Override
        public void construct2ndStep(@NotNull Node node, @NotNull Object object) {
            throw new YAMLException("Unexpected referential mapping structure. Node: " + node);
        }
    }
}
