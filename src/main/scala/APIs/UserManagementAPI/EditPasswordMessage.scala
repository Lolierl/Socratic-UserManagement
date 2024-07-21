package APIs.UserManagementAPI

case class EditPasswordMessage(userName: String, oldPassword: String, newPassword: String) extends UserManagementMessage[String]
