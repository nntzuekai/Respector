/*
 * Copyright 2018 Confluent Inc.
 *
 * Licensed under the Confluent Community License (the "License"); you may not use
 * this file except in compliance with the License.  You may obtain a copy of the
 * License at
 *
 * http://www.confluent.io/confluent-community-license
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */

package io.confluent.kafkarest.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.DynamicMessage;
import io.confluent.kafka.schemaregistry.protobuf.ProtobufSchema;
import io.confluent.kafkarest.converters.ProtobufConverter;
import io.confluent.kafkarest.converters.SchemaConverter;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

public class ProtobufConverterTest {

  private static final String recordSchemaString =
      "syntax = \"proto3\";\n"
          + "\n"
          + "option java_package = \"io.confluent.kafka.serializers.protobuf.test\";\n"
          + "option java_outer_classname = \"TestMessageProtos\";\n"
          + "\n"
          + "import \"google/protobuf/descriptor.proto\";\n"
          + "\n"
          + "message TestMessage {\n"
          + "    string test_string = 1 [json_name = \"test_str\"];\n"
          + "    bool test_bool = 2;\n"
          + "    bytes test_bytes = 3;\n"
          + "    double test_double = 4;\n"
          + "    float test_float = 5;\n"
          + "    fixed32 test_fixed32 = 6;\n"
          + "    fixed64 test_fixed64 = 7;\n"
          + "    int32 test_int32 = 8;\n"
          + "    int64 test_int64 = 9;\n"
          + "    sfixed32 test_sfixed32 = 10;\n"
          + "    sfixed64 test_sfixed64 = 11;\n"
          + "    sint32 test_sint32 = 12;\n"
          + "    sint64 test_sint64 = 13;\n"
          + "    uint32 test_uint32 = 14;\n"
          + "    uint64 test_uint64 = 15;\n"
          + "}\n";

  private static final ProtobufSchema recordSchema = new ProtobufSchema(recordSchemaString);

  private static final String arraySchemaString =
      "syntax = \"proto3\";\n"
          + "\n"
          + "option java_package = \"io.confluent.kafka.serializers.protobuf.test\";\n"
          + "option java_outer_classname = \"TestArrayProtos\";\n"
          + "\n"
          + "import \"google/protobuf/descriptor.proto\";\n"
          + "\n"
          + "message TestArray {\n"
          + "    repeated string test_array = 1;\n"
          + "}\n";

  private static final ProtobufSchema arraySchema = new ProtobufSchema(arraySchemaString);

  private static final String mapSchemaString =
      "syntax = \"proto3\";\n"
          + "\n"
          + "option java_package = \"io.confluent.kafka.serializers.protobuf.test\";\n"
          + "option java_outer_classname = \"TestMapProtos\";\n"
          + "\n"
          + "import \"google/protobuf/descriptor.proto\";\n"
          + "\n"
          + "message TestMap {\n"
          + "    map<string, string> test_map = 1;\n"
          + "}\n";

  private static final ProtobufSchema mapSchema = new ProtobufSchema(mapSchemaString);

  @Test
  public void testRecordToJson() {
    DynamicMessage.Builder builder = recordSchema.newMessageBuilder();
    Descriptor desc = builder.getDescriptorForType();
    FieldDescriptor fd = desc.findFieldByName("test_string");
    builder.setField(fd, "string");
    fd = desc.findFieldByName("test_bool");
    builder.setField(fd, true);
    fd = desc.findFieldByName("test_bytes");
    builder.setField(fd, ByteString.copyFromUtf8("hello"));
    fd = desc.findFieldByName("test_double");
    builder.setField(fd, 800.25);
    fd = desc.findFieldByName("test_float");
    builder.setField(fd, 23.4f);
    fd = desc.findFieldByName("test_fixed32");
    builder.setField(fd, 32);
    fd = desc.findFieldByName("test_fixed64");
    builder.setField(fd, 64L);
    fd = desc.findFieldByName("test_int32");
    builder.setField(fd, 32);
    fd = desc.findFieldByName("test_int64");
    builder.setField(fd, 64L);
    fd = desc.findFieldByName("test_sfixed32");
    builder.setField(fd, 32);
    fd = desc.findFieldByName("test_sfixed64");
    builder.setField(fd, 64L);
    fd = desc.findFieldByName("test_sint32");
    builder.setField(fd, 32);
    fd = desc.findFieldByName("test_sint64");
    builder.setField(fd, 64L);
    fd = desc.findFieldByName("test_uint32");
    builder.setField(fd, 32);
    fd = desc.findFieldByName("test_uint64");
    builder.setField(fd, 64L);
    DynamicMessage message = builder.build();

    SchemaConverter.JsonNodeAndSize result = new ProtobufConverter().toJson(message);
    assertTrue(result.getSize() > 0);
    assertTrue(result.getJson().isObject());
    assertTrue(result.getJson().get("test_str").isTextual());
    assertEquals("string", result.getJson().get("test_str").textValue());
    assertTrue(result.getJson().get("testBool").isBoolean());
    assertEquals(true, result.getJson().get("testBool").booleanValue());
    assertTrue(result.getJson().get("testBytes").isTextual());
    assertEquals("aGVsbG8=", result.getJson().get("testBytes").textValue());
    assertTrue(result.getJson().get("testDouble").isDouble());
    assertEquals(800.25, result.getJson().get("testDouble").doubleValue(), 0.01);
    assertTrue(result.getJson().get("testFloat").isDouble());
    assertEquals(23.4f, result.getJson().get("testFloat").doubleValue(), 0.1);
    assertTrue(result.getJson().get("testFixed32").isInt());
    assertEquals(32, result.getJson().get("testFixed32").intValue());
    assertTrue(result.getJson().get("testFixed64").isTextual());
    assertEquals("64", result.getJson().get("testFixed64").textValue());
    assertTrue(result.getJson().get("testInt32").isInt());
    assertEquals(32, result.getJson().get("testInt32").intValue());
    assertTrue(result.getJson().get("testInt64").isTextual());
    assertEquals("64", result.getJson().get("testInt64").textValue());
    assertTrue(result.getJson().get("testSfixed32").isInt());
    assertEquals(32, result.getJson().get("testSfixed32").intValue());
    assertTrue(result.getJson().get("testSfixed64").isTextual());
    assertEquals("64", result.getJson().get("testSfixed64").textValue());
    assertTrue(result.getJson().get("testSint32").isInt());
    assertEquals(32, result.getJson().get("testSint32").intValue());
    assertTrue(result.getJson().get("testSint64").isTextual());
    assertEquals("64", result.getJson().get("testSint64").textValue());
    assertTrue(result.getJson().get("testUint32").isInt());
    assertEquals(32, result.getJson().get("testUint32").intValue());
    assertTrue(result.getJson().get("testUint64").isTextual());
    assertEquals("64", result.getJson().get("testUint64").textValue());
  }

  @Test
  public void testArrayToJson() {
    DynamicMessage.Builder builder = arraySchema.newMessageBuilder();
    Descriptor desc = builder.getDescriptorForType();
    FieldDescriptor fd = desc.findFieldByName("test_array");
    builder.setField(fd, Arrays.asList("one", "two", "three"));
    DynamicMessage message = builder.build();
    SchemaConverter.JsonNodeAndSize result = new ProtobufConverter().toJson(message);
    assertTrue(result.getSize() > 0);

    JsonNode fieldNode = result.getJson().get("testArray");
    assertTrue(fieldNode.isArray());
    assertEquals(3, fieldNode.size());
    assertEquals(JsonNodeFactory.instance.textNode("one"), fieldNode.get(0));
    assertEquals(JsonNodeFactory.instance.textNode("two"), fieldNode.get(1));
    assertEquals(JsonNodeFactory.instance.textNode("three"), fieldNode.get(2));
  }

  @Test
  public void testMapToJson() {
    DynamicMessage.Builder mapBuilder = mapSchema.newMessageBuilder("TestMap.TestMapEntry");
    Descriptor mapDesc = mapBuilder.getDescriptorForType();
    FieldDescriptor keyField = mapDesc.findFieldByName("key");
    mapBuilder.setField(keyField, "first");
    FieldDescriptor valueField = mapDesc.findFieldByName("value");
    mapBuilder.setField(valueField, "one");
    DynamicMessage mapEntry = mapBuilder.build();

    DynamicMessage.Builder mapBuilder2 = mapSchema.newMessageBuilder("TestMap.TestMapEntry");
    Descriptor mapDesc2 = mapBuilder2.getDescriptorForType();
    FieldDescriptor keyField2 = mapDesc2.findFieldByName("key");
    mapBuilder2.setField(keyField2, "second");
    FieldDescriptor valueField2 = mapDesc2.findFieldByName("value");
    mapBuilder2.setField(valueField2, "two");
    DynamicMessage mapEntry2 = mapBuilder2.build();

    DynamicMessage.Builder builder = mapSchema.newMessageBuilder();
    Descriptor desc = builder.getDescriptorForType();
    FieldDescriptor fd = desc.findFieldByName("test_map");
    builder.setField(fd, Arrays.asList(mapEntry, mapEntry2));
    DynamicMessage message = builder.build();
    SchemaConverter.JsonNodeAndSize result = new ProtobufConverter().toJson(message);
    assertTrue(result.getSize() > 0);

    JsonNode fieldNode = result.getJson().get("testMap");
    assertEquals(2, fieldNode.size());
    assertNotNull(fieldNode.get("first"));
    assertEquals("one", fieldNode.get("first").asText());
    assertNotNull(fieldNode.get("second"));
    assertEquals("two", fieldNode.get("second").asText());
  }
}
