package com.dileep

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import scala.concurrent.Future
import com.dileep.BidRegistry._
import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.AskPattern._
import akka.util.Timeout

class BidRoutes(bidRegistry: ActorRef[BidRegistry.Command])(implicit val system: ActorSystem[_]) {

  //#user-routes-class
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import JsonFormats._
  //#import-json-formats

  // If ask takes more time than this to complete the request is failed
  private implicit val timeout: Timeout = Timeout.create(system.settings.config.getDuration("my-app.routes.ask-timeout"))

  def getBids: Future[Campaigns] =
    bidRegistry.ask(GetBids)
  def getBid(bidRequest: BidRequest): Future[GetBidsResponse] =
    bidRegistry.ask(GetBid(bidRequest, _))

  //#all-routes
  //#users-get-post
  //#users-get-delete
  val bidRoutes: Route =
  pathPrefix("bids") {
    concat(
      //#users-get-delete
      pathEnd {
        concat(
          get {
            complete(getBids)
          },
          post {
            entity(as[BidRequest]) { bidRequest =>
              onSuccess(getBid(bidRequest)) { bid =>
                bid.response match {
                  case Some(value) => complete(value)
                  case None => complete(StatusCodes.NoContent)
                }
                }
              }
            })
          })
      }
    //#users-get-delete
  //#all-routes
}
