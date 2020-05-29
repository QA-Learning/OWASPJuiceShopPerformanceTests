package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration._
import scala.util.Random


class CreateCustomerLoadSimulation extends Simulation {

  val httpConf = http.baseUrl("https://juice-shop.herokuapp.com/")
    .header("Accept", "application/json")

  def randomString(length: Int) = {
    val rnd = new Random()
    rnd.alphanumeric.filter(_.isLetter).take(length).mkString
  }

  val customFeeder = Iterator.continually(Map(
    "randomEmail" -> (randomString(5) + "@gmail.com")
  ))


  def createCustomerSuccessfully() = {
    repeat(5) {
      feed(customFeeder)
        .exec(http("Create Customer")
          .post("api/users")
          .body(ElFileBody("bodies/customerRequest.json")).asJson
          .check(status.is(201))
          .check(jsonPath("$.data.id").saveAs("userId")))
        .pause(5)
        .exec(http("Save security Answers")
          .post("api/SecurityAnswers")
          .body(ElFileBody("bodies/securityAnswers.json")).asJson
          .check(status.is(201)))
    }
  }


  val scn = scenario("Create Customer Sucessfully Load Simulation")
    .exec(createCustomerSuccessfully())


  setUp(
    scn.inject(
      nothingFor(5 seconds),
      atOnceUsers(1),
      rampUsers(2) during (10 seconds)
    ).protocols(httpConf.inferHtmlResources()))

}
