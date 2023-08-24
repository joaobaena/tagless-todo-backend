# tagless-todo-backend

This repo implemented todo application based on [todobackend](https://www.todobackend.com/). 

It implements an API that consists of 5 distinct operations (create a todo, view a todo, modify a todo, list all todos, delete all todos).

Backend is implemented with Scala using http4s, Circe, and abstracting the effect type using tagless final style

### TODO

* Create tests for service layer
* Create in memory repository and migration with flyway
* Wrap everything and start on main using IO in the end of the world
* Deploy with Heroku
