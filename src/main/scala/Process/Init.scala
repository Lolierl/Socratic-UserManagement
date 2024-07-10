package Process

import Common.API.{API, PlanContext, TraceID}
import Global.{ServerConfig, ServiceCenter}
import Common.DBAPI.{initSchema, writeDB}
import Common.ServiceUtils.schemaName
import cats.effect.IO
import io.circe.generic.auto.*
import org.http4s.client.Client
import Common.Object.{ParameterList, SqlParameter}
import java.util.UUID

object Init {
  def init(config:ServerConfig):IO[Unit]=
    given PlanContext=PlanContext(traceID = TraceID(UUID.randomUUID().toString),0)
    for{
      _ <- API.init(config.maximumClientConnection)
      _ <- initSchema(schemaName)
      _ <- writeDB(s"CREATE TABLE IF NOT EXISTS ${schemaName}.users (user_name TEXT, password TEXT, usertype TEXT)", List())
      _ <- writeDB(
        s"INSERT INTO ${schemaName}.users (user_name, password, usertype) VALUES (?, ?, ?)",
        List(
          SqlParameter("String", "Dragon"),
          SqlParameter("String", "yuanshen"),
          SqlParameter("String", "superuser")
        ))
    } yield ()

}