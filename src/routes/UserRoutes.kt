package com.example.routes

import com.example.authentication.JwtService
import com.example.data.model.LoginRequest
import com.example.data.model.RegisterRequest
import com.example.data.model.SimpleRequest
import com.example.data.model.User
import com.example.repository.Repo
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

const val API_VERSION = "/v1"
const val USERS = "$API_VERSION/users"
const val REGISTER_REQUEST = "$USERS/register"
const val LOGIN_REQUEST = "$USERS/login"

@Location(REGISTER_REQUEST)
class UserRegisterRoute

@Location(LOGIN_REQUEST)
class UserLoginRoute

fun Route.UserRoutes(
    db: Repo,
    jwtService: JwtService,
    hashFunction: (String)-> String
    )
{
    post<UserRegisterRoute>{
        val registerRequest = try{
            call.receive<RegisterRequest>()
        }catch (e:Exception){
            call.respond(HttpStatusCode.BadRequest, SimpleRequest(false,"Missing some fields"))
            return@post
        }
        try{
            val user = User(registerRequest.email, hashFunction(registerRequest.password), registerRequest.name)
            db.addUser(user)
            call.respond(HttpStatusCode.OK, SimpleRequest(true,jwtService.generateToken(user)))
        }catch (e:Exception){
             call.respond(HttpStatusCode.Conflict, SimpleRequest(false,e.message?:"Some problem with additing user"))
            }
    }
    post<UserLoginRoute>{
        val loginRequest = try{
            call.receive<LoginRequest>()
        }catch (e:Exception){
            call.respond(HttpStatusCode.BadRequest, SimpleRequest(false,"Missing some fields"))
            return@post
        }
        try{
            val user = db.findUserByEmail(loginRequest.email)
            if(user == null) {
                call.respond(HttpStatusCode.BadRequest, SimpleRequest(false,"Wrong email"))
            }
            else {
                if(user.hashPassword == hashFunction(loginRequest.password)) {
                    call.respond(HttpStatusCode.OK, SimpleRequest(true,jwtService.generateToken(user)))
                }
                else {
                    call.respond(HttpStatusCode.BadRequest, SimpleRequest(false,"Incorrect password"))
                }
            }
        }catch (e:Exception){
            call.respond(HttpStatusCode.Conflict, SimpleRequest(false,e.message?:"Some problem with log in"))
        }
    }
}