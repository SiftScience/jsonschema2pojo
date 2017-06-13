/**
 * Copyright © 2010-2013 Nokia
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jsonschema2pojo.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.config;
import static org.jsonschema2pojo.integration.util.CodeGenerationHelper.generateAndCompile;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class MutableBuildersIT {

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    @SuppressWarnings("unchecked")
    public void canGenerateMutablePojoWithBuilder() throws Exception {
        ClassLoader resultsClassLoader = generateAndCompile("/schema/immutable/immutable.json", "com.example",
                config("immutable", false, "generateBuilderClasses", true, "annotationStyle", "jackson2"));

        // Check that none of the generated fields are all final.
        Class generatedType = resultsClassLoader.loadClass("com.example.Immutable");
        // assertEquals(0, Modifier.FINAL & generatedType.getDeclaredField("foo").getModifiers());
        // assertEquals(0, Modifier.FINAL & generatedType.getDeclaredField("bar").getModifiers());
        // assertEquals(0, Modifier.FINAL & generatedType.getDeclaredField("baz").getModifiers());
        // assertEquals(0, Modifier.FINAL & generatedType.getDeclaredField("qux").getModifiers());

        // Build an instance.
         Method newBuilder = generatedType.getMethod("newBuilder");
         Object builder = newBuilder.invoke(generatedType);
         Class builderType = builder.getClass();
         assertTrue(builderType.getMethod("withBar", Boolean.class).invoke(builder, true) == builder);
         builderType.getMethod("withQux", List.class).invoke(builder, Lists.newArrayList(1, 2, 3));
         builderType.getMethod("withAdditionalProperty", String.class, Object.class)
                 .invoke(builder, "hello", "world");
         Object instance = builderType.getMethod("build").invoke(builder);

         // Check that any fields with Lists as values are mutable.
        ((List<Integer>) generatedType.getMethod("getQux").invoke(instance)).add(5);
        assertEquals(((List<Integer>) generatedType.getMethod("getQux").invoke(instance)).size(), 4);

        // Check that any fields with Maps as values are mutable.
        ((Map<String, Object>) generatedType.getMethod("getAdditionalProperties").invoke(instance)).put("a", 0);
        assertEquals((long) ((Map<String, Integer>)
                generatedType.getMethod("getAdditionalProperties").invoke(instance)).get("a"), 0);

        // Check that the instance has the expected values.
        assertEquals("HELLO WORLD", generatedType.getMethod("getFoo").invoke(instance));
        assertEquals(true, generatedType.getMethod("getBar").invoke(instance));
        assertNull(generatedType.getMethod("getBaz").invoke(instance));
        assertEquals(Lists.newArrayList(1, 2, 3, 5), generatedType.getMethod("getQux").invoke(instance));

        Map<String, Object> expected = new HashMap<String, Object>();
        expected.put("hello", "world");
        expected.put("a", 0);
        assertEquals(expected, generatedType.getMethod("getAdditionalProperties").invoke(instance));

        // Check that we can copy construct a builder
        Method newCopyBuilder = generatedType.getMethod("newBuilder", generatedType);
        Object builder2 = newCopyBuilder.invoke(generatedType, instance);
        Object instance2 = builder2.getClass().getMethod("build").invoke(builder2);
        assertEquals("HELLO WORLD", generatedType.getMethod("getFoo").invoke(instance2));
        assertEquals(true, generatedType.getMethod("getBar").invoke(instance2));
        assertNull(generatedType.getMethod("getBaz").invoke(instance2));
        assertEquals(Lists.newArrayList(1, 2, 3, 5), generatedType.getMethod("getQux").invoke(instance2));

        // Check that the instance has the expected values when serialized.
        JsonNode jsonified = mapper.valueToTree(instance);
        assertEquals("HELLO WORLD", jsonified.get("foo").asText());
        assertEquals(true, jsonified.get("bar").asBoolean());
        assertNull(jsonified.get("baz"));
        assertEquals(4, jsonified.get("qux").size());
        assertEquals(1, jsonified.get("qux").get(0).asInt());
        assertEquals(2, jsonified.get("qux").get(1).asInt());
        assertEquals(3, jsonified.get("qux").get(2).asInt());
        assertEquals(5, jsonified.get("qux").get(3).asInt());
        assertEquals(42, jsonified.get("default_int").asInt());
        assertEquals("world", jsonified.get("hello").asText());
         assertEquals(0, jsonified.get("a").asInt());

        // Check that deserialization works.
        assertEquals(instance, mapper.readValue(mapper.writeValueAsString(instance), generatedType));
    }
}
