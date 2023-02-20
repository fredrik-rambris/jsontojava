package com.rambris.jsontojava;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BigIntegerNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.DecimalNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.FloatNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ShortNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.text.translate.UnicodeUnescaper;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class JsonParser {
    private final ObjectMapper mapper;
    private static final int INDENT_SPACES = 4;

    public JsonParser(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    String parseJson(String json, String name) throws JsonProcessingException {
        var root = mapper.readTree(json);
        return "var " + name + " = " + value(0, root, name) + ";";
    }

    private static String indent(int indentLevel) {
        return " ".repeat(indentLevel * INDENT_SPACES);
    }

    private static String value(int indentLevel, JsonNode field, String name) {
        if (field instanceof TextNode n)
            return '"' + new UnicodeUnescaper().translate(StringEscapeUtils.escapeJava(n.textValue())) + '"';
        else if (field instanceof IntNode n)
            return Integer.toString(n.intValue());
        else if (field instanceof ShortNode n)
            return Short.toString(n.shortValue());
        else if (field instanceof DoubleNode n)
            return Double.toString(n.doubleValue());
        else if (field instanceof DecimalNode n)
            return "new BigDecimal(\"" + n.decimalValue().toString() + "\")";
        else if (field instanceof BigIntegerNode n)
            return "new BigInteger(\"" + n.bigIntegerValue().toString() + "\")";
        else if (field instanceof LongNode n)
            return n.longValue() + "L";
        else if (field instanceof FloatNode n)
            return n.floatValue() + "f";
        else if (field instanceof BooleanNode n)
            return n.toPrettyString();
        else if (field instanceof ArrayNode n)
            return list(indentLevel, name, n);
        else if (field instanceof ObjectNode n)
            return object(indentLevel, name, n);
        else if (field instanceof NullNode n)
            return "null";
        else
            return "/* %s not supported */".formatted(field.getClass().getSimpleName());
    }

    private static String object(int indentLevel, String name, ObjectNode n) {
        return classifier(name) + ".builder()\n" + indent(indentLevel + 1) +
                StreamSupport.stream(Spliterators.spliteratorUnknownSize(n.fields(), Spliterator.ORDERED), false)
                        .filter(e -> !(e.getValue() instanceof NullNode))
                        .map(e -> "." + e.getKey() + "(" + value(indentLevel + 1, e.getValue(), e.getKey()) + ")")
                        .collect(Collectors.joining("\n" + indent(indentLevel + 1))) + "\n" +
                indent(indentLevel + 1) + ".build()";
    }

    private static String list(int indentLevel, String name, ArrayNode n) {
        if (n.size() == 0)
            return "Collections.EMPTY_LIST";
        else
            return "List.of(" + StreamSupport.stream(n.spliterator(), false)
                    .filter(Predicate.not(NullNode.class::isInstance))
                    .map(child -> value(indentLevel + 1, child, singular(name)))
                    .collect(Collectors.joining(",", "\n" + indent(indentLevel + 1), "")) + ")";
    }

    private static String singular(String plural) {
        if (plural.equalsIgnoreCase("people")) {
            return plural.charAt(0) + "erson";
        } else if (plural.equalsIgnoreCase("alumni")) {
            return plural.charAt(0) + "lumnus";
        } else if (plural.endsWith("ies")) {
            return plural.substring(0, plural.length() - 3) + 'y';
        } else if ('s' == plural.charAt(plural.length() - 1)) {
            return plural.substring(0, plural.length() - 1);
        }
        return plural;
    }

    private static String classifier(String name) {
        return name.length() > 1 ? name.substring(0, 1)
                .toUpperCase() + (name.length() > 2 ? name.substring(1) : "") : name;
    }

}
