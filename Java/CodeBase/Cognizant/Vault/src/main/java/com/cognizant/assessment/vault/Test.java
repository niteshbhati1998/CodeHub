package com.cognizant.assessment.vault;

import com.cognizant.assessment.vault.model.DBObject;
import com.cognizant.assessment.vault.model.VaultDataWithAllFields;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

public class Test {
    public static void main(String args[]) {
        /*String str = "[\n" +
                "    {\n" +
                "        \"test1\" : {\n" +
                "            \"dbtype\" : \"Oracle\",\n" +
                "            \"host\" : \"46.137.199.77\",\n" +
                "            \"port\" : \"1521\",\n" +
                "            \"databasename\" : \"orcl\",\n" +
                "            \"username\": \"HR\",\n" +
                "            \"password\": \"Test2023\"\n" +
                "        }\n" +
                "    },\n" +
                "    {\n" +
                "        \"test2\" : {\n" +
                "            \"dbtype\" : \"TeraData\",\n" +
                "            \"host\" : \"46.137.199.50\",\n" +
                "            \"port\" : \"1523\",\n" +
                "            \"databasename\" : \"orcl\",\n" +
                "            \"username\": \"HR\",\n" +
                "            \"password\": \"Test2023\"\n" +
                "        }\n" +
                "    }\n" +
                "]\n";
        /*String str = "[\n" +
                "     {\n" +
                "            \"dbtype\" : \"Oracle\",\n" +
                "            \"host\" : \"46.137.199.77\",\n" +
                "            \"port\" : \"1521\",\n" +
                "            \"databasename\" : \"orcl\",\n" +
                "            \"username\": \"HR\",\n" +
                "            \"password\": \"Test2023\"\n" +
                "        },\n" +
                "     {\n" +
                "            \"dbtype\" : \"TeraData\",\n" +
                "            \"host\" : \"46.137.199.50\",\n" +
                "            \"port\" : \"1523\",\n" +
                "            \"databasename\" : \"orcl\",\n" +
                "            \"username\": \"HR\",\n" +
                "            \"password\": \"Test2023\"\n" +
                "        }\n" +
                "    \n" +
                "]\n";*/
        String str = "[{\"key\":\"oracle1\",\"value\":\"{\\n        \\\"dbtype\\\" : \\\"Oracle\\\",\\n        \\\"host\\\" : \\\"46.137.199.77\\\",\\n        \\\"port\\\" : \\\"1521\\\",\\n        \\\"databasename\\\" : \\\"orcl\\\",\\n        \\\"username\\\": \\\"HR\\\",\\n        \\\"password\\\": \\\"Test2023\\\"\\n    }\"},{\"key\":\"oracle2\",\"value\":\"{\\n    \\\"dbtype\\\" : \\\"Oracle\\\",\\n    \\\"host\\\" : \\\"46.137.199.33\\\",\\n    \\\"port\\\" : \\\"1522\\\",\\n    \\\"databasename\\\" : \\\"orc2\\\",\\n    \\\"username\\\": \\\"HR-SD\\\",\\n    \\\"password\\\": \\\"Test1233\\\"\\n}\"},{\"key\":\"oracle3\",\"value\":\"{\\n        \\\"dbtype\\\" : \\\"Oracle\\\",\\n        \\\"host\\\" : \\\"46.137.199.44\\\",\\n        \\\"port\\\" : \\\"1525\\\",\\n        \\\"databasename\\\" : \\\"orc3\\\",\\n        \\\"username\\\": \\\"HR-SD-ER\\\",\\n        \\\"password\\\": \\\"Test1255\\\"\\n    }\"},{\"key\":\"teradata1\",\"value\":\"{\\n        \\\"dbtype\\\" : \\\"Oracle\\\",\\n        \\\"host\\\" : \\\"46.137.199.77\\\",\\n        \\\"port\\\" : \\\"1521\\\",\\n        \\\"databasename\\\" : \\\"orcl\\\",\\n        \\\"username\\\": \\\"HR\\\",\\n        \\\"password\\\": \\\"Test2023\\\"\\n    }\"}]";

        try {
            ObjectMapper objectMapper = new ObjectMapper();

            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);



            // Convert JSON array to List of ConfigEntry objects

            List<VaultDataWithAllFields> configEntries = objectMapper.readValue(str, objectMapper.getTypeFactory().constructCollectionType(List.class, VaultDataWithAllFields.class));



            // Print the result

            for (VaultDataWithAllFields entry : configEntries) {

                System.out.println(entry);

            }
            //ObjectMapper objectMapper = new ObjectMapper();
            /*List<VaultDataWithAllFields> list = objectMapper.readValue(
                    str,
                    new TypeReference<List<VaultDataWithAllFields>>() {}
            );
            // Convert the list to a Map<String, DBObject>
            Map<String, DBObject> dbObjectMap = new HashMap<>();
            /*for (Map<String, DBObject> item : list) {
                dbObjectMap.putAll(item);
            }


            Set<String> objectTypes = dbObjectMap.entrySet().stream().map(es -> es.getValue().getDbtype()).collect(Collectors.toSet());


            // Print the resulting map
            System.out.println(objectTypes);

             */
        } catch (Exception e) {
            System.out.println("Tes" + e);
        }


    }
}
