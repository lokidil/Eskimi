package com.dileep

import CampaignProtocol.{Banner, Campaign, GetBannerMatch, IsCampaignMatch, Targeting}
import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import scala.collection.immutable

final case class BidRequest(id: String, imp: Option[List[Impression]], site: Site, user: Option[User], device: Option[Device])
final case class Impression(id: String, wmin: Option[Int], wmax: Option[Int], w: Option[Int], hmin: Option[Int], hmax: Option[Int], h: Option[Int], bidFloor: Option[Double])
final case class Site(id: String, domain: String)
final case class User(id: String, geo: Option[Geo])
final case class Device(id: String, geo: Option[Geo])
final case class Geo(country: Option[String])
final case class BidResponse(id: String, bidRequestId: String, price: Double, adid: Option[String], banner: Option[CampaignProtocol.Banner])
final case class Campaigns(campaigns: immutable.Seq[Campaign])

object BidRegistry {
  sealed trait Command
  final case class GetBids(replyTo: ActorRef[Campaigns]) extends Command
  //final case class CreateCampaign(campaign: Campaign, replyTo: ActorRef[GetBidsResponse]) extends Command
  final case class GetBid(request: BidRequest, replyTo: ActorRef[GetBidsResponse]) extends Command

  final case class GetBidsResponse(response: Option[BidResponse])
  final case class ActionPerformed(description: String)

  def apply(): Behavior[Command] = registry(Set.empty)

  def GetDefaultCampaigns() : Campaigns = {
    Campaigns(Seq(
      Campaign(
        id = 1,
        country = "LT",
        targeting = Targeting(
          targetedSiteIds = Seq("0006a522ce0f4bbbbaa6b3c38cafaa0f") // Use collection of your choice
        ),
        banners = List(
          Banner(
            id = 1,
            src = "https://business.eskimi.com/wp-content/uploads/2020/06/openGraph.jpeg",
            width = 300,
            height = 250
          )
        ),
        bid = 5d
      ),
      Campaign(
        id = 2,
        country = "LT",
        targeting = Targeting(
          targetedSiteIds = Seq("0006a522ce0f4bbbbaa6b3c38cafaa1s") // Use collection of your choice
        ),
        banners = List(
          Banner(
            id = 2,
            src = "https://business.eskimi.com/wp-content/uploads/2020/06/openGraph.jpeg",
            width = 200,
            height = 150
          )
        ),
        bid = 4d
      )
    ),
    )
  }

  private def registry(campaign: Set[Campaign]): Behavior[Command] =
    Behaviors.receiveMessage {
      case GetBids(replyTo) =>
        replyTo ! GetDefaultCampaigns()
        Behaviors.same
      case GetBid(request, replyTo) =>
        replyTo ! GetBidsResponse(GetDefaultCampaigns().campaigns.find(camp => IsCampaignMatch(camp,request)) match {
          case Some(value) => Option[BidResponse] {
            BidResponse(s"response${value.id}", request.id, value.bid, Option[String] {
              value.id.toString
            }, GetBannerMatch(value, request))
          }
          case None => None
        })
        Behaviors.same
    }
}
