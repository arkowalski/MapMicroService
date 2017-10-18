Inputs:
1. Path for directory storing repositories e.g. /home/arkadiusz/Applications/hmrc-development-environment/hmrc/
2. You will be provided with commands to chose from
    all                 -> all results
    repository Name     -> details found about that particular repository
    exit                -> exit program


What the project is:

The project discoverers connections between services based on their application.conf file found in conf.
Find the services a repository has in its application.conf found in Prod.microservice.services or general microservice.services

Results are individual in format: Repo(repoName:String, receiveFrom: List[String], sendTo: List[String])