package at;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import cucumber.api.DataTable;
import org.apache.http.HttpStatus;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import java.util.logging.Level;
import java.util.logging.Logger;

import static at.StepDefinitions.PORT;
import static at.StepDefinitions.REQUEST_PATH;
import static java.math.BigInteger.ZERO;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.SPACE;

public class CucumberUtils {

    private static final Logger LOGGER = Logger.getLogger(CucumberUtils.class.getName());

    public static void clearTestData() throws InterruptedException {

        boolean testDataLeft;
        int idToDelete=-1;
        do {
            Response deleteResponse = RestAssured.given().contentType(ContentType.JSON).port(PORT).when().delete(REQUEST_PATH + "/" +idToDelete);
            String response = RestAssured.given().port(PORT).when().get(REQUEST_PATH).getBody().asString();
            List responseList = new JSONArray(response).toList();
            testDataLeft = responseList.size() >0;
            idToDelete++;
        } while(testDataLeft);
    }

    public static String getNewRecipeFromTopOfTable(DataTable table, Optional<String> id) {
        JSONArray allRecipesInTable = convertDataTableToIngredientsPayload(table);
        JSONObject newRecipeAtTopOfTable = allRecipesInTable.getJSONObject(0);
        String payload =  newRecipeAtTopOfTable.toString();
        if(id.isPresent()) {
           payload = updateRecipeId(payload, id.get());
        }
        return payload;
    }

    public static String updateRecipeId(String payload, String id) {
        JSONObject recipe = new JSONObject(payload);
        recipe.put("id", id);
        return recipe.toString();
    }

    public static String getIdFromLastResponse(Response lastResponse) {
        JSONObject newRecipeAtTopOfTable = new JSONObject(lastResponse.getBody().asString());
        String id = newRecipeAtTopOfTable.get("id").toString();
        return id;
    }

    public static JSONArray convertDataTableToJSONArray(DataTable dataTable, String... columnsToIgnore) throws JSONException {
        List<List<String>> table = dataTable.raw();
        JSONArray jsonArray = new JSONArray();
        boolean headerRow = true;
        List<String> keys = table.get(ZERO.intValue());
        for (List<String> row : table) {
            jsonArray = (headerRow) ?  jsonArray : jsonArray.put(convertRowToJSONObject(keys, row, columnsToIgnore));
            headerRow=false;
        }
        return jsonArray;
    }

    public static JSONArray convertDataTableToIngredientsPayload(DataTable dataTable, String... columnsToIgnore) throws JSONException {
        List<List<String>> table = dataTable.raw();
        JSONArray jsonArray = new JSONArray();
        boolean headerRow = true;
        List<String> keys = table.get(ZERO.intValue());
        for (List<String> row : table) {
            jsonArray = (headerRow) ?  jsonArray : jsonArray.put(convertRowToJSONObjectHandlingIngredientsCSV(keys, row, columnsToIgnore));
            headerRow=false;
        }
        return jsonArray;
    }

    public static void assertRecipesMatch(JSONArray expected, JSONArray actualRaw) throws JSONException {
        JSONArray actual = formatRawResponse(actualRaw);
        JSONAssert.assertEquals(expected, actual, false);
    }

    private static JSONObject convertRowToJSONObjectHandlingIngredientsCSV(List<String> keys, List<String> row, String... columnsToIgnore)
            throws JSONException {

        JSONObject jsonObject = new JSONObject();
        int columnCounter = 0;
        for (String column : row) {
            if(keys.get(columnCounter).equals("ingredients")) {
                jsonObject.put(keys.get(columnCounter), convertCommaSeperatedStringIntoJSONArray(column));
            } else {
                jsonObject.put(keys.get(columnCounter), column);
            }
            columnCounter++;
        }
        for(String column : columnsToIgnore) {
            jsonObject.remove(column);
        }
        return jsonObject;
    }

    private static JSONObject convertRowToJSONObject(List<String> keys, List<String> row, String... columnsToIgnore) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        int columnCounter = 0;
        for (String column : row) {
            jsonObject.put(keys.get(columnCounter), column);
            columnCounter++;
        }
        for(String column : columnsToIgnore) {
            jsonObject.remove(column);
        }
        return jsonObject;
    }

    private static JSONArray formatRawResponse(JSONArray input) throws JSONException {
        JSONArray output = new JSONArray();
        for(int i=0; i<input.length(); i++) { //TODO parallelise
            JSONObject jsonObject = new JSONObject(input.get(i).toString());

            String ingredientsCommaSepString = convertJSONArrayOfIngredientsIntoCommaSeparatedStringOfIngredients(
                    new JSONArray(jsonObject.get("ingredients").toString()));

            jsonObject.remove("id");
            jsonObject.put("ingredients", ingredientsCommaSepString.replace(SPACE, EMPTY));
            output.put(jsonObject);
        }
        return output;
    }

    private static JSONArray convertCommaSeperatedStringIntoJSONArray(String commaSepString) {
        JSONObject ingredientsJSONObject = new JSONObject();

        String[] ingredientsArr = commaSepString.split(",");
        List<String> ingredients = Arrays.asList(ingredientsArr);

        JSONArray ingredientsJSONArray = new JSONArray();
        for(String ingredient : ingredients) {
            JSONObject ingredientJSONObj = new JSONObject();
            ingredientJSONObj.put("name", ingredient);
            ingredientsJSONArray.put(ingredientJSONObj);
        }

        return ingredientsJSONArray;
    }

    private static String convertJSONArrayOfIngredientsIntoCommaSeparatedStringOfIngredients(JSONArray ingredients) {
        List<String> l = getValuesForGivenKey(ingredients.toString(), "name");
        String ingredientsCommasSepString = String.join(",", l);
        return ingredientsCommasSepString;
    }

    private static List<String> getValuesForGivenKey(String jsonArrayStr, String key) throws JSONException {
        JSONArray jsonArray = new JSONArray(jsonArrayStr);
        return IntStream.range(0, jsonArray.length())
                .mapToObj(index -> {
                    return getValue(index, jsonArray, key);
                }).collect(Collectors.toList());
    }

    private static String getValue(int index, JSONArray jsonArray, String key) {
        try {
            return ((JSONObject)jsonArray.get(index)).optString(key);
        } catch (JSONException e) {
            e.printStackTrace();
            return EMPTY;
        }
    }

}