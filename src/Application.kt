package com.example

import com.example.authentication.JwtService
import com.example.authentication.hash
import com.example.data.model.User
import com.example.repository.DatabaseFactory
import com.example.repository.Repo
import com.example.routes.NoteRoutes
import com.example.routes.UserRoutes
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.sessions.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.gson.*
import io.ktor.features.*
import io.ktor.locations.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {

    DatabaseFactory.init()

    val db = Repo()
    val jwtService = JwtService()
    val hashFunction = {s:String -> hash(s)}

    install(Sessions) {
        cookie<MySession>("MY_SESSION") {
            cookie.extensions["SameSite"] = "lax"
        }
    }

    install(Authentication) {
        jwt("jwt"){
            verifier(jwtService.varifier)
            realm = "Note Server"
            validate {
                    val payload = it.payload
                    val email = payload.getClaim("email").asString()
                    val user = db.findUserByEmail(email)
                user
            }
        }
    }

    install(ContentNegotiation) {
        gson {
        }
    }

    routing {
        get("/") {
            call.respondText("HELLO WORLD!", contentType = ContentType.Text.Plain)
        }
        install(Locations)
        UserRoutes(db, jwtService, hashFunction)
        NoteRoutes(db, hashFunction)

        get("/note/{id}") {
            val id = call.parameters["id"]
            call.respond("${id}")
        }

        get("/note") {
            val id = call.request.queryParameters["id"]
            call.respond("${id}")
        }
        get ("/token"){
            val email =  call.request.queryParameters["email"]!!
            val password =  call.request.queryParameters["password"]!!
            val username =  call.request.queryParameters["username"]!!

            val user = User(email, hashFunction(password), username)
            call.respond(jwtService.generateToken(user))
        }

        route("/notes") {
            route("/create") {
                post {
                    val body = call.receive<String>()
                    call.respond(body)
                }
            }
            delete {
                val body = call.receive<String>()
                call.respond(body)
            }
        }

    }
}

data class MySession(val count: Int = 0)

