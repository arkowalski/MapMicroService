import java.io.File

import repoModels.Repo
import com.typesafe.config.{Config, ConfigFactory, ConfigResolveOptions}
import org.json._

object Main extends App {

  val filePath = "/home/arkadiusz/Applications/hmrc-development-environment/hmrc/"


  println("Put in the path of your directory holding other repo's, for example I hold my repositories here:" +
    "\"/home/arkadiusz/Applications/hmrc-development-environment/hmrc/\"")

  //val filePath2 = scala.io.StdIn.readLine()
  //  val routesFiles = getFileTree(new File(filePath)).filter(_.getName.endsWith("app.routes"))
  var serviceNameSet = Set[String]()
  println("you have entered: " + filePath)
  println("Loading data")
  val results = startSearch()

  println("now enter repo name, including all characters for details or write \"all\" for all results")

  var timeForExit = true
  while (timeForExit) {
    val currentScan = scala.io.StdIn.readLine()
    currentScan match {
      case "all" => results.foreach(println)
      case "exit" => timeForExit = false
      case potentialRepoName => println(results.find(_.repoName == potentialRepoName).getOrElse("Not Found, try again or type \"exit\" to stop"))
    }
  }

  def startSearch(): List[Repo] = {
    val repos = new File(filePath).list()

    val filtered = repos.filterNot(_.startsWith("."))
    val data = for (i <- filtered) yield {
      println(i)
      Repo(i, servicesThatCanTalkToMe(i, filtered.toList), servicesThatICanTalkTo(i))
    }
    data.toList
  }

  def servicesThatCanTalkToMe(me: String, repos: List[String]): List[String] = {
    var services: List[String] = List()
    for (i <- repos) {
      val servicesThatRepoTalksTo = getServicesThatRepoTalksTo(filePath + "/" + i)
      if (servicesThatRepoTalksTo.contains(me)) {
        services = i :: services
      }
    }
    services
  }

  def servicesThatICanTalkTo(me: String): List[String] = {
    getServicesThatRepoTalksTo(filePath + "/" + me)
  }

  def getServicesThatRepoTalksTo(path: String): List[String] = {
    //      val myConfigFile = new File(path + "/conf/application.conf")
    //      val config = ConfigFactory.parseFile(myConfigFile)
    if (!new File(path + "/conf/application.conf").exists()) {
      List("could not find application.conf")
    }
    else {
      val myConfigFileContents = scala.io.Source.fromFile(path + "/conf/application.conf").mkString
      val config = ConfigFactory.parseString(myConfigFileContents).resolve(ConfigResolveOptions.defaults().setAllowUnresolved(true))

      val prodMicroserviceServices = if (config.hasPath("Prod.microservice.services")) getProdMicroServiceServices(config) else ""
      val microserviceServices = if (config.hasPath("microservice.services")) getMicroserviceServices(config) else ""
      val combined = (prodMicroserviceServices + microserviceServices).replaceAll("[\\]\\[,\"]", " ").split(" ").filterNot(e => e == "")
      combined.toList
    }
  }

  def getProdMicroServiceServices(config: Config): String = {
    // println(config.getObject("Prod.microservice.services").unwrapped().keySet())

    val prodMicroSerevices = new JSONObject(config.getConfig("Prod.microservice.services").toString
      .replaceAll("play.\"\\$\\{appName\\}\".", "replaced").drop(26).dropRight(2)).names()
    val successOrNot = if (prodMicroSerevices != null) prodMicroSerevices.toString else ""
    successOrNot
  }

  def getMicroserviceServices(config: Config): String = {
    val microserviceServices = new JSONObject(config.getObject("microservice.services").toString.replaceAll
    ("play.\"\\$\\{appName\\}\".", "replaced").drop(19).dropRight(1)).names()
    val succesOrNot = if (microserviceServices != null) microserviceServices.toString else ""
    succesOrNot
  }
}