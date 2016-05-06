# Welcome to repository of the talk on Apache Spark (and Hadoop) @ [Goldschmiede](http://www.anderscore.com/partner/goldschmiede/)

### Attention: Currently this repository contains solely project configuration and couple of tests to ensure that the configuration on your workstation is correct. Additional code and presentation material will be added after Goldschmiede have took place.


To run tests, please do following:

1. download SBT or configure IDE capable working with SBT (e.g. Intellij IDEA)
2. checkout this project
3. Configure Postgres SQL Server on your workstation and do following

    3.1 create database called 'world'

    3.2 execute src/main/resources/world.sql onto created database

    3.3 adjust database connection url (e.g. name, password or different database name) in src/test/resources/application.conf

4. execute following sbt command in the directory you have checked out this project:
```
sbt update test
```

If tests complete successfully, you're all set.