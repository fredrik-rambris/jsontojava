# JSON to Java
Convert a JSON object to a static Java object source code

## Usage
    java -jar jsontojava.jar myObject.json

Given input:
```json
{
  "id": 47,
  "name": "Andy Sipowicz",
  "address": {
    "street": "Hill Street",
    "city": "New York City"
  },
  "tags": ["police", "detective"]
}
```

Produces output:
```java
var myObject = MyObject.builder()
        .id(47)
        .name("Andy Sipowicz")
        .address(Address.builder()
            .street("Hill Street")
            .city("New York City")
            .buid())
        .tags(List.of(
            "police",
            "detective"        
        ))
        .build();
```

It has no knowledge of your Java classes and derives class names from the field and file name.