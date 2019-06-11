package at;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import cucumber.api.DataTable;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static at.CucumberUtils.*;
import static org.apache.commons.lang3.StringUtils.*;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
public class StepDefinitions {

    static final int PORT = 8081;
    static final String REQUEST_PATH = "/api/recipes";
    private static Response lastResponse;

    @Given("^I post the following recipe:$")
    public void i_post_the_following_recipe(DataTable table) throws InterruptedException {
        clearTestData();
        String payload = getNewRecipeFromTopOfTable(table, Optional.empty());
        lastResponse = RestAssured.given().contentType(ContentType.JSON).body(payload).port(PORT).when().post(REQUEST_PATH);
    }

    @Given("^I update the recipe like this:$")
    public void i_update_the_recipe_like_this(DataTable table) {
        String id = getIdFromLastResponse(lastResponse);
        String payload = getNewRecipeFromTopOfTable(table, Optional.of(id));
        lastResponse = RestAssured.given().contentType(ContentType.JSON).body(payload).port(PORT).when().put(REQUEST_PATH + "/" + id);
    }

    @When("^a request is made to retrieve all recipes$")
    public void a_request_is_made_to_retrieve_all_recipes() {
        lastResponse = RestAssured.given().port(PORT).when().get(REQUEST_PATH);
    }

    @When("^a request is made to retrieve my recipe \"([^\"]*)\"$")
    public void a_request_is_made_to_retrieve_my_recipe(String myRecipe) throws Throwable {
        lastResponse = RestAssured.given().port(PORT).when().get(REQUEST_PATH + "/name/" + myRecipe);
    }

    @When("^a request is made to delete my recipe \"([^\"]*)\"$")
    public void a_request_is_made_to_delete_my_recipe(String arg1) throws Throwable {
        String id = getIdFromLastResponse(lastResponse);
        lastResponse = RestAssured.given().contentType(ContentType.JSON).port(PORT).when().delete(REQUEST_PATH + "/" + id);
    }

    @Then("^the following recipes should be returned:$")
    public void the_following_recipes_should_be_returned(DataTable table) throws JSONException {
        JSONArray expected = convertDataTableToJSONArray(table, "id");
        JSONArray actual = new JSONArray(lastResponse.getBody().asString());
        assertRecipesMatch(expected, actual);
    }

    @Then("^no recipe should be returned$")
    public void no_recipe_should_be_returned() {
        String expected = EMPTY;
        String actual = lastResponse.getBody().asString();
        assertEquals(expected, actual);
    }

}