package paulo.baena.todo.api

import cats.data.OptionT
import cats.effect.IO
import io.circe.{Decoder, Encoder}
import io.circe.generic.auto.*
import org.http4s.*
import org.http4s.circe.*
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory
import paulo.baena.todo.api.Messages.{CreateTodoRequest, UpdateTodoRequest}
import paulo.baena.todo.persistence.TestTodoRepository

object ApiTestPrimitives {
  import cats.effect.unsafe.implicits.global

  val nonExistingId: Long = Long.MaxValue

  implicit val loggerFactory: LoggerFactory[IO] = Slf4jFactory.create[IO]

  private val testTodoRepository: TestTodoRepository[IO] = TestTodoRepository.inMemory[IO].unsafeRunSync()

  val routes: HttpRoutes[IO] = Routes.live(testTodoRepository, "http://localhost").unsafeRunSync()

  implicit def circeJsonDecoder[A: Decoder]: EntityDecoder[IO, A] = jsonOf[IO, A]

  implicit def circeJsonEncoder[A: Encoder]: EntityEncoder[IO, A] = jsonEncoderOf[A]

  def postRequest(uri: String, createTodoRequest: CreateTodoRequest): IO[Response[IO]] =
    getResponse(routes.run(Request[IO](Method.POST, Uri.unsafeFromString(uri)).withEntity(createTodoRequest)))

  def patchRequest(uri: String, updateTodoRequest: UpdateTodoRequest): IO[Response[IO]] =
    getResponse(routes.run(Request[IO](Method.PATCH, Uri.unsafeFromString(uri)).withEntity(updateTodoRequest)))

  def getRequest(uri: String): IO[Response[IO]] =
    getResponse(routes.run(Request[IO](Method.GET, Uri.unsafeFromString(uri))))

  def deleteRequest(uri: String): IO[Response[IO]] =
    getResponse(routes.run(Request[IO](Method.DELETE, Uri.unsafeFromString(uri))))

  def getResponse(maybeResponse: OptionT[IO, Response[IO]]): IO[Response[IO]] =
    maybeResponse.value.flatMap {
      case Some(response) => IO.pure(response)
      case None           => IO.raiseError(new RuntimeException("Response not found"))
    }

}
