import akka.actor.ActorSystem
import akka.stream.alpakka.sqs.{MessageAction, SqsSourceSettings}

import scala.util.{Failure, Success}
import akka.stream.alpakka.sqs.scaladsl.{SqsAckSink, SqsSource}
import akka.stream.scaladsl.Sink
import com.github.matsluni.akkahttpspi.AkkaHttpClient
import software.amazon.awssdk.auth.credentials.{AwsBasicCredentials, StaticCredentialsProvider}
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.model.Message

import scala.collection.immutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

object Listener{

  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem()
    val queueUrl: String = "https://sqs.us-east-2.amazonaws.com/queue-path"

    val credentialsProvider = StaticCredentialsProvider.create(AwsBasicCredentials.create("accesskey", "secretkey"))
    implicit val awsSqsClient: SqsAsyncClient = SqsAsyncClient
      .builder()
      .credentialsProvider(credentialsProvider)
      .region(Region.US_EAST_2)
      .httpClient(AkkaHttpClient.builder().withActorSystem(system).build())
      .build()


    val messages: Future[immutable.Seq[Message]] =
      SqsSource(
        queueUrl,
        SqsSourceSettings().withCloseOnEmptyReceive(true).withWaitTime(10.millis)
      ).runWith(Sink.seq)

    messages.onComplete({
      case Success(message: Seq[Message]) => {
        message.foreach(x => {
          println("Incoming message: "+x.body())
          val deleteMessage = SqsSource(queueUrl)
            .take(1)
            .map(MessageAction.Delete(_))
            .runWith(SqsAckSink(queueUrl))
          deleteMessage.onComplete({
            case Success(someMessage) => {
              println("Message consumed successfully "+someMessage.toString)
            }
          })
        })
      }
    })

    system.registerOnTermination(awsSqsClient.close())

  }
}

