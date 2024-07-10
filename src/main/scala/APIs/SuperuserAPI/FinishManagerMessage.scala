package APIs.SuperuserAPI

case class FinishManagerMessage(userName: String, allowed:Boolean) extends SuperuserMessage[String]

