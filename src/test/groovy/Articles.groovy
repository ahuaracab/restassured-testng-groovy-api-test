import io.restassured.response.Response
import org.testng.Assert
import org.testng.annotations.Test

import static io.restassured.RestAssured.*
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath

class Articles extends Base{
    @Test
    void getArticlesReturnList(){
        Response response = get("/articles")
        ArrayList<String> allArticles = response.path("data._id")
        Assert.assertTrue(allArticles.size()>0, "No articles returned")
    }

    @Test
    void articlesSchema(){
        get("/articles")
                .then()
                .assertThat()
                .body(matchesJsonSchemaInClasspath("articlesSchema.json"))
    }

    @Test(groups = "Smoke")
    void createAndDeleteArticle(){
        File articleFile = new File(getClass().getResource("/article.json").toURI())
        Response createResponse = given()
                .body(articleFile)
                .when()
                .post("/articles")

        String responseID = createResponse.jsonPath().getString("post.article_id")

        Assert.assertEquals(createResponse.getStatusCode(),201)

        Response deleteResponse = given()
                .body("{\n" +
                        "    \"article_id\": " + responseID + "\n" +
                        "}")
                .when()
                .delete("/articles")

        Assert.assertEquals(deleteResponse.getStatusCode(),200)
        Assert.assertEquals(deleteResponse.jsonPath().getString("message"),"Article successfully deleted")
    }

    @Test
    void deleteNoneExistentArticleFileMessage(){
        String nonExistentArticleID = "54343"

        Response deleteResponse = given()
                .body("{\n" +
                        "    \"article_id\": " + nonExistentArticleID + "\n" +
                        "}")
                .when()
                .delete("/articles")

        Assert.assertEquals(deleteResponse.getStatusCode(),500)
        Assert.assertEquals(deleteResponse.jsonPath().getString("error"),"Unable to find article id: " + nonExistentArticleID)
    }
}
