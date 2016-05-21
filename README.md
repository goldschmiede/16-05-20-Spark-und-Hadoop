# Welcome to repository of the talk on Apache Spark (and Hadoop) @ [Goldschmiede](http://www.anderscore.com/partner/goldschmiede/)

Thank you all for coming, participating and, to some of you, for drinking a beer with me afterwards. It was great evening
and a lot of very interesting discussions. I learned a lot!

MfG
reinis

#### Presentation of the talk

Click -> ![Spark @ Goldschmiede](SparkAtGoldschmiede.pdf)

#### Playing with code examples

To run tests, please do following:

1. if you don't already have it, install java (I suggest java 8)
2. if you do not intend to use IDE, go to next step. If you choose to use IDE, install it and ensure that it has active plugins for git, sbt, scala
3. if you are not using IDE, download and install (attention you do NOT need scala)
    1. git (current version)
    2. sbt 0.13.8
3. checkout this project with url from github
4. install and configure Postgres SQL Server on your workstation. Then do following
    1. create database called 'world'
    2. execute src/main/resources/world.sql onto created database
    3. adjust database connection url (e.g. name, password or different database name) in src/test/resources/application.conf
5. execute following sbt command in the directory you have checked out this project:
```
sbt update test
```

If tests complete successfully, you're all set.

#### Final words

Code and additional resources are extensively commented, if there are still questions or you would like to continue discussion please either contact me [Reinis Vicups](https://www.xing.com/profile/Reinis_Vicups) or [Sibylle Ohm](mailto:sibylle.ohm@anderScore.com)