package com.dileep

object CampaignProtocol {
  final case class Campaign(id: Int, country: String, targeting: Targeting, banners: List[Banner], bid: Double)
  final case class Targeting(targetedSiteIds: Seq[String])
  final case class Banner(id: Int, src: String, width: Int, height: Int)
  final case class Area(width: Int, height: Int)

  def IsCampaignMatch(campaign: Campaign, bidRequest: BidRequest): Boolean  = {
    val bidFloorMatch = bidRequest.imp match {
      case Some(value) => value.exists(bid => bid.bidFloor.getOrElse(0D) <= campaign.bid)
      case  None => false
    }
    val countryMatch = ((bidRequest.device match {
      case Some(value) => value.geo
      case  None => None
    }) match {
      case Some(value) => value.country
      case None => None
    }) match {
      case Some(value) => value.equalsIgnoreCase(campaign.country)
      case None => false
    }

    val siteIdMatch = campaign.targeting.targetedSiteIds.contains(bidRequest.site.id)

    val heightWidthsLst = GetHeightWeightLst(bidRequest)

    var heightMatch = false
    var widthMatch = false

    heightMatch = heightWidthsLst.iterator.exists(area => campaign.banners.exists(ban => ban.height == area.height))
    widthMatch = heightWidthsLst.iterator.exists(area => campaign.banners.exists(ban => ban.width == area.width))

    bidFloorMatch && countryMatch && siteIdMatch && heightMatch && widthMatch
  }

  private def GetHeightWeightLst(bidRequest: BidRequest) = {
    val heightWidthsLst = bidRequest.imp match {
      case Some(value) => value.map(imp => {
        var height = 0
        var width = 0
        var heightMin = 0
        var widthMin = 0
        var heightMax = 0
        var widthMax = 0
        if (imp.h.nonEmpty) height = imp.h match {
          case Some(value) => value
          case None => 0
        }

        if (imp.w.nonEmpty) width = imp.w match {
          case Some(value) => value
          case None => 0
        }

        if (imp.hmin.nonEmpty) heightMin = imp.hmin match {
          case Some(value) => value
          case None => 0
        }

        if (imp.wmin.nonEmpty) widthMin = imp.wmin match {
          case Some(value) => value
          case None => 0
        }

        if (imp.hmax.nonEmpty) heightMax = imp.hmax match {
          case Some(value) => value
          case None => 0
        }

        if (imp.wmax.nonEmpty) widthMax = imp.wmax match {
          case Some(value) => value
          case None => 0
        }

        if (height == 0) {
          height = if (heightMin > 0) heightMin else if (heightMax > 0) heightMax else heightMin / heightMax
        }

        if (width == 0) {
          width = if (widthMin > 0) widthMin else if (widthMax > 0) widthMax else widthMin / widthMax
        }
        Area(width, height)
      })
      case None => None
    }
    heightWidthsLst
  }

  def GetBannerMatch(campaign: Campaign, bidRequest: BidRequest): Option[Banner]  = {
    val heightWidthsLst = GetHeightWeightLst(bidRequest)
    campaign.banners.find(ban => heightWidthsLst.iterator.exists(hw => hw.width == ban.width && hw.height == ban.height))
  }
}
