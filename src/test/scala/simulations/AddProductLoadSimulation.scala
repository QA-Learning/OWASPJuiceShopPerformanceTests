package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration._
import scala.util.Random


class AddProductLoadSimulation extends Simulation {

  val httpConf = http.baseUrl("https://juice-shop.herokuapp.com/")
    .header("Accept", "application/json")


  def addProductToCart() = {
    repeat(1) {
      exec(http("Login user")
        .post("rest/user/login")
        .body(ElFileBody("bodies/loginRequest.json")).asJson
        .check(status.is(200))
        .check(jsonPath("$.authentication.token").saveAs("token"))
        .check(jsonPath("$.authentication.bid").saveAs("bid")))

        .pause(5)
        .exec(http("Get All Prducts")
          .get("rest/products/search?q=")
          .header("Authorization", "Bearer " + "${token}")
          .check(status.is(200))
          .check(jsonPath("$.data[0].id").saveAs("productId")))
        .pause(5)
        .exec(http("Add First Product To Basket")
          .post("api/BasketItems/")
          .header("Authorization", "Bearer " + "${token}")
          .body(StringBody("{\"ProductId\":${productId},\"BasketId\":\"${bid}\",\"quantity\":1}")).asJson
          .check(status.is(200)))
    }
  }


  val scn = scenario("Add Prodcut to Cart Simulation")
    .exec(addProductToCart())


  setUp(
    scn.inject(
      nothingFor(5 seconds),
      atOnceUsers(1),
      rampUsers(2) during (10 seconds)
    ).protocols(httpConf.inferHtmlResources()))

}
