import io.restassured.response.Response
import org.testng.Assert
import org.testng.annotations.Test

import static io.restassured.RestAssured.*
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath

class Books extends Base{
    @Test
    void getBooks(){
        Response response = get("/books")
        ArrayList allBooks = response.path("data._id")
        Assert.assertTrue(allBooks.size()>0, "No books returned")
    }

    @Test
    void booksSchema(){
        get("/books")
        .then()
        .assertThat()
        .body(matchesJsonSchemaInClasspath("booksSchema.json"))
    }

    @Test(groups = "Smoke")
    void createAndDeleteBook(){
        File bookFile = new File(getClass().getResource("/book.json").toURI())
        Response createResponse = given()
            .body(bookFile)
            .when()
            .post("/books")

        String responseID = createResponse.jsonPath().getString("post.book_id")

        Assert.assertEquals(createResponse.getStatusCode(),201)

        Response deleteResponse = given()
            .body("{\n" +
                    "    \"book_id\": " + responseID + "\n" +
                    "}")
            .when()
            .delete("/books")

        Assert.assertEquals(deleteResponse.getStatusCode(),200)
        Assert.assertEquals(deleteResponse.jsonPath().getString("message"),"Book successfully deleted")
    }

    @Test
    void deleteNoneExistentBookFileMessage(){
        String nonExistentBookID = "54343"

        Response deleteResponse = given()
                .body("{\n" +
                        "    \"book_id\": " + nonExistentBookID + "\n" +
                        "}")
                .when()
                .delete("/books")

        Assert.assertEquals(deleteResponse.getStatusCode(),500)
        Assert.assertEquals(deleteResponse.jsonPath().getString("error"),"Unable to find book id: " + nonExistentBookID)
    }
}
