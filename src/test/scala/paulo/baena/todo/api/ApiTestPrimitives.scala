package paulo.baena.todo.api

import cats.data.OptionT
import cats.effect.{IO, Resource}
import io.circe.{Decoder, Encoder}
import io.circe.generic.auto.*
import org.http4s.*
import org.http4s.circe.*
import paulo.baena.todo.api.Messages.{CreateTodoRequest, UpdateTodoRequest}
import paulo.baena.todo.persistence.TestTodoRepository

object ApiTestPrimitives {
  import cats.effect.unsafe.implicits.global

  val nonExistingId: Long = Long.MaxValue

  // TODO: is it possible to pass IO[HttpRoutes[IO] and interpret it on the test suite?
  val routes: HttpRoutes[IO] =
    TestTodoRepository.inMemory[IO].map(Routes.live(_, "http://localhost")).unsafeRunSync()

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
