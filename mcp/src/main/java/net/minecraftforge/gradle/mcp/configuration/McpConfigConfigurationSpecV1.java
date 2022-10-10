/*
 * ForgeGradle
 * Copyright (C) 2018 Forge Development LLC
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 */

package net.minecraftforge.gradle.mcp.configuration;

import com.google.gson.*;
import net.minecraftforge.gradle.common.config.VersionedConfiguration;
import net.minecraftforge.gradle.common.util.Utils;

import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.Optional;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

public class McpConfigConfigurationSpecV1 extends VersionedConfiguration {
    protected static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(McpConfigConfigurationSpecV1.Step.class, new McpConfigConfigurationSpecV1.Step.Deserializer())
            .setPrettyPrinting().create();

    public static McpConfigConfigurationSpecV1 get(InputStream stream) {
        return GSON.fromJson(new InputStreamReader(stream), McpConfigConfigurationSpecV1.class);
    }
    public static McpConfigConfigurationSpecV1 get(byte[] data) {
        return get(new ByteArrayInputStream(data));
    }

    protected String version; // Minecraft version
    @Nullable
    protected Map<String, Object> data;
    @Nullable
    protected Map<String, List<Step>> steps;
    @Nullable
    protected Map<String, Function> functions;
    @Nullable
    protected Map<String, List<String>> libraries;

    @Input
    public String getVersion() {
        return version;
    }

    @Nested
    @Optional
    public Map<String, Object> getData() {
        return data == null ? Collections.emptyMap() : data;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public String getData(String... path) {
        if (data == null)
            return null;
        Map<String, Object> level = data;
        for (String part : path) {
            if (!level.containsKey(part))
                return null;
            Object val = level.get(part);
            if (val instanceof String)
                return (String)val;
            if (val instanceof Map)
                level = (Map<String, Object>)val;
        }
        return null;
    }

    @Optional
    @Nested
    @Nullable
    public Map<String, List<Step>> getSteps() {
        return steps;
    }

    public List<Step> getSteps(String side) {
        List<Step> ret = steps == null ? null : steps.get(side);
        return ret == null ? Collections.emptyList() : ret;
    }

    @Nullable
    public Function getFunction(String name) {
        return functions == null ? null : functions.get(name);
    }

    @Nested
    @Optional
    public Map<String, Function> getFunctions() {
        return functions == null ? Collections.emptyMap() : functions;
    }

    public List<String> getLibraries(String side) {
        List<String> ret = libraries == null ? null : libraries.get(side);
        return ret == null ? Collections.emptyList() : ret;
    }

    @Nested
    @Optional
    @Nullable
    public Map<String, List<String>> getLibraries() {
        return libraries;
    }

    public static class Step {
        private final String type;
        private final String name;
        @Nullable
        private final Map<String, String> values;

        private Step(String type, String name, @Nullable Map<String, String> values) {
            this.type = type;
            this.name = name;
            this.values = values;
        }

        @Input
        public String getType() {
            return type;
        }

        @Input
        public String getName() {
            return name;
        }

        @Nested
        public Map<String, String> getValues() {
            return values == null ? Collections.emptyMap() : values;
        }

        @Nullable
        public String getValue(String key) {
            return values == null ? null : values.get(key);
        }

        public static class Deserializer implements JsonDeserializer<Step> {
            @Override
            public Step deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                JsonObject obj = json.getAsJsonObject();
                if (!obj.has("type"))
                    throw new JsonParseException("Could not parse step: Missing 'type'");
                String type = obj.get("type").getAsString();
                String name = obj.has("name") ? obj.get("name").getAsString() : type;
                Map<String, String> values = obj.entrySet().stream()
                        .filter(e -> !"type".equals(e.getKey()) && !"name".equals(e.getKey()))
                        .collect(Collectors.toMap(Entry::getKey, e -> e.getValue().getAsString()));
                return new Step(type, name, values);
            }
        }
    }

    public static class Function {
        protected String version; //Maven artifact for the jar to run
        @Nullable
        protected String repo; //Maven repo to download the jar from
        @Nullable
        protected List<String> args;
        @Nullable
        protected List<String> jvmargs;

        @Input
        public String getVersion() {
            return version;
        }
        public void setVersion(String value) {
            this.version = value;
        }

        @Input
        @Optional
        public String getRepo() {
            return repo == null ? Utils.MOJANG_MAVEN : repo;
        }
        public void setRepo(String value) {
            this.repo = value;
        }

        @Input
        @Optional
        public List<String> getArgs() {
            return args == null ? Collections.emptyList() : args;
        }
        public void setArgs(List<String> value) {
            this.args = value;
        }

        @Input
        @Optional
        public List<String> getJvmArgs() {
            return jvmargs == null ? Collections.emptyList() : jvmargs;
        }
        public void setJvmArgs(List<String> value) {
            this.jvmargs = value;
        }
    }
}
