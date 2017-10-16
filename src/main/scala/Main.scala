import java.io.File
import repoModels.Repo
import scala.io.Source
import com.typesafe.config.{Config, ConfigFactory}
import org.json._

object Main extends App {

  val filePath = "/home/arkadiusz/Applications/hmrc-development-environment/hmrc/"


  println("Put in the path of your directory holding other repo's, for example I hold my repositories here:" +
    "\"/home/arkadiusz/Applications/hmrc-development-environment/hmrc/\"")

  val filePath2 = scala.io.StdIn.readLine()
  val routesFiles = getFileTree(new File(filePath)).filter(_.getName.endsWith("app.routes"))
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

//  def startSearch(): List[Repo] = {
//    println("Starting Search")
//    val content = routesFiles.map(file => {
//      serviceNameSet += getServiceName(file)
//      createRepo(serviceNameSet.size, getServiceName(file), findScalaFiles(getServiceName(file),
//        readFile(file)), findApplicationConf(file.getAbsolutePath.dropRight(10))) match {
//        case Some(repo) => repo
//      }
//    }
//    ).distinct.toList
//    content
//  }
def startSearch(): List[Repo] = {
      println("Starting Search")
      val content = routesFiles.map(file => {
        serviceNameSet += getServiceName(file)
        createRepo(serviceNameSet.size, getServiceName(file), searchOtherRepoForMeInApplicationConf(getServiceName(file)), findApplicationConf(file.getAbsolutePath.dropRight(10))) match {
          case Some(repo) => repo
        }
      }
      ).distinct.toList
      content
    }



  def createRepo(countSize: Int, repoName: String, receiveFrom: List[String], sendTo: List[String]): Option[Repo] = {
    if (serviceNameSet.size > countSize) None
    else Some(Repo(repoName, receiveFrom, sendTo))
  }

  def readFile(appRoutesFile: File): List[String] = {
    var tempSet = Set[String]()

    val lines = Source.fromFile(appRoutesFile).getLines.filter(s => !s.contains("#")).toList
    lines.foreach {
      var t = ""
      e =>
        if (e.indexOf("/") > -1) t = e.substring(e.indexOf("/") + 1) else t = e

        if (t.indexOf("/") > -1) t = t.substring(0, t.indexOf("/")) else t = ""

        if (t.length > 1) tempSet += getServiceName(appRoutesFile) + "/" + t
    }
    tempSet.toList
  }

//  def findScalaFiles(repoNameForSearch: String, list: List[String]): List[String] = {
//    val scalaFiles = getFileTree(new File(filePath)).filter(_.getName.endsWith(".scala")).toList.map(x => findService(x, list) match {
//      case Some(f) => f
//      case None => "dasdasd1'#,-asdas"
//    })
//    scalaFiles.distinct.filterNot(x => x == "dasdasd1'#,-asdas")
//  }

  def searchOtherRepoForMeInApplicationConf(repoNameForSearch: String): List[String] = {
    val repos = getFileTree(new File(filePath)).toList
//    val appConfFiles = files.filter(_.getName.endsWith("application.conf")).
//      toList.map(x => println(x.getAbsolutePath))
//    val listOfServices = repos.foreach(service => findApplicationConf(service.getAbsolutePath))

    val listOfServices = for(i <- repos) yield{
      findApplicationConf(i.getAbsolutePath).contains(repoNameForSearch)
      a
    }

    //.map(x => findApplicationConf(x.getAbsolutePath.dropRight(10)) )
  val a = listOfServices.filterNot(result => result.size == 0).flatMap(x=>x)
    a

  //scalaFiles.distinct.filterNot(x => x == "dasdasd1'#,-asdas").flatMap(e=> e)
  }

  def findApplicationConf(path: String): List[String] = {
    val myConfigFile = new File(path + "/conf/application.conf")
    val config = ConfigFactory.parseFile(myConfigFile)

    val prodMicroserviceServices = if (config.hasPath("Prod.microservice.services")) getProdMicroServiceServices(config) else ""
    val microserviceServices = if (config.hasPath("microservice.services")) getMicroserviceServices(config) else ""
    val combined = (prodMicroserviceServices + microserviceServices).replaceAll("[\\]\\[,\"]", " ").split(" ").filterNot(e => e == "")
    combined.toList
  }

  def getProdMicroServiceServices(config: Config): String = {
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

  def findService(file: File, name: List[String]): Option[String] = {
    val source = Source.fromFile(file)
    val list = for (i <- name) yield source.getLines.filter(_ contains "/" + i + "/").toList
    val flattenedList = list.flatten
    source.close()
    if (flattenedList.nonEmpty) Some(getServiceName(file)) else None
  }

  def getServiceName(fileName: File): String = {
    val temp = fileName.getAbsolutePath.replace(filePath, "")
    temp.substring(0, temp.indexOf("/"))
  }

  def getFileTree(f: File): Stream[File] =
    f #:: (if (f.isDirectory) f.listFiles().toStream.flatMap(getFileTree)
    else Stream.empty)
}