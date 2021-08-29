package com.dileep

import com.dileep.CampaignProtocol.{Banner, Campaign, Targeting}
//import com.dileep.UserRegistry.ActionPerformed

//#json-formats
import spray.json.DefaultJsonProtocol

object JsonFormats  {
  // import the default encoders for primitive types (Int, String, Lists etc)
  import DefaultJsonProtocol._

//  implicit val userJsonFormat = jsonFormat3(User)
//  implicit val usersJsonFormat = jsonFormat1(Users)

  implicit val banJsonFormat = jsonFormat4(Banner)
  implicit val targJsonFormat = jsonFormat1(Targeting)
  implicit val campJsonFormat = jsonFormat5(Campaign)
  implicit val campsJsonFormat = jsonFormat1(Campaigns)

  implicit val impJsonFormat = jsonFormat8(Impression)
  implicit val siteJsonFormat = jsonFormat2(Site)
  implicit val geoJsonFormat = jsonFormat1(Geo)
  implicit val deviceJsonFormat = jsonFormat2(Device)
  implicit val usrJsonFormat = jsonFormat2(User)
  implicit val bidReqJsonFormat = jsonFormat5(BidRequest)
  implicit val bidResJsonFormat = jsonFormat5(BidResponse)

  //implicit val actionPerformedJsonFormat = jsonFormat1(ActionPerformed)
}
//#json-formats
