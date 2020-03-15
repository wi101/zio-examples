# ZIO with http4s and doobie

This example is about how to use ZIO with http4s and doobie that are using cats-effect.
ZIO has a separate module called: `zio-interop-cats`
which contains instances for the Cats Effect library, 
and allows you to use ZIO with any libraries that rely on Cats Effect like in our case Http4s and Doobie.

The example that is built in this package is a simple application that enable us to create/read/delete a user in Database via HTTP calls:

![1_luJ2CoGjZ5ZYkMYuCsmKXQ](https://user-images.githubusercontent.com/3535357/76684079-33526c80-6609-11ea-98e8-d7348c712964.png)

Let's start:
## Application Dependencies:
Using ZIO environment we can define the dependencies that are used in our Application and we can have different implementations for these dependencies.

1. [Configuration](https://github.com/wi101/zio-examples/blob/master/src/main/scala/com/zio/examples/http4s_doobie/configuration/package.scala)
2. [UserPersistence](https://github.com/wi101/zio-examples/tree/master/src/main/scala/com/zio/examples/http4s_doobie/persistence)
3. Http [Api](https://github.com/wi101/zio-examples/blob/master/src/main/scala/com/zio/examples/http4s_doobie/http/Api.scala):
- GET → get the user for a given id
- POST → create a new user
- DELETE → delete a user with a given id

At the end build a ZIO application:

Create the [Main](https://github.com/wi101/zio-examples/blob/master/src/main/scala/com/zio/examples/http4s_doobie/Main.scala) App using the zio interpreter that extends `zio.App`
This program requires these dependencies:
```
  type AppEnvironment = Clock with Blocking with UserPersistence
```
- load configuration using [pure-config](https://github.com/wi101/zio-examples/blob/master/src/main/scala/com/zio/examples/http4s_doobie/configuration/ConfigPrd.scala)
- open an API Connection to serve the requests
- persist the data using [doobie](https://github.com/wi101/zio-examples/blob/master/src/main/scala/com/zio/examples/http4s_doobie/persistence/UserPersistenceService.scala)

```scala
import zio._

object Main extends zio.App {
 def run(args: List[String]): ZIO[ZEnv, Nothing, Int] = ???
}
```

For more details checkout this blog post: [https://medium.com/@wiemzin/zio-with-http4s-and-doobie-952fba51d089]
