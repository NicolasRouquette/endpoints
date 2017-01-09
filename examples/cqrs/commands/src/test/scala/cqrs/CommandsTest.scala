package cqrs

import java.time.{LocalDateTime, OffsetDateTime, ZoneOffset}
import java.util.UUID

import akka.stream.Materializer
import org.scalatest.{AsyncFreeSpec, BeforeAndAfterAll}
import endpoints.play.client.{CirceEntities, Endpoints}
import play.api.libs.ws.ahc.{AhcWSClient, AhcWSClientConfig}
import play.core.server.NettyServer

import scala.concurrent.Future
import scala.math.BigDecimal

class CommandsTest extends AsyncFreeSpec with BeforeAndAfterAll {

  private val server = NettyServer.fromRouter()(Commands.routes)

  implicit val materializer: Materializer = server.materializer
  private val wsClient = AhcWSClient(AhcWSClientConfig())

  object client
    extends Endpoints("http://localhost:9000", wsClient)
      with CirceEntities
      with CommandsEndpoints

  override def afterAll(): Unit = {
    wsClient.close()
    server.stop()
  }

  "Commands" - {

    val arbitraryDate = OffsetDateTime.of(LocalDateTime.of(2017, 1, 8, 12, 34, 56), ZoneOffset.UTC)
    val arbitraryValue = BigDecimal(10)

    "create a new meter" in {
      client.command(CreateMeter).map { maybeEvent =>
        assert(maybeEvent.collect { case MeterCreated(_) => () }.nonEmpty)
      }
    }
    "create a meter and add readings to it" in {
      for {
        maybeCreatedEvent <- client.command(CreateMeter)
        id <-
          maybeCreatedEvent
            .collect { case MeterCreated(id) => id }
            .fold[Future[UUID]](Future.failed(new NoSuchElementException))(Future.successful)
        maybeAddedEvent <- client.command(AddRecord(id, arbitraryDate, arbitraryValue))
        _ <-
          maybeAddedEvent
            .collect { case RecordAdded(`arbitraryDate`, `arbitraryValue`) => () }
            .fold[Future[Unit]](Future.failed(new NoSuchElementException))(Future.successful)
      } yield assert(true)
    }
  }

}
