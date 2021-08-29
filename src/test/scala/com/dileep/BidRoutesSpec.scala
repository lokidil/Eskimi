package com.dileep

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import JsonFormats._
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Route

class BidRoutesSpec extends AnyWordSpec with Matchers with ScalaFutures with ScalatestRouteTest {
  lazy val testKit: ActorTestKit = ActorTestKit()
  implicit def typedSystem: ActorSystem[Nothing] = testKit.system
  override def createActorSystem(): akka.actor.ActorSystem =
    testKit.system.classicSystem

  protected val bidRegistry: ActorRef[BidRegistry.Command] = testKit.spawn(BidRegistry())
  lazy val routes: Route = new BidRoutes(bidRegistry).bidRoutes

  "BidRoutes" should {
    "return default bid if present (GET /bids)" in {
      // note that there's no need for the host part in the uri:
      val request = HttpRequest(uri = "/bids")

      request ~> routes ~> check {
        status should ===(StatusCodes.OK)

        // we expect the response to be json:
        contentType should ===(ContentTypes.`application/json`)

        // and no entries should be in the list:
        entityAs[String] should !== (null)
      }
    }

    "be able to retrive bid (POST /bids)" in {
            val bidReq = BidRequest("SGu1Jpq1IO", Option(List[Impression]{ Impression("1",Option(50) , Option(300), Option(300),Option(100), Option(300), Option(250), Option(3.12123)) }),
              Site("0006a522ce0f4bbbbaa6b3c38cafaa0f", "fake.tld"), Option(User("USARIO1", Option(Geo(Option("LT"))))),
              Option(Device("440579f4b408831516ebd02f6e1c31b4", Option(Geo(Option("LT")))))) //Option[User("1", Option[Geo(Option["IL"])])]
            val bidReqEntity = Marshal(bidReq).to[MessageEntity].futureValue // futureValue is from ScalaFutures

            // using the RequestBuilding DSL:
            val request = Post("/bids").withEntity(bidReqEntity)

            request ~> routes ~> check {
              status should ===(StatusCodes.OK)

              // we expect the response to be json:
              contentType should ===(ContentTypes.`application/json`)

              // and we know what message we're expecting back:
              entityAs[String] should !== ("")
              //("""{"adid": "1","banner": {"height": 250,"id": 1,"src": "https://business.eskimi.com/wp-content/uploads/2020/06/openGraph.jpeg","width": 300},"bidRequestId": "SGu1Jpq1IO","id": "response1","price": 5.0}""".stripMargin.trim)
            }
          }
    "be no bid found (POST /bids)" in {
      val bidReq = BidRequest("SGu1Jpq1IO11", Option(List[Impression]{ Impression("1",Option(50) , Option(100), Option(100),Option(100), Option(300), Option(250), Option(3.12123)) }),
        Site("0006a522ce0f4bbbbaa6b3c38cafaa0f", "fake.tld"), Option(User("USARIO1", Option(Geo(Option("LT"))))),
        Option(Device("440579f4b408831516ebd02f6e1c31b4", Option(Geo(Option("LT")))))) //Option[User("1", Option[Geo(Option["IL"])])]
      val bidReqEntity = Marshal(bidReq).to[MessageEntity].futureValue // futureValue is from ScalaFutures

      // using the RequestBuilding DSL:
      val request = Post("/bids").withEntity(bidReqEntity)

      request ~> routes ~> check {
        status should ===(StatusCodes.NoContent)

        // we expect the response to be json:
        contentType should !==(ContentTypes.`application/json`)

        // and we know what message we're expecting back:
        entityAs[String] should === ("")
        //("""{"adid": "1","banner": {"height": 250,"id": 1,"src": "https://business.eskimi.com/wp-content/uploads/2020/06/openGraph.jpeg","width": 300},"bidRequestId": "SGu1Jpq1IO","id": "response1","price": 5.0}""".stripMargin.trim)
      }
    }
  }
}
