# ♕ BYU CS 240 Chess

This project demonstrates mastery of proper software design, client/server architecture, networking using HTTP and WebSocket, database persistence, unit testing, serialization, and security.

## 10k Architecture Overview

The application implements a multiplayer chess server and a command line chess client.

[![Sequence Diagram](10k-architecture.png)](https://sequencediagram.org/index.html#initialData=C4S2BsFMAIGEAtIGckCh0AcCGAnUBjEbAO2DnBElIEZVs8RCSzYKrgAmO3AorU6AGVIOAG4jUAEyzAsAIyxIYAERnzFkdKgrFIuaKlaUa0ALQA+ISPE4AXNABWAexDFoAcywBbTcLEizS1VZBSVbbVc9HGgnADNYiN19QzZSDkCrfztHFzdPH1Q-Gwzg9TDEqJj4iuSjdmoMopF7LywAaxgvJ3FC6wCLaFLQyHCdSriEseSm6NMBurT7AFcMaWAYOSdcSRTjTka+7NaO6C6emZK1YdHI-Qma6N6ss3nU4Gpl1ZkNrZwdhfeByy9hwyBA7mIT2KAyGGhuSWi9wuc0sAI49nyMG6ElQQA)

## Modules

The application has three modules.

- **Client**: The command line program used to play a game of chess over the network.
- **Server**: The command line program that listens for network requests from the client and manages users and games.
- **Shared**: Code that is used by both the client and the server. This includes the rules of chess and tracking the state of a game.

## Starter Code

As you create your chess application you will move through specific phases of development. This starts with implementing the moves of chess and finishes with sending game moves over the network between your client and server. You will start each phase by copying course provided [starter-code](starter-code/) for that phase into the source code of the project. Do not copy a phases' starter code before you are ready to begin work on that phase.

## IntelliJ Support

Open the project directory in IntelliJ in order to develop, run, and debug your code using an IDE.

## Maven Support

You can use the following commands to build, test, package, and run your code.

| Command                    | Description                                     |
| -------------------------- | ----------------------------------------------- |
| `mvn compile`              | Builds the code                                 |
| `mvn package`              | Run the tests and build an Uber jar file        |
| `mvn package -DskipTests`  | Build an Uber jar file                          |
| `mvn install`              | Installs the packages into the local repository |
| `mvn test`                 | Run all the tests                               |
| `mvn -pl shared test`      | Run all the shared tests                        |
| `mvn -pl client exec:java` | Build and run the client `Main`                 |
| `mvn -pl server exec:java` | Build and run the server `Main`                 |

These commands are configured by the `pom.xml` (Project Object Model) files. There is a POM file in the root of the project, and one in each of the modules. The root POM defines any global dependencies and references the module POM files.

## Running the program using Java

Once you have compiled your project into an uber jar, you can execute it with the following command.

```sh
java -jar client/target/client-jar-with-dependencies.jar

♕ 240 Chess Client: chess.ChessPiece@7852e922
```

Diagram Links:
-presentation: https://sequencediagram.org/index.html?presentationMode=readOnly#initialData=IYYwLg9gTgBAwgGwJYFMB2YBQAHYUxIhK4YwDKKUAbpTngUSWDABLBoAmCtu+hx7ZhWqEUdPo0EwAIsDDAAgiBAoAzqswc5wAEbBVKGBx2ZM6MFACeq3ETQBzGAAYAdAHZM9qBACu2GADEaMBUljAASij2SKoWckgQaIEA7gAWSGBiiKikALQAfOSUNFAAXDAA2gAKAPJkACoAujAA9D4GUAA6aADeAETtlMEAtih9pX0wfQA0U7jqydAc45MzUyjDwEgIK1MAvpjCJTC5BfDI5uUALE5O3f2j6sD2YxMAolDeZTB6HDBQKAAjj41GA+gcjpQToU2JxuN9+oMoCNXqtZn15qpFlBlhMpuiNlsdnjwZhYVwoWcjqJygDorFKAAKSL0zJQSLA0EASkOxVE0Jk2iUKnU5ReYAAqh1GUiUTzZPJhWpVALjKUAGJITgwKWUBXAH5hWXAUaYfVK9QnKl8lTlBQ+MCpXVQRnO-U86kqK0w9gU772x1I10dd1k33w71FaiUcrq4AIAy86OwU6FbKXGA3ADM9z6j1Uz1RHy+5WdKJg8YBwA4YXkAGt0KTzcplZHPShymgfAgEEmqPyzs2RapSiAq5lnTKQ9oPTbDKnWOGYzA4wmxOSIwvId9V4nt5H0xhygBWW65-OFlbF6DlABmRJQf0gMDHKDkhiRTaFLctZzVCg4P43W0PsB0KIdlVHccUADVJGWAB1UlDdtIw3Zdd3XJcU2tZNY3jPdikpM5DzAE8z16PNlUvd5PhvGB722R8YGfBDHXMQh3xgNAUGSGBPwOCDf0Kf9AJgWD9TNb9hzbOc7UQoNgPkWcRC9Bc0O+FkYjZSJVG7LB1Jk3CYERDoUUmco0SmVjUnqCAGzQcypn2PsiLTC4jxgAAmciTKGE0xhgCz8SsxDbPsxzVgOdAOFMLxfH8AJoHYF4YAAGQgaIkgCNIMiydzmBwkpymqOomlaAx1ASNB7mNUZZkxbEOAhQjsLcnJSMzHzKKeF4r1o75fn+IEQViUl9zOdTyl85F-N2NYMX0LEljmglNm2OaDgMwqaSG1kmU0hl2WG7lQNU8CpMgmBxUnWqUHlC6hKMHQNS1IDp3kQ0+NM-zJMVH8VS3WSV3wsQULUrC8LXMM4VcqMiuBqHxuI-KyLuCj8wLXqaJLHUOiMCA1C4iBmBQAAPLSxrnAVBJHF9oNg+DENDGnVWegC-nEkCWcBlSOwRgjedQiH+cwmHWrh9CQZc8WSOuJwAEZzyorG+mvb4fGCRDoCQAAvR9KcF8GxfKA7tLUPTob9Qz4emszAqc+brLCxt7ci6WDxRryutuiLgr6J27JdoLSWi2LvD8QIvBQdA0oy3xmGy9JMkwEiBW3YrpDeVK3nqN5mhaYxugD+z3YXWXOrRh5laLfrykGgFOVG5rkwFSaK1CwO0EtzdCnbMUUDAWDJ2L9BlP7M7BT+4d+8HxDh47+z7qn1s-2eldXtxvVtE+2Dna74xIxp0s8YASTQW8IF+xR-utnah+lJEz4vseBwm4WJWCR1tb1t7aC2wrlwfzkKkb+TFnSl2Ru1OWit0bVz6jjDW1lQHLGbuPecb9jZiTng-DoT8IA8i2r3IGCA44OknPfSgL8J5HyGsMCANBnSqAZhQqAS9r7SVXrSDY9CUCMNglfC0ANtq2hgPUKAIJTroJ9JgsREj-5EKMrI0GLUPZQK9mjMwnAw7xUCACP4qUtIwAAOL+RVInXKKd8ppxasVIxOd872H8kXBe6AIFtQzDcSu3VMY1xxvXY6TdS7SL9OUEeXdCFwx2uKFhjNHR7yofOc6y9RRXQHjEsJbDBGs1jBvRSBodBhF3p3TQOhD4PVps6GAeCBE3x5mguSjobq4PPvgyRQtMFAK1lAXWYCOjd1hunEWbjzhqJuDAquPVfF0UQV0npKC2mDnKTPBQPYTGPEZJk2pwk16cisBWHsV1TElLKck2mKyEDGKOdzYRfMDGxAqGslA+pGgLIwSEtKWkHn+Wef08Wgy7lgC+aMH5SN3EeW8l4xxjxxgfPuY81QzyopaM8OHBK2AfBQGwNweA0FLmjBSEnPK7VrFGRKg0BxTjNZxM7vcKFKAABy-lUGw3Lp4pWkz4F0X8Y3MEzKUzBPhKElxaBZh0sZaaCJfdUmz0adKDJCyknsMutE7BTJ5Xc22Tk7UeSd7CuOQuGhlTqlgzeYKnUVKQHdJ-pvKAvzraS0Riosunsxnsp8Zy9WFrkFflObfERMTH4tISe095AbmnPztTc8or53yPMZGK-ywbFmnOjdBONCbRibI4ZqisoluK8TpTUjhUahkmoFQ6xM8iJY7ilqCkZGZTxeIvCrNWeFGJPggHTN8mRDmjFJAfA1SyYDYG6ZsKwABpFAlgi0rxLXSk+MUy2LkwfOmKVbBmruGeXCFMBujGT6KumFB7-ILpDsiuKEcAiWBQD2CAvEABSEAtR4sMAEHQCBQB1kscSgB3xqgSjKi0OlzjqX2XuFi4A16oBwAgCQqAoqT3SD5aojxXVm1TIGtWIaPKDbHFNcuMJswegQagzBuDCHRgnyQ5GhR9TpXpOFUmxVgjlmqpdOq8p2T17avevkwpeqB3JqVSko1LSZ2-hLWGygeCk34f9Gxqc0mg00ciSIkoSBbyWDjSRygZHoAUZQFRpjk9hO03FOmk9HAs0rxzbssIdKDMLv1UJljvbDNiY1apvmjyJJLrbhhVoMB6Xdl7OumxpanWQIbWhuB2M6IMW4B2+iG86W4bAl58o6nNNfShM+wtfnhZKNeeWmtjqW7OtGU4LMu6kgTPdXF74lY3w1mYsAcKm0sL2tKwLPDUWPI3GPDV-d6GPXlEZBwNQY5iAEESDACAt4YCUC+FyUkYXFHiOUeVvrHUIWaJiiinRAQvCQfOG+WAwBsBYo4jNrKOVk6p1-RnLOOc86tGMFul1MWOUNbrlhhuI1eVBOXe8xAZ2TMqalaDvA4OwbMf+tG7geBzn6g2eJgGOaoewCRKoAzOP26OkgPZDQBXjZFZJ36SNj3REbY+2oiFbrqJ9DID4f6ABCEzegDAvkR3otLqk5MailmtoqGFafRabbF1Wtd6IPiSyAHnZ6YpAA
-edit: https://sequencediagram.org/index.html#initialData=IYYwLg9gTgBAwgGwJYFMB2YBQAHYUxIhK4YwDKKUAbpTngUSWDABLBoAmCtu+hx7ZhWqEUdPo0EwAIsDDAAgiBAoAzqswc5wAEbBVKGBx2ZM6MFACeq3ETQBzGAAYAdAE5M9qBACu2GADEaMBUljAASij2SKoWckgQaIEA7gAWSGBiiKikALQAfOSUNFAAXDAA2gAKAPJkACoAujAA9D4GUAA6aADeAETtlMEAtih9pX0wfQA0U7jqydAc45MzUyjDwEgIK1MAvpjCJTAFrOxclOX9g1AjYxNTs33zqotQyw9rfRtbO58HbE43FgpyOonKUCiMUyUAAFJForFKJEAI4+NRgACUh2KohOhVk8iUKnU5XsKDAAFUOrCbndsYTFMo1Kp8UYdKUAGJITgwamURkwHRhOnAUaYRnElknUG4lTlNA+BAIHEiFRsyXM0kgSFyFD8uE3RkM7RS9Rs4ylBQcDh8jqM1VUPGnTUk1SlHUoPUKHxgVKw4C+1LGiWmrWs06W622n1+h1g9W5U6Ai5lCJQpFQSKqJVYFPAmWFI6XGDXDp3SblVZPQN++oQADW6ErU32jsohfgyHM5QATE4nN0y0MxWMYFXHlNa6l6020C3Vgd0BxTF5fP4AtB2OSYAAZCDRJIBNIZLLdvJF4ol6p1JqtAzqBJoIei0azF5vDhLzir7x+QJeCg6B7gevjMMe6SZJg2TmGyxZphU0gAKK7kh9RIc0LQPqoT7dNOs7oGYP6eH+G6Qrau7QjAADio6shBp7QeezCytQ17UWhmH2KOeFBgRaBESuJHroE2A+FA2DcPAuqZDRo4pJBZ45Cxl5sQhtQNFxPHBHxjboEO3GjAAcqO35CWu-4BJYKDKhAyQwAAUhAPJyaMgQ6AgoANkxylwVe6mUneLSGSgvF1npz69JJwDWVAcAQAg0CzCFACS0hmb+IkBF4MVdl6sDANgkmEPEiQKYxMEXkUanlIhKFoRhrTGIJmBAA

