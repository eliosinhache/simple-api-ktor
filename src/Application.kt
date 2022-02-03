package com.practicaElio

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.gson.*
import io.ktor.features.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(ContentNegotiation) {
        gson {
        }
    }

    user()

    routing {
        get("/hello") {
            call.respondText("HELLO WORLD!", contentType = ContentType.Text.Plain)
        }

        get("/json/gson") {
            call.respond(mapOf("hello" to "world"))
        }
    }
}

data class User(val id: Long, val name: String, val surName: String) {
    override fun equals(other: Any?): Boolean {
        if (other is User) {
            return other.id == id
        }
        return super.equals(other)
    }

    override fun hashCode(): Int = id.hashCode()
}

data class Errors(val message: String)

val users = mutableListOf<User>()

fun Application.user() {
    routing {
        route("/user"){
            get { call.respond(users) }
            get ("/{id}") {
                val result = when(val candidate = call.parameters["id"]?.toLongOrNull()){
                            null -> call.respond(HttpStatusCode.BadRequest, Error("Id bust be long"))
                            else -> {
                                when(val user = users.firstOrNull() {it.id == candidate}) {
                                    null -> call.respond(HttpStatusCode.NotFound, Errors("User with Id $candidate not Found"))
                                    else -> call.respond(user)
                                }
                            }
                }
            }
            post {
                val candidate = call.receive<User>()
                users.add(candidate)
                call.respond(HttpStatusCode.Created, Errors("User Added"))}
            put {
                val candidate = call.receive<User>()
                val result = when(users.contains(candidate)) {
                    null -> call.respond(HttpStatusCode.NotFound, Errors("User not found"))
                    else -> {
                        users[users.indexOf(candidate)] = candidate
                        call.respond(HttpStatusCode.OK, Errors("User Updated correctly"))
                    }
                }
            }
            delete("/id"){
                val candidateID = call.parameters["id"]?.toLongOrNull()
                val result = when (candidateID) {
                    null -> call.respond(HttpStatusCode.BadRequest, "Id must be long")
                    else -> {
                        val user = users.firstOrNull() {it.id == candidateID}
                        when (user) {
                            null -> call.respond(HttpStatusCode.NotFound, Errors("User with id $candidateID Not Found"))
                            else -> {
                                users.remove(user)
                                call.respond(HttpStatusCode.OK, "User deleted correctly")
                            }
                        }
                    }
                }
            }

        }
    }
}

